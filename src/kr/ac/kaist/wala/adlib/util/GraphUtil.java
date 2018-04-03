package kr.ac.kaist.wala.adlib.util;

import com.ibm.wala.util.graph.traverse.BFSPathFinder;
import kr.ac.kaist.wala.adlib.analysis.ReachableAPIFlowGraph;
import kr.ac.kaist.wala.adlib.dataflow.ifds.PropagationGraph;
import kr.ac.kaist.wala.adlib.dataflow.ifds.PropagationPoint;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by leesh on 14/04/2017.
 */
public class GraphUtil {

    public static void writeGraph(ReachableAPIFlowGraph graph, String fileName) throws IOException {
        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(graph);
        oos.close();
    }

    public static ReachableAPIFlowGraph read(String fileName) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        ReachableAPIFlowGraph graph = (ReachableAPIFlowGraph) ois.readObject();
        ois.close();
        return graph;
    }

    public static Set<List<PropagationPoint>> findPathTo(PropagationGraph graph, PropagationPoint to) {
        Set<List<PropagationPoint>> paths = new HashSet<>();

        for(PropagationPoint seed : graph.getSeeds()) {
            BFSPathFinder pathFinder = new BFSPathFinder(graph, seed, to);
            List<PropagationPoint> r = pathFinder.find();
            if(r != null)
                paths.add(r);
        }
        return paths;
    }
}
