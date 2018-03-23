package kr.ac.kaist.wala.adlib.model.thread;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.ipa.summaries.SummarizedMethodWithNames;
import com.ibm.wala.ipa.summaries.VolatileMethodSummary;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.*;
import com.ibm.wala.util.ssa.SSAValue;
import com.ibm.wala.util.ssa.TypeSafeInstructionFactory;
import kr.ac.kaist.wala.adlib.model.ModelClass;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A modeling class for Java built-in java/util/Timer.
 * Created by leesh on 14/01/2017.
 */
public class JavaTimerModelClass extends ModelClass {


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

        final int pc_ret = run.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret);
        run.addStatement(retInst);

        return new SummarizedMethodWithNames(runRef, run, this);
    }

    private SummarizedMethod clinit() {
        final MethodReference clinitRef = MethodReference.findOrCreate(this.getReference(), MethodReference.clinitSelector);
        final VolatileMethodSummary clinit = new VolatileMethodSummary(new MethodSummary(clinitRef));
        clinit.setStatic(true);

        return new SummarizedMethodWithNames(clinitRef, clinit, this);
    }
}
