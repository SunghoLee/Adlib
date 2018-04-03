package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.impl.SparseNumberedGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 2018. 4. 2..
 */
public class PropagationGraph extends SparseNumberedGraph<PropagationPoint> {
    private Set<PropagationPoint> seeds = new HashSet<>();

    public void addSeed(PathEdge<BasicBlockInContext, DataFact> seed){
        PropagationPoint seedPP = PropagationPoint.make(seed.getFromNode(), seed.getFromFact());
        this.addNode(seedPP);
        seeds.add(seedPP);
    }

    public void addEdge(PathEdge<BasicBlockInContext, DataFact> pre, PathEdge<BasicBlockInContext, DataFact> to){
        PropagationPoint prePP = PropagationPoint.make(pre.getToNode(), pre.getToFact());

        if(!this.containsNode(prePP))
            Assertions.UNREACHABLE("Pre node must be already added to a propagation graph: " + prePP);

        PropagationPoint postPP = PropagationPoint.make(to.getToNode(), to.getToFact());
        this.addNode(postPP);

        this.addEdge(prePP, postPP);
    }

    public Set<PropagationPoint> getSeeds(){
        return this.seeds;
    }
}

