package kr.ac.kaist.wala.adlib.model.entries;

import com.ibm.wala.analysis.typeInference.ConeType;
import com.ibm.wala.analysis.typeInference.PrimitiveType;
import com.ibm.wala.analysis.typeInference.TypeAbstraction;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.IInvokeInstruction;
import com.ibm.wala.ssa.SSAArrayStoreInstruction;
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.SSANewInstruction;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.warnings.Warning;
import com.ibm.wala.util.warnings.Warnings;
import com.ibm.wala.types.ClassLoaderReference;

import java.util.Arrays;

/**
 * A entry model class for analysis entry. As contrast with the WALA FakeRootMethod, this makes each arguments of constructors or methods recursively, conserving exact types.
 * Created by leesh on 09/02/2017.
 */
public class RecursiveParamDefFakeRootMethod extends FakeRootMethod {
    public RecursiveParamDefFakeRootMethod(IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache) {
        super(cha, options, cache);
    }

    /**
     * Add new instruction to the entry model.
     * @param T class type made in the entry
     * @return new instruction added to the entry model
     */
    public SSANewInstruction addEntryAllocation(TypeReference T) {
        return addEntryAllocation(T, true);
    }

    /**
     * Add a New statement of the given type
     *
     * @return instruction added, or null
     * @throws IllegalArgumentException if T is null
     */
    private SSANewInstruction addEntryAllocation(TypeReference T, boolean invokeCtor) {
        if (T == null) {
            throw new IllegalArgumentException("T is null");
        }
        int instance = nextLocal++;
        SSANewInstruction result = null;

        if (T.isReferenceType()) {
            if(T.toString().contains("Landroid/content/Context"))
                T = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/test/mock/MockContext");
            NewSiteReference ref = NewSiteReference.make(statements.size(), T);
            if (T.isArrayType()) {
                int[] sizes = new int[ArrayClass.getArrayTypeDimensionality(T)];
                Arrays.fill(sizes, getValueNumberForIntConstant(1));
                result = insts.NewInstruction(statements.size(), instance, ref, sizes);
            } else {
                result = insts.NewInstruction(statements.size(), instance, ref);
            }
            statements.add(result);

            IClass klass = cha.lookupClass(T);
            if (klass == null) {
                Warnings.add(new AllocationFailure(T));
                return null;
            }

            if (klass.isArrayClass()) {
                int arrayRef = result.getDef();
                TypeReference e = klass.getReference().getArrayElementType();
                while (e != null && !e.isPrimitiveType()) {
                    // allocate an instance for the array contents
                    NewSiteReference n = NewSiteReference.make(statements.size(), e);
                    int alloc = nextLocal++;
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
                IMethod ctor = findLeastArgInitMethod(klass);
                if (ctor != null) {
                    addInvocation(makeArgs(ctor, instance), CallSiteReference.make(statements.size(), ctor.getReference(),
                            IInvokeInstruction.Dispatch.SPECIAL));
                }
            }
        }
        cache.invalidate(this, Everywhere.EVERYWHERE);
        return result;
    }

    private int[] makeArgs(IMethod m, int instance){
        int paramValues[];
        paramValues = new int[m.getNumberOfParameters()];
        paramValues[0] = instance;

        for (int j = 1; j < paramValues.length; j++) {
            paramValues[j] = makeArgument(m, j);
            if (paramValues[j] == -1) {
                // there was a problem
                return null;
            }
        }
        return paramValues;
    }

    protected int makeArgument(IMethod m, int i) {
        TypeReference[] p = new TypeReference[]{m.getParameterType(i)};
        if (p.length == 0) {
            return -1;
        } else if (p.length == 1) {
            if (p[0].isPrimitiveType()) {
                return addLocal();
            } else {
                SSANewInstruction n = addEntryAllocation(p[0]);
                return (n == null) ? -1 : n.getDef();
            }
        } else {
            int[] values = new int[p.length];
            int countErrors = 0;
            for (int j = 0; j < p.length; j++) {
                SSANewInstruction n = addEntryAllocation(p[j]);
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

            return addPhi(values);
        }
    }

    private IMethod findLeastArgInitMethod(IClass k){
        Pair<IMethod, Integer> init = Pair.make(null, Integer.MAX_VALUE);

        for(IMethod m : k.getDeclaredMethods()){
            if(m.isInit()){
                if(m.getNumberOfParameters() < init.snd)
                    init = Pair.make(m, m.getNumberOfParameters());
            }
        }
        if(init.snd == Integer.MAX_VALUE)
            return findLeastArgInitMethod(k.getSuperclass());
        return init.fst;
    }


    /**
     * Add invocation instruction to the entry model.
     * @param params parameter values for the invocation
     * @param site call site of the invocation instruction
     * @return invocation instruction added to the entry model
     */
    public SSAInvokeInstruction addInvocation(int[] params, CallSiteReference site) {
        if (site == null) {
            throw new IllegalArgumentException("site is null");
        }
        CallSiteReference newSite = CallSiteReference.make(statements.size(), site.getDeclaredTarget(), site.getInvocationCode());
        SSAInvokeInstruction s = null;
        if (newSite.getDeclaredTarget().getReturnType().equals(TypeReference.Void)) {
            s = insts.InvokeInstruction(statements.size(), params, nextLocal++, newSite, null);
        } else {
            s = insts.InvokeInstruction(statements.size(), nextLocal++, params, nextLocal++, newSite, null);
        }
        statements.add(s);
        cache.invalidate(this, Everywhere.EVERYWHERE);
        return s;
    }

    private class AllocationFailure extends Warning {

        final TypeReference t;

        public AllocationFailure(TypeReference t) {
            super(Warning.SEVERE);
            this.t = t;
        }

        @Override
        public String getMsg() {
            return getClass().toString() + " : " + t;
        }

    }
}
