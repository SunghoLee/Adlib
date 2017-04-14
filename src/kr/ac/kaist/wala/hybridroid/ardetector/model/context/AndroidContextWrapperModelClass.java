package kr.ac.kaist.wala.hybridroid.ardetector.model.context;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.ipa.summaries.SummarizedMethodWithNames;
import com.ibm.wala.ipa.summaries.VolatileMethodSummary;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.shrikeCT.ClassConstants;
import com.ibm.wala.ssa.ConstantValue;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.*;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.ssa.SSAValue;
import com.ibm.wala.util.ssa.TypeSafeInstructionFactory;
import com.ibm.wala.util.strings.Atom;

import java.util.*;

/**
 * A modeling class for Android built-in android/content/ContextWrapper.
 * Created by leesh on 16/01/2017.
 */
public class AndroidContextWrapperModelClass extends SyntheticClass {
    public enum SystemService{
        LOCATION_SERVICE_MANAGER("location"),
        WINDOW_SERVICE_MANAGER("window"),
        LAYOUT_INFLATER_SERVICE_MANAGER("layout_inflater"),
        ACTIVITY_SERVICE_MANAGER("activity"),
        POWER_SERVICE_MANAGER("power"),
        ALARM_SERVICE_MANAGER("alarm"),
        NOTIFICATION_SERVICE_MANAGER("notification"),
        KEYGUARD_SERVICE_MANAGER("keyguard"),
        SEARCH_SERVICE_MANAGER("search"),
        VIBRATOR_SERVICE_MANAGER("vibrator"),
        CONNECTIVITY_SERVICE_MANAGER("connection"),
        WIFI_SERVICE_MANAGER("wifi"),
        WIFI_P2P_SERVICE_MANAGER("wifip2p"),
        UI_MODE_SERVICE_MANAGER("uimode"),
        DOWNLOAD_SERVICE_MANAGER("download"),
        BATTERY_SERVICE_MANAGER("batterymanager"),
        JOB_SCHEDULER_SERVICE_MANAGER("taskmanager"),
        NETWORK_STATS_SERVICE_MANAGER("netstats"),
        INPUT_METHOD_SERVICE_MANAGER("input_method"),
        HARDWARE_PROPERTIES_SERVICE_MANAGER("hardware_properties");

        private final String name;
        private int num;

        private SystemService(String name){
            this.name = name;
        }

        public void setSSA(int num){
            this.num = num;
        }

        public int getSSA(){
            return this.num;
        }
        public String getName(){
            return name;
        }

        @Override
        public String toString(){
            return name;
        }
    }

    public static boolean isSubClassOfContextWrapper(IClass k){
        if(k == null)
            return false;
        if(k.getName().equals(AndroidContextWrapperModelClass.ANDROID_CONTEXT_WRAPPER_MODEL_CLASS.getName()))
            return true;

        return isSubClassOfContextWrapper(k.getSuperclass());
    }

    public static final TypeReference ANDROID_CONTEXT_WRAPPER_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Landroid/content/ContextWrapper"));

    public static TypeName CONTEXT_TYPE = TypeName.findOrCreate("Landroid/content/Context");
    public static Selector GETSYSTEMSERVICE_SELECTOR = Selector.make("getSystemService(Ljava/lang/String;)Ljava/lang/Object;");
    public static Selector GETAPPLICATIONCONTEXT_SELECTOR = Selector.make("getApplicationContext()Landroid/content/Context;");

    private IClassHierarchy cha;

    private static AndroidContextWrapperModelClass klass;
    public static AndroidContextWrapperModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new AndroidContextWrapperModelClass(cha);
            return klass;
        }

        return klass;
    }

    private AndroidContextWrapperModelClass(IClassHierarchy cha) {
        super(ANDROID_CONTEXT_WRAPPER_MODEL_CLASS, cha);
        this.cha = cha;

        initMethods();
        this.addMethod(this.clinit());
//        this.cha.addClass(this);
    }

    private void initMethods(){
        this.addMethod(getSystemService(GETSYSTEMSERVICE_SELECTOR));
        this.addMethod(getApplicationContext(GETAPPLICATIONCONTEXT_SELECTOR));
    }

    private SummarizedMethod getApplicationContext(Selector s){
        final MethodReference getACRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary getAC = new VolatileMethodSummary(new MethodSummary(getACRef));
        getAC.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        //TODO: can we finally change TypeSafeInstructionFactory with JavaInstructionFactory?
        //this instruction factory is only for type cast
        final JavaLanguage.JavaInstructionFactory jvInstructionFactory = new JavaLanguage.JavaInstructionFactory();

        SSAValue receiverActV = new SSAValue(1, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/app/Application"), getACRef);
//        SSAValue castedV = new SSAValue(2, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/content/Context"), getACRef);
//        SSAInstruction castInst = jvInstructionFactory.ConversionInstruction(getAC.getNextProgramCounter(), 2, 1, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/content/ContextWrapper"), TypeReference.find(ClassLoaderReference.Primordial, "Landroid/content/Context"), false);
//        getAC.addStatement(castInst);
        SSAInstruction returnInst = instructionFactory.ReturnInstruction(getAC.getNextProgramCounter(), receiverActV);
        getAC.addStatement(returnInst);

        return new SummarizedMethodWithNames(getACRef, getAC, this);
    }

    private SummarizedMethod getSystemService(Selector s){
        final MethodReference getSSRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary getSS = new VolatileMethodSummary(new MethodSummary(getSSRef));
        getSS.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        /*
        v1 : Context (receiver)
        v2 : argument
        v3 : true (constant)
        v4 : "location (constant)
        v5 : "window" (constant)
        v6 : "layout_inflater" (constant)
        v7 : "activity" (constant)
        v8 : "power" (constant)
        v9 : "alarm" (constant)
        v10 : "notification" (constant)
        v11 : "keyguard" (constant)
        v12 : "search" (constant)
        v13 : "vibrator" (constant)
        v14 : "connection" (constant)
        v15 : "wifi" (constant)
        v16 : "wifip2p" (constant)
        v17 : "uimode" (constant)
        v18 : "download" (constant)
        v19 : "batterymanager" (constant)
        v20 : "taskmanager" (constant)
        v21 : "netstats" (constant)
        v22 : "input_method" (constant)
        v23 : "hardware_properties" (constant)
         */
        int ssaNo = 4;
        final SSAValue paramStringV = new SSAValue(2, TypeReference.JavaLangString, getSSRef);

        getSS.addConstant(3, new ConstantValue(Boolean.TRUE));
        //for conditional branch about equals that compare the input parameter with service name

        for(SystemService service : SystemService.values()){
            service.setSSA(ssaNo);
            getSS.addConstant(ssaNo++, new ConstantValue(service.getName()));
        }

        int CBTargetPC = SystemService.values().length * 2;
//        List<SSAInstruction> insts = new ArrayList<>();
        for(SystemService service : SystemService.values()){
            final SSAValue serviceStringV = new SSAValue(service.getSSA(), TypeReference.JavaLangString, getSSRef);
            final SSAValue equalsRetV = new SSAValue(ssaNo++, TypeReference.Boolean, getSSRef);
            final SSAValue exception = new SSAValue(ssaNo++, TypeReference.JavaLangException, getSSRef);
            final MethodReference equalsMR = MethodReference.findOrCreate(TypeReference.JavaLangString, Selector.make("equals(Ljava/lang/Object;)Z >"));
            final List<SSAValue> equalsParams = new ArrayList<SSAValue>();
            equalsParams.add(paramStringV);
            equalsParams.add(serviceStringV);
            final int callEqualsPc = getSS.getNextProgramCounter();
            final CallSiteReference equalsCallSite = CallSiteReference.make(callEqualsPc, equalsMR, IInvokeInstruction.Dispatch.VIRTUAL);
            final SSAInstruction equalsCall = instructionFactory.InvokeInstruction(callEqualsPc, equalsRetV, equalsParams, exception, equalsCallSite);
            getSS.addStatement(equalsCall);
//            insts.add(equalsCall);
            final SSAInstruction cbInst = instructionFactory.ConditionalBranchInstruction(getSS.getNextProgramCounter(), IConditionalBranchInstruction.Operator.EQ, TypeReference.Boolean, equalsRetV.getNumber(), 3, CBTargetPC);
            getSS.addStatement(cbInst);
//            insts.add(cbInst);
            CBTargetPC += 2;
        }

        for(SystemService service : SystemService.values()){
            //TODO: mapping from string constant to creation of service manager.
            NewSiteReference ssNewSiteRef = null;
            SSAValue ssV = null;
            final int newPC = getSS.getNextProgramCounter();
            switch(service){
                case ACTIVITY_SERVICE_MANAGER:
                    final TypeReference acitivtyTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/ActivityManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, acitivtyTR);
                    ssV = new SSAValue(ssaNo++, acitivtyTR, getSSRef);
                    break;
                case ALARM_SERVICE_MANAGER:
                    final TypeReference alarmTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/AlarmManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, alarmTR);
                    ssV = new SSAValue(ssaNo++, alarmTR, getSSRef);
                    break;
                case BATTERY_SERVICE_MANAGER:
                    final TypeReference batteryTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/os/BatteryManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, batteryTR);
                    ssV = new SSAValue(ssaNo++, batteryTR, getSSRef);
                    break;
                case CONNECTIVITY_SERVICE_MANAGER:
                    final TypeReference connectivityTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/net/ConnectivityManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, connectivityTR);
                    ssV = new SSAValue(ssaNo++, connectivityTR, getSSRef);
                    break;
                case DOWNLOAD_SERVICE_MANAGER:
                    final TypeReference downloadTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/DownloadManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, downloadTR);
                    ssV = new SSAValue(ssaNo++, downloadTR, getSSRef);
                    break;
                case HARDWARE_PROPERTIES_SERVICE_MANAGER:
                    final TypeReference hardwareTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/os/HardwarePropertiesManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, hardwareTR);
                    ssV = new SSAValue(ssaNo++, hardwareTR, getSSRef);
                    break;
                case INPUT_METHOD_SERVICE_MANAGER:
                    final TypeReference inputTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/view/inputmethod/InputMethodManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, inputTR);
                    ssV = new SSAValue(ssaNo++, inputTR, getSSRef);
                    break;
                case JOB_SCHEDULER_SERVICE_MANAGER:
                    final TypeReference jobTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/job/JobScheduler");
                    ssNewSiteRef = NewSiteReference.make(newPC, jobTR);
                    ssV = new SSAValue(ssaNo++, jobTR, getSSRef);
                    break;
                case KEYGUARD_SERVICE_MANAGER:
                    final TypeReference keyguardTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/KeyguardManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, keyguardTR);
                    ssV = new SSAValue(ssaNo++, keyguardTR, getSSRef);
                    break;
                case LAYOUT_INFLATER_SERVICE_MANAGER:
                    final TypeReference layoutTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/view/LayoutInflater");
                    ssNewSiteRef = NewSiteReference.make(newPC, layoutTR);
                    ssV = new SSAValue(ssaNo++, layoutTR, getSSRef);
                    break;
                case LOCATION_SERVICE_MANAGER:
                    final TypeReference locationTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/location/LocationManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, locationTR);
                    ssV = new SSAValue(ssaNo++, locationTR, getSSRef);
                    break;
                case NETWORK_STATS_SERVICE_MANAGER:
                    final TypeReference networkTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/usage/NetworkStatsManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, networkTR);
                    ssV = new SSAValue(ssaNo++, networkTR, getSSRef);
                    break;
                case NOTIFICATION_SERVICE_MANAGER:
                    final TypeReference notificationTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/NotificationManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, notificationTR);
                    ssV = new SSAValue(ssaNo++, notificationTR, getSSRef);
                    break;
                case POWER_SERVICE_MANAGER:
                    final TypeReference powerTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/os/PowerManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, powerTR);
                    ssV = new SSAValue(ssaNo++, powerTR, getSSRef);
                    break;
                case SEARCH_SERVICE_MANAGER:
                    final TypeReference searchTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/SearchManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, searchTR);
                    ssV = new SSAValue(ssaNo++, searchTR, getSSRef);
                    break;
                case UI_MODE_SERVICE_MANAGER:
                    final TypeReference uiTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/UiModeManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, uiTR);
                    ssV = new SSAValue(ssaNo++, uiTR, getSSRef);
                    break;
                case VIBRATOR_SERVICE_MANAGER:
                    final TypeReference vibratorTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/os/Vibrator");
                    ssNewSiteRef = NewSiteReference.make(newPC, vibratorTR);
                    ssV = new SSAValue(ssaNo++, vibratorTR, getSSRef);
                    break;
                case WIFI_P2P_SERVICE_MANAGER:
                    final TypeReference wifiP2pTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/net/wifi/p2p/WifiP2pManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, wifiP2pTR);
                    ssV = new SSAValue(ssaNo++, wifiP2pTR, getSSRef);
                    break;
                case WIFI_SERVICE_MANAGER:
                    final TypeReference wifiTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/net/wifi/WifiManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, wifiTR);
                    ssV = new SSAValue(ssaNo++, wifiTR, getSSRef);
                    break;
                case WINDOW_SERVICE_MANAGER:
                    final TypeReference windowTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/view/WindowManager");
                    ssNewSiteRef = NewSiteReference.make(newPC, windowTR);
                    ssV = new SSAValue(ssaNo++, windowTR, getSSRef);
                    break;
                default:
                    Assertions.UNREACHABLE("The system service is not supported: " + service);
            }
            SSAInstruction newInst = instructionFactory.NewInstruction(newPC, ssV, ssNewSiteRef);
            getSS.addStatement(newInst);
//            insts.add(newInst);

            SSAInstruction returnInst = instructionFactory.ReturnInstruction(getSS.getNextProgramCounter(), ssV);
            getSS.addStatement(returnInst);
//            insts.add(returnInst);
        }

        //for debugging
//        for(SSAInstruction i : insts){
//            System.out.println(i.iindex + ") " + i);
//        }

        return new SummarizedMethodWithNames(getSSRef, getSS, this);
    }

    private SummarizedMethod clinit() {
        final MethodReference clinitRef = MethodReference.findOrCreate(this.getReference(), MethodReference.clinitSelector);
        final VolatileMethodSummary clinit = new VolatileMethodSummary(new MethodSummary(clinitRef));
        clinit.setStatic(true);

        return new SummarizedMethodWithNames(clinitRef, clinit, this);
    }

    private Map<Selector, IMethod> methods = HashMapFactory.make(); // does not contain macroModel

    @Override
    public IMethod getMethod(Selector selector) {
        //assert (macroModel != null) : "Macro Model was not set yet!";
        if(methods.containsKey(selector)){
            return methods.get(selector);
        }
        throw new IllegalArgumentException("Could not resolve " + selector);
    }

    @Override
    public Collection<IMethod> getDeclaredMethods() {
        Set<IMethod> methods = HashSetFactory.make();
        methods.addAll(this.methods.values());

        return Collections.unmodifiableCollection(methods);
    }

    @Override
    public Collection<IMethod> getAllMethods()  {
        return getDeclaredMethods();
    }

    public void addMethod(IMethod method) {
        if (this.methods.containsKey(method.getSelector())) {
            // TODO: Check this matches on signature not on contents!
            // TODO: What on different Context versions
            throw new IllegalStateException("The AndroidThreadModelClass already contains a Method called " + method.getName());
        }
        assert(this.methods != null);
        this.methods.put(method.getSelector(), method);
    }

    @Override
    public IMethod getClassInitializer()  {
        return getMethod(MethodReference.clinitSelector);
    }

    //
    //  Contents of the class: Fields
    //  We have none...
    //
    private Map<Atom, IField> fields = new HashMap<Atom, IField>();

    @Override
    public IField getField(Atom name) {
        if (fields.containsKey(name)) {
            return fields.get(name);
        } else {
            return null;
        }
    }

    public void putField(Atom name, TypeReference type) {
        final FieldReference fdRef = FieldReference.findOrCreate(this.getReference(), name, type);
        final int accessFlags = ClassConstants.ACC_STATIC | ClassConstants.ACC_PUBLIC;
        final IField field = new FieldImpl(this, fdRef, accessFlags, null, null);

        this.fields.put(name, field);
    }

    /**
     *  This class does not contain any fields.
     */
    @Override
    public Collection<IField> getAllFields()  {
        return fields.values();
    }

    /**
     *  This class does not contain any fields.
     */
    @Override
    public Collection<IField> getDeclaredStaticFields() {
        return fields.values();
    }

    /**
     *  This class does not contain any fields.
     */
    @Override
    public Collection<IField> getAllStaticFields() {
        return fields.values();
    }

    /**
     *  This class does not contain any fields.
     */
    @Override
    public Collection<IField> getDeclaredInstanceFields() throws UnsupportedOperationException {
        return Collections.emptySet();
    }

    /**
     *  This class does not contain any fields.
     */
    @Override
    public Collection<IField> getAllInstanceFields()  {
        return Collections.emptySet();
    }



    //
    //  Class Modifiers
    //

    /**
     *  This is a public final class.
     */
    @Override
    public int getModifiers() {
        return  ClassConstants.ACC_PUBLIC |
                ClassConstants.ACC_FINAL;
    }
    @Override
    public boolean isPublic() {         return true;  }
    @Override
    public boolean isPrivate() {        return false; }
    @Override
    public boolean isInterface() {      return false; }
    @Override
    public boolean isAbstract() {       return false; }
    @Override
    public boolean isArrayClass () {    return false; }

    /**
     *  This is a subclass of the root class.
     */
    @Override
    public IClass getSuperclass() throws UnsupportedOperationException {
        return getClassHierarchy().getRootClass();
    }

    /**
     *  This class does not impement any interfaces.
     */
    @Override
    public Collection<IClass> getAllImplementedInterfaces() {
        return Collections.emptySet();
    }

    @Override
    public Collection<IClass> getDirectInterfaces() {
        return Collections.emptySet();
    }

    //
    //  Misc
    //

    @Override
    public boolean isReferenceType() {
        return getReference().isReferenceType();
    }
}
