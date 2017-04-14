package kr.ac.kaist.wala.hybridroid.ardetector.model.components;

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
 * A modeling class for Android built-in android/os/Handler.
 * Created by leesh on 14/01/2017.
 */
public class AndroidHandlerModelClass extends SyntheticClass{


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
    public static final TypeReference ANDROID_HANDLER_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Landroid/os/Handler"));
    public static final TypeReference ANDROID_MESSAGE_TYPE = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Landroid/os/Message"));
    public static final TypeName RUNNABLE_TYPE_NAME = TypeName.string2TypeName("Ljava/lang/Runnable");
    public static final Selector SEND_MESSAGE_SELECTOR = Selector.make("sendMessage(Landroid/os/Message;)Z");
    public static final Selector HANDLER_MESSAGE_SELECTOR = Selector.make("handleMessage(Landroid/os/Message;;)V");
    public static final Selector POST_DELAYED_SELECTOR = Selector.make("postDelayed(Ljava/lang/Runnable;J)Z");
    public static final Selector POST_SELECTOR = Selector.make("post(Ljava/lang/Runnable;)Z");
    public static final Selector RUN_SELECTOR = Selector.make("run()V");

    private IClassHierarchy cha;

    private static AndroidHandlerModelClass klass;

    public static AndroidHandlerModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new AndroidHandlerModelClass(cha);
        }
        return klass;
    }

    private AndroidHandlerModelClass(IClassHierarchy cha) {
        super(ANDROID_HANDLER_MODEL_CLASS, cha);
        this.cha = cha;

        initMethodsForThread();

        this.addMethod(this.clinit());
    }

    private void initMethodsForThread(){
        this.addMethod(this.send(SEND_MESSAGE_SELECTOR)); this.addMethod(this.postDelayed(POST_DELAYED_SELECTOR));
        this.addMethod(this.post(POST_SELECTOR));
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *
     *  run call Runnable's run method
     */
    private SummarizedMethod post(Selector s) {
        final MethodReference postRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary post = new VolatileMethodSummary(new MethodSummary(postRef));
        post.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        TypeReference runnableTR = TypeReference.findOrCreate(ClassLoaderReference.Application, RUNNABLE_TYPE_NAME);

        final SSAValue runnableV = new SSAValue(2, runnableTR, postRef);

        int ssaNo = 3;

        //TODO: call run
        final int pc_call_post = post.getNextProgramCounter();
        final MethodReference runMR = MethodReference.findOrCreate(runnableTR, RUN_SELECTOR);
        final List<SSAValue> paramsRun = new ArrayList<SSAValue>();
        paramsRun.add(runnableV);

        final SSAValue exceptionPos = new SSAValue(ssaNo++, TypeReference.JavaLangException, postRef);
        final CallSiteReference sitePos = CallSiteReference.make(pc_call_post, runMR, IInvokeInstruction.Dispatch.VIRTUAL);
        final SSAInstruction runPosCall = instructionFactory.InvokeInstruction(pc_call_post, paramsRun, exceptionPos, sitePos);
        post.addStatement(runPosCall);

        final int pc_ret = post.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret);
        post.addStatement(retInst);

        return new SummarizedMethodWithNames(postRef, post, this);
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *
     *  run call Runnable's run method
     */
    private SummarizedMethod postDelayed(Selector s) {
        final MethodReference postRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary post = new VolatileMethodSummary(new MethodSummary(postRef));
        post.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        TypeReference runnableTR = TypeReference.findOrCreate(ClassLoaderReference.Application, RUNNABLE_TYPE_NAME);

        final SSAValue runnableV = new SSAValue(2, runnableTR, postRef);

        int ssaNo = 3;

        //TODO: call run
        final int pc_call_post = post.getNextProgramCounter();
        final MethodReference runMR = MethodReference.findOrCreate(runnableTR, RUN_SELECTOR);
        final List<SSAValue> paramsRun = new ArrayList<SSAValue>();
        paramsRun.add(runnableV);

        final SSAValue exceptionPos = new SSAValue(ssaNo++, TypeReference.JavaLangException, postRef);
        final CallSiteReference sitePos = CallSiteReference.make(pc_call_post, runMR, IInvokeInstruction.Dispatch.VIRTUAL);
        final SSAInstruction runPosCall = instructionFactory.InvokeInstruction(pc_call_post, paramsRun, exceptionPos, sitePos);
        post.addStatement(runPosCall);

        final int pc_ret = post.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret);
        post.addStatement(retInst);

        return new SummarizedMethodWithNames(postRef, post, this);
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *
     *  run call Runnable's run method
     */
    private SummarizedMethod send(Selector s) {
        final MethodReference sendRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary send = new VolatileMethodSummary(new MethodSummary(sendRef));
        send.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue thisV = new SSAValue(1, ANDROID_HANDLER_MODEL_CLASS, sendRef);
        final SSAValue msgV = new SSAValue(2, ANDROID_MESSAGE_TYPE, sendRef);

        int ssaNo = 3;

        //TODO: call handleMessage
        final int pc_call_handle = send.getNextProgramCounter();
        final MethodReference handleMsgMR = MethodReference.findOrCreate(ANDROID_HANDLER_MODEL_CLASS, HANDLER_MESSAGE_SELECTOR);
        final List<SSAValue> paramsHandle = new ArrayList<SSAValue>();
        paramsHandle.add(thisV);
        paramsHandle.add(msgV);

        final SSAValue exceptionPos = new SSAValue(ssaNo++, TypeReference.JavaLangException, sendRef);
        final CallSiteReference sitePos = CallSiteReference.make(pc_call_handle, handleMsgMR, IInvokeInstruction.Dispatch.VIRTUAL);
        final SSAInstruction runPosCall = instructionFactory.InvokeInstruction(pc_call_handle, paramsHandle, exceptionPos, sitePos);
        send.addStatement(runPosCall);

        final int pc_ret = send.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret);
        send.addStatement(retInst);

        return new SummarizedMethodWithNames(sendRef, send, this);
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
