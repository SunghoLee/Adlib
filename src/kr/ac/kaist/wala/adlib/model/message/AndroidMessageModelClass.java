package kr.ac.kaist.wala.adlib.model.message;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.ipa.summaries.SummarizedMethodWithNames;
import com.ibm.wala.ipa.summaries.VolatileMethodSummary;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction;
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
public class AndroidMessageModelClass extends AbstractModelClass {

    public static final TypeReference ANDROID_MESSAGE_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Landroid/os/Message"));
    public static final Selector OBTAIN_MESSAGE_SELECTOR1 = Selector.make("obtain(Landroid/os/Handler;)Landroid/os/Message;");
    public static final Selector OBTAIN_MESSAGE_SELECTOR2 = Selector.make("obtain(Landroid/os/Handler;I)Landroid/os/Message;");
    public static final Selector OBTAIN_MESSAGE_SELECTOR3 = Selector.make("obtain(Landroid/os/Handler;Ljava/lang/Runnable;)Landroid/os/Message;");
    public static final Selector OBTAIN_MESSAGE_SELECTOR4 = Selector.make("obtain(Landroid/os/Message;)Landroid/os/Message;");
    public static final Selector OBTAIN_MESSAGE_SELECTOR5 = Selector.make("obtain()Landroid/os/Message;");
    public static final Selector SEND_TO_TARGET_SELECTOR = Selector.make("sendToTarget()V");
    public static final Selector INIT_SELECTOR = Selector.make("<init>()V");

    private IClassHierarchy cha;

    private static AndroidMessageModelClass klass;
    private final IClass oriClass;

    public static AndroidMessageModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new AndroidMessageModelClass((ClassHierarchy)cha);
        }
        return klass;
    }

    public IMethod match(TypeReference t, Selector s){
        if(t.getName().equals(ANDROID_MESSAGE_MODEL_CLASS.getName()) && methods.containsKey(s))
            return methods.get(s);

        return null;
    }

    private AndroidMessageModelClass(ClassHierarchy cha) {
        super(ANDROID_MESSAGE_MODEL_CLASS, cha);
        this.cha = cha;
        this.oriClass = cha.lookupClass(ANDROID_MESSAGE_MODEL_CLASS);
        initMethodsForHandler();
        initField();

        this.addMethod(this.clinit());
        cha.remove(ANDROID_MESSAGE_MODEL_CLASS);
        cha.addClass(this);
    }

    private void initField(){
        FieldImpl targetF = new FieldImpl(this,
                FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("target"), TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler")),
                ClassConstants.ACC_PUBLIC,
                null, null);
        this.fields.put(Atom.findOrCreateAsciiAtom("target"), targetF);
    }

    private void initMethodsForHandler(){
        this.addMethod(this.obtain1(OBTAIN_MESSAGE_SELECTOR1));
        this.addMethod(this.obtain2(OBTAIN_MESSAGE_SELECTOR2));
        this.addMethod(this.obtain3(OBTAIN_MESSAGE_SELECTOR3));
        this.addMethod(this.obtain4(OBTAIN_MESSAGE_SELECTOR4));
        this.addMethod(this.obtain5(OBTAIN_MESSAGE_SELECTOR5));
        this.addMethod(this.sendToTarget(SEND_TO_TARGET_SELECTOR));
        this.addMethod(this.init(INIT_SELECTOR));
    }

    /**
     *  <init>()V
     */
    private SummarizedMethod init(Selector s) {
        final MethodReference obtainRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary obtain = new VolatileMethodSummary(new MethodSummary(obtainRef));
        obtain.setStatic(false);

        return new SummarizedMethodWithNames(obtainRef, obtain, this);
    }

    /**
     *  sendToTarget()V
     */
    private SummarizedMethod sendToTarget(Selector s) {
        final MethodReference obtainRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary obtain = new VolatileMethodSummary(new MethodSummary(obtainRef));
        obtain.setStatic(false);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue argThisV = new SSAValue(1, ANDROID_MESSAGE_MODEL_CLASS, obtainRef);

        int ssaNo = 2;

        // 2 = 1.target;
        final int getTargetPC = obtain.getNextProgramCounter();
        final SSAValue targetV = new SSAValue(ssaNo++, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"), obtainRef);
        final FieldReference targetFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("target"), TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"));
        final SSAInstruction getTargetInst = instructionFactory.GetInstruction(getTargetPC, targetV, argThisV, targetFR);
        obtain.addStatement(getTargetInst);

        // sendMessage(2, 1);
        final int sendMsgPC = obtain.getNextProgramCounter();
        final CallSiteReference sendMsgCSR = CallSiteReference.make(sendMsgPC, MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"), Selector.make("sendMessage(Landroid/os/Message;)Z")), IInvokeInstruction.Dispatch.VIRTUAL);
        final List<SSAValue> paramsSendMsg = new ArrayList<SSAValue>();
        paramsSendMsg.add(targetV);
        paramsSendMsg.add(argThisV);
        final SSAValue retV = new SSAValue(ssaNo++, TypeReference.Boolean, obtainRef);
        final SSAValue sendMsgExcV = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final SSAInstruction sendMsgInst = instructionFactory.InvokeInstruction(sendMsgPC, retV, paramsSendMsg, sendMsgExcV, sendMsgCSR);
        obtain.overwriteStatement(sendMsgInst);

        return new SummarizedMethodWithNames(obtainRef, obtain, this);
    }

    /**
     * obtain()Landroid/os/Message;
     * @param s selector
     * @return method model for obtain5
     */
    private SummarizedMethod obtain5(Selector s) {
        final MethodReference obtainRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary obtain = new VolatileMethodSummary(new MethodSummary(obtainRef));
        obtain.setStatic(true);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = 1;

        // 1 = new Message;
        final int newMsgPC = obtain.getNextProgramCounter();
        final NewSiteReference msgNSR = NewSiteReference.make(newMsgPC, ANDROID_MESSAGE_MODEL_CLASS);
        final SSAValue msgV = new SSAValue(ssaNo++, ANDROID_MESSAGE_MODEL_CLASS, obtainRef);
        final SSAInstruction newMsgInst = instructionFactory.NewInstruction(newMsgPC, msgV, msgNSR);
        obtain.addStatement(newMsgInst);

        // Message(1);
        final int initMsgPC = obtain.getNextProgramCounter();
        final CallSiteReference initMsgCSR = CallSiteReference.make(initMsgPC, MethodReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Selector.make("<init>()V")), IInvokeInstruction.Dispatch.SPECIAL);
        final List<SSAValue> paramsInitMsg = new ArrayList<SSAValue>();
        paramsInitMsg.add(msgV);
        final SSAValue initExcV = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final SSAInstruction initMsgInst = instructionFactory.InvokeInstruction(initMsgPC, paramsInitMsg, initExcV, initMsgCSR);
        obtain.addStatement(initMsgInst);

        // return 1;
        final int retPC = obtain.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(retPC, msgV);
        obtain.addStatement(retInst);

        return new SummarizedMethodWithNames(obtainRef, obtain, this);
    }

    /**
     *  obtain(Landroid/os/Message;)Landroid/os/Message;
     */
    private SummarizedMethod obtain4(Selector s) {
        final MethodReference obtainRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary obtain = new VolatileMethodSummary(new MethodSummary(obtainRef));
        obtain.setStatic(true);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue argMsgV = new SSAValue(1, ANDROID_MESSAGE_MODEL_CLASS, obtainRef);
        int ssaNo = 2;

        // 2 = new Message;
        final int newMsgPC = obtain.getNextProgramCounter();
        final NewSiteReference msgNSR = NewSiteReference.make(newMsgPC, ANDROID_MESSAGE_MODEL_CLASS);
        final SSAValue msgV = new SSAValue(ssaNo++, ANDROID_MESSAGE_MODEL_CLASS, obtainRef);
        final SSAInstruction newMsgInst = instructionFactory.NewInstruction(newMsgPC, msgV, msgNSR);
        obtain.addStatement(newMsgInst);

        // Message(2);
        final int initMsgPC = obtain.getNextProgramCounter();
        final CallSiteReference initMsgCSR = CallSiteReference.make(initMsgPC, MethodReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Selector.make("<init>()V")), IInvokeInstruction.Dispatch.SPECIAL);
        final List<SSAValue> paramsInitMsg = new ArrayList<SSAValue>();
        paramsInitMsg.add(msgV);
        final SSAValue initExcV = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final SSAInstruction initMsgInst = instructionFactory.InvokeInstruction(initMsgPC, paramsInitMsg, initExcV, initMsgCSR);
        obtain.addStatement(initMsgInst);

        // 3 = 1.what;
        final int getWhatPC = obtain.getNextProgramCounter();
        final SSAValue whatV = new SSAValue(ssaNo++, TypeReference.Int, obtainRef);
        final FieldReference whatFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("what"), TypeReference.Int);
        final SSAInstruction getWhatInst = instructionFactory.GetInstruction(getWhatPC, whatV, argMsgV, whatFR);
        obtain.addStatement(getWhatInst);

        // 2.what = 3;
        final int putWhatPC = obtain.getNextProgramCounter();
        final SSAInstruction putWhatInst = instructionFactory.PutInstruction(putWhatPC, msgV, whatV, whatFR);
        obtain.addStatement(putWhatInst);


        // 4 = 1.arg1;
        final int getArg1PC = obtain.getNextProgramCounter();
        final SSAValue arg1V = new SSAValue(ssaNo++, TypeReference.Int, obtainRef);
        final FieldReference arg1FR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("arg1"), TypeReference.Int);
        final SSAInstruction getArg1Inst = instructionFactory.GetInstruction(getArg1PC, arg1V, argMsgV, arg1FR);
        obtain.addStatement(getArg1Inst);


        // 2.arg1 = 4;
        final int putArg1PC = obtain.getNextProgramCounter();
        final SSAInstruction putArg1Inst = instructionFactory.PutInstruction(putArg1PC, msgV, arg1V, arg1FR);
        obtain.addStatement(putArg1Inst);

        // 5 = 1.arg2;
        final int getArg2PC = obtain.getNextProgramCounter();
        final SSAValue arg2V = new SSAValue(ssaNo++, TypeReference.Int, obtainRef);
        final FieldReference arg2FR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("arg2"), TypeReference.Int);
        final SSAInstruction getArg2Inst = instructionFactory.GetInstruction(getArg2PC, arg2V, argMsgV, arg2FR);
        obtain.addStatement(getArg2Inst);


        // 2.arg2 = 5;
        final int putArg2PC = obtain.getNextProgramCounter();
        final SSAInstruction putArg2Inst = instructionFactory.PutInstruction(putArg2PC, msgV, arg2V, arg2FR);
        obtain.addStatement(putArg2Inst);

        // 6 = 1.obj;
        final int getObjPC = obtain.getNextProgramCounter();
        final SSAValue objV = new SSAValue(ssaNo++, TypeReference.JavaLangObject, obtainRef);
        final FieldReference objFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("obj"), TypeReference.JavaLangObject);
        final SSAInstruction getObjInst = instructionFactory.GetInstruction(getObjPC, objV, argMsgV, objFR);
        obtain.addStatement(getObjInst);

        // 2.obj = 6;
        final int putObjPC = obtain.getNextProgramCounter();
        final SSAInstruction putObjInst = instructionFactory.PutInstruction(putObjPC, msgV, objV, objFR);
        obtain.addStatement(putObjInst);

        // 7 = 1.replyTo;
        final int getReplyToPC = obtain.getNextProgramCounter();
        final SSAValue replyToV = new SSAValue(ssaNo++, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Messenger"), obtainRef);
        final FieldReference replyToFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("replyTo"), TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Messenger"));
        final SSAInstruction getReplyToInst = instructionFactory.GetInstruction(getReplyToPC, replyToV, argMsgV, replyToFR);
        obtain.addStatement(getReplyToInst);

        // 2.replyTo = 7;
        final int putReplyToPC = obtain.getNextProgramCounter();
        final SSAInstruction putReplyToInst = instructionFactory.PutInstruction(putReplyToPC, msgV, replyToV, replyToFR);
        obtain.addStatement(putReplyToInst);

        // 8 = 1.target;
        final int getTargetPC = obtain.getNextProgramCounter();
        final SSAValue targetV = new SSAValue(ssaNo++, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"), obtainRef);
        final FieldReference targetFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("target"), TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"));
        final SSAInstruction getTargetInst = instructionFactory.GetInstruction(getTargetPC, targetV, argMsgV, targetFR);
        obtain.addStatement(getTargetInst);

        // 2.target = 8;
        final int putTargetPC = obtain.getNextProgramCounter();
        final SSAInstruction putTargetInst = instructionFactory.PutInstruction(putTargetPC, msgV, targetV, targetFR);
        obtain.addStatement(putTargetInst);

        // 9 = 1.callback;
        final int getCallbackPC = obtain.getNextProgramCounter();
        final SSAValue callbackV = new SSAValue(ssaNo++, TypeReference.find(ClassLoaderReference.Primordial, "Ljava/lang/Runnable"), obtainRef);
        final FieldReference callbackFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("callback"), TypeReference.find(ClassLoaderReference.Primordial, "Ljava/lang/Runnable"));
        final SSAInstruction getCallbackInst = instructionFactory.GetInstruction(getCallbackPC, callbackV, argMsgV, callbackFR);
        obtain.addStatement(getCallbackInst);

        // 2.callback = 9;
        final int putCallbackPC = obtain.getNextProgramCounter();
        final SSAInstruction putCallbackInst = instructionFactory.PutInstruction(putCallbackPC, msgV, callbackV, callbackFR);
        obtain.addStatement(putCallbackInst);

        // 10 = 1.data;
        final int getDataPC = obtain.getNextProgramCounter();
        final SSAValue dataV = new SSAValue(ssaNo++, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Bundle"), obtainRef);
        final FieldReference dataFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("data"), TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Bundle"));
        final SSAInstruction getDataInst = instructionFactory.GetInstruction(getDataPC, dataV, argMsgV, dataFR);
        obtain.addStatement(getDataInst);

        final int ifPC = obtain.getNextProgramCounter();
        final int newBundlePC = obtain.getNextProgramCounter();
        final int initBundlePC = obtain.getNextProgramCounter();
        final int retPC = obtain.getNextProgramCounter();

        // if 10 != null
        final SSAValue nullV = new SSAValue(ssaNo++, TypeReference.Null, obtainRef);
        final SSAInstruction ifInst = instructionFactory.ConditionalBranchInstruction(ifPC, IConditionalBranchInstruction.Operator.NE, TypeReference.JavaLangObject, dataV.getNumber(), nullV.getNumber(), retPC);
        obtain.addStatement(ifInst);

        // 11 = new Bundle;
        final NewSiteReference bundleNSR = NewSiteReference.make(newBundlePC, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Bundle"));
        final SSAValue bundleV = new SSAValue(ssaNo++, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Bundle"), obtainRef);
        final SSAInstruction newBundleInst = instructionFactory.NewInstruction(newBundlePC, bundleV, bundleNSR);
        obtain.overwriteStatement(newBundleInst);

        // Bundle(11);
        final CallSiteReference initBundleCSR = CallSiteReference.make(initBundlePC, MethodReference.findOrCreate(TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Bundle"), Selector.make("<init>(Landroid/os/Bundle;)V")), IInvokeInstruction.Dispatch.SPECIAL);
        final List<SSAValue> paramsInitBundle = new ArrayList<SSAValue>();
        paramsInitBundle.add(bundleV);
        paramsInitBundle.add(dataV);
        final SSAValue initBundleExcV = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final SSAInstruction initBundleInst = instructionFactory.InvokeInstruction(initBundlePC, paramsInitBundle, initBundleExcV, initBundleCSR);
        obtain.overwriteStatement(initBundleInst);

        // return 2;
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(retPC, msgV);
        obtain.overwriteStatement(retInst);

        return new SummarizedMethodWithNames(obtainRef, obtain, this);
    }

    /**
     *  obtain(Landroid/os/Handler;Ljava/lang/Runnable;)Landroid/os/Message;
     */
    private SummarizedMethod obtain3(Selector s) {
        final MethodReference obtainRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary obtain = new VolatileMethodSummary(new MethodSummary(obtainRef));
        obtain.setStatic(true);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue argHandlerV = new SSAValue(1, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"), obtainRef);
        final SSAValue argRunnableV = new SSAValue(2, TypeReference.find(ClassLoaderReference.Primordial, "Ljava/lang/Runnable"), obtainRef);
        int ssaNo = 3;

        // 3 = new Message;
        final int newMsgPC = obtain.getNextProgramCounter();
        final NewSiteReference msgNSR = NewSiteReference.make(newMsgPC, ANDROID_MESSAGE_MODEL_CLASS);
        final SSAValue msgV = new SSAValue(ssaNo++, ANDROID_MESSAGE_MODEL_CLASS, obtainRef);
        final SSAInstruction newMsgInst = instructionFactory.NewInstruction(newMsgPC, msgV, msgNSR);
        obtain.addStatement(newMsgInst);

        // Message(3);
        final int initMsgPC = obtain.getNextProgramCounter();
        final CallSiteReference initMsgCSR = CallSiteReference.make(initMsgPC, MethodReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Selector.make("<init>()V")), IInvokeInstruction.Dispatch.SPECIAL);
        final List<SSAValue> paramsInitMsg = new ArrayList<SSAValue>();
        paramsInitMsg.add(msgV);
        final SSAValue initExcV = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final SSAInstruction initMsgInst = instructionFactory.InvokeInstruction(initMsgPC, paramsInitMsg, initExcV, initMsgCSR);
        obtain.addStatement(initMsgInst);

        // 3.target = 1;
        final int putTargetPC = obtain.getNextProgramCounter();
        final FieldReference targetFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("target"), TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"));
        final SSAInstruction putTargetInst = instructionFactory.PutInstruction(putTargetPC, msgV, argHandlerV, targetFR);
        obtain.addStatement(putTargetInst);

        // 3.callback = 2;
        final int putCallbackPC = obtain.getNextProgramCounter();
        final FieldReference callbackFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("callback"), TypeReference.find(ClassLoaderReference.Primordial, "Ljava/lang/Runnable"));
        final SSAInstruction putCallbackInst = instructionFactory.PutInstruction(putCallbackPC, msgV, argRunnableV, callbackFR);
        obtain.addStatement(putCallbackInst);

        // return 3;
        final int retPC = obtain.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(retPC, msgV);
        obtain.addStatement(retInst);

        return new SummarizedMethodWithNames(obtainRef, obtain, this);
    }

    /**
     *  obtain(Landroid/os/Handler;I)Landroid/os/Message;
     */
    private SummarizedMethod obtain2(Selector s) {
        final MethodReference obtainRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary obtain = new VolatileMethodSummary(new MethodSummary(obtainRef));
        obtain.setStatic(true);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue argHandlerV = new SSAValue(1, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"), obtainRef);
        final SSAValue argIntV = new SSAValue(2, TypeReference.Int, obtainRef);

        int ssaNo = 3;

        // 3 = new Message;
        final int newMsgPC = obtain.getNextProgramCounter();
        final NewSiteReference msgNSR = NewSiteReference.make(newMsgPC, ANDROID_MESSAGE_MODEL_CLASS);
        final SSAValue msgV = new SSAValue(ssaNo++, ANDROID_MESSAGE_MODEL_CLASS, obtainRef);
        final SSAInstruction newMsgInst = instructionFactory.NewInstruction(newMsgPC, msgV, msgNSR);
        obtain.addStatement(newMsgInst);

        // Message(3);
        final int initMsgPC = obtain.getNextProgramCounter();
        final CallSiteReference initMsgCSR = CallSiteReference.make(initMsgPC, MethodReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Selector.make("<init>()V")), IInvokeInstruction.Dispatch.SPECIAL);
        final List<SSAValue> paramsInitMsg = new ArrayList<SSAValue>();
        paramsInitMsg.add(msgV);
        final SSAValue initExcV = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final SSAInstruction initMsgInst = instructionFactory.InvokeInstruction(initMsgPC, paramsInitMsg, initExcV, initMsgCSR);
        obtain.addStatement(initMsgInst);

        // 3.target = 1;
        final int putTargetPC = obtain.getNextProgramCounter();
        final FieldReference targetFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("target"), TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"));
        final SSAInstruction putTargetInst = instructionFactory.PutInstruction(putTargetPC, msgV, argHandlerV, targetFR);
        obtain.addStatement(putTargetInst);

        // 3.what = 2;
        final int putWhatPC = obtain.getNextProgramCounter();
        final FieldReference whatFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("what"), TypeReference.Int);
        final SSAInstruction putWhatInst = instructionFactory.PutInstruction(putWhatPC, msgV, argIntV, whatFR);
        obtain.addStatement(putWhatInst);

        // return 3;
        final int retPC = obtain.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(retPC, msgV);
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
        obtain.setStatic(true);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        final SSAValue argHandlerV = new SSAValue(1, TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"), obtainRef);

        int ssaNo = 2;

        // 2 = new Message;
        final int newMsgPC = obtain.getNextProgramCounter();
        final NewSiteReference msgNSR = NewSiteReference.make(newMsgPC, ANDROID_MESSAGE_MODEL_CLASS);
        final SSAValue msgV = new SSAValue(ssaNo++, ANDROID_MESSAGE_MODEL_CLASS, obtainRef);
        final SSAInstruction newMsgInst = instructionFactory.NewInstruction(newMsgPC, msgV, msgNSR);
        obtain.addStatement(newMsgInst);

        // Message(2);
        final int initMsgPC = obtain.getNextProgramCounter();
        final CallSiteReference initMsgCSR = CallSiteReference.make(initMsgPC, MethodReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Selector.make("<init>()V")), IInvokeInstruction.Dispatch.SPECIAL);
        final List<SSAValue> paramsInitMsg = new ArrayList<SSAValue>();
        paramsInitMsg.add(msgV);
        final SSAValue initExcV = new SSAValue(ssaNo++, TypeReference.JavaLangException, obtainRef);
        final SSAInstruction initMsgInst = instructionFactory.InvokeInstruction(initMsgPC, paramsInitMsg, initExcV, initMsgCSR);
        obtain.addStatement(initMsgInst);

        // 2.target = 1;
        final int putTargetPC = obtain.getNextProgramCounter();
        final FieldReference targetFR = FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("target"), TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler"));
        final SSAInstruction putTargetInst = instructionFactory.PutInstruction(putTargetPC, msgV, argHandlerV, targetFR);
        obtain.addStatement(putTargetInst);

        // return 2;
        final int retPC = obtain.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(retPC, msgV);
        obtain.addStatement(retInst);

        return new SummarizedMethodWithNames(obtainRef, obtain, this);
    }

    private SummarizedMethod clinit() {
        final MethodReference clinitRef = MethodReference.findOrCreate(this.getReference(), MethodReference.clinitSelector);
        final VolatileMethodSummary clinit = new VolatileMethodSummary(new MethodSummary(clinitRef));
        clinit.setStatic(true);

        return new SummarizedMethodWithNames(clinitRef, clinit, this);
    }

    @Override
    public IMethod getMethod(Selector selector) {
        if(methods.containsKey(selector)){
            return methods.get(selector);
        }else{
            IMethod m = oriClass.getMethod(selector);
            if(m != null)
                return m;
        }
        throw new IllegalArgumentException("Could not resolve " + selector);
    }

    /**
     * public static final Selector OBTAIN_MESSAGE_SELECTOR1 = Selector.make("obtain(Landroid/os/Handler;)Landroid/os/Message;");
     public static final Selector OBTAIN_MESSAGE_SELECTOR2 = Selector.make("obtain(Landroid/os/Handler;I)Landroid/os/Message;");
     public static final Selector OBTAIN_MESSAGE_SELECTOR3 = Selector.make("obtain(Landroid/os/Handler;Ljava/lang/Runnable;)Landroid/os/Message;");
     public static final Selector OBTAIN_MESSAGE_SELECTOR4 = Selector.make("obtain(Landroid/os/Message;)Landroid/os/Message;");
     public static final Selector OBTAIN_MESSAGE_SELECTOR5 = Selector.make("obtain()Landroid/os/Message;");
     public static final Selector SEND_TO_TARGET_SELECTOR = Selector.make("sendToTarget()V");
     public static final Selector INIT_SELECTOR = Selector.make("<init>()V");
     * @return
     */
    @Override
    public Collection<IMethod> getDeclaredMethods() {
        Set<IMethod> methods = HashSetFactory.make();
        methods.addAll(this.methods.values());

        for(IMethod m : oriClass.getDeclaredMethods()){
            if(!m.getSelector().equals(OBTAIN_MESSAGE_SELECTOR1) &&
                    !m.getSelector().equals(OBTAIN_MESSAGE_SELECTOR2) &&
                    !m.getSelector().equals(OBTAIN_MESSAGE_SELECTOR3) &&
                    !m.getSelector().equals(OBTAIN_MESSAGE_SELECTOR4) &&
                    !m.getSelector().equals(OBTAIN_MESSAGE_SELECTOR5) &&
                    !m.getSelector().equals(SEND_TO_TARGET_SELECTOR) &&
                    !m.getSelector().equals(INIT_SELECTOR))
                methods.add(m);

        }
        return Collections.unmodifiableCollection(methods);
    }

    @Override
    public Collection<IMethod> getAllMethods()  {
        Set<IMethod> methods = HashSetFactory.make();
        methods.addAll(this.methods.values());

        for(IMethod m : oriClass.getAllMethods()){
            if(!m.getSelector().equals(OBTAIN_MESSAGE_SELECTOR1) &&
                    !m.getSelector().equals(OBTAIN_MESSAGE_SELECTOR2) &&
                    !m.getSelector().equals(OBTAIN_MESSAGE_SELECTOR3) &&
                    !m.getSelector().equals(OBTAIN_MESSAGE_SELECTOR4) &&
                    !m.getSelector().equals(OBTAIN_MESSAGE_SELECTOR5) &&
                    !m.getSelector().equals(SEND_TO_TARGET_SELECTOR) &&
                    !m.getSelector().equals(INIT_SELECTOR))
                methods.add(m);

        }
        return Collections.unmodifiableCollection(methods);
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
        return oriClass.getClassInitializer();
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
            return oriClass.getField(name);
        }
    }

    @Override
    public IField getField(Atom name, TypeName typeName) {
        if(fields.containsKey(name)) {
            IField f = fields.get(name);
            if (f.getFieldTypeReference().getName().equals(typeName))
                return f;
        }
        return oriClass.getField(name, typeName);
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
        Set<IField> res = new HashSet<>();
        res.addAll(oriClass.getAllFields());
        res.addAll(fields.values());
        return res;
    }

    /**
     *  This class does not contain any fields.
     */
    @Override
    public Collection<IField> getDeclaredStaticFields() {
        return oriClass.getDeclaredStaticFields();
    }

    /**
     *  This class does not contain any fields.
     */
    @Override
    public Collection<IField> getAllStaticFields() {
        return oriClass.getAllStaticFields();
    }

    /**
     *  This class does not contain any fields.
     */
    @Override
    public Collection<IField> getDeclaredInstanceFields() throws UnsupportedOperationException {
        Set<IField> res = new HashSet<>();
        res.addAll(oriClass.getDeclaredInstanceFields());
        res.addAll(fields.values());
        return res;
    }

    /**
     *  This class does not contain any fields.
     */
    @Override
    public Collection<IField> getAllInstanceFields()  {
        Set<IField> res = new HashSet<>();
        res.addAll(oriClass.getAllInstanceFields());
        res.addAll(fields.values());
        return res;
    }



    //
    //  Class Modifiers
    //

    /**
     *  This is a public final class.
     */
    @Override
    public int getModifiers() {
        return oriClass.getModifiers();
    }
    @Override
    public boolean isPublic() {         return oriClass.isPublic();  }
    @Override
    public boolean isPrivate() {        return oriClass.isPrivate(); }
    @Override
    public boolean isInterface() {      return oriClass.isInterface(); }
    @Override
    public boolean isAbstract() {       return oriClass.isAbstract(); }
    @Override
    public boolean isArrayClass () {    return oriClass.isArrayClass(); }

    /**
     *  This is a subclass of the root class.
     */
    @Override
    public IClass getSuperclass() throws UnsupportedOperationException {
        return oriClass.getSuperclass();
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
        Set<IClass> res = new HashSet<>();
        res.addAll(oriClass.getDirectInterfaces());
        return res;
    }

    //
    //  Misc
    //

    @Override
    public boolean isReferenceType() {
        return oriClass.isReferenceType();
    }
}
