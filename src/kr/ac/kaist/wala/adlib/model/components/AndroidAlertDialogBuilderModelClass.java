package kr.ac.kaist.wala.adlib.model.components;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.ipa.summaries.SummarizedMethodWithNames;
import com.ibm.wala.ipa.summaries.VolatileMethodSummary;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.ssa.ConstantValue;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.*;
import com.ibm.wala.util.ssa.SSAValue;
import com.ibm.wala.util.ssa.TypeSafeInstructionFactory;
import kr.ac.kaist.wala.adlib.model.ModelClass;

import java.util.ArrayList;
import java.util.List;

/**
 * A modeling class for Android built-in android/app/AlertDialog$Builder.
 * Created by leesh on 14/01/2017.
 */
public class AndroidAlertDialogBuilderModelClass extends ModelClass {
    public static final TypeReference ANDROID_ALERT_DIALOG_BUILDER_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Landroid/app/AlertDialog$Builder"));

    public static final Selector SHOW_SELECTOR = Selector.make("show()Landroid/app/AlertDialog;");
    public static final Selector ON_CLICK_SELECTOR = Selector.make("onClick(Landroid/content/DialogInterface;I)V");
    public static final FieldReference CONTROLLER__FIELD = FieldReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/AlertDialog$Builder", "P", "Lcom/android/internal/app/AlertController$AlertParams");
    public static final FieldReference POSITIVE_BTN_CLICK_FIELD = FieldReference.findOrCreate(ClassLoaderReference.Primordial, "Lcom/android/internal/app/AlertController$AlertParams", "mPositiveButtonListener", "Landroid/content/DialogInterface$OnClickListener");
    public static final FieldReference NEGATIVE_BTN_CLICK_FIELD = FieldReference.findOrCreate(ClassLoaderReference.Primordial, "Lcom/android/internal/app/AlertController$AlertParams", "mNegativeButtonListener", "Landroid/content/DialogInterface$OnClickListener");

    private IClassHierarchy cha;

    private static AndroidAlertDialogBuilderModelClass klass;

    public static AndroidAlertDialogBuilderModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new AndroidAlertDialogBuilderModelClass(cha);
        }
        return klass;
    }

    private AndroidAlertDialogBuilderModelClass(IClassHierarchy cha) {
        super(ANDROID_ALERT_DIALOG_BUILDER_MODEL_CLASS, cha);
        this.cha = cha;

        initMethodsForThread();

        addMethod(this.clinit());
    }

    private void initMethodsForThread(){
        addMethod(this.show(SHOW_SELECTOR));
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *
     *  run call Runnable's run method
     */
    private SummarizedMethod show(Selector s) {
        final MethodReference showRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary show = new VolatileMethodSummary(new MethodSummary(showRef));
        show.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue thisV = new SSAValue(1, ANDROID_ALERT_DIALOG_BUILDER_MODEL_CLASS, showRef);

        int ssaNo = 5;

        show.addConstant(ssaNo++, new ConstantValue(0));
        final SSAValue nullV = new SSAValue(ssaNo-1, TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/content/DialogInterface"), showRef);
        show.addConstant(ssaNo++, new ConstantValue(0));
        final SSAValue zeroV = new SSAValue(ssaNo-1, TypeReference.Int, showRef);

        TypeReference controllerTR = CONTROLLER__FIELD.getFieldType();

        final SSAValue controllerV = new SSAValue(ssaNo++, controllerTR, showRef);
        final int pc_get_controller = show.getNextProgramCounter();
        final SSAInstruction getP = instructionFactory.GetInstruction(pc_get_controller, controllerV, thisV, CONTROLLER__FIELD);
        show.addStatement(getP);


        TypeReference positiveTR = POSITIVE_BTN_CLICK_FIELD.getFieldType();

        final SSAValue positiveV = new SSAValue(ssaNo++, positiveTR, showRef);
        final int pc_get_positive = show.getNextProgramCounter();
        final SSAInstruction getPositive = instructionFactory.GetInstruction(pc_get_positive, positiveV, controllerV, POSITIVE_BTN_CLICK_FIELD);
        show.addStatement(getPositive);

        //TODO: call positive onClick
        final int pc_call_positive = show.getNextProgramCounter();
        final MethodReference onClickMR = MethodReference.findOrCreate(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/content/DialogInterface$OnClickListener"), ON_CLICK_SELECTOR);
        final List<SSAValue> paramsPositive = new ArrayList<SSAValue>();
        paramsPositive.add(positiveV);
        paramsPositive.add(nullV);
        paramsPositive.add(zeroV);
        final SSAValue exceptionPos = new SSAValue(ssaNo++, TypeReference.JavaLangException, showRef);
        final CallSiteReference sitePos = CallSiteReference.make(pc_call_positive, onClickMR, IInvokeInstruction.Dispatch.VIRTUAL);
        final SSAInstruction runPosCall = instructionFactory.InvokeInstruction(pc_call_positive, paramsPositive, exceptionPos, sitePos);
        show.addStatement(runPosCall);


        TypeReference negativeTR = NEGATIVE_BTN_CLICK_FIELD.getFieldType();

        final SSAValue negativeV = new SSAValue(ssaNo++, negativeTR, showRef);
        final int pc_get_negative = show.getNextProgramCounter();
        final SSAInstruction getNegative = instructionFactory.GetInstruction(pc_get_negative, negativeV, controllerV, NEGATIVE_BTN_CLICK_FIELD);
        show.addStatement(getNegative);

        //TODO: call negative onClick
        final int pc_call_negative = show.getNextProgramCounter();
        final List<SSAValue> paramsNegative = new ArrayList<SSAValue>();
        paramsNegative.add(negativeV);
        paramsNegative.add(nullV);
        paramsNegative.add(zeroV);
        final SSAValue exceptionNeg = new SSAValue(ssaNo++, TypeReference.JavaLangException, showRef);
        final CallSiteReference siteNeg = CallSiteReference.make(pc_call_negative, onClickMR, IInvokeInstruction.Dispatch.VIRTUAL);
        final SSAInstruction runNegCall = instructionFactory.InvokeInstruction(pc_call_negative, paramsNegative, exceptionNeg, siteNeg);
        show.addStatement(runNegCall);

        final int pc_ret = show.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret);
        show.addStatement(retInst);

        return new SummarizedMethodWithNames(showRef, show, this);
    }

    private SummarizedMethod clinit() {
        final MethodReference clinitRef = MethodReference.findOrCreate(this.getReference(), MethodReference.clinitSelector);
        final VolatileMethodSummary clinit = new VolatileMethodSummary(new MethodSummary(clinitRef));
        clinit.setStatic(true);

        return new SummarizedMethodWithNames(clinitRef, clinit, this);
    }
}
