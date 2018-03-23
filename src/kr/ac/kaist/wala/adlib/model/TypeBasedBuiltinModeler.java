package kr.ac.kaist.wala.adlib.model;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.summaries.MethodSummary;
import com.ibm.wala.ipa.summaries.SummarizedMethod;
import com.ibm.wala.ipa.summaries.SummarizedMethodWithNames;
import com.ibm.wala.ipa.summaries.VolatileMethodSummary;
import com.ibm.wala.ssa.ConstantValue;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.HashSetFactory;
import com.ibm.wala.util.ssa.SSAValue;
import com.ibm.wala.util.ssa.TypeSafeInstructionFactory;

import java.util.*;

/**
 * A modeling class for Android built-in android/os/Handler.
 * Created by leesh on 14/01/2017.
 */
public class TypeBasedBuiltinModeler{
    private Set<IClass> alreadyModeled = HashSetFactory.make();
    private Map<TypeReference, ModelClass> classMap = HashMapFactory.make();

    public ModelClass getClass(IClass c){
        return classMap.get(c.getReference());
    }

    public IClassHierarchy model(IClassHierarchy cha) {
        Iterator<IClass> iKlass = cha.iterator();
        List<ModelClass> newKlasses = new ArrayList<>();
//        test(cha);
        while(iKlass.hasNext()){
            IClass klass = iKlass.next();

            model(cha, klass, newKlasses);
        }

        for(ModelClass newKlass : newKlasses){
            classMap.put(newKlass.getReference(), newKlass);
        }

        return cha;
    }

    private IClass findSuperClass(IClass c, List<ModelClass> classes){
        TypeName superName = c.getSuperclass().getReference().getName();

        for(IClass klass : classes){
            if(klass.getName().equals(superName))
                return klass;
        }
        return null;
    }

    private IClass model(IClassHierarchy cha, IClass klass, List<ModelClass> newKlasses){

        IClass superClass = klass.getSuperclass();
        IClass newSuperClass = null;

        if(superClass != null) {
            newSuperClass = model(cha, superClass, newKlasses);
            if(newSuperClass == null)
                newSuperClass = findSuperClass(klass, newKlasses);
        }

        if(!klass.isInterface() && klass.getClassLoader().getReference().equals(ClassLoaderReference.Primordial) && !alreadyModeled.contains(klass)) {
            alreadyModeled.add(klass);
            ModelClass c = new ModelClass(klass.getReference(), cha);
            for (IMethod m : klass.getDeclaredMethods()) {
                if (m.getReturnType().isReferenceType()) {
                    SummarizedMethod modeledM = typeBasedRetMethod(c, m, cha);
                    c.addMethod(modeledM);
                }
            }
            c.setSuperClass(newSuperClass);
            newKlasses.add(c);
            return c;
        }
        return null;
    }

    private SummarizedMethod typeBasedRetMethod(IClass c, IMethod m, IClassHierarchy cha) {
        final MethodReference mRef = m.getReference();
        final VolatileMethodSummary newM = new VolatileMethodSummary(new MethodSummary(mRef));
        newM.setStatic(m.isStatic());
        
        final TypeSafeInstructionFactory instructionFactory = new TypeSafeInstructionFactory(cha);

        int ssaNo = m.getNumberOfParameters() + 1;

        // 1 = new X;
        final int newPC = newM.getNextProgramCounter();
        final SSAValue newV = new SSAValue(ssaNo++, m.getReturnType(), mRef);
        final NewSiteReference nsr = NewSiteReference.make(newPC, m.getReturnType());

        // for array type
        if(m.getReturnType().isArrayType()){
            final SSAValue arrayLength = new SSAValue(ssaNo++, TypeReference.Int, mRef);
            newM.addConstant(ssaNo-1, new ConstantValue(1));
            arrayLength.setAssigned();

            final ArrayList<SSAValue> params = new ArrayList<SSAValue>(1);
            params.add(arrayLength);

            final SSAInstruction newInst = instructionFactory.NewInstruction(newPC, newV, nsr, params);
            newM.addStatement(newInst);

            final int newElePC = newM.getNextProgramCounter();
            final SSAValue newEleV = new SSAValue(ssaNo++, m.getReturnType().getArrayElementType(), mRef);
            final NewSiteReference eleNSR = NewSiteReference.make(newElePC, m.getReturnType().getArrayElementType());
            final SSAInstruction newEleInst = instructionFactory.NewInstruction(newElePC, newEleV, eleNSR);
            newM.addStatement(newEleInst);

            final int storePC = newM.getNextProgramCounter();
            final SSAInstruction storeInst = instructionFactory.ArrayStoreInstruction(storePC, newV, 0, newEleV);
            newM.addStatement(storeInst);
        }else {
            final SSAInstruction newInst = instructionFactory.NewInstruction(newPC, newV, nsr);
            newM.addStatement(newInst);
        }
//        // sendMessage(2, 1);
//        final int initPC = newM.getNextProgramCounter();
//        final CallSiteReference initCSR = CallSiteReference.make(initPC, MethodReference.findOrCreate(m.getReturnType(), Selector.make("<init>()V")), IInvokeInstruction.Dispatch.SPECIAL);
//        final List<SSAValue> paramsInit = new ArrayList<SSAValue>();
//        paramsInit.add(newV);
//        final SSAValue initExcV = new SSAValue(ssaNo++, TypeReference.JavaLangException, mRef);
//        final SSAInstruction initStrInst = instructionFactory.InvokeInstruction(initPC, paramsInit, initExcV, initCSR);
//        newM.addStatement(initStrInst);

        final int retPC = newM.getNextProgramCounter();
        final SSAInstruction retInst = instructionFactory.ReturnInstruction(retPC, newV);
        newM.addStatement(retInst);

        return new SummarizedMethodWithNames(mRef, newM, c);
    }

}
