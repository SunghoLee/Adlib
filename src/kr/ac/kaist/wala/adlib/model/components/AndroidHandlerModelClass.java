package kr.ac.kaist.wala.adlib.model.components;

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
import com.ibm.wala.util.strings.Atom;
import kr.ac.kaist.wala.adlib.model.ModelClass;

import java.util.ArrayList;
import java.util.List;

/**
 * A modeling class for Android built-in android/os/Handler.
 * Created by leesh on 14/01/2017.
 */
public class AndroidHandlerModelClass extends ModelClass {

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

    private AndroidHandlerModelClass(IClassHierarchy cha) {
        super(ANDROID_HANDLER_MODEL_CLASS, cha);
        this.cha = cha;

        initMethodsForHandler();

        addMethod(this.clinit());
    }

    private void initMethodsForHandler(){
        addMethod(this.send(SEND_MESSAGE_SELECTOR));
        addMethod(this.postDelayed(POST_DELAYED_SELECTOR));
        addMethod(this.post(POST_SELECTOR));
        addMethod(this.obtain1(OBTAIN_MESSAGE_SELECTOR1));
        addMethod(this.obtain2(OBTAIN_MESSAGE_SELECTOR2));
        addMethod(this.obtain3(OBTAIN_MESSAGE_SELECTOR3));
        addMethod(this.obtain4(OBTAIN_MESSAGE_SELECTOR4));
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
}
