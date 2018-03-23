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
import java.util.List;

/**
 * Created by leesh on 14/01/2017.
 */
public class JavaThreadPoolExecutorModelClass extends ModelClass {


    public static final TypeReference JAVA_THREAD_POOL_EXECUTOR_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Ljava/util/concurrent/ThreadPoolExecutor"));


    public static final TypeName RUNNABLE_TYPE_NAME = TypeName.string2TypeName("Ljava/lang/Runnable");
    public static final Selector RUN_SELECTOR = Selector.make("run()V");

    public static Selector EXECUTE_SELECTOR = Selector.make("execute(Ljava/lang/Runnable;)V");

    private IClassHierarchy cha;

    static private JavaThreadPoolExecutorModelClass klass;

    public static JavaThreadPoolExecutorModelClass getInstance(IClassHierarchy cha) {
        if(klass == null)
            klass = new JavaThreadPoolExecutorModelClass(cha);
        return klass;
    }

    private JavaThreadPoolExecutorModelClass(IClassHierarchy cha) {
        super(JAVA_THREAD_POOL_EXECUTOR_MODEL_CLASS, cha);
        this.cha = cha;

        initMethodsForThreadPoolExecutor();

        this.addMethod(this.clinit());
    }

    private void initMethodsForThreadPoolExecutor(){ this.addMethod(this.executeOfThreadPoolExecutor(EXECUTE_SELECTOR));}
    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *
     *  run call Runnable's run method
     */
    private SummarizedMethod executeOfThreadPoolExecutor(Selector s) {
        final MethodReference runRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary run = new VolatileMethodSummary(new MethodSummary(runRef));
        run.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = 2;

        TypeReference timerTaskTR = TypeReference.findOrCreate(ClassLoaderReference.Application, RUNNABLE_TYPE_NAME);

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
