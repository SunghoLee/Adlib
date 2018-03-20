package kr.ac.kaist.wala.adlib.model.string;

import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.cha.ClassHierarchy;
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
public class JavaStringModelClass extends AbstractModelClass {

    public static final TypeReference JAVA_STRING_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Ljava/lang/String"));

    public static final Selector[] mSelectors = {
    Selector.make("chatAt(I)C"),
    Selector.make("chatPointAt(I)I"),
    Selector.make("chatPointBefore(I)I"),
    Selector.make("chatPointCount(II)I"),
    Selector.make("compareTo(Ljava/lang/String;)I"),
    Selector.make("compareToIgnoreCase(Ljava/lang/String;)I"),
    Selector.make("concat(Ljava/lang/String;)Ljava/lang/String;"),
    Selector.make("contains(Ljava/lang/CharSequence;)Z"),
    Selector.make("contentEquals(Ljava/lang/CharSequence;)Z"),
    Selector.make("contentEquals(Ljava/lang/StringBuffer;)Z"),
    Selector.make("copyValueOf([C)Ljava/lang/String;"),
    Selector.make("copyValueOf([CII)Ljava/lang/String;"),
    Selector.make("endsWith(Ljava/lang/String;)Z"),
    Selector.make("equals(Ljava/lang/Object;)Z"),
    Selector.make("equalsIgnoreCase(Ljava/lang/String;)Z"),
    Selector.make("format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"),
    Selector.make("format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"),
    Selector.make("getBytes()[Z"),
    Selector.make("getBytes(Ljava/nio/charset/Charset;)[Z"),
    Selector.make("getBytes(II[ZI)V"),
    Selector.make("getBytes(Ljava/lang/String;)[Z"),
    Selector.make("getChars(II[CI)V"),
    Selector.make("hashCode()I"),
    Selector.make("indexOf(I)I"),
    Selector.make("indexOf(II)I"),
    Selector.make("indexOf(Ljava/lang/String;)I"),
    Selector.make("indexOf(Ljava/lang/String;I)I"),
    Selector.make("intern()Ljava/lang/String;"),
    Selector.make("isEmpty()Z"),
    Selector.make("lastIndexOf(I)I"),
    Selector.make("lastIndexOf(II)I"),
    Selector.make("lastIndexOf(Ljava/lang/String;)I"),
    Selector.make("lastIndexOf(Ljava/lang/String;I)I"),
    Selector.make("length()I"),
    Selector.make("matches(Ljava/lang/String;)Z"),
    Selector.make("offsetByCodePoints(II)I"),
    Selector.make("regionMatches(ZIL/java/lang/String;II)Z"),
    Selector.make("regionMatches(IL/java/lang/String;II)Z"),
    Selector.make("replace(CC)Ljava/lang/String;"),
    Selector.make("replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"),
    Selector.make("replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
    Selector.make("replaceFirst(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"),
    Selector.make("split(Ljava/lang/String;)[Ljava/lang/String;"),
    Selector.make("split(Ljava/lang/String;I)[Ljava/lang/String;"),
    Selector.make("startsWith(Ljava/lang/String;)Z"),
    Selector.make("startsWith(Ljava/lang/String;I)Z"),
    Selector.make("subSequence(II)Ljava/lang/CharSequence;"),
    Selector.make("substring(I)Ljava/lang/String;"),
    Selector.make("substring(II)Ljava/lang/String;"),
    Selector.make("toCharArray()[C"),
    Selector.make("toLowerCase()Ljava/lang/String;"),
    Selector.make("toLowerCase(Ljava/util/Locale;)Ljava/lang/String;"),
    Selector.make("toString()Ljava/lang/String;"),
    Selector.make("toUpperCase()Ljava/lang/String;"),
    Selector.make("toUpperCase(Ljava/util/Locale;)Ljava/lang/String;"),
    Selector.make("trim()Ljava/lang/String;"),
    Selector.make("valueOf(Z)Ljava/lang/String;"),
    Selector.make("valueOf(C)Ljava/lang/String;"),
    Selector.make("valueOf([C)Ljava/lang/String;"),
    Selector.make("valueOf([CII)Ljava/lang/String;"),
    Selector.make("valueOf(D)Ljava/lang/String;"),
    Selector.make("valueOf(F)Ljava/lang/String;"),
    Selector.make("valueOf(I)Ljava/lang/String;"),
    Selector.make("valueOf(J)Ljava/lang/String;"),
    Selector.make("valueOf(Ljava/lang/Object;)Ljava/lang/String;"),
    };

    private IClassHierarchy cha;

    private static JavaStringModelClass klass;
    private final IClass oriClass;

    public static JavaStringModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new JavaStringModelClass((ClassHierarchy)cha);
        }
        return klass;
    }

    public IMethod match(TypeReference t, Selector s){
        if(t.getName().equals(JAVA_STRING_MODEL_CLASS.getName()) && methods.containsKey(s))
            return methods.get(s);

        return null;
    }

    private JavaStringModelClass(ClassHierarchy cha) {
        super(JAVA_STRING_MODEL_CLASS, cha);
        this.cha = cha;
        this.oriClass = cha.lookupClass(JAVA_STRING_MODEL_CLASS);
        initMethodsForHandler();

        this.addMethod(this.clinit());
        cha.remove(JAVA_STRING_MODEL_CLASS);
        cha.addClass(this);
    }

    private void initMethodsForHandler(){
        for(Selector s : mSelectors){
            if(s.getDescriptor().getReturnType().equals(JAVA_STRING_MODEL_CLASS.getName()))
                addMethod(unconditionalRetNewString(s));
        }
    }

    /**
     *  sendToTarget()V
     */
    private SummarizedMethod unconditionalRetNewString(Selector s) {
        final MethodReference newStrRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary newStr = new VolatileMethodSummary(new MethodSummary(newStrRef));
        newStr.setStatic(false);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = s.getDescriptor().getNumberOfParameters() + 2;

        // 2 = 1.target;
        final int newStrPC = newStr.getNextProgramCounter();
        final SSAValue strV = new SSAValue(ssaNo++, JAVA_STRING_MODEL_CLASS, newStrRef);
        final NewSiteReference strNSR = NewSiteReference.make(newStrPC, JAVA_STRING_MODEL_CLASS);
        final SSAInstruction newStrInst = instructionFactory.NewInstruction(newStrPC, strV, strNSR);
        newStr.addStatement(newStrInst);

        // sendMessage(2, 1);
        final int initStrPC = newStr.getNextProgramCounter();
        final CallSiteReference initStrCSR = CallSiteReference.make(initStrPC, MethodReference.findOrCreate(JAVA_STRING_MODEL_CLASS, Selector.make("<init>()V")), IInvokeInstruction.Dispatch.SPECIAL);
        final List<SSAValue> paramsInitStr = new ArrayList<SSAValue>();
        paramsInitStr.add(strV);
        final SSAValue initStrExcV = new SSAValue(ssaNo++, TypeReference.JavaLangException, newStrRef);
        final SSAInstruction initStrInst = instructionFactory.InvokeInstruction(initStrPC, paramsInitStr, initStrExcV, initStrCSR);
        newStr.addStatement(initStrInst);

        final int retPC = newStr.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(retPC, strV);
        newStr.addStatement(retInst);

        return new SummarizedMethodWithNames(newStrRef, newStr, this);
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

    private boolean isIn(Selector s){
        for(Selector ms : mSelectors){
            if(ms.equals(s))
                return true;
        }
        return false;
    }

    @Override
    public Collection<IMethod> getDeclaredMethods() {
        Set<IMethod> methods = HashSetFactory.make();
        methods.addAll(this.methods.values());

        for(IMethod m : oriClass.getDeclaredMethods()){
            if(!isIn(m.getSelector()))
                methods.add(m);

        }
        return Collections.unmodifiableCollection(methods);
    }

    @Override
    public Collection<IMethod> getAllMethods()  {
        Set<IMethod> methods = HashSetFactory.make();
        methods.addAll(this.methods.values());

        for(IMethod m : oriClass.getAllMethods()){
            if(!isIn(m.getSelector()))
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
