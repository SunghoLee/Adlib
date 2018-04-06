package kr.ac.kaist.wala.adlib.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.*;
import kr.ac.kaist.wala.adlib.InitInstsParser;
import kr.ac.kaist.wala.adlib.bridge.BridgeClass;
import kr.ac.kaist.wala.adlib.hybridroid.HybriDroidDriver;
import kr.ac.kaist.wala.adlib.model.entries.ConcreteTypeParamEntryPoint;

import java.util.*;

/**
 * A entry modeling class for android library that is implemented using hybrid communication. This class uses the HybriDroid Driver to find bridges.
 * Created by leesh on 06/01/2017.
 */
public class HybridSDKModel {

    private static Set<IMethod> bridgeEntries = new HashSet<>();
    private static int AT_FIRST_ORDER = 0;
    private static int BEFORE_LOOP_ORDER = Integer.MAX_VALUE / 8;
    private static int START_OF_LOOP_ORDER = Integer.MAX_VALUE / 8 * 2;
    private static int MIDDLE_OF_LOOP_ORDER = Integer.MAX_VALUE / 8 * 3;
    private static int MULTIPLE_TIMES_IN_LOOP_ORDER = Integer.MAX_VALUE / 8 * 4;
    private static int END_OF_LOOP_ORDER = Integer.MAX_VALUE / 8 * 5;
    private static int AFTER_LOOP_ORDER = Integer.MAX_VALUE / 8 * 6;
    private static int AT_LAST_ORDER = Integer.MAX_VALUE / 8 * 7;


    //Lcom/millennialmedia/internal/JSBridge$JSBridgeMMJS . vibrate(Ljava/lang/String;)V
    public static boolean TEST_MODE = true;
    public static MethodReference TEST_BRIDGE_METHOD = MethodReference.findOrCreate(TypeReference.findOrCreate(ClassLoaderReference.Application, "Lcom/nativex/monetization/mraid/JSIAdToDevice"), Selector.make("storePicture(Ljava/lang/String;)V"));
//    public static MethodReference TEST_BRIDGE_METHOD = MethodReference.findOrCreate(TypeReference.findOrCreate(ClassLoaderReference.Application, "Lcom/nativex/monetization/mraid/JSIAdToDevice"), Selector.make("shouldEnableCloseRegion(Ljava/lang/String;)V"));
//    public static MethodReference TEST_BRIDGE_METHOD = MethodReference.findOrCreate(TypeReference.findOrCreate(ClassLoaderReference.Application, "Lcom/nativex/monetization/mraid/JSIAdToDevice"), Selector.make("setPageSize(Ljava/lang/String;)V"));
//    public static MethodReference TEST_BRIDGE_METHOD = MethodReference.findOrCreate(TypeReference.findOrCreate(ClassLoaderReference.Application, "Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge"), Selector.make("deleteFile(Ljava/lang/String;)V"));
//    public static MethodReference TEST_BRIDGE_METHOD = MethodReference.findOrCreate(TypeReference.findOrCreate(ClassLoaderReference.Application, "Lcom/smaato/soma/internal/connector/OrmmaBridge"), Selector.make("storePicture(Ljava/lang/String;)V"));
    //Lcom/tapjoy/mraid/controller/Assets
    /**
     * Find entry points in a SDK. The entry points consists of init instructions, Activity lifecycles, and bridge methdos.
     * @param prop WalaProperties
     * @param cha ClassHierarchy
     * @param initInsts a list of init instructions
     * @return iterable object for Android entry points of the SDK
     * @throws ClassHierarchyException
     */
    public static Iterable<AndroidEntryPoint> getEntrypoints(Properties prop, IClassHierarchy cha, InitInstsParser.InitInst[] initInsts) throws ClassHierarchyException {
        Set<AndroidEntryPoint> entries = new HashSet<>();
        Thread t;
        HybriDroidDriver driver = new HybriDroidDriver(prop, cha);
        Set<BridgeClass> bridges = driver.getBridgeClassesViaAnn();

        // attach bridge methods invocation to entry model
        for(BridgeClass bridge : bridges){
                for(BridgeClass.BridgeMethod m : bridge.getAccessibleMethods()){
                    IMethod entry = cha.resolveMethod(m.getMethodReference());
                    if(entry != null){
                        if(TEST_MODE){
                            if(bridgeEntries.size() == 1)
                                break;
                            else if(TEST_BRIDGE_METHOD != null && entry.getReference().equals(TEST_BRIDGE_METHOD)){
                                bridgeEntries.add(entry);
                                entries.add(new ConcreteTypeParamEntryPoint(new AndroidEntryPoint.ExecutionOrder(MIDDLE_OF_LOOP_ORDER++), entry, cha));
                            }
                        }
                        else {
                            if(entry.getDeclaringClass().isInterface() || entry.isAbstract()){
                                for(IClass c : cha.computeSubClasses(entry.getDeclaringClass().getReference())){
                                    if(!c.isInterface())
                                        for(IMethod cm : c.getDeclaredMethods()){
                                            if(cm.getSelector().equals(entry.getSelector()) && !cm.isAbstract()){
                                                bridgeEntries.add(entry);
                                                entries.add(new ConcreteTypeParamEntryPoint(new AndroidEntryPoint.ExecutionOrder(MIDDLE_OF_LOOP_ORDER++), entry, cha));
                                            }
                                        }
                                }
                            }else {
                                bridgeEntries.add(entry);
                                entries.add(new ConcreteTypeParamEntryPoint(new AndroidEntryPoint.ExecutionOrder(MIDDLE_OF_LOOP_ORDER++), entry, cha));
                            }
                        }
                    }
                }
        }

        // attach init instructions to entry model
        for(int i=0; i<initInsts.length; i++){
            InitInstsParser.InitInst initInst = initInsts[i];
            entries.addAll(findNearestEntries(cha, initInst.getReceiverType().getName(), initInst.getMethodSelector()));
        }

        //attach Activity lifecycle invocation to entry model
        Set<IClass> activities = findActivities(cha);
        Selector onCreate = Selector.make("onCreate(Landroid/os/Bundle;)V");
        Selector onResume = Selector.make("onResume()V");
        Selector onStart = Selector.make("onStart()V");
        Selector onPause = Selector.make("onPause()V");
        Selector onStop = Selector.make("onStop()V");
        Selector onDestroy = Selector.make("onDestroy()V");

        for(IClass activity : activities){
            entries.add(new ConcreteTypeParamEntryPoint(new AndroidEntryPoint.ExecutionOrder(AT_FIRST_ORDER++), activity, activity.getMethod(onCreate), cha));
            entries.add(new ConcreteTypeParamEntryPoint(new AndroidEntryPoint.ExecutionOrder(AT_FIRST_ORDER++), activity, activity.getMethod(onStart), cha));
            entries.add(new ConcreteTypeParamEntryPoint(new AndroidEntryPoint.ExecutionOrder(AT_FIRST_ORDER++), activity, activity.getMethod(onResume), cha));
            entries.add(new ConcreteTypeParamEntryPoint(new AndroidEntryPoint.ExecutionOrder(AT_FIRST_ORDER++), activity, activity.getMethod(onPause), cha));
            entries.add(new ConcreteTypeParamEntryPoint(new AndroidEntryPoint.ExecutionOrder(AT_FIRST_ORDER++), activity, activity.getMethod(onStop), cha));
            entries.add(new ConcreteTypeParamEntryPoint(new AndroidEntryPoint.ExecutionOrder(AT_FIRST_ORDER++), activity, activity.getMethod(onDestroy), cha));
        }

        //JVM 1.8
        List<AndroidEntryPoint> entryList = new ArrayList<>();
        entryList.addAll(entries);
        Collections.sort(entryList, (AndroidEntryPoint o1, AndroidEntryPoint o2) -> o1.order.compareTo(o2.order));

        System.out.println("##################### ENTRIES #####################");
        for(AndroidEntryPoint aep : entryList){
            System.out.println("#Entry: " + aep);
        }
        System.out.println("###################################################");
        //JVM 1.8
        return () -> entryList.iterator();
    }

    /**
     * Find methods from a class and make entry points using the methods.
     * @param cha ClassHierarchy
     * @param klassName a target class that have the methods
     * @param methods a list of target methods
     * @return
     */
    private static Set<ConcreteTypeParamEntryPoint> findNearestEntries(IClassHierarchy cha, TypeName klassName, Selector... methods){
        Set<ConcreteTypeParamEntryPoint> res = new HashSet<>();

        for(IClass c: cha){
//            if(c.toString().contains("TJPlacement"))
//                findNearestMethod(c, null);
            if(c.getReference().getName().equals(klassName)){
                for(int i=0; i<methods.length; i++){
                    res.add(new ConcreteTypeParamEntryPoint(new AndroidEntryPoint.ExecutionOrder(AT_FIRST_ORDER++), c, findNearestMethod(c, methods[i]), cha));
                }
            }
        }
//        System.exit(-1);
        return res;
    }

    /**
     * Find a target method of a class. If the class does not have the method, find the target tracking the parentclass of the class.
     * @param klass a target class of a method
     * @param method a method selector is need to be found
     * @return
     */
    private static IMethod findNearestMethod(IClass klass, Selector method){
        IMethod m = klass.getMethod(method);
        return m;
    }

    /**
     * Find subclasses of Activities. The targets are only Activities of Application.
     * @param cha ClassHierarchy
     * @return subclasses of Activities
     */
    private static Set<IClass> findActivities(IClassHierarchy cha){
        TypeReference activityTR = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/app/Activity");
        IClass activityClass = cha.lookupClass(activityTR);

        Set<IClass> activities = new HashSet<>();

        for(IClass c : cha){
            if(c.getClassLoader().getReference().equals(ClassLoaderReference.Application) &&
                    cha.isSubclassOf(c, activityClass))
                activities.add(c);

        }

        return activities;
    }

    /**
     * Get bridge methods.
     * @return a set of bridge method
     */
    public static Set<IMethod> getBridgeEntries(){
        return bridgeEntries;
    }
}
