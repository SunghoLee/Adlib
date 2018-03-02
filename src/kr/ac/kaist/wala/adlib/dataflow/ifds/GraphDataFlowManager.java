package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.ConstantKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.intset.OrdinalSet;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.hybridroid.util.data.Pair;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

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
    private final PointerAnalysis<InstanceKey> pa;

    public GraphDataFlowManager(ICFGSupergraph supergraph, PointerAnalysis<InstanceKey> pa, SummaryEdgeManager seManager){
        this.supergraph = supergraph;
        this.normalPathFilter = new DefaultPathFilter();
        this.callPathFilter = new DefaultPathFilter();
        this.exitPathFilter = new DefaultPathFilter();
        this.normalDataFilter = new DefaultDataFilter();
        this.callDataFilter = new DefaultDataFilter();
        this.callToRetDataFilter = new DefaultDataFilter();
        this.seManager = seManager;
        this.pa = pa;
    }

    boolean debug = false;
    public Set<Pair<BasicBlockInContext, DataFact>> getNormalNexts(BasicBlockInContext node, DataFact fact){
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        try {
            if(fact.equals(DataFact.DEFAULT_FACT)) {
                for(BasicBlockInContext succ : getNormalSuccessors(node)){
                    res.add(Pair.make(succ, fact));
                }
            }else{
                // TODO: need to handle each instruction's semantics
                // TODO: do we need to consider the dense propagation or only sparse one?
                for(BasicBlockInContext succ : getSparseNormalSuccessors(node, fact)){
                    res.add(Pair.make(succ, fact));
                }
                SSAInstruction inst;
            }
        } catch (InfeasiblePathException e) {
            e.printStackTrace();
        }
        debug = false;
        return res;
    }

    public Set<Pair<BasicBlockInContext, DataFact>> getCalleeNexts(BasicBlockInContext node, DataFact fact) throws InfeasiblePathException {
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) node.getLastInstruction();

        if(invokeInst == null)
            throw new InfeasiblePathException("Call node must have an instruction: " + node);

        for(BasicBlockInContext callee : getCalleeSuccessors(node)) {
            DataFact calleeDataFact = getCalleeDataFact(callee.getNode(), invokeInst, fact);
            res.add(Pair.make(callee, calleeDataFact));
        }

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

    public Set<Pair<BasicBlockInContext, DataFact>> getRetInfo(BasicBlockInContext call, BasicBlockInContext exit, DataFact fact) throws InfeasiblePathException {
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) call.getLastInstruction();

        if(!invokeInst.hasDef())
            return res;

        int defV = invokeInst.getDef();

        // TODO: we need to handle globlal data fact.
        if((fact instanceof LocalDataFact) == false)
            return res;

        LocalDataFact ldf = (LocalDataFact) fact;
        int v = ldf.getVar();

        //TODO: we need to handle objects that do not be returned, but are alias with others.
        if(!isReturned(exit.getNode().getDU(), v))
            return res;

        Iterator<BasicBlockInContext> iRetSites = supergraph.getReturnSites(call, exit.getNode());
        while(iRetSites.hasNext()){
            BasicBlockInContext retSite = iRetSites.next();
            res.add(Pair.make(retSite, new LocalDataFact(retSite.getNode(), defV, ldf.getField())));
        }
        return res;
    }

    public Set<Pair<BasicBlockInContext, DataFact>> getCallerInfo(BasicBlockInContext entry, DataFact fact) {
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        Iterator<BasicBlockInContext> iCallerBlock = supergraph.getPredNodes(entry);

        if(fact instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) fact;

            while(iCallerBlock.hasNext()){
                BasicBlockInContext callerBlock = iCallerBlock.next();
                SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) callerBlock.getLastInstruction();
                int useV = invokeInst.getUse(ldf.getVar()-1);
                res.add(Pair.make(callerBlock, new LocalDataFact(callerBlock.getNode(), useV, ldf.getField())));
            }

        }
        return res;
    }

    public Set<Pair<BasicBlockInContext, DataFact>> getExitNexts(BasicBlockInContext node, DataFact fact){
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        for(BasicBlockInContext ret : getExitSuccessors(node)) {
            if (callToRetDataFilter.accept(node, ret, fact))
                res.add(Pair.make(ret, fact));
        }

        return res;
    }

    private boolean isReturned(DefUse du, int v){
        Iterator<SSAInstruction> iUse = du.getUses(v);
        while(iUse.hasNext()){
            SSAInstruction useInst = iUse.next();

            if(useInst instanceof SSAReturnInstruction){
                if(useInst.getNumberOfUses() > 0 && useInst.getUse(0) == v)
                    return true;
            }
        }
        return false;
    }

    public Set<Pair<BasicBlockInContext, DataFact>> getCallerDataToEntry(BasicBlockInContext entry, DataFact fact) throws InfeasiblePathException {
        Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

        if((fact instanceof LocalDataFact) == false)
            return res;

        Iterator<BasicBlockInContext> iPred = supergraph.getPredNodes(entry);

        while(iPred.hasNext()){
            BasicBlockInContext pred = iPred.next();
            if(pred.getLastInstruction() == null)
                throw new InfeasiblePathException("Call node must have an instruction: " + pred);

            if(fact instanceof GlobalDataFact){
                res.add(Pair.make(pred, fact));
            }else if(fact instanceof LocalDataFact){
                SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) pred.getLastInstruction();
                int index = getArgumentIndex((LocalDataFact)fact);
                LocalDataFact callerFact = makeNewLocalDataFact(pred.getNode(), invokeInst.getUse(index), fact.getField());

            }
        }

        return res;
    }

    private int getArgumentIndex(LocalDataFact fact){
        return fact.getVar() -1;
    }

    private LocalDataFact makeNewLocalDataFact(CGNode node, int numV, Field f){
        return new LocalDataFact(node, numV, f);
    }

    protected DataFact getCalleeDataFact(CGNode callee, SSAAbstractInvokeInstruction invokeInst, DataFact fact){
        // TODO: need to calculate a callee data fact regarding to the caller data fact used in the invoke instruction
        if(fact instanceof GlobalDataFact){
            return fact;
        }else if(fact instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) fact;
            int v = ldf.getVar();
            int i=0;
            for(; i<invokeInst.getNumberOfUses(); i++){
                if(invokeInst.getUse(i) == v)
                    break;
            }

            return new LocalDataFact(callee, i+1, fact.getField());
        }
        return fact;
    }

    private Set<BasicBlockInContext> getNextClosestUseBlocks(BasicBlockInContext bb, DefUse du, int v) throws InfeasiblePathException {
        Set<BasicBlockInContext> res = new HashSet<>();
        Iterator<SSAInstruction> iUse = du.getUses(v);

        Set<Integer> useIndexSet = new HashSet<>();

        while(iUse.hasNext()){
            SSAInstruction useInst = iUse.next();

            if(useInst.iindex <= bb.getLastInstructionIndex())
                continue;

            useIndexSet.add(useInst.iindex);
        }

        Queue<BasicBlockInContext> bfsQueue = new LinkedBlockingQueue<>();
        bfsQueue.add(bb);

        while(!bfsQueue.isEmpty()){
            BasicBlockInContext succ = bfsQueue.poll();
            for(BasicBlockInContext succOfSucc : getNormalSuccessors(succ)){
                if(succOfSucc.isExitBlock())
                    res.add(succOfSucc);
                else if(useIndexSet.contains(succOfSucc.getLastInstructionIndex())){
                    res.add(succOfSucc);
                }else
                    bfsQueue.add(succOfSucc);
            }
        }

        return res;
    }

    protected Set<BasicBlockInContext> getSparseNormalSuccessors(BasicBlockInContext bb, DataFact df) throws InfeasiblePathException {
        if(df instanceof GlobalDataFact){
            return getNormalSuccessors(bb);
        }else if(df instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) df;
            CGNode n = ldf.getNode();
            DefUse du = n.getDU();
            return getNextClosestUseBlocks(bb, du, ldf.getVar());
        }else
            throw new InfeasiblePathException("A data fact used to find sparse successors must be either GlobalDataFact or LocalDataFact: " + df.getClass().getName());
    }

    protected Set<BasicBlockInContext> getNormalSuccessors(BasicBlockInContext bb) throws InfeasiblePathException {
        Set<BasicBlockInContext> res = new HashSet<>();
        // this method only handles normal nodes. So reject call and exit nodes.
        if(bb.isExitBlock())
            return res;

        boolean isRet = false;
        boolean isSwitch = false;
        int[] possibleLabels = null;
        boolean isIf = false;
        Boolean ifTrueCase = null;
        if(bb.getLastInstruction() != null) {
            if(bb.getLastInstruction() instanceof SSAReturnInstruction)
                isRet = true;
            else if(bb.getLastInstruction() instanceof SSASwitchInstruction) {
                isSwitch = true;
                SSASwitchInstruction switchInst = (SSASwitchInstruction) bb.getLastInstruction();
                int[] caseLabels = switchInst.getCasesAndLabels();
                int condV = switchInst.getUse(0);
                PointerKey condPK = pa.getHeapModel().getPointerKeyForLocal(bb.getNode(), condV);
                OrdinalSet<InstanceKey> ikSet = pa.getPointsToSet(condPK);
                possibleLabels = new int[ikSet.size()];
                int index = 0;
                for(InstanceKey ik : ikSet){
                    // we only handle switch statements when all possible condition values are constant.
                    if(ik instanceof ConstantKey){
                        isSwitch = true;
                        int caseValue = (Integer)((ConstantKey) ik).getValue();
                        int beforeCasesSize = index;
                        for(int i=0; i<caseLabels.length-1; i+=2){
                            if(caseValue == caseLabels[i]) {
                                possibleLabels[index++] = caseLabels[i+1];
                            }
                        }
                        if(beforeCasesSize == index)
                            possibleLabels[index++] = switchInst.getDefault();
                    }else{
                        isSwitch = false;
                        break;
                    }
                }
            }else if(bb.getLastInstruction() instanceof SSAConditionalBranchInstruction) {
                SSAConditionalBranchInstruction ifInst = (SSAConditionalBranchInstruction) bb.getLastInstruction();
                if(ifInst.isIntegerComparison()){
                    int condV1 = ifInst.getUse(0);
                    int condV2 = ifInst.getUse(1);

                    PointerKey condPK1 = pa.getHeapModel().getPointerKeyForLocal(bb.getNode(), condV1);
                    PointerKey condPK2 = pa.getHeapModel().getPointerKeyForLocal(bb.getNode(), condV2);

                    for(InstanceKey condIK1 : pa.getPointsToSet(condPK1)){
                        boolean partialRes = true;
                        for(InstanceKey condIK2 : pa.getPointsToSet(condPK2)){
                            if(condIK1 instanceof ConstantKey && condIK2 instanceof ConstantKey){
                                int condValue1 = (Integer)((ConstantKey) condIK1).getValue();
                                int condValue2 = (Integer)((ConstantKey) condIK2).getValue();

                                boolean localCond = false;
                                IConditionalBranchInstruction.Operator op = (IConditionalBranchInstruction.Operator)ifInst.getOperator();
                                switch(op){
                                    case EQ:
                                        if(condValue1 == condValue2)
                                            localCond = true;
                                        else
                                            localCond = false;
                                        break;
                                    case NE:
                                        if(condValue1 != condValue2)
                                            localCond = true;
                                        else
                                            localCond = false;
                                        break;
                                    case LT:
                                        if(condValue1 < condValue2)
                                            localCond = true;
                                        else
                                            localCond = false;
                                        break;
                                    case GE:
                                        if(condValue1 >= condValue2)
                                            localCond = true;
                                        else
                                            localCond = false;
                                        break;
                                    case GT:
                                        if(condValue1 > condValue2)
                                            localCond = true;
                                        else
                                            localCond = false;
                                        break;
                                    case LE:
                                        if(condValue1 <= condValue2)
                                            localCond = true;
                                        else
                                            localCond = false;
                                        break;
                                    default:
                                        throw new InfeasiblePathException("The operator must belong to 6 types: " + ifInst.getOperator());
                                }
                                if(ifTrueCase == null)
                                    ifTrueCase = localCond;
                                else if(ifTrueCase != localCond) {
                                    isIf = false;
                                    partialRes = false;
                                    break;
                                }
                            }else{
                                isIf = false;
                                partialRes = false;
                                break;
                            }
                        }
                        if(partialRes == false)
                            break;
                    }
                }
            }
        }
        Iterator<BasicBlockInContext> iSucc = supergraph.getSuccNodes(bb);

        while(iSucc.hasNext()){
            BasicBlockInContext succ = iSucc.next();

            // exclude exception paths
            if(!isRet && succ.isExitBlock())
                continue;
            if(succ.isEntryBlock())
                continue;
            else if(isSwitch){ // when we can slice infeasible cases
                int label = succ.getLastInstructionIndex();
                boolean feasibleCheck = false;
                for(int i=0; i<possibleLabels.length; i++) {
                    if(possibleLabels[i] == label)
                        feasibleCheck = true;
                }
                if(!feasibleCheck)
                    continue;
            }else if(isIf){ // when we can slice an infeasible condition

            }

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

    class SwitchFilter {
        public boolean accept(BasicBlockInContext bb, BasicBlockInContext succ, Set<InstanceKey> ikSet){
            return true;
        }
    }

    class IfFilter{

    }
}
