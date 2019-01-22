package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.util.collections.Pair;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 22/02/2018.
 */
public class PathEdgeManager {
    private final Set<PathEdge> pathSet = new HashSet<>();
    private static Set<Pair> totalDF = new HashSet<>();
    public PathEdgeManager(){}

    public boolean propagate(PathEdge pe){
        totalDF.add(Pair.make(pe.getToNode(), pe.getToFact()));
        return pathSet.add(pe);
    }

    public boolean contains(PathEdge pe){
        return pathSet.contains(pe);
    }

    public Set<PathEdge> findLocalEdgeTo(BasicBlockInContext bb, DataFact df){
        Set<PathEdge> res = new HashSet<>();

        for(PathEdge pe : pathSet){
            if(pe.getToNode().equals(bb) && pe.getToFact().equals(df))
                res.add(pe);
        }

        return res;
    }

    public int size(){
        return pathSet.size();
    }

    public Set<PathEdge> getEdges(){
        return pathSet;
    }

    public void clear(){
        pathSet.clear();
    }

    public Set<Pair> getTotalDF(){
        return totalDF;
    }
}
