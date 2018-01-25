package kr.ac.kaist.wala.adlib.model.components;

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
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.ssa.SSAValue;
import com.ibm.wala.util.ssa.TypeSafeInstructionFactory;
import com.ibm.wala.util.strings.Atom;
import kr.ac.kaist.wala.adlib.model.AbstractModelClass;

import java.util.*;

/**
 * A modeling class for Android built-in android/os/Handler.
 * Created by leesh on 14/01/2017.
 */
public class AndroidHandlerModelClass extends AbstractModelClass {

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
    public static final Selector OBTAIN_MESSAGE_SELECTOR1 = Selector.make("obtainMessage(I)Landroid/os/Message;");
    public static final Selector OBTAIN_MESSAGE_SELECTOR2 = Selector.make("obtainMessage(III)Landroid/os/Message;");
    public static final Selector OBTAIN_MESSAGE_SELECTOR3 = Selector.make("obtainMessage(ILjava/lang/Object;)Landroid/os/Message;");
    public static final Selector OBTAIN_MESSAGE_SELECTOR4 = Selector.make("obtainMessage(IIILjava/lang/Object;)Landroid/os/Message;");

    private IClassHierarchy cha;

    private static AndroidHandlerModelClass klass;

    public static AndroidHandlerModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new AndroidHandlerModelClass(cha);
        }
        return klass;
    }

    public IMethod match(TypeReference t, Selector s){
        if(t.getName().equals(ANDROID_HANDLER_MODEL_CLASS.getName()) && methods.containsKey(s))
            return methods.get(s);

        return null;
    }

    private AndroidHandlerModelClass(IClassHierarchy cha) {
        super(ANDROID_HANDLER_MODEL_CLASS, cha);
        this.cha = cha;

        initMethodsForHandler();

        this.addMethod(this.clinit());
    }

    private void initMethodsForHandler(){
        this.addMethod(this.send(SEND_MESSAGE_SELECTOR)); this.addMethod(this.postDelayed(POST_DELAYED_SELECTOR));
        this.addMethod(this.post(POST_SELECTOR)); this.addMethod(this.obtain1(OBTAIN_MESSAGE_SELECTOR1));
        this.addMethod(this.obtain2(OBTAIN_MESSAGE_SELECTOR2)); this.addMethod(this.obtain3(OBTAIN_MESSAGE_SELECTOR3));
        this.addMethod(this.obtain4(OBTAIN_MESSAGE_SELECTOR4));
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *  for obtainMessage(IIILjava/lang/Object;)Landroid/os/Message;
     */
    private SummarizedMethod obtain4(Selector s) {
        final MethodReference obtainRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary obtain = new VolatileMethodSummary(new MethodSummary(obtainRef));
        obtain.setStatic(false);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue thisV = new SSAValue(1, ANDROID_HANDLER_MODEL_CLASS, obtainRef);
        final SSAValue whatV = new SSAValue(2, TypeReference.Int, obtainRef);
        final SSAValue arg1V = new SSAValue(3, TypeReference.Int, obtainRef);
        final SSAValue arg2V = new SSAValue(4, TypeReference.Int, obtainRef);
        final SSAValue objV = new SSAValue(5, TypeReference.JavaLangObject, obtainRef);

        final int callObtainPC = obtain.getNextProgramCounter();

        int ssaNo = 6;

        final CallSiteReference messageCSR = CallSiteReference.make(callObtainPC, MethodReference.findOrCreate(ANDROID_MESSAGE_TYPE, Selector.make("obtain(Landroid/os/Handler;)Landroid/os/Message;")), IInvokeInstruction.Dispatch.STATIC);
        final SSAValue messageV = new SSAValue(ssaNo++, ANDROID_MESSAGE_TYPE, obtainRef);

        final SSAValue exceptionPos = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final MethodReference obtainMR = MethodReference.findOrCreate(ANDROID_MESSAGE_TYPE, Selector.make("obtain(Landroid/os/Handler;)Landroid/os/Message;"));
        final List<SSAValue> paramsObtain = new ArrayList<SSAValue>();
        paramsObtain.add(thisV);
        final SSAInstruction callObtain = instructionFactory.InvokeInstruction(callObtainPC, messageV, paramsObtain, exceptionPos, messageCSR);
        obtain.addStatement(callObtain);

        final SSAInstruction putWhatInst = instructionFactory.PutInstruction(obtain.getNextProgramCounter(), messageV, whatV, FieldReference.findOrCreate(ANDROID_MESSAGE_TYPE, Atom.findOrCreateAsciiAtom("what"), TypeReference.Int));
        obtain.addStatement(putWhatInst);

        final SSAInstruction putArg1Inst = instructionFactory.PutInstruction(obtain.getNextProgramCounter(), messageV, arg1V, FieldReference.findOrCreate(ANDROID_MESSAGE_TYPE, Atom.findOrCreateAsciiAtom("arg1"), TypeReference.Int));
        obtain.addStatement(putArg1Inst);

        final SSAInstruction putArg2Inst = instructionFactory.PutInstruction(obtain.getNextProgramCounter(), messageV, arg2V, FieldReference.findOrCreate(ANDROID_MESSAGE_TYPE, Atom.findOrCreateAsciiAtom("arg2"), TypeReference.Int));
        obtain.addStatement(putArg2Inst);

        final SSAInstruction putObjInst = instructionFactory.PutInstruction(obtain.getNextProgramCounter(), messageV, objV, FieldReference.findOrCreate(ANDROID_MESSAGE_TYPE, Atom.findOrCreateAsciiAtom("obj"), TypeReference.JavaLangObject));
        obtain.addStatement(putObjInst);

        final int pc_ret = obtain.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret, messageV);
        obtain.addStatement(retInst);

        return new SummarizedMethodWithNames(obtainRef, obtain, this);
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *  for obtainMessage(ILjava/lang/Object;)Landroid/os/Message;
     */
    private SummarizedMethod obtain3(Selector s) {
        final MethodReference obtainRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary obtain = new VolatileMethodSummary(new MethodSummary(obtainRef));
        obtain.setStatic(false);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue thisV = new SSAValue(1, ANDROID_HANDLER_MODEL_CLASS, obtainRef);
        final SSAValue whatV = new SSAValue(2, TypeReference.Int, obtainRef);
        final SSAValue objV = new SSAValue(3, TypeReference.JavaLangObject, obtainRef);

        final int callObtainPC = obtain.getNextProgramCounter();

        int ssaNo = 4;

        final CallSiteReference messageCSR = CallSiteReference.make(callObtainPC, MethodReference.findOrCreate(ANDROID_MESSAGE_TYPE, Selector.make("obtain(Landroid/os/Handler;)Landroid/os/Message;")), IInvokeInstruction.Dispatch.STATIC);
        final SSAValue messageV = new SSAValue(ssaNo++, ANDROID_MESSAGE_TYPE, obtainRef);

        final SSAValue exceptionPos = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final MethodReference obtainMR = MethodReference.findOrCreate(ANDROID_MESSAGE_TYPE, Selector.make("obtain(Landroid/os/Handler;)Landroid/os/Message;"));
        final List<SSAValue> paramsObtain = new ArrayList<SSAValue>();
        paramsObtain.add(thisV);
        final SSAInstruction callObtain = instructionFactory.InvokeInstruction(callObtainPC, messageV, paramsObtain, exceptionPos, messageCSR);
        obtain.addStatement(callObtain);

        final SSAInstruction putWhatInst = instructionFactory.PutInstruction(obtain.getNextProgramCounter(), messageV, whatV, FieldReference.findOrCreate(ANDROID_MESSAGE_TYPE, Atom.findOrCreateAsciiAtom("what"), TypeReference.Int));
        obtain.addStatement(putWhatInst);

        final SSAInstruction putObjInst = instructionFactory.PutInstruction(obtain.getNextProgramCounter(), messageV, objV, FieldReference.findOrCreate(ANDROID_MESSAGE_TYPE, Atom.findOrCreateAsciiAtom("obj"), TypeReference.JavaLangObject));
        obtain.addStatement(putObjInst);

        final int pc_ret = obtain.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret, messageV);
        obtain.addStatement(retInst);

        return new SummarizedMethodWithNames(obtainRef, obtain, this);
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *  for obtainMessage(III)Landroid/os/Message;
     */
    private SummarizedMethod obtain2(Selector s) {
        final MethodReference obtainRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary obtain = new VolatileMethodSummary(new MethodSummary(obtainRef));
        obtain.setStatic(false);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue thisV = new SSAValue(1, ANDROID_HANDLER_MODEL_CLASS, obtainRef);
        final SSAValue whatV = new SSAValue(2, TypeReference.Int, obtainRef);
        final SSAValue arg1V = new SSAValue(3, TypeReference.Int, obtainRef);
        final SSAValue arg2V = new SSAValue(4, TypeReference.Int, obtainRef);

        final int callObtainPC = obtain.getNextProgramCounter();

        int ssaNo = 5;

        final CallSiteReference messageCSR = CallSiteReference.make(callObtainPC, MethodReference.findOrCreate(ANDROID_MESSAGE_TYPE, Selector.make("obtain(Landroid/os/Handler;)Landroid/os/Message;")), IInvokeInstruction.Dispatch.STATIC);
        final SSAValue messageV = new SSAValue(ssaNo++, ANDROID_MESSAGE_TYPE, obtainRef);

        final SSAValue exceptionPos = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final MethodReference obtainMR = MethodReference.findOrCreate(ANDROID_MESSAGE_TYPE, Selector.make("obtain(Landroid/os/Handler;)Landroid/os/Message;"));
        final List<SSAValue> paramsObtain = new ArrayList<SSAValue>();
        paramsObtain.add(thisV);
        final SSAInstruction callObtain = instructionFactory.InvokeInstruction(callObtainPC, messageV, paramsObtain, exceptionPos, messageCSR);
        obtain.addStatement(callObtain);

        final SSAInstruction putWhatInst = instructionFactory.PutInstruction(obtain.getNextProgramCounter(), messageV, whatV, FieldReference.findOrCreate(ANDROID_MESSAGE_TYPE, Atom.findOrCreateAsciiAtom("what"), TypeReference.Int));
        obtain.addStatement(putWhatInst);

        final SSAInstruction putArg1Inst = instructionFactory.PutInstruction(obtain.getNextProgramCounter(), messageV, arg1V, FieldReference.findOrCreate(ANDROID_MESSAGE_TYPE, Atom.findOrCreateAsciiAtom("arg1"), TypeReference.Int));
        obtain.addStatement(putArg1Inst);

        final SSAInstruction putArg2Inst = instructionFactory.PutInstruction(obtain.getNextProgramCounter(), messageV, arg2V, FieldReference.findOrCreate(ANDROID_MESSAGE_TYPE, Atom.findOrCreateAsciiAtom("arg2"), TypeReference.Int));
        obtain.addStatement(putArg2Inst);

        final int pc_ret = obtain.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret, messageV);
        obtain.addStatement(retInst);

        return new SummarizedMethodWithNames(obtainRef, obtain, this);
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *  for obtainMessage(I)Landroid/os/Message;
     */
    private SummarizedMethod obtain1(Selector s) {
        final MethodReference obtainRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary obtain = new VolatileMethodSummary(new MethodSummary(obtainRef));
        obtain.setStatic(false);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue thisV = new SSAValue(1, ANDROID_HANDLER_MODEL_CLASS, obtainRef);
        final SSAValue fintV = new SSAValue(2, TypeReference.Int, obtainRef);

        final int callObtainPC = obtain.getNextProgramCounter();

        int ssaNo = 3;

        final CallSiteReference messageCSR = CallSiteReference.make(callObtainPC, MethodReference.findOrCreate(ANDROID_MESSAGE_TYPE, Selector.make("obtain(Landroid/os/Handler;)Landroid/os/Message;")), IInvokeInstruction.Dispatch.STATIC);
        final SSAValue messageV = new SSAValue(ssaNo++, ANDROID_MESSAGE_TYPE, obtainRef);

        final SSAValue exceptionPos = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final MethodReference obtainMR = MethodReference.findOrCreate(ANDROID_MESSAGE_TYPE, Selector.make("obtain(Landroid/os/Handler;)Landroid/os/Message;"));
        final List<SSAValue> paramsObtain = new ArrayList<SSAValue>();
        paramsObtain.add(thisV);
        final SSAInstruction callObtain = instructionFactory.InvokeInstruction(callObtainPC, messageV, paramsObtain, exceptionPos, messageCSR);
        obtain.addStatement(callObtain);

        final int putWhatPC = obtain.getNextProgramCounter();
        final SSAInstruction putWhatInst = instructionFactory.PutInstruction(putWhatPC, messageV, fintV, FieldReference.findOrCreate(ANDROID_MESSAGE_TYPE, Atom.findOrCreateAsciiAtom("what"), TypeReference.Int));
        obtain.addStatement(putWhatInst);

        final int pc_ret = obtain.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret, messageV);
        obtain.addStatement(retInst);

        return new SummarizedMethodWithNames(obtainRef, obtain, this);
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
