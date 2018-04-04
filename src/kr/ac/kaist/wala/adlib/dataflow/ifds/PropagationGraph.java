package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.impl.SparseNumberedGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 2018. 4. 2..
 */
public class PropagationGraph extends SparseNumberedGraph<PropagationPoint> {
    private Set<PropagationPoint> seeds = new HashSet<>();

    public void addSeed(PropagationPoint seedPP){
        addNode(seedPP);
        seeds.add(seedPP);
    }

    public void addEdge(PropagationPoint prePP, PropagationPoint postPP){
        if(!this.containsNode(prePP))
            Assertions.UNREACHABLE("Pre node must be already added to a propagation graph: " + prePP);

        super.addNode(postPP);
        super.addEdge(prePP, postPP);
    }

    public Set<PropagationPoint> getSeeds(){
        return this.seeds;
    }
}

