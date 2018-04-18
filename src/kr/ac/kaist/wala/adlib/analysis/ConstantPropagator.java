package kr.ac.kaist.wala.adlib.analysis;

import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.SSAInstruction;

import java.util.Collections;
import java.util.Set;

/**
 * Created by leesh on 17/04/2018.
 */
public class ConstantPropagator {
    private final ICFGSupergraph supergraph;

    public ConstantPropagator(ICFGSupergraph supergraph){
        this.supergraph = supergraph;
    }

    public Set<Integer> find(BasicBlockInContext bb, int v){
        CGNode n = bb.getNode();
        DefUse du = n.getDU();

        SSAInstruction def = du.getDef(v);
        return Collections.emptySet();
    }


}
