package kr.ac.kaist.wala.hybridroid.ardetector.hybridroid;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.TypeReference;
import kr.ac.kaist.hybridroid.types.HybriDroidTypes;
import kr.ac.kaist.wala.hybridroid.ardetector.bridge.BridgeClass;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

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
