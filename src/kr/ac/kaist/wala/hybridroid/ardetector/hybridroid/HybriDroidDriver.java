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

    public HybriDroidDriver(Properties prop, IClassHierarchy cha) throws ClassHierarchyException {
        properties = prop;
        this.cha = cha;
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
}
