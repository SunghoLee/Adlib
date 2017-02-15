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

import java.util.Collections;

/**
 * Created by leesh on 09/02/2017.
 */
public class ConcreteTypeParamEntryPoint extends AndroidEntryPoint {
    private final IClass klass;

    public ConcreteTypeParamEntryPoint(AndroidEntryPointLocator.AndroidPossibleEntryPoint p, IMethod method, IClassHierarchy cha, AndroidComponent inComponent) {
        super(p, method, cha, inComponent);
        klass = null;
    }

    public ConcreteTypeParamEntryPoint(AndroidEntryPointLocator.AndroidPossibleEntryPoint p, IMethod method, IClassHierarchy cha) {
        super(p, method, cha);
        klass = null;
    }

    public ConcreteTypeParamEntryPoint(ExecutionOrder o, IMethod method, IClassHierarchy cha, AndroidComponent inComponent) {
        super(o, method, cha, inComponent);
        klass = null;
    }

    public ConcreteTypeParamEntryPoint(ExecutionOrder o, IMethod method, IClassHierarchy cha) {
        super(o, method, cha);
        klass = null;
    }

    public ConcreteTypeParamEntryPoint(ExecutionOrder o, IClass klass, IMethod method, IClassHierarchy cha) {
        super(o, method, cha);
        this.klass = klass;
    }

    public IClass getReceiverClass(){
        return (klass == null)? method.getDeclaringClass() : klass;
    }

    @Override
    protected int makeArgument(AbstractRootMethod m, int i) {
        TypeReference[] p = null;

        //SPECIAL: if this argument is a receiver
        if(method.isStatic() == false && i == 0 && klass != null) {
            p = Collections.singletonList(klass.getReference()).toArray(new TypeReference[0]);
        }else
            p = getParameterTypes(i);

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
}
