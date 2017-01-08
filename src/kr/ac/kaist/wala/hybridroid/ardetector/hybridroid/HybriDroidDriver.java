package kr.ac.kaist.wala.hybridroid.ardetector.hybridroid;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.JarFileModule;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.dalvik.util.AndroidEntryPointLocator;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyClassTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.ClassHierarchyMethodTargetSelector;
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.*;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.io.FileProvider;
import com.ibm.wala.util.strings.Atom;
import kr.ac.kaist.hybridroid.types.HybriDroidTypes;
import kr.ac.kaist.hybridroid.utils.LocalFileReader;
import kr.ac.kaist.wala.hybridroid.ardetector.bridge.BridgeClass;
import java.io.*;
import java.util.*;
import java.util.jar.JarFile;

/**
 * Created by leesh on 05/01/2017.
 */
public class HybriDroidDriver {
    private final Properties properties;
    private final IClassHierarchy cha;

    public HybriDroidDriver(String sdk, String prop) throws CallGraphBuilderCancelException, ClassHierarchyException {
        File propFile = new File(prop);
        properties = new Properties();
        try {
            properties.load(new FileInputStream(propFile));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        this.cha = buildClassHierarchy(sdk);
    }

    public Set<BridgeClass> getBridgeClassesUsedInAJI(){
        Set<BridgeClass> res = new HashSet<BridgeClass>();
        IMethod addJsMethod = cha.resolveMethod(HybriDroidTypes.ADDJAVASCRIPTINTERFACE_APP_METHODREFERENCE);
        IRFactory<IMethod> factory = new DefaultIRFactory();

        for(IClass c : cha){
              for(IMethod m : c.getAllMethods()){
                  IR ir = factory.makeIR(m, Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
                  if(ir != null){
                      Iterator<CallSiteReference> icsr = ir.iterateCallSites();
                      while (icsr.hasNext()) {
                          CallSiteReference csr = icsr.next();
                          if (!csr.getDeclaredTarget().equals(addJsMethod))
                              continue;

                          for (SSAAbstractInvokeInstruction invokeInst : ir.getCalls(csr)) {
                              TypeReference tr = findClassType(m, invokeInst.getUse(1));
                              if(tr != null) {
                                  IClass klass = cha.lookupClass(tr);
                                  if (klass != null)
                                      res.add(new BridgeClass(klass));
                              }
                          }
                      }
                  }
              }
        }
        return res;
    }

    private TypeReference findClassType(IMethod m, int objVar){
        //Only for local created object, parameter or field?


        return null;
    }

    public Set<BridgeClass> getBridgeClassesViaAnn(){
        Set<BridgeClass> res = new HashSet<BridgeClass>();
        for(IClass c : cha){
            for (IMethod m : c.getAllMethods()) {
                if (HybriDroidTypes.hasJavascriptInterfaceAnnotation(m)) {
                    res.add(new BridgeClass(c));
                    break;
                }
            }
        }
        return res;
    }

    private IClassHierarchy buildClassHierarchy(String sdk) throws CallGraphBuilderCancelException, ClassHierarchyException {
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

        return ClassHierarchy.make(scope);
    }
}
