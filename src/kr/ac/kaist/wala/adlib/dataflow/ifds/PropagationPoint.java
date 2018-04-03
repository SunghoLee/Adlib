package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.cfg.InducedCFG;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.INodeWithNumber;
import kr.ac.kaist.wala.hybridroid.util.data.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by leesh on 2018. 4. 2..
 */
public class PropagationPoint implements INodeWithNumber {
    private static int ID = 1;
    private static Map<BasicBlockInContext, Set<PropagationPoint>> points = new HashMap<>();
    private int id;

    private final BasicBlockInContext bb;
    private final DataFact fromFact;
    private final DataFact toFact;

    public static PropagationPoint make(BasicBlockInContext bb, DataFact from, DataFact to){
        if(points.containsKey(bb)){
            for(PropagationPoint p : points.get(bb)){
                if(p.fromFact.equals(from) && p.toFact.equals(to))
                    return p;
            }
        }else
            points.put(bb, new HashSet<>());

        PropagationPoint p = new PropagationPoint(bb, from, to);
        points.get(bb).add(p);

        return p;
    }


    private PropagationPoint(BasicBlockInContext bb, DataFact from, DataFact to){
        this.bb = bb;
        this.fromFact = from;
        this.toFact = to;
    }

    @Override
    public int getGraphNodeId() {
        return id;
    }

    @Override
    public void setGraphNodeId(int i) {
        Assertions.UNREACHABLE("Did not implement this!");
    }

    public DataFact getFromFact(){
        return this.fromFact;
    }

    public DataFact getToFact(){
        return this.toFact;
    }

    public BasicBlockInContext getBlock(){
        return this.bb;
    }
}
