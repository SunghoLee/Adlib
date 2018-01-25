package kr.ac.kaist.wala.adlib.dataflow.flows;

import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.collections.Pair;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leesh on 14/09/2017.
 */
public class PropagateFlowFunction extends IdentityFlowFunction {
    //for singleton of same functions
    private static Map<Pair<Integer, Integer>, PropagateFlowFunction> instanceMap = new HashMap<>();
    private int from;
    private int to;

    public static PropagateFlowFunction getInstance(int from, int to){
        Pair p = Pair.make(from, to);

        if(!instanceMap.containsKey(p))
            instanceMap.put(p, new PropagateFlowFunction(from, to));
        return instanceMap.get(p);
    }

    private PropagateFlowFunction(int from, int to) {
        super();

        this.from = from;
        this.to = to;
    }

    public int getFrom(){
        return this.from;
    }

    public int getTo(){
        return this.to;
    }

    @Override
    public int match(SSAInstruction inst, int v) {
        if(v == from)
            return to;
        return super.match(inst, v);
    }
}