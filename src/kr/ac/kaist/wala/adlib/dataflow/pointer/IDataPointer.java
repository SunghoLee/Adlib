package kr.ac.kaist.wala.adlib.dataflow.pointer;

import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ssa.SSAInstruction;
import kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction;

/**
 * Created by leesh on 19/09/2017.
 */
public interface IDataPointer {
    public PointerKey getPointerKey(PointerAnalysis<InstanceKey> pa);
    public IDataPointer visit(SSAInstruction inst, IFlowFunction f);
    //public boolean isNodeMatched(CGNode n);
}