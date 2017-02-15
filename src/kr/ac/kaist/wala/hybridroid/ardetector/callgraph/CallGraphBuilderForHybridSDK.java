package kr.ac.kaist.wala.hybridroid.ardetector.callgraph;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyClassTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.ExplicitCallGraph;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.summaries.LambdaMethodTargetSelector;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.io.FileProvider;
import kr.ac.kaist.hybridroid.utils.LocalFileReader;
import kr.ac.kaist.wala.hybridroid.ardetector.model.components.AndroidAlertDialogBuilderModelClass;
import kr.ac.kaist.wala.hybridroid.ardetector.model.components.AndroidHandlerModelClass;
import kr.ac.kaist.wala.hybridroid.ardetector.model.context.AndroidContextWrapperModelClass;
import kr.ac.kaist.wala.hybridroid.ardetector.model.entries.RecursiveParamDefFakeRootMethod;
import kr.ac.kaist.wala.hybridroid.ardetector.model.thread.*;

import java.io.*;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarFile;

/**
 * Created by leesh on 06/01/2017.
 */
public class CallGraphBuilderForHybridSDK {
    private final Properties properties;
    private final Iterable<AndroidEntryPoint> entries;
    private final AnalysisScope scope;
    private final AnalysisOptions options;
    private final IClassHierarchy cha;
    private final CallGraphBuilder delegate;
    private PointerAnalysis<InstanceKey> pa;

    public CallGraphBuilderForHybridSDK(String prop, String sdk) throws ClassHierarchyException {
        File propFile = new File(prop);
        this.properties = new Properties();
        try {
            this.properties.load(new FileInputStream(propFile));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        this.scope = makeAnalysisScope(sdk);
        this.cha = buildClassHierarchy(this.scope);
        this.entries = findEntrypoints(this.cha);
        this.options = makeAnalysisOptions(this.scope, this.entries);
        this.delegate = makeDelegateBuilder(this.cha, this.options);
        setTargetSelectors(this.options, this.cha);
    }

    private IClassHierarchy buildClassHierarchy(AnalysisScope scope) throws ClassHierarchyException {
        return ClassHierarchy.make(scope);
    }

    protected Iterable<AndroidEntryPoint> findEntrypoints(IClassHierarchy cha) throws ClassHierarchyException {
        return HybridSDKModel.getEntrypoints(properties, cha);

//        Set<AndroidEntryPointLocator.LocatorFlags> flags = HashSetFactory.make();
//        flags.add(AndroidEntryPointLocator.LocatorFlags.INCLUDE_CALLBACKS);
//        flags.add(AndroidEntryPointLocator.LocatorFlags.EP_HEURISTIC);
//        flags.add(AndroidEntryPointLocator.LocatorFlags.CB_HEURISTIC);
//        AndroidEntryPointLocator eps = new AndroidEntryPointLocator(flags);
//        List<AndroidEntryPoint> es = eps.getEntryPoints(cha);
//
//        System.out.println("Entry: " + es.size());
//
//        return () -> es.iterator();
    }

    protected AnalysisScope makeAnalysisScope(String sdk) {
        AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
        //Set DexClassLoader as class loader.
        scope.setLoaderImpl(ClassLoaderReference.Primordial, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
        scope.setLoaderImpl(ClassLoaderReference.Application, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");

        File exclusionsFile = new File(CallGraphTestUtil.REGRESSION_EXCLUSIONS);

        try {
            //Set exclusions.
            InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile) : FileProvider.class.getClassLoader()
                    .getResourceAsStream(exclusionsFile.getName());
            scope.setExclusions(new FileOfClasses(fs));
            fs.close();

            //Add Android libraries to analysis scope.
            String lib = LocalFileReader.androidJar(properties).getPath();
            if (lib.endsWith(".dex"))
                scope.addToScope(ClassLoaderReference.Primordial, DexFileModule.make(new File(lib)));
            else if (lib.endsWith(".jar"))
                scope.addToScope(ClassLoaderReference.Primordial, new JarFileModule(new JarFile(new File(lib))));

            //Add a SDK to analysis scope.
            if (sdk.endsWith(".jar"))
                scope.addToScope(ClassLoaderReference.Application, new JarFileModule(new JarFile(new File(sdk))));
            else if (sdk.endsWith(".apk")) {
                scope.addToScope(ClassLoaderReference.Application, DexFileModule.make(new File(sdk)));
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return scope;
    }

    protected AnalysisOptions makeAnalysisOptions(AnalysisScope scope, Iterable<AndroidEntryPoint> entries) {
        AnalysisOptions options = new AnalysisOptions(scope, null);
        options.setEntrypoints(entries);
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);
        return options;
    }

    protected CallGraphBuilder makeDelegateBuilder(IClassHierarchy cha, AnalysisOptions options) {
//        nCFABuilder builder = new nCFABuilder(2, cha, options, new AnalysisCache(), null, null);
//        for(IClass k : cha){
//            if(k.toString().contains("Reference")){
//                System.out.println("#c: " + k);
//                for(IMethod m : k.getDeclaredMethods()){
//                        System.out.println("\t#m: " + m);
//                }
//                for(IField f : k.getDeclaredInstanceFields()){
//                    System.out.println("\t#F: " + f);
//                }
//            }
//        }
//        System.exit(-1);
        nCFABuilder builder = new nCFABuilder(0, cha, options, new AnalysisCache(new DexIRFactory()), null, null) {
            @Override
            protected ExplicitCallGraph createEmptyCallGraph(IClassHierarchy cha, AnalysisOptions options) {
                return new ExplicitCallGraph(cha, options, getAnalysisCache()) {
                    @Override
                    protected CGNode makeFakeRootNode() throws CancelException {
                        return findOrCreateNode(new RecursiveParamDefFakeRootMethod(cha, options, getAnalysisCache()), Everywhere.EVERYWHERE);
                    }

                    ;
                };
            }

            ;
        };

        Set<TypeReference> entryClasses = new HashSet<TypeReference>();
        for (Entrypoint e : entries) {
            IClass klass = e.getMethod().getDeclaringClass();

            entryClasses.add(e.getMethod().getDeclaringClass().getReference());
        }
        builder.setInstanceKeys(new SelectiveClassBasedInstanceKey(new ClassBasedInstanceKeys(options, cha), new AllocationSiteInNodeFactory(options, cha), entryClasses));
        return builder;
    }

    protected void setTargetSelectors(AnalysisOptions options, IClassHierarchy cha) {
//        com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);
        options.setSelector(new ThreadModelMethodTargetSelector(new ContextModelMethodTargetSelector(new LambdaMethodTargetSelector(new ClassHierarchyMethodTargetSelector(cha)), cha), cha));
        options.setSelector(new ClassHierarchyClassTargetSelector(cha));
        options.setUseConstantSpecificKeys(true);
    }

    public PointerAnalysis<InstanceKey> getPointerAnalysis(){
        return pa;
    }

    public CallGraph makeCallGraph() throws CallGraphBuilderCancelException {
        CallGraph cg = delegate.makeCallGraph(options, null);
        PointerAnalysis<InstanceKey> pa = delegate.getPointerAnalysis();
        Slicer sc;
        this.pa = pa;
//        for(CGNode n: cg){
//            if(n.toString().contains("Node: < Application, Lcom/millennialmedia/internal/utils/EnvironmentUtils, init(Landroid/app/Application;)V > Context: Everywhere")){
//                PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, 4);
//                System.out.println(pk.toString());
//                for(InstanceKey ik : pa.getPointsToSet(pk)){
//                    System.out.println("ik: " + ik);
//                }
//                PointerKey npk = pa.getHeapModel().getPointerKeyForLocal(n, 1);
//                System.out.println(npk.toString());
//                for(InstanceKey ik : pa.getPointsToSet(npk)){
//                    System.out.println("ik: " + ik);
//                }
//            }
//            else if(n.toString().contains("Node: synthetic < Primordial, Landroid/content/Context, getApplicationContext()Landroid/content/Context; > Context: Everywhere")){
////                PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, 2);
////                System.out.println(pk.toString());
////                for(InstanceKey ik : pa.getPointsToSet(pk)){
////                    System.out.println("ik: " + ik);
////                }
//                PointerKey pk2 = pa.getHeapModel().getPointerKeyForLocal(n, 1);
//                System.out.println(pk2.toString());
//                for(InstanceKey ik : pa.getPointsToSet(pk2)){
//                    System.out.println("ik: " + ik);
//                }
//            }
//        }

        return cg;
    }

    private class SelectiveClassBasedInstanceKey implements InstanceKeyFactory {
        private final InstanceKeyFactory classBased;
        private final InstanceKeyFactory siteBased;
        private final Set<TypeReference> instanceTypes;

        public SelectiveClassBasedInstanceKey(ClassBasedInstanceKeys classBased, AllocationSiteInNodeFactory siteBased, Set<TypeReference> instanceTypes) {
            this.classBased = classBased;
            this.siteBased = siteBased;
            this.instanceTypes = instanceTypes;
        }

        @Override
        public InstanceKey getInstanceKeyForAllocation(CGNode node, NewSiteReference allocation) {
            if (options.getClassTargetSelector() == null)
                Assertions.UNREACHABLE("Must set a ClassTargetSelector to use SelectiveClassBasedInstanceKey");

            IClass klass = options.getClassTargetSelector().getAllocatedTarget(node, allocation);
            if (klass != null) {
                TypeReference allocationType = options.getClassTargetSelector().getAllocatedTarget(node, allocation).getReference();
                if (instanceTypes.contains(allocationType))
                    return classBased.getInstanceKeyForAllocation(node, allocation);
                else
                    return siteBased.getInstanceKeyForAllocation(node, allocation);
            } else
                return siteBased.getInstanceKeyForAllocation(node, allocation);
        }

        @Override
        public InstanceKey getInstanceKeyForMultiNewArray(CGNode node, NewSiteReference allocation, int dim) {
            return siteBased.getInstanceKeyForMultiNewArray(node, allocation, dim);
        }

        @Override
        public <T> InstanceKey getInstanceKeyForConstant(TypeReference type, T S) {
            return siteBased.getInstanceKeyForConstant(type, S);
        }

        @Override
        public InstanceKey getInstanceKeyForPEI(CGNode node, ProgramCounter instr, TypeReference type) {
            return siteBased.getInstanceKeyForPEI(node, instr, type);
        }

        @Override
        public InstanceKey getInstanceKeyForMetadataObject(Object obj, TypeReference objType) {
            return siteBased.getInstanceKeyForMetadataObject(obj, objType);
        }
    }

    public static class ContextModelMethodTargetSelector implements MethodTargetSelector {
        final private MethodTargetSelector base;
        final private IClassHierarchy cha;
        final private IClass contextWrapperModelClass;
        final private IClass alertDialogBuilderModelClass;
        final private IClass handlerModelClass;
        final private IClass referenceModelClass;

        public ContextModelMethodTargetSelector(MethodTargetSelector base, IClassHierarchy cha) {
            this.base = base;
            this.cha = cha;
            this.contextWrapperModelClass = AndroidContextWrapperModelClass.getInstance(cha);
            this.alertDialogBuilderModelClass = AndroidAlertDialogBuilderModelClass.getInstance(cha);
            this.handlerModelClass = AndroidHandlerModelClass.getInstance(cha);
            referenceModelClass = JavaReferenceModelClass.getInstance(cha);
        }

        @Override
        public IMethod getCalleeTarget(CGNode caller, CallSiteReference site, IClass receiver) {
            IMethod target = base.getCalleeTarget(caller, site, receiver);

            if (site.isDispatch() && receiver != null && target != null) {
                if (AndroidContextWrapperModelClass.isSubClassOfContextWrapper(target.getDeclaringClass()) && site.getDeclaredTarget().getSelector().equals(AndroidContextWrapperModelClass.GETSYSTEMSERVICE_SELECTOR)) {
                    return contextWrapperModelClass.getMethod(site.getDeclaredTarget().getSelector());
                } else if (AndroidContextWrapperModelClass.isSubClassOfContextWrapper(target.getDeclaringClass()) && site.getDeclaredTarget().getSelector().equals(AndroidContextWrapperModelClass.GETAPPLICATIONCONTEXT_SELECTOR)) {
                    return contextWrapperModelClass.getMethod(site.getDeclaredTarget().getSelector());
                }else if (target.getDeclaringClass().getName().equals(AndroidAlertDialogBuilderModelClass.ANDROID_ALERT_DIALOG_BUILDER_MODEL_CLASS.getName()) && site.getDeclaredTarget().getSelector().equals(AndroidAlertDialogBuilderModelClass.SHOW_SELECTOR)) {
                    return alertDialogBuilderModelClass.getMethod(site.getDeclaredTarget().getSelector());
                }else if (target.getDeclaringClass().getName().equals(AndroidHandlerModelClass.ANDROID_HANDLER_MODEL_CLASS.getName()) && site.getDeclaredTarget().getSelector().equals(AndroidHandlerModelClass.SEND_MESSAGE_SELECTOR)) {
                    return handlerModelClass.getMethod(site.getDeclaredTarget().getSelector());
                }else if (target.getDeclaringClass().getName().equals(JavaReferenceModelClass.JAVA_REFERENCE_MODEL_CLASS.getName()) && site.getDeclaredTarget().getSelector().equals(JavaReferenceModelClass.GET_SELECTOR)) {
                    return referenceModelClass.getMethod(site.getDeclaredTarget().getSelector());
                }

                //JavaReferenceModelClass
            }
            return target;
        }
    }

    public static class ThreadModelMethodTargetSelector implements MethodTargetSelector {
        private MethodTargetSelector base;

        private static Set<Selector> SCHEDULE_SELECTOR_SET;
        private static Set<Selector> RUN_SELECTOR_SET;
        private static Set<Selector> EXECUTE_SELECTOR_SET;
        private IClass timerModelClass;
        private IClass threadModelClass;
        private IClass threadPoolExecutorModelClass;
        private IClass viewModelClass;
        final private IClass handlerModelClass;
        final private IClass asyncTaskModelClass;

        static {
            SCHEDULE_SELECTOR_SET = new HashSet<>();
            SCHEDULE_SELECTOR_SET.add(JavaTimerModelClass.SCHEDULE_AT_FIXED_RATE_SELECTOR1);
            SCHEDULE_SELECTOR_SET.add(JavaTimerModelClass.SCHEDULE_AT_FIXED_RATE_SELECTOR2);
            SCHEDULE_SELECTOR_SET.add(JavaTimerModelClass.SCHEDULE_SELECTOR1);
            SCHEDULE_SELECTOR_SET.add(JavaTimerModelClass.SCHEDULE_SELECTOR2);
            SCHEDULE_SELECTOR_SET.add(JavaTimerModelClass.SCHEDULE_SELECTOR3);
            SCHEDULE_SELECTOR_SET.add(JavaTimerModelClass.SCHEDULE_SELECTOR4);

            RUN_SELECTOR_SET = new HashSet<>();
            RUN_SELECTOR_SET.add(JavaThreadModelClass.START_SELECTOR);

            EXECUTE_SELECTOR_SET = new HashSet<>();
            EXECUTE_SELECTOR_SET.add(JavaThreadPoolExecutorModelClass.EXECUTE_SELECTOR);
        }

        public ThreadModelMethodTargetSelector(MethodTargetSelector base, IClassHierarchy cha) {
            this.base = base;
            handlerModelClass = AndroidHandlerModelClass.getInstance(cha);
            asyncTaskModelClass = AndroidAsyncTaskModelClass.getInstance(cha);
            initThreadModel(cha);
        }

        private void initThreadModel(IClassHierarchy cha) {
            if (timerModelClass == null)
                timerModelClass = JavaTimerModelClass.getInstance(cha);
            if (threadModelClass == null)
                threadModelClass = JavaThreadModelClass.getInstance(cha);
            if (threadPoolExecutorModelClass == null)
                threadPoolExecutorModelClass = JavaThreadPoolExecutorModelClass.getInstance(cha);
            if (viewModelClass == null)
                viewModelClass = AndroidViewModelClass.getInstance(cha);
        }

        @Override
        public IMethod getCalleeTarget(CGNode caller, CallSiteReference site, IClass receiver) {
            IMethod target = base.getCalleeTarget(caller, site, receiver);

            if (site.isDispatch() && receiver != null && target != null) {
                if (target.getDeclaringClass().getName().equals(JavaTimerModelClass.JAVA_TIMER_MODEL_CLASS.getName()) && SCHEDULE_SELECTOR_SET.contains(site.getDeclaredTarget().getSelector())) {
                    return timerModelClass.getMethod(site.getDeclaredTarget().getSelector());
                } else if (target.getDeclaringClass().getName().equals(JavaThreadModelClass.JAVA_THREAD_MODEL_CLASS.getName()) && RUN_SELECTOR_SET.contains(site.getDeclaredTarget().getSelector())) {
                    return threadModelClass.getMethod(site.getDeclaredTarget().getSelector());
                } else if (target.getDeclaringClass().getName().equals(JavaThreadPoolExecutorModelClass.JAVA_THREAD_POOL_EXECUTOR_MODEL_CLASS.getName()) && EXECUTE_SELECTOR_SET.contains(site.getDeclaredTarget().getSelector())) {
                    return threadPoolExecutorModelClass.getMethod(site.getDeclaredTarget().getSelector());
                } else if (target.getDeclaringClass().getName().equals(AndroidViewModelClass.ANDROID_VIEW_MODEL_CLASS.getName()) && AndroidViewModelClass.POST_SELECTOR.equals(site.getDeclaredTarget().getSelector())) {
                    return viewModelClass.getMethod(site.getDeclaredTarget().getSelector());
                } else if (target.getDeclaringClass().getName().equals(AndroidHandlerModelClass.ANDROID_HANDLER_MODEL_CLASS.getName()) && AndroidHandlerModelClass.POST_DELAYED_SELECTOR.equals(site.getDeclaredTarget().getSelector())) {
                    return handlerModelClass.getMethod(site.getDeclaredTarget().getSelector());
                } else if (target.getDeclaringClass().getName().equals(AndroidAsyncTaskModelClass.ANDROID_ASYNC_TASK_MODEL_CLASS.getName()) && (AndroidAsyncTaskModelClass.EXECUTE_SELECTOR1.equals(site.getDeclaredTarget().getSelector()) || AndroidAsyncTaskModelClass.EXECUTE_SELECTOR2.equals(site.getDeclaredTarget().getSelector()))) {
                    return asyncTaskModelClass.getMethod(site.getDeclaredTarget().getSelector());
                }
            }
            return target;
        }
    }
}


