package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.SSAInstruction;

/**
 * Created by leesh on 19/09/2017.
 */
public class LocalDataPointer implements IDataPointer {
    private CGNode n;
    private int var;

    public LocalDataPointer(CGNode n, int var){
        this.n = n;
        this.var = var;
    }

    public CGNode getNode(){
        return n;
    }

    public int getVar(){
        return var;
    }

    @Override
    public IDataPointer visit(SSAInstruction inst, IFlowFunction f) {
        int newVar = f.match(inst, var);

        if(newVar != var)
            return new LocalDataPointer(n, newVar);

        return this;
    }

    @Override
    public PointerKey getPointerKey(PointerAnalysis<InstanceKey> pa) {
        return pa.getHeapModel().getPointerKeyForLocal(n, var);
    }

    @Override
    public int hashCode(){
        return this.n.hashCode() + var;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) o;
            if(ldp.n.equals(n) && ldp.var == var)
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "[Local] " + var + " in " + n;
    }
}
