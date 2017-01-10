package kr.ac.kaist.wala.hybridroid.ardetector.callgraph;

import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.FileProvider;
import kr.ac.kaist.hybridroid.utils.LocalFileReader;

import java.io.*;
import java.util.Properties;
import java.util.jar.JarFile;

/**
 * Created by leesh on 06/01/2017.
 */
public class CallGraphBuilderForHybridSDK {
    private final Properties properties;
    private final Iterable<Entrypoint> entries;
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

    protected Iterable<Entrypoint> findEntrypoints() throws ClassHierarchyException {
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

            //Add Android application to analysis scope.
            if (sdk.endsWith(".jar"))
                scope.addToScope(ClassLoaderReference.Application, new JarFileModule(new JarFile(new File(sdk))));

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return scope;
    }

    protected AnalysisOptions makeAnalysisOptions(AnalysisScope scope, Iterable<Entrypoint> entries){
        AnalysisOptions options = new AnalysisOptions(scope, null);
        options.setEntrypoints(entries);
        options.setReflectionOptions(AnalysisOptions.ReflectionOptions.NONE);
        return options;
    }

    protected CallGraphBuilder makeDelegateBuilder(IClassHierarchy cha, AnalysisOptions options){
        return new nCFABuilder(2, cha, options, new AnalysisCache(), null, null);
    }

    protected void setTargetSelectors(AnalysisOptions options, IClassHierarchy cha){
        com.ibm.wala.ipa.callgraph.impl.Util.addDefaultSelectors(options, cha);
        options.setUseConstantSpecificKeys(true);
    }

    public CallGraph makeCallGraph() throws CallGraphBuilderCancelException {
        return delegate.makeCallGraph(options, null);
    }
}
