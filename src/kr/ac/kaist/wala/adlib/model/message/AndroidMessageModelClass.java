package kr.ac.kaist.wala.adlib.model.message;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.FieldImpl;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
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
public class AndroidMessageModelClass extends ModelClass {

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

    private AndroidMessageModelClass(ClassHierarchy cha) {
        super(ANDROID_MESSAGE_MODEL_CLASS, cha);
        this.cha = cha;
        this.oriClass = cha.lookupClass(ANDROID_MESSAGE_MODEL_CLASS);
        initMethodsForHandler();
        initField();
        addMethod(this.clinit());

//        cha.remove(ANDROID_MESSAGE_MODEL_CLASS);
        this.setSuperClass(oriClass.getSuperclass());
        cha.addClass(this);
    }

    private void initField(){
        FieldImpl targetF = new FieldImpl(this,
                FieldReference.findOrCreate(ANDROID_MESSAGE_MODEL_CLASS, Atom.findOrCreateAsciiAtom("target"), TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Handler")),
                ClassConstants.ACC_PUBLIC,
                null, null);
        addField(targetF);
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
}
