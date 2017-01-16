package kr.ac.kaist.wala.hybridroid.ardetector.callgraph;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyClassTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.LambdaMethodTargetSelector;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.io.FileProvider;
import kr.ac.kaist.hybridroid.utils.LocalFileReader;
import kr.ac.kaist.wala.hybridroid.ardetector.model.thread.AndroidThreadModelClass;

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
        this.entries = findEntrypoints();
        this.options = makeAnalysisOptions(this.scope, this.entries);
        this.delegate = makeDelegateBuilder(this.cha, this.options);
        setTargetSelectors(this.options, this.cha);
    }

    private IClassHierarchy buildClassHierarchy(AnalysisScope scope) throws ClassHierarchyException {
        return ClassHierarchy.make(scope);
    }

    protected Iterable<AndroidEntryPoint> findEntrypoints() throws ClassHierarchyException {
        return HybridSDKModel.getEntrypoints(properties, cha);
    }

    protected AnalysisScope makeAnalysisScope(String sdk){
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

            //Add a SDK library to analysis scope.
            if (sdk.endsWith(".jar"))
                scope.addToScope(ClassLoaderReference.Application, new JarFileModule(new JarFile(new File(sdk))));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return scope;
    }

    protected AnalysisOptions makeAnalysisOptions(AnalysisScope scope, Iterable<AndroidEntryPoint> entries){
        AnalysisOptions options = new AnalysisOptions(scope, null);
        options.setEntrypoints(entries);
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);
        return options;
    }

    protected CallGraphBuilder makeDelegateBuilder(IClassHierarchy cha, AnalysisOptions options){
//        nCFABuilder builder = new nCFABuilder(2, cha, options, new AnalysisCache(), null, null);
        nCFABuilder builder = new nCFABuilder(0, cha, options, new AnalysisCache(), null, null);
        Set<TypeReference> entryClasses = new HashSet<TypeReference>();
        for(Entrypoint e : entries){
            IClass klass = e.getMethod().getDeclaringClass();

            entryClasses.add(e.getMethod().getDeclaringClass().getReference());
        }
        builder.setInstanceKeys(new SelectiveClassBasedInstanceKey(new ClassBasedInstanceKeys(options, cha), new AllocationSiteInNodeFactory(options, cha), entryClasses));
        return builder;
    }

    protected void setTargetSelectors(AnalysisOptions options, IClassHierarchy cha){
//        com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);
        options.setSelector(new ThreadModelMethodTargetSelector(new LambdaMethodTargetSelector(new ClassHierarchyMethodTargetSelector(cha)), cha));
        options.setSelector(new ClassHierarchyClassTargetSelector(cha));
        options.setUseConstantSpecificKeys(true);
    }

    public CallGraph makeCallGraph() throws CallGraphBuilderCancelException {
        CallGraph cg = delegate.makeCallGraph(options, null);
        PointerAnalysis<InstanceKey> pa = delegate.getPointerAnalysis();
//        for(PointerKey pk : pa.getPointerKeys()){
//            if(pk.toString().contains("Primordial"))
//                continue;
//            System.out.println("PK: " + pk);
//            for(InstanceKey ik : pa.getPointsToSet(pk)){
//                if(ik instanceof ConcreteTypeKey)
//                    System.out.println("\tIK: " + ik);
//            }
//        }

        return cg;
    }

    private class SelectiveClassBasedInstanceKey implements InstanceKeyFactory{
        private final InstanceKeyFactory classBased;
        private final InstanceKeyFactory siteBased;
        private final Set<TypeReference> instanceTypes;

        public SelectiveClassBasedInstanceKey(ClassBasedInstanceKeys classBased, AllocationSiteInNodeFactory siteBased, Set<TypeReference> instanceTypes){
            this.classBased = classBased;
            this.siteBased = siteBased;
            this.instanceTypes = instanceTypes;
        }

        @Override
        public InstanceKey getInstanceKeyForAllocation(CGNode node, NewSiteReference allocation) {
            if(options.getClassTargetSelector() == null)
                Assertions.UNREACHABLE("Must set a ClassTargetSelector to use SelectiveClassBasedInstanceKey");

            IClass klass = options.getClassTargetSelector().getAllocatedTarget(node, allocation);
            if(klass != null) {
                TypeReference allocationType = options.getClassTargetSelector().getAllocatedTarget(node, allocation).getReference();
                if (instanceTypes.contains(allocationType))
                    return classBased.getInstanceKeyForAllocation(node, allocation);
                else
                    return siteBased.getInstanceKeyForAllocation(node, allocation);
            }else
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

    public static class ThreadModelMethodTargetSelector implements MethodTargetSelector{
        private MethodTargetSelector base;
        private static TypeName TIMER_TYPE = TypeName.string2TypeName("Ljava/util/Timer");
        private static TypeName THREAD_TYPE = TypeName.string2TypeName("Ljava/lang/Thread");
        private static Set<Selector> SCHEDULE_SELECTOR_SET;
        private static Set<Selector> RUN_SELECTOR_SET;
        private IClass threadModelClass;

        static{
            SCHEDULE_SELECTOR_SET = new HashSet<>();
            SCHEDULE_SELECTOR_SET.add(AndroidThreadModelClass.SCHEDULE_AT_FIXED_RATE_SELECTOR1);
            SCHEDULE_SELECTOR_SET.add(AndroidThreadModelClass.SCHEDULE_AT_FIXED_RATE_SELECTOR2);
            SCHEDULE_SELECTOR_SET.add(AndroidThreadModelClass.SCHEDULE_SELECTOR1);
            SCHEDULE_SELECTOR_SET.add(AndroidThreadModelClass.SCHEDULE_SELECTOR2);
            SCHEDULE_SELECTOR_SET.add(AndroidThreadModelClass.SCHEDULE_SELECTOR3);
            SCHEDULE_SELECTOR_SET.add(AndroidThreadModelClass.SCHEDULE_SELECTOR4);

            RUN_SELECTOR_SET = new HashSet<>();
            RUN_SELECTOR_SET.add(AndroidThreadModelClass.START_SELECTOR);
        }

        public ThreadModelMethodTargetSelector(MethodTargetSelector base, IClassHierarchy cha){
            this.base = base;
            initThreadModel(cha);

        }

        private void initThreadModel(IClassHierarchy cha){
            threadModelClass = cha.lookupClass(AndroidThreadModelClass.ANDROID_THREAD_MODEL_CLASS);

            if (threadModelClass == null) {
                // add to cha
                threadModelClass = AndroidThreadModelClass.getInstance(cha);
                cha.addClass(threadModelClass);
            }
        }

        @Override
        public IMethod getCalleeTarget(CGNode caller, CallSiteReference site, IClass receiver) {
            if(site.isDispatch() && receiver != null) {
                if(receiver.getName().equals(TIMER_TYPE) && SCHEDULE_SELECTOR_SET.contains(site.getDeclaredTarget().getSelector())) {
                    return threadModelClass.getMethod(site.getDeclaredTarget().getSelector());
                }else if(receiver.getName().equals(THREAD_TYPE) && RUN_SELECTOR_SET.contains(site.getDeclaredTarget().getSelector())){
                    return threadModelClass.getMethod(site.getDeclaredTarget().getSelector());
                }
            }

            return base.getCalleeTarget(caller, site, receiver);
        }
    }
}
