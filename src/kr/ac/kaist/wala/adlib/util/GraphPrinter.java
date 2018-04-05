package kr.ac.kaist.wala.adlib.util;

import kr.ac.kaist.wala.adlib.dataflow.ifds.PropagationGraph;
import kr.ac.kaist.wala.adlib.dataflow.ifds.PropagationPoint;
import kr.ac.kaist.wala.hybridroid.util.graph.visualize.Visualizer;

import java.util.Iterator;
import java.util.List;

/**
 * Created by leesh on 09/04/2017.
 */
public class GraphPrinter {
    public static void print(PropagationGraph graph){
        Visualizer vis = Visualizer.getInstance();
        vis.setType(Visualizer.GraphType.Digraph);

        for(PropagationPoint n : graph){
            Iterator<PropagationPoint> isucc = graph.getSuccNodes(n);

            while(isucc.hasNext()){
                PropagationPoint succ = isucc.next();
                vis.fromAtoB(n, succ);
            }
        }

        vis.printGraph("ppgraph.dot");
    }

    public static String print(String name, List<PropagationPoint> path){
        Visualizer vis = Visualizer.getInstance();
        vis.setType(Visualizer.GraphType.Digraph);

        PropagationPoint pre = null;

        for(PropagationPoint n : path){
            if(pre != null)
                vis.fromAtoB(n, pre);
            pre = n;
        }

        vis.printGraph(name + ".dot");

        return name + ".dot";
    }
}
