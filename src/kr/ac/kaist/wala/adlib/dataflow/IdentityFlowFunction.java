package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.ssa.SSAInstruction;

/**
 * Created by leesh on 14/09/2017.
 */
public class IdentityFlowFunction implements IFlowFunction {
    // for singleton
    private static IdentityFlowFunction instance;

    public static IdentityFlowFunction getInstance(){
        if(instance == null)
            instance = new IdentityFlowFunction();
        return instance;
    }

    protected IdentityFlowFunction(){}

    @Override
    public int match(SSAInstruction inst, int v) {
        return v;
    }
}