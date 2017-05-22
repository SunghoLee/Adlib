package kr.ac.kaist.wala.adlib.hybridroid;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.TypeReference;
import kr.ac.kaist.wala.hybridroid.types.HybriDroidTypes;
import kr.ac.kaist.wala.adlib.bridge.BridgeClass;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Driver class for HybriDroid. It extracts bridge information from a class hierarchy using 'JavascriptInterace' annotation. Some type information is supported by HybriDroid.
 * Created by leesh on 05/01/2017.
 */
public class HybriDroidDriver {
    private final Properties properties;
    private final IClassHierarchy cha;

    public HybriDroidDriver(Properties prop, IClassHierarchy cha) throws ClassHierarchyException {
        properties = prop;
        this.cha = cha;
    }

    /**
     * Find bridge information using 'addJavascriptInterface' call sites. The bridge information is gotten tracking an object used as the second argument of 'addJavascriptInterface'. If this object is not created locally, it fails to find the bridge.
     * @return a set of bridge information
     */
    public Set<BridgeClass> getBridgeClassesUsedInAJI(){
        Set<BridgeClass> res = new HashSet<BridgeClass>();
        IMethod addJsMethod = cha.resolveMethod((HybriDroidTypes.ADDJAVASCRIPTINTERFACE_APP_METHODREFERENCE != null)? HybriDroidTypes.ADDJAVASCRIPTINTERFACE_APP_METHODREFERENCE : HybriDroidTypes.ADDJAVASCRIPTINTERFACE_PRI_METHODREFERENCE);
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

    /**
     * Find bridge information using 'JavascriptInterface'. If the target version is less than 17, this approach fails to find bridge information.
     * @return a set of bridge information
     */
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
