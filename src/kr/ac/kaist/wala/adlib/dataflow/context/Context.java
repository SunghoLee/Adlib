package kr.ac.kaist.wala.adlib.dataflow.context;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import kr.ac.kaist.wala.adlib.dataflow.Node;

import java.util.Set;

/**
 * Created by leesh on 15/12/2017.
 */
public interface Context {
    public enum TYPE{
        NORMAL,
        CALL,
        EXIT,
    };

    public Set<Node> getNext(Node curNode, Set<BasicBlockInContext> nexts, TYPE type);

    public boolean isFeasibleReturn(CGNode n, SSAAbstractInvokeInstruction invokeInst);

    @Override
    public int hashCode();

    @Override
    public boolean equals(Object o);
}
