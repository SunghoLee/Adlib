package kr.ac.kaist.wala.hybridroid.ardetector.model.thread;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.ipa.summaries.SummarizedMethodWithNames;
import com.ibm.wala.ipa.summaries.VolatileMethodSummary;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.shrikeCT.ClassConstants;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.*;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.ssa.SSAValue;
import com.ibm.wala.util.ssa.TypeSafeInstructionFactory;
import com.ibm.wala.util.strings.Atom;

import java.util.*;

/**
 * Created by leesh on 14/01/2017.
 */
public class JavaTimerModelClass extends SyntheticClass{


    public static final TypeReference JAVA_TIMER_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Ljava/util/Timer"));

    public static final Selector RUN_SELECTOR = Selector.make("run()V");

    public static final TypeName TIMERTASK_TYPE_NAME = TypeName.string2TypeName("Ljava/util/TimerTask");
    public static Selector SCHEDULE_AT_FIXED_RATE_SELECTOR1 = Selector.make("scheduleAtFixedRate(Ljava/util/TimerTask;JJ)V");
    public static Selector SCHEDULE_AT_FIXED_RATE_SELECTOR2 = Selector.make("scheduleAtFixedRate(Ljava/util/TimerTask;Ljava/util/Date;J)V");
    public static Selector SCHEDULE_SELECTOR1 = Selector.make("schedule(Ljava/util/TimerTask;J)V");
    public static Selector SCHEDULE_SELECTOR2 = Selector.make("schedule(Ljava/util/TimerTask;JJ)V");
    public static Selector SCHEDULE_SELECTOR3 = Selector.make("schedule(Ljava/util/TimerTask;Ljava/util/Date;)V");
    public static Selector SCHEDULE_SELECTOR4 = Selector.make("schedule(Ljava/util/TimerTask;Ljava/util/Date;J)V");

    private static Set<Selector> RUN_SELECTOR_SET;

    static{
        RUN_SELECTOR_SET = new HashSet<>();
        RUN_SELECTOR_SET.add(SCHEDULE_AT_FIXED_RATE_SELECTOR1);
        RUN_SELECTOR_SET.add(SCHEDULE_AT_FIXED_RATE_SELECTOR2);
        RUN_SELECTOR_SET.add(SCHEDULE_SELECTOR1);
        RUN_SELECTOR_SET.add(SCHEDULE_SELECTOR2);
        RUN_SELECTOR_SET.add(SCHEDULE_SELECTOR3);
        RUN_SELECTOR_SET.add(SCHEDULE_SELECTOR4);
    }

    private IClassHierarchy cha;

    private static JavaTimerModelClass klass;

    public static JavaTimerModelClass getInstance(IClassHierarchy cha) {
        if(klass == null)
            klass = new JavaTimerModelClass(cha);
        return klass;
    }

    private JavaTimerModelClass(IClassHierarchy cha) {
        super(JAVA_TIMER_MODEL_CLASS, cha);
        this.cha = cha;

        initMethodsForTimeTask();

        this.addMethod(this.clinit());
    }

    private void initMethodsForTimeTask(){
        for(Selector s : RUN_SELECTOR_SET)
            this.addMethod(this.runOfTimeTask(s));
    }

    /**
     *  Generate schedule of Timer for AndroidThreadModelClass.
     *
     *  run call TimerTask's run method
     */
    private SummarizedMethod runOfTimeTask(Selector s) {
        final MethodReference runRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary run = new VolatileMethodSummary(new MethodSummary(runRef));
        run.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = 2;

        TypeReference timerTaskTR = TypeReference.findOrCreate(ClassLoaderReference.Application, TIMERTASK_TYPE_NAME);

        final SSAValue timerTaskV = new SSAValue(ssaNo++, timerTaskTR, runRef);
        final int pc = run.getNextProgramCounter();
        final MethodReference timerTaskRunMR = MethodReference.findOrCreate(timerTaskTR, RUN_SELECTOR);
        final List<SSAValue> params = new ArrayList<SSAValue>();
        params.add(timerTaskV);
        final SSAValue exception = new SSAValue(ssaNo++, TypeReference.JavaLangException, runRef);
        final CallSiteReference site = CallSiteReference.make(pc, timerTaskRunMR, IInvokeInstruction.Dispatch.VIRTUAL);

        final SSAInstruction runCall = instructionFactory.InvokeInstruction(pc, params, exception, site);
        run.addStatement(runCall);

        return new SummarizedMethodWithNames(runRef, run, this);
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
