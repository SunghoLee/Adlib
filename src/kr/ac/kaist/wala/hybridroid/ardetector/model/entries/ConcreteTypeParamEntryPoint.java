package kr.ac.kaist.wala.hybridroid.ardetector.model.entries;

import com.ibm.wala.analysis.typeInference.ConeType;
import com.ibm.wala.analysis.typeInference.PrimitiveType;
import com.ibm.wala.analysis.typeInference.TypeAbstraction;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.dalvik.util.AndroidComponent;
import com.ibm.wala.dalvik.util.AndroidEntryPointLocator;
import com.ibm.wala.ipa.callgraph.impl.AbstractRootMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.types.TypeReference;

/**
 * Created by leesh on 09/02/2017.
 */
public class ConcreteTypeParamEntryPoint extends AndroidEntryPoint {
    public ConcreteTypeParamEntryPoint(AndroidEntryPointLocator.AndroidPossibleEntryPoint p, IMethod method, IClassHierarchy cha, AndroidComponent inComponent) {
        super(p, method, cha, inComponent);
    }

    public ConcreteTypeParamEntryPoint(AndroidEntryPointLocator.AndroidPossibleEntryPoint p, IMethod method, IClassHierarchy cha) {
        super(p, method, cha);
    }

    public ConcreteTypeParamEntryPoint(ExecutionOrder o, IMethod method, IClassHierarchy cha, AndroidComponent inComponent) {
        super(o, method, cha, inComponent);
    }

    public ConcreteTypeParamEntryPoint(ExecutionOrder o, IMethod method, IClassHierarchy cha) {
        super(o, method, cha);
    }

    @Override
    protected int makeArgument(AbstractRootMethod m, int i) {
        TypeReference[] p = getParameterTypes(i);
        if (p.length == 0) {
            return -1;
        } else if (p.length == 1) {
            if (p[0].isPrimitiveType()) {
                return m.addLocal();
            } else {
                SSANewInstruction n = ((RecursiveParamDefFakeRootMethod) m).addEntryAllocation(p[0]);
                return (n == null) ? -1 : n.getDef();
            }
        } else {
            int[] values = new int[p.length];
            int countErrors = 0;
            for (int j = 0; j < p.length; j++) {
                SSANewInstruction n = ((RecursiveParamDefFakeRootMethod) m).addEntryAllocation(p[j]);
                int value = (n == null) ? -1 : n.getDef();
                if (value == -1) {
                    countErrors++;
                } else {
                    values[j - countErrors] = value;
                }
            }
            if (countErrors > 0) {
                int[] oldValues = values;
                values = new int[oldValues.length - countErrors];
                System.arraycopy(oldValues, 0, values, 0, values.length);
            }

            TypeAbstraction a;
            if (p[0].isPrimitiveType()) {
                a = PrimitiveType.getPrimitive(p[0]);
                for (i = 1; i < p.length; i++) {
                    a = a.meet(PrimitiveType.getPrimitive(p[i]));
                }
            } else {
                IClassHierarchy cha = m.getClassHierarchy();
                IClass p0 = cha.lookupClass(p[0]);
                a = new ConeType(p0);
                for (i = 1; i < p.length; i++) {
                    IClass pi = cha.lookupClass(p[i]);
                    a = a.meet(new ConeType(pi));
                }
            }

            return m.addPhi(values);
        }
    }

    @Override
    public SSAAbstractInvokeInstruction addCall(AbstractRootMethod m) {
        int paramValues[];
        CallSiteReference site = makeSite(0);
        if (site == null) {
            return null;
        }
        System.err.println("About: " + this);
        paramValues = new int[getNumberOfParameters()];
        for (int j = 0; j < paramValues.length; j++) {
            paramValues[j] = makeArgument(m, j);
            if (paramValues[j] == -1) {
                // there was a problem
                return null;
            }
        }

        return m.addInvocation(paramValues, site);
    }

    /*
    private SSANewInstruction addAllocation(AbstractRootMethod m, TypeReference T, boolean invokeCtor) {
        if (T == null) {
            throw new IllegalArgumentException("T is null");
        }
        int instance = m.addLocal();
        SSANewInstruction result = null;

        if (T.isReferenceType()) {
            NewSiteReference ref = NewSiteReference.make(m.getStatements().length, T);
            if (T.isArrayType()) {
                int[] sizes = new int[ArrayClass.getArrayTypeDimensionality(T)];
                Arrays.fill(sizes, m.getValueNumberForIntConstant(1));
                result = insts.NewInstruction(statements.size(), instance, ref, sizes);
            } else {
                result = insts.NewInstruction(statements.size(), instance, ref);
            }
            statements.add(result);

            IClass klass = cha.lookupClass(T);
            System.err.println("\tK: " + klass);
            if (klass == null) {
                Warnings.add(AbstractRootMethod.AllocationFailure.create(T));
                return null;
            }

            if (klass.isArrayClass()) {
                int arrayRef = result.getDef();
                TypeReference e = klass.getReference().getArrayElementType();
                while (e != null && !e.isPrimitiveType()) {
                    // allocate an instance for the array contents
                    NewSiteReference n = NewSiteReference.make(statements.size(), e);
                    int alloc = m.addLocal();
                    SSANewInstruction ni = null;
                    if (e.isArrayType()) {
                        int[] sizes = new int[((ArrayClass)cha.lookupClass(T)).getDimensionality()];
                        Arrays.fill(sizes, getValueNumberForIntConstant(1));
                        ni = insts.NewInstruction(statements.size(), alloc, n, sizes);
                    } else {
                        ni = insts.NewInstruction(statements.size(), alloc, n);
                    }
                    statements.add(ni);

                    // emit an astore
                    SSAArrayStoreInstruction store = insts.ArrayStoreInstruction(statements.size(), arrayRef, getValueNumberForIntConstant(0), alloc, e);
                    statements.add(store);

                    e = e.isArrayType() ? e.getArrayElementType() : null;
                    arrayRef = alloc;
                }
            }
            if (invokeCtor) {
                IMethod ctor = cha.resolveMethod(klass, MethodReference.initSelector);
                if (ctor != null) {
                    System.err.println("\tCTOR:" + ctor);
                    m.addInvocation(new int[] { instance }, CallSiteReference.make(statements.size(), ctor.getReference(),
                            IInvokeInstruction.Dispatch.SPECIAL));
                }
            }
        }
        cache.invalidate(this, Everywhere.EVERYWHERE);
        return result;
    }

    protected int getValueNumberForIntConstant(int c) {
        ConstantValue v = new ConstantValue(c);
        Integer result = constant2ValueNumber.get(v);
        if (result == null) {
            result = nextLocal++;
            constant2ValueNumber.put(v, result);
        }
        return result;
    }
    */
}
