package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import kr.ac.kaist.wala.hybridroid.util.data.Pair;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by leesh on 23/02/2018.
 */
public class GraphDataFlowManager {
    private final ICFGSupergraph supergraph;
    private final InfeasiblePathFilter normalPathFilter;
    private final InfeasiblePathFilter callPathFilter;
    private final InfeasiblePathFilter exitPathFilter;
    private final InfeasibleDataFilter normalDataFilter;
    private final InfeasibleDataFilter callDataFilter;
    private final InfeasibleDataFilter callToRetDataFilter;
    private final SummaryEdgeManager seManager;

    public GraphDataFlowManager(ICFGSupergraph supergraph, SummaryEdgeManager seManager){
        this.supergraph = supergraph;
        this.normalPathFilter = new DefaultPathFilter();
        this.callPathFilter = new DefaultPathFilter();
        this.exitPathFilter = new DefaultPathFilter();
        this.normalDataFilter = new DefaultDataFilter();
        this.callDataFilter = new DefaultDataFilter();
        this.callToRetDataFilter = new DefaultDataFilter();
        this.seManager = seManager;
    }

    public Set<Pair<BasicBlockInContext, DataFact>> getNormalNexts(BasicBlockInContext node, DataFact fact){
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        SSAInstruction inst = node.getLastInstruction();

        if(inst == null || fact.equals(DataFact.DEFAULT_FACT)) {
            // if it is an empty block
            for(BasicBlockInContext succ : getNormalSuccessors(node)){
                res.add(Pair.make(succ, fact));
            }
        }else{
            // TODO: need to handle each instruction's semantics
        }
        return res;
    }

    public Set<Pair<BasicBlockInContext, DataFact>> getCalleeNexts(BasicBlockInContext node, DataFact fact) throws InfeasiblePathException {
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) node.getLastInstruction();

        if(invokeInst == null)
            throw new InfeasiblePathException("Call node must have an instruction: " + node);

        DataFact calleeDataFact = getCalleeDataFact(invokeInst, fact);
        for(BasicBlockInContext callee : getCalleeSuccessors(node))
            res.add(Pair.make(callee, calleeDataFact));

        return res;
    }

    public Set<Pair<BasicBlockInContext, DataFact>> getCallToReturnNexts(BasicBlockInContext node, DataFact fact) throws InfeasiblePathException {
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) node.getLastInstruction();

        if(invokeInst == null)
            throw new InfeasiblePathException("Call node must have an instruction: " + node);

        for(BasicBlockInContext ret : getCallToReturnSuccessors(node))
            if(callToRetDataFilter.accept(node, ret, fact))
                res.add(Pair.make(ret, fact));

        return res;
    }

    public Set<Pair<BasicBlockInContext, DataFact>> getExitNexts(BasicBlockInContext node, DataFact fact){
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        //TODO: revise this!
        for(BasicBlockInContext ret : getExitSuccessors(node))
            if(callToRetDataFilter.accept(node, ret, fact))
                res.add(Pair.make(ret, fact));

        return res;
    }

    public Set<Pair<BasicBlockInContext, DataFact>> getCallerDataToEntry(BasicBlockInContext entry, DataFact fact) throws InfeasiblePathException {
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        Iterator<BasicBlockInContext> iPred = supergraph.getPredNodes(entry);

        while(iPred.hasNext()){
            BasicBlockInContext pred = iPred.next();
            if(pred.getLastInstruction() == null)
                throw new InfeasiblePathException("Call node must have an instruction: " + pred);

            SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) pred.getLastInstruction();
            int index = getArgumentIndex(fact);
            DataFact callerFact = makeNewDataFact(pred.getNode(), invokeInst.getUse(index));

        }

        return res;
    }

    private int getArgumentIndex(DataFact fact){
        // TODO: Implement this!
        return 0;
    }

    private DataFact makeNewDataFact(CGNode node, int numV){
        // TODO: Implement this!
        return new DataFact();
    }

    protected DataFact getCalleeDataFact(SSAAbstractInvokeInstruction invokeInst, DataFact fact){
        // TODO: need to caculcate a callee data fact regarding to the caller data fact used in the invoke instruction
        return fact;
    }

    protected Set<BasicBlockInContext> getNormalSuccessors(BasicBlockInContext bb){
        Set<BasicBlockInContext> res = new HashSet<>();

        boolean isRet = false;

        if(bb.getLastInstruction() != null && bb.getLastInstruction() instanceof SSAReturnInstruction)
            isRet = true;
        Iterator<BasicBlockInContext> iSucc = supergraph.getSuccNodes(bb);
        while(iSucc.hasNext()){
            BasicBlockInContext succ = iSucc.next();

            // exclude exception paths
            if(!isRet && succ.isExitBlock())
                continue;
            if(normalPathFilter.accept(bb, succ))
                res.add(succ);
        }
        return res;
    }

    protected Set<BasicBlockInContext> getCalleeSuccessors(BasicBlockInContext bb){
        Set<BasicBlockInContext> res = new HashSet<>();

        Iterator<BasicBlockInContext> iSucc = supergraph.getCalledNodes(bb);

        while(iSucc.hasNext()){
            BasicBlockInContext succ = iSucc.next();
            if(callPathFilter.accept(bb, succ))
                res.add(succ);
        }
        return res;
    }

    protected Set<BasicBlockInContext> getCallToReturnSuccessors(BasicBlockInContext bb){
        Set<BasicBlockInContext> res = new HashSet<>();

        // getReturnSites method does not use the second argument. So, we just pass null.
        // TODO: Do we need to check that the return sites include exceptional paths?
        Iterator<BasicBlockInContext> iSucc = supergraph.getReturnSites(bb, null);

        while(iSucc.hasNext()){
            BasicBlockInContext succ = iSucc.next();
            // exclude exception paths
            if(succ.isExitBlock())
                continue;
            if(callPathFilter.accept(bb, succ))
                res.add(succ);
        }
        return res;
    }

    protected Set<BasicBlockInContext> getExitSuccessors(BasicBlockInContext bb){
        Set<BasicBlockInContext> res = new HashSet<>();

        Iterator<BasicBlockInContext> iSucc = supergraph.getCalledNodes(bb);

        while(iSucc.hasNext()){
            BasicBlockInContext succ = iSucc.next();
            if(exitPathFilter.accept(bb, succ))
                res.add(succ);
        }
        return res;
    }

    interface InfeasiblePathFilter {
        public boolean accept(BasicBlockInContext bb, BasicBlockInContext succ);
    }

    class DefaultPathFilter implements InfeasiblePathFilter {
        @Override
        public boolean accept(BasicBlockInContext bb, BasicBlockInContext succ) {
            return true;
        }
    }

    interface InfeasibleDataFilter {
        public boolean accept(BasicBlockInContext bb, BasicBlockInContext succ, DataFact df);
    }

    class DefaultDataFilter implements InfeasibleDataFilter {
        @Override
        public boolean accept(BasicBlockInContext bb, BasicBlockInContext succ, DataFact df) {
            return true;
        }
    }
}
