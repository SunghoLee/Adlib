package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.ssa.SSAInstruction;

/**
 * Created by leesh on 14/09/2017.
 */
public interface IFlowFunction {
    public static int RETURN_VARIABLE = -2;
    public static int ANY = -3;
    public static int TERMINATE = -4;

    public int match(SSAInstruction inst, int v);
}
