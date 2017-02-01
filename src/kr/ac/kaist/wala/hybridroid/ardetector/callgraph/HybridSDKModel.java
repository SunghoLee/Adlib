package kr.ac.kaist.wala.hybridroid.ardetector.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import kr.ac.kaist.wala.hybridroid.ardetector.bridge.BridgeClass;
import kr.ac.kaist.wala.hybridroid.ardetector.hybridroid.HybriDroidDriver;

import java.util.*;

/**
 * Created by leesh on 06/01/2017.
 */
public class HybridSDKModel {

    public static Iterable<AndroidEntryPoint> getEntrypoints(Properties prop, IClassHierarchy cha) throws ClassHierarchyException {
        Set<AndroidEntryPoint> entries = new HashSet<>();
        Thread t;
        HybriDroidDriver driver = new HybriDroidDriver(prop, cha);
        Set<BridgeClass> bridges = driver.getBridgeClassesViaAnn();

        for(BridgeClass bridge : bridges){
            for(BridgeClass.BridgeMethod m : bridge.getAccessibleMethods()){
                IMethod entry = cha.resolveMethod(m.getMethodReference());
                if(entry.getName().toString().equals("open"))
                if(entry != null){
                    System.err.println("#Entry: " + entry);
                    entries.add(new AndroidEntryPoint(AndroidEntryPoint.ExecutionOrder.MIDDLE_OF_LOOP, entry, cha));
                }
            }
        }

        for(IClass c : cha){
            if(c.getName().getClassName().toString().endsWith("MMSDK"))
            for(IMethod m : c.getAllMethods()){
                if(m.toString().contains("initialize"))
                    entries.add(new AndroidEntryPoint(AndroidEntryPoint.ExecutionOrder.AT_FIRST, m, cha));
            }
            if(c.getName().getClassName().toString().endsWith("InterstitialAd"))
                for(IMethod m : c.getAllMethods()){
                    if(m.toString().contains("load"))
                        entries.add(new AndroidEntryPoint(AndroidEntryPoint.ExecutionOrder.AT_FIRST, m, cha));
                    if(m.toString().contains("show"))
                        entries.add(new AndroidEntryPoint(AndroidEntryPoint.ExecutionOrder.AT_FIRST, m, cha));
                }
        }

        //JVM 1.8
        List<AndroidEntryPoint> entryList = new ArrayList<>();
        entryList.addAll(entries);
        Collections.sort(entryList, (AndroidEntryPoint o1, AndroidEntryPoint o2) -> o1.order.compareTo(o2.order));

        // for JVM version < 1.8
//        Collections.sort(entryList, new Comparator<AndroidEntryPoint>() {
//            @Override
//            public int compare(AndroidEntryPoint o1, AndroidEntryPoint o2) {
//                    return o1.order.compareTo(o2.order);
//            }
//        });

        //JVM 1.8
        return () -> entryList.iterator();

        // for JVM version < 1.8
//        return new Iterable<AndroidEntryPoint>(){
//            @Override
//            public Iterator<AndroidEntryPoint> iterator() {
//                return entryList.iterator();
//            }
//        };
    }
}
