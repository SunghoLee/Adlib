package kr.ac.kaist.wala.adlib.model.thread;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.ipa.summaries.SummarizedMethodWithNames;
import com.ibm.wala.ipa.summaries.VolatileMethodSummary;
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
 * A modeling class for Java built-in java/lang/ref/Reference.
 * Created by leesh on 14/01/2017.
 */
public class JavaReferenceModelClass extends SyntheticClass{

    public static final TypeReference JAVA_REFERENCE_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Ljava/lang/ref/Reference"));

    public static final TypeName OBJECT_TYPE_NAME = TypeName.findOrCreate("Ljava/lang/Object");
    public static final Selector GET_SELECTOR = Selector.make("get()Ljava/lang/Object;");

    public static final FieldReference REFERENT_FIELD = FieldReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/ref/Reference", "referent", "Ljava/lang/Object");

    private IClassHierarchy cha;

    private static JavaReferenceModelClass klass;

    public static JavaReferenceModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new JavaReferenceModelClass(cha);
        }
        return klass;
    }

    private JavaReferenceModelClass(IClassHierarchy cha) {
        super(JAVA_REFERENCE_MODEL_CLASS, cha);
        this.cha = cha;

        initMethodsForThread();

        this.addMethod(this.clinit());
    }

    private void initMethodsForThread(){
        this.addMethod(this.get(GET_SELECTOR));
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *
     *  run call Runnable's run method
     */
    private SummarizedMethod get(Selector s) {
        final MethodReference getRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary get = new VolatileMethodSummary(new MethodSummary(getRef));
        get.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = 1;
        final SSAValue thisV = new SSAValue(ssaNo++, JAVA_REFERENCE_MODEL_CLASS, getRef);
        final SSAValue referV = new SSAValue(ssaNo++, TypeReference.findOrCreate(ClassLoaderReference.Primordial, OBJECT_TYPE_NAME), getRef);
        final int pc_get_ref = get.getNextProgramCounter();
        final SSAInstruction getRefInst = instructionFactory.GetInstruction(pc_get_ref, referV, thisV, REFERENT_FIELD);
        get.addStatement(getRefInst);

        final int pc_ret = get.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(pc_ret, referV);
        get.addStatement(retInst);

        return new SummarizedMethodWithNames(getRef, get, this);
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
