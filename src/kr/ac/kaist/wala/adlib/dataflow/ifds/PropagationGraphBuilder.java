package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;

import java.io.IOException;

/**
 * Created by leesh on 04/04/2018.
 */
public class PropagationGraphBuilder {
    private final PropagationGraph graph;

    public PropagationGraphBuilder(){
        graph = new PropagationGraph();
    }

    public void addSeed(PathEdge<BasicBlockInContext, DataFact> seed){
        PropagationPoint seedPP = PropagationPoint.make(seed.getFromNode(), seed.getFromFact());
        graph.addSeed(seedPP);
    }

    public void addEdge(PathEdge<BasicBlockInContext, DataFact> pre, PathEdge<BasicBlockInContext, DataFact> to){
        PropagationPoint prePP = PropagationPoint.make(pre.getToNode(), pre.getToFact());
        PropagationPoint postPP = PropagationPoint.make(to.getToNode(), to.getToFact());

        if(pre.toString().contains("qwdvszef")){
            System.out.println("#FROM: " + prePP);
            System.out.println("#TO: " + postPP);
            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        graph.addEdge(prePP, postPP);
    }

    public PropagationGraph getGraph(){
        return this.graph;
    }
}
