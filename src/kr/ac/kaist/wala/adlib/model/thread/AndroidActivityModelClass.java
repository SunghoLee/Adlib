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
 * A modeling class for Android built-in android/view/View.
 * Created by leesh on 14/01/2017.
 */
public class AndroidActivityModelClass extends ModelClass {


    public static final TypeReference ANDROID_VIEW_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Landroid/app/Activity"));

    public static final TypeName RUNNABLE_TYPE_NAME = TypeName.string2TypeName("Ljava/lang/Runnable");
    public static final Selector RUN_ON_UI_THREAD_SELECTOR = Selector.make("runOnUiThread(Ljava/lang/Runnable;)V");

    private IClassHierarchy cha;

    private static AndroidActivityModelClass klass;

    public static AndroidActivityModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new AndroidActivityModelClass(cha);
        }
        return klass;
    }

    private AndroidActivityModelClass(IClassHierarchy cha) {
        super(ANDROID_VIEW_MODEL_CLASS, cha);
        this.cha = cha;

        initMethodsForThread();

        this.addMethod(this.clinit());
    }

    private void initMethodsForThread(){
        this.addMethod(this.runOfRunnable(RUN_ON_UI_THREAD_SELECTOR));
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *
     *  run call Runnable's run method
     */
    private SummarizedMethod runOfRunnable(Selector s) {
        final MethodReference runRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary run = new VolatileMethodSummary(new MethodSummary(runRef));
        run.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = 2;

        TypeReference runnalbeTR = TypeReference.findOrCreate(ClassLoaderReference.Application, RUNNABLE_TYPE_NAME);

        final SSAValue runnalbeV = new SSAValue(ssaNo++, runnalbeTR, runRef);
        final int pc = run.getNextProgramCounter();
        final MethodReference runMR = MethodReference.findOrCreate(runnalbeTR, Selector.make("run()V"));
        final List<SSAValue> params = new ArrayList<SSAValue>();
        params.add(runnalbeV);
        final SSAValue exception = new SSAValue(ssaNo++, TypeReference.JavaLangException, runRef);
        final CallSiteReference site = CallSiteReference.make(pc, runMR, IInvokeInstruction.Dispatch.VIRTUAL);
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
}
