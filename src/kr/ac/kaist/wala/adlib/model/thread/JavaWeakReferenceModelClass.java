package kr.ac.kaist.wala.adlib.model.thread;

import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.ipa.summaries.SummarizedMethodWithNames;
import com.ibm.wala.ipa.summaries.VolatileMethodSummary;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.*;
import com.ibm.wala.util.ssa.SSAValue;
import com.ibm.wala.util.ssa.TypeSafeInstructionFactory;
import kr.ac.kaist.wala.adlib.model.ModelClass;

/**
 * A modeling class for Java built-in java/lang/ref/Reference.
 * Created by leesh on 14/01/2017.
 */
public class JavaWeakReferenceModelClass extends ModelClass {

    public static final TypeReference JAVA_WEAK_REFERENCE_MODEL_CLASS = TypeReference.findOrCreate(
            ClassLoaderReference.Primordial, TypeName.string2TypeName("Ljava/lang/ref/WeakReference"));

    public static final TypeName OBJECT_TYPE_NAME = TypeName.findOrCreate("Ljava/lang/Object");
    public static final Selector INIT_SELECTOR = Selector.make("<init>(Ljava/lang/Object;)V");

    public static final FieldReference REFERENT_FIELD = FieldReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/ref/Reference", "referent", "Ljava/lang/Object");

    private IClassHierarchy cha;

    private static JavaWeakReferenceModelClass klass;

    public static JavaWeakReferenceModelClass getInstance(IClassHierarchy cha) {
        if(klass == null){
            klass = new JavaWeakReferenceModelClass(cha);
        }
        return klass;
    }

    private JavaWeakReferenceModelClass(IClassHierarchy cha) {
        super(JAVA_WEAK_REFERENCE_MODEL_CLASS, cha);
        this.cha = cha;

        initMethodsForThread();

        this.addMethod(this.clinit());
    }

    private void initMethodsForThread(){
        this.addMethod(this.init(INIT_SELECTOR));
    }

    /**
     *  Generate run of Thread for AndroidThreadModelClass.
     *
     *  run call Runnable's run method
     */
    private SummarizedMethod init(Selector s) {
        final MethodReference getRef = MethodReference.findOrCreate(this.getReference(), s);
        final VolatileMethodSummary get = new VolatileMethodSummary(new MethodSummary(getRef));
        get.setStatic(false);
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = 1;
        final SSAValue thisV = new SSAValue(ssaNo++, JAVA_WEAK_REFERENCE_MODEL_CLASS, getRef);
        final SSAValue referV = new SSAValue(ssaNo++, TypeReference.findOrCreate(ClassLoaderReference.Primordial, OBJECT_TYPE_NAME), getRef);
        final int pc_put_ref = get.getNextProgramCounter();
        final SSAInstruction putInst = instructionFactory.PutInstruction(pc_put_ref, thisV, referV, REFERENT_FIELD);
        get.addStatement(putInst);

        return new SummarizedMethodWithNames(getRef, get, this);
    }

    private SummarizedMethod clinit() {
        final MethodReference clinitRef = MethodReference.findOrCreate(this.getReference(), MethodReference.clinitSelector);
        final VolatileMethodSummary clinit = new VolatileMethodSummary(new MethodSummary(clinitRef));
        clinit.setStatic(true);

        return new SummarizedMethodWithNames(clinitRef, clinit, this);
    }
}
