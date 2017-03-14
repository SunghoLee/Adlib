package kr.ac.kaist.wala.hybridroid.ardetector.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import kr.ac.kaist.wala.hybridroid.ardetector.bridge.BridgeClass;
import kr.ac.kaist.wala.hybridroid.ardetector.hybridroid.HybriDroidDriver;
import kr.ac.kaist.wala.hybridroid.ardetector.model.entries.ConcreteTypeParamEntryPoint;

import java.util.*;

/**
 * Created by leesh on 06/01/2017.
 */
public class HybridSDKModel {

    private static Set<IMethod> bridgeEntries = new HashSet<>();

    public static Iterable<AndroidEntryPoint> getEntrypoints(Properties prop, IClassHierarchy cha) throws ClassHierarchyException {
        Set<AndroidEntryPoint> entries = new HashSet<>();
        Thread t;
        HybriDroidDriver driver = new HybriDroidDriver(prop, cha);
        Set<BridgeClass> bridges = driver.getBridgeClassesViaAnn();

        for(BridgeClass bridge : bridges){
            if(bridge.getReference().getName().toString().contains("MRAIDJavascriptCallsProxy"))
            for(BridgeClass.BridgeMethod m : bridge.getAccessibleMethods()){
                IMethod entry = cha.resolveMethod(m.getMethodReference());
                if(entry.getName().toString().equals("open"))
//                if(entry.getName().toString().equals("savePictureToPhotoLibrary"))
                if(entry != null){
                    System.err.println("#Entry: " + entry);
                    bridgeEntries.add(entry);
                    entries.add(new ConcreteTypeParamEntryPoint(AndroidEntryPoint.ExecutionOrder.MIDDLE_OF_LOOP, entry, cha));
                }
            }
        }

        entries.addAll(findNearestEntries(cha, TypeName.findOrCreate("Lcom/vervewireless/advert/InterstitialAdActivity"), Selector.make("onCreate(Landroid/os/Bundle;)V")));
        entries.addAll(findNearestEntries(cha, TypeName.findOrCreate("Lcom/vervewireless/advert/VerveAdSDK"), Selector.make("initialize(Landroid/app/Application;Ljava/lang/String;)V")));
        entries.addAll(findNearestEntries(cha, TypeName.findOrCreate("Lcom/vervewireless/advert/InterstitialAd"), Selector.make("setInterstitialAdListener(Lcom/vervewireless/advert/InterstitialAdListener;)V")));
        entries.addAll(findNearestEntries(cha, TypeName.findOrCreate("Lcom/vervewireless/advert/InterstitialAd"), Selector.make("requestAd(Lcom/vervewireless/advert/AdRequest;)V")));
        entries.addAll(findNearestEntries(cha, TypeName.findOrCreate("Lcom/vervewireless/advert/InterstitialAd"), Selector.make("display()V")));

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

    private static Set<ConcreteTypeParamEntryPoint> findNearestEntries(IClassHierarchy cha, TypeName klassName, Selector... methods){
        Set<ConcreteTypeParamEntryPoint> res = new HashSet<>();

        for(IClass c: cha){
            if(c.getReference().getName().equals(klassName)){
                for(int i=0; i<methods.length; i++){
                    res.add(new ConcreteTypeParamEntryPoint(AndroidEntryPoint.ExecutionOrder.AT_FIRST, c, findNearestMethod(c, methods[i]), cha));
                }
            }
        }

        return res;
    }

    private static IMethod findNearestMethod(IClass klass, Selector method){
//        System.out.println("#C: " + klass);
//        for(IMethod m : klass.getDeclaredMethods())
//            System.out.println("\t#M: " + m);
//        if(klass.toString().contains("InterstitialAdActivity"))
//            System.exit(-1);
        IMethod m = klass.getMethod(method);
        System.err.println("#Entry: " + m);
        return m;
    }

    public static Set<IMethod> getBridgeEntries(){
        return bridgeEntries;
    }
}
