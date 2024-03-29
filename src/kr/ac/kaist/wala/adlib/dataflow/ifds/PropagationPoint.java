package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.INodeWithNumber;

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
    private final DataFact fact;
    private boolean isTarget = false;

    public static PropagationPoint make(BasicBlockInContext bb, DataFact fact){
        if(points.containsKey(bb)){
            for(PropagationPoint p : points.get(bb)){
                if(p.fact.equals(fact))
                    return p;
            }
        }else
            points.put(bb, new HashSet<>());

        PropagationPoint p = new PropagationPoint(bb, fact);
        points.get(bb).add(p);

        return p;
    }


    private PropagationPoint(BasicBlockInContext bb, DataFact fact){
        this.bb = bb;
        this.fact = fact;
        this.id = ID++;
    }

    @Override
    public int getGraphNodeId() {
        return id;
    }

    @Override
    public void setGraphNodeId(int i) {
        Assertions.UNREACHABLE("Did not implement this!");
    }

    public DataFact getFact(){
        return this.fact;
    }

    public BasicBlockInContext getBlock(){
        return this.bb;
    }

    public boolean isTarget(){
        return isTarget;
    }

    public void setTarget(){
        isTarget = true;
    }

    public void unsetTarget(){
        isTarget = false;
    }

    @Override
    public int hashCode(){
        return bb.hashCode() + fact.hashCode();
    }

    @Override
    public String toString(){
        if(bb.isEntryBlock()){
            return "[PP@ ENTRY ( " + fact + " )] " + bb.getNode();
        }else if(bb.isExitBlock()){
            return "[PP@ EXIT ( " + fact + " )] " + bb.getNode();
        }else{
            return "[PP@ NORMAL ( " + fact + " )] ( " + bb.getLastInstructionIndex() + " ) " + bb.getLastInstruction() + " IN " + bb.getNode();
        }
    }
}
