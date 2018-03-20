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
public class JavaStringBuilderModelClass extends AbstractModelClass {

    public static final TypeReference JAVA_STRINGBUILDER_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Ljava/lang/StringBuilder"));

    public static final Selector[] mSelectors = {
            Selector.make("<init>()V"),
            Selector.make("<init>(Ljava/lang/CharSequence;)V"),
            Selector.make("<init>(I)V"),
            Selector.make("<init>(Ljava/lang/String;)V"),
            Selector.make("append(Z)Ljava/lang/StringBuilder;"),
            Selector.make("append(C)Ljava/lang/StringBuilder;"),
            Selector.make("append([C)Ljava/lang/StringBuilder;"),
            Selector.make("append([CII)Ljava/lang/StringBuilder;"),
            Selector.make("append(Ljava/lang/CharSequence;)Ljava/lang/StringBuilder;"),
            Selector.make("append(Ljava/lang/CharSequence;II)Ljava/lang/StringBuilder;"),
            Selector.make("append(D)Ljava/lang/StringBuilder;"),
            Selector.make("append(F)Ljava/lang/StringBuilder;"),
            Selector.make("append(I)Ljava/lang/StringBuilder;"),
            Selector.make("append(J)Ljava/lang/StringBuilder;"),
            Selector.make("append(Ljava/lang/Object;)Ljava/lang/StringBuilder;"),
            Selector.make("append(Ljava/lang/String;)Ljava/lang/StringBuilder;"),
            Selector.make("append(Ljava/lang/StringBuffer;)Ljava/lang/StringBuilder;"),
            Selector.make("appendCodePoint(I)Ljava/lang/StringBuilder;"),
            Selector.make("capacity()I"),
            Selector.make("charAt(I)C"),
            Selector.make("codePointAt(I)I"),
            Selector.make("codePointBefore(I)I"),
            Selector.make("codePointCount(II)I"),
            Selector.make("delete(II)Ljava/lang/StringBuilder;"),
            Selector.make("deleteCharAt(I)Ljava/lang/StringBuilder;"),
            Selector.make("ensureCapacity(I)V"),
            Selector.make("getChars(II[CI)V"),
            Selector.make("indexOf(Ljava/lang/String;)I"),
            Selector.make("indexOf(Ljava/lang/String;I)I"),
            Selector.make("insert(IZ)Ljava/lang/StringBuilder;"),
            Selector.make("insert(IC)Ljava/lang/StringBuilder;"),
            Selector.make("insert(I[C)Ljava/lang/StringBuilder;"),
            Selector.make("insert(I[CII)Ljava/lang/StringBuilder;"),
            Selector.make("insert(ILjava/lang/CharSequence;)Ljava/lang/StringBuilder;"),
            Selector.make("insert(ILjava/lang/CharSequence;II)Ljava/lang/StringBuilder;"),
            Selector.make("insert(ID)Ljava/lang/StringBuilder;"),
            Selector.make("insert(IF)Ljava/lang/StringBuilder;"),
            Selector.make("insert(II)Ljava/lang/StringBuilder;"),
            Selector.make("insert(IJ)Ljava/lang/StringBuilder;"),
            Selector.make("insert(ILjava/lang/Object;)Ljava/lang/StringBuilder;"),
            Selector.make("insert(ILjava/lang/String;)Ljava/lang/StringBuilder;"),
            Selector.make("lastIndexOf(Ljava/lang/String;)I"),
            Selector.make("lastIndexOf(Ljava/lang/String;I)I"),
            Selector.make("length()I"),
            Selector.make("offsetByCodePoints(II)I"),
            Selector.make("replace(IILjava/lang/String;)Ljava/lang/StringBuilder;"),
            Selector.make("reverse()Ljava/lang/StringBuilder;"),
            Selector.make("setCharAt(IC)V"),
            Selector.make("setLength(I)V"),
            Selector.make("subSequence(II)Ljava/lang/CharSequence;"),
            Selector.make("subString(I)Ljava/lang/String;"),
            Selector.make("subString(II)Ljava/lang/String;"),
            Selector.make("toString()Ljava/lang/String;"),
            Selector.make("trimToSize()V"),
    };

    private IClassHierarchy cha;

    private static JavaStringBuilderModelClass klass;
    private final IClass oriClass;

    public static JavaStringBuilderModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new JavaStringBuilderModelClass((ClassHierarchy)cha);
        }
        return klass;
    }

    public IMethod match(TypeReference t, Selector s){
        if(t.getName().equals(JAVA_STRINGBUILDER_MODEL_CLASS.getName()) && methods.containsKey(s))
            return methods.get(s);

        return null;
    }

    private JavaStringBuilderModelClass(ClassHierarchy cha) {
        super(JAVA_STRINGBUILDER_MODEL_CLASS, cha);
        this.cha = cha;
        this.oriClass = cha.lookupClass(JAVA_STRINGBUILDER_MODEL_CLASS);
        initMethodsForHandler();

        this.addMethod(this.clinit());
        cha.remove(JAVA_STRINGBUILDER_MODEL_CLASS);
        cha.addClass(this);
    }

    private void initMethodsForHandler(){
        for(Selector s : mSelectors){
            if(s.getDescriptor().getReturnType().equals(JAVA_STRINGBUILDER_MODEL_CLASS.getName())) {

                addMethod(unconditionalRetNewString(s));
            }
        }
    }

    private SummarizedMethod unconditionalRetNewString(Selector s) {
        final MethodReference newStrRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary newStr = new VolatileMethodSummary(new MethodSummary(newStrRef));
        newStr.setStatic(false);

        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = s.getDescriptor().getNumberOfParameters() + 2;
        
        final int newStrPC = newStr.getNextProgramCounter();
        final SSAValue strV = new SSAValue(ssaNo++, JAVA_STRINGBUILDER_MODEL_CLASS, newStrRef);
        final NewSiteReference strNSR = NewSiteReference.make(newStrPC, JAVA_STRINGBUILDER_MODEL_CLASS);
        final SSAInstruction newStrInst = instructionFactory.NewInstruction(newStrPC, strV, strNSR);
        newStr.addStatement(newStrInst);

        final int initStrPC = newStr.getNextProgramCounter();
        final CallSiteReference initStrCSR = CallSiteReference.make(initStrPC, MethodReference.findOrCreate(JAVA_STRINGBUILDER_MODEL_CLASS, Selector.make("<init>()V")), IInvokeInstruction.Dispatch.SPECIAL);
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
