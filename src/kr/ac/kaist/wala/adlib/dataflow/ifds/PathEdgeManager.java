package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 22/02/2018.
 */
public class PathEdgeManager {
    private final Set<PathEdge> pathSet = new HashSet<>();

    public PathEdgeManager(){}

    public boolean propagate(PathEdge pe){
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
}
