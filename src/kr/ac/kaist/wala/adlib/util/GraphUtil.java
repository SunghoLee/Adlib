package kr.ac.kaist.wala.adlib.util;

import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.traverse.BFSPathFinder;
import kr.ac.kaist.wala.adlib.dataflow.ifds.PropagationGraph;
import kr.ac.kaist.wala.adlib.dataflow.ifds.PropagationPoint;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Created by leesh on 14/04/2017.
 */
public class GraphUtil {
    public static Set<List<PropagationPoint>> findPathTo(PropagationGraph graph, PropagationPoint to) {
        Set<List<PropagationPoint>> paths = new HashSet<>();

        for(PropagationPoint seed : graph.getSeeds()) {
            List<PropagationPoint> r = findPathFromTo(graph, seed, to);

            if(r == null)
                continue;

            if(r != null)
                paths.add(r);
        }
        return paths;
    }

    public static String convertDotToSvg(String ori){
        File f = new File(ori);
        if(!f.exists())
            Assertions.UNREACHABLE("The file, " + ori +", does not exist.");

        String svg = ori.substring(0, ori.lastIndexOf(".")) + ".svg";

        String cmd[] = {
          "dot",
                "-Tsvg",
                ori,
                "-o",
                svg
        };
        try {
            Runtime.getRuntime().exec(cmd, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return svg;
    }

    private static List<PropagationPoint> findPathFromTo(PropagationGraph graph, PropagationPoint from, PropagationPoint to){
        BFSPathFinder pathFinder = new BFSPathFinder(graph, from, to);

        List<PropagationPoint> r = pathFinder.find();

        if(r == null)
            return null;

        if(!isMatchedWithCut(graph, r)){
            return findPathFromTo(graph, from, to);
        }

        return r;
    }

    private static boolean isMatchedWithCut(PropagationGraph graph, List<PropagationPoint> path){
        Stack fn = new Stack();

        for(int i=0; i<path.size(); i++){
            PropagationPoint pp = path.get(i);
            if(pp.getBlock().isEntryBlock())
                fn.add(pp.getBlock().getNode());

            else if(pp.getBlock().isExitBlock() && i+1 < path.size()){
                PropagationPoint nextPP = path.get(i+1);
                if(!fn.pop().equals(nextPP)) {
                    System.out.println("#MISSMATCH FROM: " + pp);
                    System.out.println("#MISSMATCH TO: " + nextPP);
                    graph.removeEdge(pp, nextPP);
                    System.out.println("CUTTED? " + graph.hasEdge(pp, nextPP));
                    return false;
                }
            }
        }

        return true;
    }
}
