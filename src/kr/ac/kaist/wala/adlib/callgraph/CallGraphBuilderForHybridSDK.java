package kr.ac.kaist.wala.adlib.callgraph;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.*;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.summaries.LambdaMethodTargetSelector;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.collections.ComposedIterator;
import com.ibm.wala.util.collections.FilterIterator;
import com.ibm.wala.util.collections.MapIterator;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.functions.Function;
import com.ibm.wala.util.strings.Atom;
import kr.ac.kaist.wala.adlib.Config;
import kr.ac.kaist.wala.adlib.InitInstsParser;
import kr.ac.kaist.wala.adlib.callgraph.context.FirstMethodContextSelector;
import kr.ac.kaist.wala.adlib.model.entries.RecursiveParamDefFakeRootMethod;
import kr.ac.kaist.wala.hybridroid.models.AndroidHybridAppModel;
import kr.ac.kaist.wala.hybridroid.utils.LocalFileReader;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;

/**
 * CallGraphBuilder for Android libraries that use hybrid communication.
 * Created by leesh on 06/01/2017.
 */
public class CallGraphBuilderForHybridSDK {
    private final Properties properties;
    private final Iterable<AndroidEntryPoint> entries;
    private final AnalysisScope scope;
    private final AnalysisOptions options;
    private final IClassHierarchy cha;
    private final CallGraphBuilder delegate;
    private final InitInstsParser.InitInst[] initInst;

    private PointerAnalysis<InstanceKey> pa;

    public CallGraphBuilderForHybridSDK(String prop, String sdk, InitInstsParser.InitInst[] initInsts) throws ClassHierarchyException {
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
        this.initInst = initInsts;
        this.scope = makeAnalysisScope(sdk);
        this.cha = buildClassHierarchy(this.scope);

//        for(IClass c: cha){
//            if(c.toString().contains("Lcom/fyber/Fyber")){
//                System.out.println("#C: " + c);
//                for(IMethod m : c.getAllMethods()){
//                    if(m.toString().contains("start"))
//                        System.out.println("\t#M: " + m);
//                }
//
//            }
//        }
//        System.exit(-1);
        this.entries = findEntrypoints(this.cha);
        this.options = makeAnalysisOptions(this.scope, this.entries);
        this.delegate = makeDelegateBuilder(this.cha, this.options);

        setTargetSelectors(this.options, this.cha);
    }

    public AnalysisOptions getOptions(){
        return this.options;
    }

    private IClassHierarchy buildClassHierarchy(AnalysisScope scope) throws ClassHierarchyException {
        return ClassHierarchy.make(scope);
    }

    /**
     * Find entry points from ClassHierarchy.
     * @param cha ClassHierarchy
     * @return Iterable object for Android entry points
     * @throws ClassHierarchyException
     */
    protected Iterable<AndroidEntryPoint> findEntrypoints(IClassHierarchy cha) throws ClassHierarchyException {
        return HybridSDKModel.getEntrypoints(properties, cha, this.initInst);
    }

    /**
     * Make analysis scope for Android library. It automatically loads Java built-in classes and Android frameworks from WalaProperties, as well as loads the library to analysis scope.
     * Now, only support apk and jar type libraries.
     * @param sdk file path of the library
     * @return analysis scope for the library
     */
    protected AnalysisScope makeAnalysisScope(String sdk) {
        AnalysisScope scope = AnalysisScope.createJavaAnalysisScope();
        //Set DexClassLoader as class loader.
        scope.setLoaderImpl(ClassLoaderReference.Primordial, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");
        scope.setLoaderImpl(ClassLoaderReference.Application, "com.ibm.wala.dalvik.classLoader.WDexClassLoaderImpl");

        File exclusionsFile = new File(CallGraphTestUtil.REGRESSION_EXCLUSIONS);

        try {
            //Set exclusions.
            InputStream fs = exclusionsFile.exists() ? new FileInputStream(exclusionsFile) : AndroidHybridAppModel.class
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

    /**
     * Make analysis option for the analysis scope and entry points. By default, the WALA reflection option is turned off, but our own reflection model always works.
     * @param scope analysis scope
     * @param entries iterable object for entry points
     * @return analysis option
     */
    protected AnalysisOptions makeAnalysisOptions(AnalysisScope scope, Iterable<AndroidEntryPoint> entries) {
        AnalysisOptions options = new AnalysisOptions(scope, null);
        options.setEntrypoints(entries);
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);
        return options;
    }

    /**
     * Make delegate callgraph builder. The delegate constructs the call graph.
     * Our default delegate builder has our entry model for hybrid communcation and selective object abstraction model for receivers of bridge methods.
     * Now, we use ReflectionResolvingCallGraph as the delegate for resolving some reflection.
     * @param cha ClassHierarchy
     * @param options analysis option
     * @return call graph builder
     */
    protected CallGraphBuilder makeDelegateBuilder(IClassHierarchy cha, AnalysisOptions options) {
        SSAPropagationCallGraphBuilder builder = null;
        switch(Config.sensitivity()){
            case PATH_SEPERATION:
                builder = new ReflectionResolvingCallGraphBuilderPathSeperation(Config.sensitivityDepth(), 
                                            cha, options, new AnalysisCache(new DexIRFactory())){
                    @Override
                    protected ExplicitCallGraph createEmptyCallGraph(IClassHierarchy cha, AnalysisOptions options) {
                        return new ExplicitCallGraph(cha, options, getAnalysisCache()) {
                            @Override
                            protected CGNode makeFakeRootNode() throws CancelException {
                                return findOrCreateNode(new RecursiveParamDefFakeRootMethod(cha, options, getAnalysisCache()), Everywhere.EVERYWHERE);
                            }
                        };
                    }
                };
            break;
            case INSENSITIVITY:
                builder = new ReflectionResolvingCallGraphBuilderCFA(0, cha, options, new AnalysisCache(new DexIRFactory())){
                @Override
                protected ExplicitCallGraph createEmptyCallGraph(IClassHierarchy cha, AnalysisOptions options) {
                    return new ExplicitCallGraph(cha, options, getAnalysisCache()) {
                        @Override
                        protected CGNode makeFakeRootNode() throws CancelException {
                            return findOrCreateNode(new RecursiveParamDefFakeRootMethod(cha, options, getAnalysisCache()), Everywhere.EVERYWHERE);
                        }
                    };
                }
            };
            break;
            case CALLSITE_SENSITIVITY:
                builder = new ReflectionResolvingCallGraphBuilderCFA(Config.sensitivityDepth(), cha, options, new AnalysisCache(new DexIRFactory())){
                @Override
                protected ExplicitCallGraph createEmptyCallGraph(IClassHierarchy cha, AnalysisOptions options) {
                    return new ExplicitCallGraph(cha, options, getAnalysisCache()) {
                        @Override
                        protected CGNode makeFakeRootNode() throws CancelException {
                            return findOrCreateNode(new RecursiveParamDefFakeRootMethod(cha, options, getAnalysisCache()), Everywhere.EVERYWHERE);
                        }
                    };
                }
            };
            break;
            case OBJ_SENSITIVITY:
                builder = new ReflectionResolvingCallGraphBuilderOBJ(cha, options, new AnalysisCache(new DexIRFactory()), null, null, Config.sensitivityDepth()){
                @Override
                protected ExplicitCallGraph createEmptyCallGraph(IClassHierarchy cha, AnalysisOptions options) {
                    return new ExplicitCallGraph(cha, options, getAnalysisCache()) {
                        @Override
                        protected CGNode makeFakeRootNode() throws CancelException {
                            return findOrCreateNode(new RecursiveParamDefFakeRootMethod(cha, options, getAnalysisCache()), Everywhere.EVERYWHERE);
                        }
                    };
                }
            };
            break;
        }   

        Set<TypeReference> entryClasses = new HashSet<TypeReference>();
        for (Entrypoint e : entries) {
            IClass klass = e.getMethod().getDeclaringClass();

            entryClasses.add(e.getMethod().getDeclaringClass().getReference());
        }
        builder.setInstanceKeys(new SelectiveClassBasedInstanceKey(new ClassBasedInstanceKeys(options, cha), new AllocationSiteInNodeFactory(options, cha), entryClasses));
        return builder;
    }

    /**
     * Set class and method target selector to analysis option. By default, we turn on every modeling options to select a target method.
     * @param options analysis option
     * @param cha ClassHierarhcy
     */
    protected void setTargetSelectors(AnalysisOptions options, IClassHierarchy cha) {
//        com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);
        options.setSelector(new ModelingMethodTargetSelector(new LambdaMethodTargetSelector(new ClassHierarchyMethodTargetSelector(cha)), cha));
        options.setSelector(new ModelingClassTargetSelector(new ClassHierarchyClassTargetSelector(cha), cha));
        options.setUseConstantSpecificKeys(true);
    }

    /**
     * Get pointer analysis result.
     * @return pointer analysis
     */
    public PointerAnalysis<InstanceKey> getPointerAnalysis(){
        return pa;
    }

    /**
     * Builder a call graph.
     * @return call graph
     * @throws CallGraphBuilderCancelException
     */
    public CallGraph makeCallGraph() throws CallGraphBuilderCancelException {
        CallGraph cg = delegate.makeCallGraph(options, null);
        PointerAnalysis<InstanceKey> pa = delegate.getPointerAnalysis();
        Slicer sc;
        this.pa = pa;
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
                else {
                    try {
                        return siteBased.getInstanceKeyForAllocation(node, allocation);
                    }catch(NullPointerException e){
                        return null;
                    }
                }
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

}


