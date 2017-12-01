package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.SSAInstruction;

/**
 * Created by leesh on 19/09/2017.
 */
public class StaticFieldDataPointer implements IDataPointer{
    private IField f;

    public StaticFieldDataPointer(IField f){
        this.f = f;
    }

    public IField getField(){
        return f;
    }

    @Override
    public IDataPointer visit(SSAInstruction inst, IFlowFunction f) {
        return this;
    }

    @Override
    public PointerKey getPointerKey(PointerAnalysis<InstanceKey> pa) {
        return pa.getHeapModel().getPointerKeyForStaticField(f);
    }

    @Override
    public int hashCode(){
        return f.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof StaticFieldDataPointer){
            StaticFieldDataPointer sfdp = (StaticFieldDataPointer) o;
            if(sfdp.f.equals(this.f))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "[StaticField] " + f;
    }
}
