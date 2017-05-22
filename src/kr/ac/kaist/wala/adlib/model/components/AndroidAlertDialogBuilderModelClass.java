package kr.ac.kaist.wala.adlib.model.components;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.ipa.summaries.SummarizedMethodWithNames;
import com.ibm.wala.ipa.summaries.VolatileMethodSummary;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.shrikeCT.ClassConstants;
import com.ibm.wala.ssa.ConstantValue;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.*;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.ssa.SSAValue;
import com.ibm.wala.util.ssa.TypeSafeInstructionFactory;
import com.ibm.wala.util.strings.Atom;

import java.util.*;

/**
 * A modeling class for Android built-in android/app/AlertDialog$Builder.
 * Created by leesh on 14/01/2017.
 */
public class AndroidAlertDialogBuilderModelClass extends SyntheticClass{


    /*
    NNode: < Primordial, Landroid/app/AlertDialog$Builder, setNegativeButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder; > Context: Everywhere
== IR ==
< Primordial, Landroid/app/AlertDialog$Builder, setNegativeButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder; >
1   v5 = getfield < Primordial, Landroid/app/AlertDialog$Builder, P, <Primordial,Lcom/android/internal/app/AlertController$AlertParams> > v1
3   putfield v5.< Primordial, Lcom/android/internal/app/AlertController$AlertParams, mNegativeButtonText, <Primordial,Ljava/lang/CharSequence> > = v2
5   v6 = getfield < Primordial, Landroid/app/AlertDialog$Builder, P, <Primordial,Lcom/android/internal/app/AlertController$AlertParams> > v1
7   putfield v6.< Primordial, Lcom/android/internal/app/AlertController$AlertParams, mNegativeButtonListener, <Primordial,Landroid/content/DialogInterface$OnClickListener> > = v3
9   return v1


onClick(DialogInterface paramDialogInterface, int paramInt);
     */
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

        this.addMethod(this.clinit());
    }

    private void initMethodsForThread(){
        this.addMethod(this.show(SHOW_SELECTOR));
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
