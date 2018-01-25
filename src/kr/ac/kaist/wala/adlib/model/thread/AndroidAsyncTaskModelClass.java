package kr.ac.kaist.wala.adlib.model.thread;

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
import kr.ac.kaist.wala.adlib.model.AbstractModelClass;

import java.util.*;

/**
 * A modeling class for Android built-in android/os/AsyncTask.
 * Created by leesh on 14/01/2017.
 */
public class AndroidAsyncTaskModelClass extends AbstractModelClass {

    public static final TypeReference ANDROID_ASYNC_TASK_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Landroid/os/AsyncTask"));

    public static final TypeName RUNNABLE_TYPE_NAME = TypeName.string2TypeName("Ljava/lang/Runnable");
    public static final TypeName OBJECT_ARR_TYPE_NAME = TypeName.string2TypeName("[Ljava/lang/Object");
    public static final TypeName OBJECT_TYPE_NAME = TypeName.string2TypeName("Ljava/lang/Object");

    public static final Selector RUN_SELECTOR = Selector.make("run()V");
    public static final Selector EXECUTE_SELECTOR1 = Selector.make("execute([Ljava/lang/Object;)Landroid/os/AsyncTask;");
    public static final Selector EXECUTE_SELECTOR2 = Selector.make("execute(Ljava/lang/Runnable;)V");
    public static final Selector ON_PRE_EXECUTE_SELECTOR = Selector.make("onPreExecute()V");
    public static final Selector ON_POST_EXECUTE_SELECTOR = Selector.make("onPostExecute(Ljava/lang/Object;)V");
    public static final Selector DO_IN_BACKGROUND_SELECTOR = Selector.make("doInBackground([Ljava/lang/Object;)Ljava/lang/Object;");

    public static final FieldReference PARAM_FIELD = FieldReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/os/AsyncTask$WorkerRunnable", "mParams", "[Ljava/lang/Object");

    private IClassHierarchy cha;

    private static AndroidAsyncTaskModelClass klass;

    public static AndroidAsyncTaskModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new AndroidAsyncTaskModelClass(cha);
        }
        return klass;
    }

    private AndroidAsyncTaskModelClass(IClassHierarchy cha) {
        super(ANDROID_ASYNC_TASK_MODEL_CLASS, cha);
        this.cha = cha;

        initMethodsForThread();

        this.addMethod(this.clinit());
    }

    private void initMethodsForThread(){
        this.addMethod(this.execute2(EXECUTE_SELECTOR2));
        this.addMethod(this.execute1(EXECUTE_SELECTOR1));
    }

    /**
     * Generate AsyncTask.execute([Ljava/lang/Object;)Landroid/os/AsyncTask; model
     *
     * @param s execute method selector
     * @return modeled method
     */
    private SummarizedMethod execute1(Selector s) {
        final MethodReference execRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary exec = new VolatileMethodSummary(new MethodSummary(execRef));
        exec.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = 1;
        final SSAValue thisV = new SSAValue(ssaNo++, ANDROID_ASYNC_TASK_MODEL_CLASS, execRef);
        final SSAValue paramV = new SSAValue(ssaNo++, TypeReference.findOrCreate(ClassLoaderReference.Primordial, OBJECT_ARR_TYPE_NAME), execRef);

        //call onPreExecute
        final int prePC = exec.getNextProgramCounter();
        final MethodReference preMR = MethodReference.findOrCreate(ANDROID_ASYNC_TASK_MODEL_CLASS, ON_PRE_EXECUTE_SELECTOR);
        final List<SSAValue> preParams = new ArrayList<SSAValue>();
        preParams.add(thisV);
        final SSAValue preException = new SSAValue(ssaNo++, TypeReference.JavaLangException, execRef);
        final CallSiteReference preSite = CallSiteReference.make(prePC, preMR, IInvokeInstruction.Dispatch.VIRTUAL);

        final SSAInstruction preCall = instructionFactory.InvokeInstruction(prePC, preParams, preException, preSite);
        exec.addStatement(preCall);

        //call doInBackground
        final int doPC = exec.getNextProgramCounter();
        final MethodReference doMR = MethodReference.findOrCreate(ANDROID_ASYNC_TASK_MODEL_CLASS, DO_IN_BACKGROUND_SELECTOR);
        final List<SSAValue> doParams = new ArrayList<SSAValue>();
        doParams.add(thisV);
        doParams.add(paramV);
        final SSAValue resultV = new SSAValue(ssaNo++, TypeReference.findOrCreate(ClassLoaderReference.Primordial, OBJECT_TYPE_NAME), execRef);
        final SSAValue doException = new SSAValue(ssaNo++, TypeReference.JavaLangException, execRef);
        final CallSiteReference doSite = CallSiteReference.make(doPC, doMR, IInvokeInstruction.Dispatch.VIRTUAL);

        final SSAInstruction doCall = instructionFactory.InvokeInstruction(doPC, resultV, doParams, doException, doSite);
        exec.addStatement(doCall);

        //NOTE: do we need to model for onProgressUpdate? now, we don't consider about that, because the predominance method might be called in doInBackground method explicitly.

        //call onPostExecute
        final int postPC = exec.getNextProgramCounter();
        final MethodReference postMR = MethodReference.findOrCreate(ANDROID_ASYNC_TASK_MODEL_CLASS, ON_POST_EXECUTE_SELECTOR);
        final List<SSAValue> postParams = new ArrayList<SSAValue>();
        postParams.add(thisV);
        postParams.add(resultV);
        final SSAValue postException = new SSAValue(ssaNo++, TypeReference.JavaLangException, execRef);
        final CallSiteReference postSite = CallSiteReference.make(postPC, postMR, IInvokeInstruction.Dispatch.VIRTUAL);
        final SSAInstruction postCall = instructionFactory.InvokeInstruction(postPC, postParams, postException, postSite);
        exec.addStatement(postCall);

        final int pc_ret = exec.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret);
        exec.addStatement(retInst);

        return new SummarizedMethodWithNames(execRef, exec, this);
    }

    /**
     * Generate AsyncTask.execute(Ljava/lang/Runnable;)V model
     *
     * @param s execute method selector
     * @return modeled method
     */
    private SummarizedMethod execute2(Selector s) {
        final MethodReference execRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary exec = new VolatileMethodSummary(new MethodSummary(execRef));
        exec.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = 2;

        TypeReference runnableTR = TypeReference.findOrCreate(ClassLoaderReference.Application, RUNNABLE_TYPE_NAME);

        final SSAValue runnableV = new SSAValue(ssaNo++, runnableTR, execRef);
        final int pc = exec.getNextProgramCounter();
        final MethodReference runMR = MethodReference.findOrCreate(runnableTR, RUN_SELECTOR);
        final List<SSAValue> params = new ArrayList<SSAValue>();
        params.add(runnableV);
        final SSAValue exception = new SSAValue(ssaNo++, TypeReference.JavaLangException, execRef);
        final CallSiteReference site = CallSiteReference.make(pc, runMR, IInvokeInstruction.Dispatch.VIRTUAL);

        final SSAInstruction runCall = instructionFactory.InvokeInstruction(pc, params, exception, site);
        exec.addStatement(runCall);

        final int pc_ret = exec.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret);
        exec.addStatement(retInst);

        return new SummarizedMethodWithNames(execRef, exec, this);
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
