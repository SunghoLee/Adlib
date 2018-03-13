package kr.ac.kaist.wala.adlib.util;

import kr.ac.kaist.wala.adlib.analysis.ReachableAPIFlowGraph;

import java.io.*;

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
}
