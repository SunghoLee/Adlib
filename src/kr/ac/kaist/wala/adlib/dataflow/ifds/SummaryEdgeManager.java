package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import kr.ac.kaist.wala.hybridroid.util.data.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 23/02/2018.
 */
public class SummaryEdgeManager {
    private final Set<PathEdge> edgeSet = new HashSet<>();

    public SummaryEdgeManager(){}

    public boolean add(PathEdge pe){
        return edgeSet.add(pe);
    }

    public Set<Pair<BasicBlockInContext, DataFact>> getSummaryFrom(BasicBlockInContext fromNode, DataFact fromFact){
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        for(PathEdge<BasicBlockInContext, DataFact> pe : edgeSet){
            if(pe.getFromNode().equals(fromNode) && pe.getFromFact().equals(fromFact)){
                res.add(Pair.make(pe.getToNode(), pe.getToFact()));
            }
        }
        return res;
    }
}
