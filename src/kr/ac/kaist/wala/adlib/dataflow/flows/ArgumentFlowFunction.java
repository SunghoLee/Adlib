package kr.ac.kaist.wala.adlib.dataflow.flows;

import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;

/**
 * Created by leesh on 25/09/2017.
 */
public class ArgumentFlowFunction implements IFlowFunction {

    @Override
    public int match(SSAInstruction inst, int v) {
        if(inst instanceof SSAAbstractInvokeInstruction){
            for(int index = 0; index < inst.getNumberOfUses(); index++){
                if(inst.getUse(index) == v)
                    return index + 1;
            }
        }

        return -1;
    }
}
