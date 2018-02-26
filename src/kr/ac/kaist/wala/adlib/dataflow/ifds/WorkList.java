package kr.ac.kaist.wala.adlib.dataflow.ifds;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by leesh on 22/02/2018.
 */
public class WorkList {
    private final Queue<PathEdge> queue = new LinkedBlockingQueue<>();

    public WorkList(){}

    public void put(PathEdge pe){
        // Duplicated PathEdges are filtered by PathEdgeManager, so they cannot come in this method.
        queue.add(pe);
    }

    public PathEdge poll(){
        return queue.poll();
    }

    public boolean isEmpty(){
        return queue.isEmpty();
    }
}
