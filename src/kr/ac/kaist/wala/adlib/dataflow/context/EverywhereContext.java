package kr.ac.kaist.wala.adlib.dataflow.context;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import kr.ac.kaist.wala.adlib.dataflow.Node;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by leesh on 19/12/2017.
 */
public class EverywhereContext implements Context{
    private static EverywhereContext instance;

    public static EverywhereContext getInstance(){
        if(instance == null)
            instance = new EverywhereContext();
        return instance;
    }

    private EverywhereContext(){}

    @Override
    public Set<Node> getNext(Node curNode, Set<BasicBlockInContext> nexts, TYPE type) {
        return nexts.stream().map((bb) -> new Node(bb, EverywhereContext.getInstance())).collect(Collectors.toSet());
    }

    @Override
    public boolean isFeasibleReturn(CGNode n, SSAAbstractInvokeInstruction invokeInst) {
        return true;
    }

    @Override
    public int hashCode(){
        return 1947285013;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof EverywhereContext)
            return true;
        return false;
    }
}
