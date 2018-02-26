package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;

/**
 * Created by leesh on 22/02/2018.
 */
public class PathEdge<T, S> {
    private final T fromNode;
    private final S fromFact;
    private final T toNode;
    private final S toFact;

    public PathEdge(T fromNode, S fromFact, T toNode, S toFact){
        this.fromNode = fromNode;
        this.fromFact = fromFact;
        this.toNode = toNode;
        this.toFact = toFact;
    }

    public T getFromNode(){
        return this.fromNode;
    }

    public S getFromFact(){
        return this.fromFact;
    }

    public T getToNode(){
        return this.toNode;
    }

    public S getToFact(){
        return this.toFact;
    }

    @Override
    public int hashCode(){
        //TODO: revise this hashCode to avoid redundant calculation that may occur when a PathEdge is putted to a Set.
        return fromNode.hashCode() + fromFact.hashCode() + toNode.hashCode() + toFact.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof PathEdge){
            PathEdge pe = (PathEdge) o;
            if(pe.fromNode.equals(fromNode) && pe.fromFact.equals(fromFact) &&
                    pe.toNode.equals(toNode) && pe.toFact.equals(toFact))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return fromNode + " [ " + fromFact + " ] -> " + toNode + " [ " + toFact + " ] at I: " + ((BasicBlockInContext)toNode).getLastInstruction();
    }
}
