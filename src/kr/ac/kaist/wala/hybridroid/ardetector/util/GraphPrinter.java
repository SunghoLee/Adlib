package kr.ac.kaist.wala.hybridroid.ardetector.util;

import kr.ac.kaist.wala.hybridroid.ardetector.analyzer.APICallNode;
import kr.ac.kaist.wala.hybridroid.ardetector.analyzer.ReachableAPIFlowGraph;
import kr.ac.kaist.wala.hybridroid.util.graph.visualize.Visualizer;

import java.util.Iterator;

/**
 * Created by leesh on 09/04/2017.
 */
public class GraphPrinter {
    public static void print(ReachableAPIFlowGraph graph){
        Visualizer vis = Visualizer.getInstance();
        vis.setType(Visualizer.GraphType.Digraph);

        for(APICallNode n : graph){
            Iterator<APICallNode> isucc = graph.getSuccNodes(n);

            while(isucc.hasNext()){
                APICallNode succ = isucc.next();
                vis.fromAtoB(n, succ);
            }
        }

        vis.printGraph("apigraph.dot");
    }
}
