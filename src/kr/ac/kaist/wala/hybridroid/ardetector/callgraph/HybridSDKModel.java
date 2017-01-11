package kr.ac.kaist.wala.hybridroid.ardetector.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import kr.ac.kaist.wala.hybridroid.ardetector.bridge.BridgeClass;
import kr.ac.kaist.wala.hybridroid.ardetector.hybridroid.HybriDroidDriver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Created by leesh on 06/01/2017.
 */
public class HybridSDKModel {

    public static Iterable<Entrypoint> getEntrypoints(Properties prop, IClassHierarchy cha) throws ClassHierarchyException {
        Set<Entrypoint> entries = new HashSet<Entrypoint>();

        HybriDroidDriver driver = new HybriDroidDriver(prop, cha);
        Set<BridgeClass> bridges = driver.getBridgeClassesViaAnn();

        for(BridgeClass bridge : bridges){
            for(BridgeClass.BridgeMethod m : bridge.getAccessibleMethods()){
                IMethod entry = cha.resolveMethod(m.getMethodReference());
                if(entry != null){
                    entries.add(new AndroidEntryPoint(AndroidEntryPoint.ExecutionOrder.MIDDLE_OF_LOOP, entry, cha));
                }
            }
        }

        for(IClass c : cha){
            if(c.toString().contains("OuterClass"))
            for(IMethod m : c.getAllMethods()){
                if(m.toString().contains("<init>"))
                    entries.add(new AndroidEntryPoint(AndroidEntryPoint.ExecutionOrder.AT_FIRST, m, cha));
            }
        }
        return new Iterable<Entrypoint>(){
            @Override
            public Iterator<Entrypoint> iterator() {
                return entries.iterator();
            }
        };
    }


}
