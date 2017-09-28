package kr.ac.kaist.wala.adlib.callgraph;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.ipa.callgraph.propagation.ConstantKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableSparseIntSet;
import com.ibm.wala.util.intset.OrdinalSet;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

/**
 * Created by leesh on 24/07/2017.
 */
public class BranchSlicerForConstant {
    private CallGraph cg;
    private PointerAnalysis<InstanceKey> pa;

    public BranchSlicerForConstant(CallGraph cg, PointerAnalysis<InstanceKey> pa){
        this.cg = cg;
        this.pa = pa;
    }

    public CallGraph prune(){
        Set<HotSpot> spots = findHotSpots();

        PrunedCallGraph pcg = new PrunedCallGraph(this.cg);

        for(HotSpot spot : spots){
            prune(pcg, spot);
            System.out.println("SPOT: " + spot);
        }

        return pcg;
//        return cg;
    }

    private CallGraph prune(PrunedCallGraph pcg, HotSpot spot){
        pcg.prune(spot);

        return pcg;
    }

    private Set<HotSpot> findHotSpots(){
        Set<HotSpot> spots = new HashSet<>();

        for(CGNode n : cg){
            IR ir = n.getIR();

            if(ir == null)
                continue;

            for(SSAInstruction inst : ir.getInstructions()){
                if(inst instanceof SSAConditionalBranchInstruction){
                    SSAConditionalBranchInstruction condInst = (SSAConditionalBranchInstruction) inst;
                    int var1 = condInst.getUse(0);
                    int var2 = condInst.getUse(1);

                    PointerKey pk1 = pa.getHeapModel().getPointerKeyForLocal(n, var1);
                    PointerKey pk2 = pa.getHeapModel().getPointerKeyForLocal(n, var2);

                    if(isConstantCondition(n, var1) && isConstantCondition(n, var2)){
                        OrdinalSet<InstanceKey> ik1Set = pa.getPointsToSet(pk1);
                        OrdinalSet<InstanceKey> ik2Set = pa.getPointsToSet(pk2);

                        Set<Integer> i1Set = new HashSet<>();
                        for(InstanceKey ik1 : ik1Set){
                            i1Set.add((Integer)((ConstantKey) ik1).getValue());
                        }

                        Set<Integer> i2Set = new HashSet<>();
                        for(InstanceKey ik2 : ik2Set){
                            i2Set.add((Integer)((ConstantKey) ik2).getValue());
                        }

                        spots.add(new IfHotSpot(n, condInst, i1Set, i2Set, (IConditionalBranchInstruction.Operator) condInst.getOperator()));
                    }
                }else if(inst instanceof SSASwitchInstruction){
                    SSASwitchInstruction switchInst = (SSASwitchInstruction) inst;
                    int var = switchInst.getUse(0);

                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, var);

                    if(isConstantCondition(n, var)){
                        OrdinalSet<InstanceKey> ikSet = pa.getPointsToSet(pk);

                        Set<Integer> iSet = new HashSet<>();
                        for(InstanceKey ik : ikSet){
                            iSet.add((Integer)((ConstantKey) ik).getValue());
                        }

                        spots.add(new SwitchHotSpot(n, switchInst, iSet));
                    }
                }
            }
        }


        return spots;
    }

    private boolean isConstantCondition(CGNode n, int conditionValue){
        PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, conditionValue);

        OrdinalSet<InstanceKey> ikSet = pa.getPointsToSet(pk);

        if(ikSet.size() == 0)
            return false;

        for(InstanceKey ik : ikSet){
            if(!(ik instanceof ConstantKey) || !((ConstantKey)ik).getConcreteType().getReference().equals(TypeReference.JavaLangInteger))
                return false;
        }

        return true;
    }

    private boolean isConditionalBranch(SSAInstruction inst){
        if(inst instanceof SSAConditionalBranchInstruction)
            return true;
        return false;
    }


    interface HotSpot{
        static int INST_SWITCH = 1;
        static int INST_IF = 2;

        public CGNode getNode();

        public int getIndex();

        public SSAInstruction getInstruction();

        public int getType();
    }


    static abstract class AbstractHotSpot implements HotSpot {
        private CGNode n;
        private int iindex;
        private int type;

        public AbstractHotSpot(CGNode n, int iindex, int type){
            this.n = n;
            this.iindex = iindex;
            this.type = type;
        }

        public AbstractHotSpot(CGNode n, SSAInstruction inst, int type){
            this.n = n;
            this.iindex = inst.iindex;
            this.type = type;
        }

        public CGNode getNode(){
            return this.n;
        }

        public int getIndex(){
            return this.iindex;
        }

        public SSAInstruction getInstruction(){
            return n.getIR().getInstructions()[iindex];
        }

        public int getType(){
            return this.type;
        }
    }


    static class SwitchHotSpot extends AbstractHotSpot {
        private Set<Integer> values = new HashSet<>();

        public SwitchHotSpot(CGNode n, int iindex, int value){
            super(n, iindex, HotSpot.INST_SWITCH);
            this.values.add(value);
        }

        public SwitchHotSpot(CGNode n, SSASwitchInstruction inst, int value){
            super(n, inst, HotSpot.INST_SWITCH);
            this.values.add(value);
        }

        public SwitchHotSpot(CGNode n, SSASwitchInstruction inst, Set<Integer> values){
            super(n, inst, HotSpot.INST_SWITCH);
            this.values.addAll(values);
        }

        public Set<Integer> getConditionValue(){
            return this.values;
        }

        @Override
        public String toString(){
            return "[SWITCH] V: " + this.values + " of " + getInstruction() + " in " + getNode();
        }
    }


    static class IfHotSpot extends AbstractHotSpot {
        public static int TRUE = 0X001;
        public static int FALSE = 0X010;
        public static int TOP = 0X100;

        private Set<Integer> value1 = new HashSet<>();
        private Set<Integer> value2 = new HashSet<>();
        private IConditionalBranchInstruction.Operator op;

        public IfHotSpot(CGNode n, int iindex, int value1, int value2, IConditionalBranchInstruction.Operator op){
            super(n, iindex, HotSpot.INST_IF);
            this.value1.add(value1);
            this.value2.add(value2);
            this.op = op;
        }

        public IfHotSpot(CGNode n, SSAConditionalBranchInstruction inst, int value1, int value2, IConditionalBranchInstruction.Operator op){
            super(n, inst, HotSpot.INST_IF);
            inst.getOperator();
            this.value1.add(value1);
            this.value2.add(value2);
            this.op = op;
        }

        public IfHotSpot(CGNode n, SSAConditionalBranchInstruction inst, Set<Integer> value1, Set<Integer> value2, IConditionalBranchInstruction.Operator op){
            super(n, inst, HotSpot.INST_IF);
            inst.getOperator();
            this.value1.addAll(value1);
            this.value2.addAll(value2);
            this.op = op;
        }

        private Set<Pair<Integer, Integer>> makeCartesianProduct(){
            Set<Pair<Integer, Integer>> res = new HashSet<>();

            for(Integer i : value1){
                for(Integer j : value2)
                    res.add(Pair.make(i, j));
            }

            return res;
        }

        public int getBooleanCondition(){
            Set<Pair<Integer, Integer>> pairs = makeCartesianProduct();

            int res = 0x000;

            switch(op){
                case EQ:
                    for(Pair<Integer, Integer> p : pairs)
                        if(p.fst == p.snd)
                            res |= TRUE;
                        else
                            res |= FALSE;
                    break;
                case NE:
                    for(Pair<Integer, Integer> p : pairs)
                        if(p.fst != p.snd)
                            res |= TRUE;
                        else
                            res |= FALSE;
                    break;
                case LT:
                    for(Pair<Integer, Integer> p : pairs)
                        if(p.fst < p.snd)
                            res |= TRUE;
                        else
                            res |= FALSE;
                    break;
                case GE:
                    for(Pair<Integer, Integer> p : pairs)
                        if(p.fst >= p.snd)
                            res |= TRUE;
                        else
                            res |= FALSE;
                    break;
                case GT:
                    for(Pair<Integer, Integer> p : pairs)
                        if(p.fst > p.snd)
                            res |= TRUE;
                        else
                            res |= FALSE;
                    break;
                case LE:
                    for(Pair<Integer, Integer> p : pairs)
                        if(p.fst <= p.snd)
                            res |= TRUE;
                        else
                            res |= FALSE;
                    break;
                default:
                    Assertions.UNREACHABLE("Incompatible operation! " + op);
            }
            return res;
        }

        @Override
        public String toString(){
            return "[IF] V1: " + this.value1 + " Op: " + this.op + " V2: " + this.value2 + " of " + getInstruction() + " in " + getNode();
        }
    }


    class PrunedCallGraph implements CallGraph {
        private CallGraph delegate;
        private Map<CGNode, Set<CGNode>> outgoingRemovedEdge = new HashMap<>();
        private Map<CGNode, Set<CGNode>> incomingRemovedEdge = new HashMap<>();
        private Map<Pair<CGNode, CallSiteReference>, Set<CGNode>> removedTargets = new HashMap<>();
        private Map<Pair<CGNode, CGNode>, Set<CallSiteReference>> removedSites = new HashMap<>();

        private void prune(HotSpot spot){
            Set<SSAAbstractInvokeInstruction> invokeInsts = calcReachableCallInstructions(spot);
            pruneEdges(spot.getNode(), invokeInsts);
            pruneTargets(spot.getNode(), invokeInsts);
        }

        private Set<SSAAbstractInvokeInstruction> calcReachableCallInstructions(HotSpot spot){
            CGNode n = spot.getNode();
            IR ir = n.getIR();
            SSACFG cfg = ir.getControlFlowGraph();

            //breadth first search
            Queue<SSACFG.BasicBlock> queue = new LinkedBlockingQueue<>();
            Set<SSAAbstractInvokeInstruction> insts = new HashSet<>();
            Set<Integer> visited = new HashSet<>();

            queue.add(cfg.entry());
            visited.add(cfg.entry().getNumber());

            while(!queue.isEmpty()){
                SSACFG.BasicBlock bb = queue.poll();

                boolean dealed = false;

                for(SSAInstruction inst : bb.getAllInstructions()){
                    if(inst == null)
                        continue;

//                    if(n.toString().contains("Node: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice$JSIAdToDeviceInnerHandler, handleMessage(Landroid/os/Message;)V > Context: FirstMethodContextPair: [First: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice, close()V >] : Everywhere"))
//                        System.out.println("(" + inst.iindex + ") " + inst);

                    //deal with if statement
                    if(spot.getType() == HotSpot.INST_IF && spot.getIndex() == inst.iindex){
                        queue.addAll(calcReachableBlocksForIf(visited, cfg, bb, (IfHotSpot)spot));
                        dealed = true;
                    }else if(spot.getType() == HotSpot.INST_SWITCH && spot.getIndex() == inst.iindex){ // deal with switch statement
                        queue.addAll(calcReachableBlocksForSwitch(visited, cfg, bb, (SwitchHotSpot)spot));
                        dealed = true;
                    }else if(inst instanceof SSAAbstractInvokeInstruction){ // deal with invoke instruction
                        insts.add((SSAAbstractInvokeInstruction) inst);
                    }
                }

                // for normal blocks; not If and Switch
                if(!dealed){
                    Iterator<ISSABasicBlock> iSucc = cfg.getSuccNodes(bb);

                    while(iSucc.hasNext()){
                        SSACFG.BasicBlock succ = (SSACFG.BasicBlock)iSucc.next();
                        if(!visited.contains(succ.getNumber())) {
                            visited.add(succ.getNumber());
                            queue.add(succ);
                        }
                    }
                }
            }

            return insts;
        }

        private Set<SSACFG.BasicBlock> calcReachableBlocksForIf(Set<Integer> visited, SSACFG cfg, SSACFG.BasicBlock bb, IfHotSpot spot){
            int condition = spot.getBooleanCondition();
            Set<SSACFG.BasicBlock> res = new HashSet<>();

            Iterator<ISSABasicBlock> iSucc = cfg.getSuccNodes(bb);
            int trueBlockIndex = cfg.getBlockForInstruction(((SSAConditionalBranchInstruction)spot.getInstruction()).getTarget()).getNumber();

            while(iSucc.hasNext()){
                SSACFG.BasicBlock succ = (SSACFG.BasicBlock) iSucc.next();

                if(IfHotSpot.TRUE == condition && succ.getNumber() == trueBlockIndex){
                    if(!visited.contains(succ.getNumber())){
                        visited.add(succ.getNumber());
                        res.add(succ);
                    }
                }else if(IfHotSpot.FALSE == condition && succ.getNumber() != trueBlockIndex){
                    if(!visited.contains(succ.getNumber())){
                        visited.add(succ.getNumber());
                        res.add(succ);
                    }
                }else if(IfHotSpot.TOP == condition){
                    if(!visited.contains(succ.getNumber())){
                        visited.add(succ.getNumber());
                        res.add(succ);
                    }
                }
            }

            return res;
        }

        private Set<SSACFG.BasicBlock> calcReachableBlocksForSwitch(Set<Integer> visited, SSACFG cfg, SSACFG.BasicBlock bb, SwitchHotSpot spot){
            Set<Integer> conditions = spot.getConditionValue();
            Set<SSACFG.BasicBlock> res = new HashSet<>();

            Iterator<ISSABasicBlock> iSucc = cfg.getSuccNodes(bb);
            SSASwitchInstruction switchInst = (SSASwitchInstruction)spot.getInstruction();

            Set<Integer> targetIndexes = new HashSet<>();

            for(int condition : conditions) {
                int targetBlockIndex = cfg.getBlockForInstruction(switchInst.getTarget(condition)).getNumber();
                targetIndexes.add(targetBlockIndex);
            }

            while (iSucc.hasNext()) {
                SSACFG.BasicBlock succ = (SSACFG.BasicBlock) iSucc.next();

                if (targetIndexes.contains(succ.getNumber())) {
                    if (!visited.contains(succ.getNumber())) {
                        visited.add(succ.getNumber());
                        res.add(succ);
                    }
                }
            }

            return res;
        }

        private void pruneEdges(CGNode n, Set<SSAAbstractInvokeInstruction> possibleInvokes){
            Set<CGNode> possibleTargets = new HashSet<>();

            for(SSAAbstractInvokeInstruction invokeInst : possibleInvokes){
                CallSiteReference csr = invokeInst.getCallSite();

                possibleTargets.addAll(getPossibleTargets(n, csr));
            }

            Iterator<CGNode> iSucc = this.getSuccNodes(n);
            Set<CGNode> removes = new HashSet<>();

            while(iSucc.hasNext()){
                removes.add(iSucc.next());
            }

            removes.removeAll(possibleTargets);

            if(!outgoingRemovedEdge.containsKey(n))
                outgoingRemovedEdge.put(n, new HashSet<>());

            for(CGNode t : removes){
                outgoingRemovedEdge.get(n).add(t);
            }

            for(CGNode t : removes){
                if(!incomingRemovedEdge.containsKey(t))
                    incomingRemovedEdge.put(t, new HashSet<>());

                incomingRemovedEdge.get(t).add(n);
            }
        }

        private void pruneTargets(CGNode n, Set<SSAAbstractInvokeInstruction> possibleInvokes){
            Iterator<CallSiteReference> iCallSite = n.getIR().iterateCallSites();

            // initialize all possible target nodes and sites
            while(iCallSite.hasNext()){
                CallSiteReference csr = iCallSite.next();

                Pair<CGNode, CallSiteReference> ncPair = Pair.make(n, csr);

                if(!removedTargets.containsKey(ncPair))
                    removedTargets.put(ncPair, new HashSet<>());

                for(CGNode target : cg.getPossibleTargets(n, csr)){
                    Pair<CGNode, CGNode> ntPair = Pair.make(n, target);

                    if(!removedSites.containsKey(ntPair))
                        removedSites.put(ntPair, new HashSet<>());

                    removedTargets.get(ncPair).add(target);
                    removedSites.get(ntPair).add(csr);
                }
            }

            // remove possible target nodes and sites in pruned graph
            for(SSAAbstractInvokeInstruction invokeInst : possibleInvokes){
                CallSiteReference csr = invokeInst.getCallSite();
                Set<CGNode> targets = cg.getPossibleTargets(n, csr);

                Pair<CGNode, CallSiteReference> ncPair = Pair.make(n, csr);
                for(CGNode target : targets){
                    Pair<CGNode, CGNode> ntPair = Pair.make(n, target);

                    removedTargets.get(ncPair).remove(target);
                    removedSites.get(ntPair).remove(csr);
                }
            }
        }

        public PrunedCallGraph(CallGraph delegate){
            this.delegate = delegate;
        }

        @Override
        public void forEach(Consumer<? super CGNode> action) {
            delegate.forEach(action);
        }

        @Override
        public Spliterator<CGNode> spliterator() {
            return delegate.spliterator();
        }

        @Override
        public int getNumber(CGNode N) {
            return delegate.getNumber(N);
        }

        @Override
        public CGNode getNode(int number) {
            return delegate.getNode(number);
        }

        @Override
        public IntSet getSuccNodeNumbers(CGNode node) {
            IntSet is = delegate.getSuccNodeNumbers(node);
            Set<CGNode> succSet = outgoingRemovedEdge.get(node);

            MutableSparseIntSet newIs = MutableSparseIntSet.make(is);

            if(succSet != null) {
                for (CGNode succ : succSet)
                    newIs.remove(delegate.getNumber(succ));
            }

            return newIs;
        }

        @Override
        public int getMaxNumber() {
            return delegate.getMaxNumber();
        }

        @Override
        public IntSet getPredNodeNumbers(CGNode node) {
            IntSet is = delegate.getPredNodeNumbers(node);
            Set<CGNode> predSet = incomingRemovedEdge.get(node);

            MutableSparseIntSet newIs = MutableSparseIntSet.make(is);

            if(predSet != null) {
                for (CGNode pred : predSet)
                    newIs.remove(delegate.getNumber(pred));
            }

            return newIs;
        }

        @Override
        public Iterator<CGNode> iterateNodes(IntSet s) {
            return delegate.iterateNodes(s);
        }

        @Override
        public Iterator<CGNode> getPredNodes(CGNode n) {
            Iterator<CGNode> iPred = delegate.getPredNodes(n);
            Set<CGNode> predNodes = new HashSet<>();
            Set<CGNode> removedPredNodes = incomingRemovedEdge.get(n);

            if(removedPredNodes == null)
                removedPredNodes = new HashSet<>();

            for(; iPred.hasNext();){
                CGNode pred = iPred.next();
                if(!removedPredNodes.contains(pred))
                    predNodes.add(pred);
            }

            return predNodes.iterator();
        }

        @Override
        public Iterator<CGNode> iterator() {
            return delegate.iterator();
        }

        @Override
        public int getPredNodeCount(CGNode n) {
            return this.getPredNodeNumbers(n).size();
        }

        @Override
        public CGNode getFakeRootNode() {
            return delegate.getFakeRootNode();
        }

        @Override
        public int getNumberOfNodes() {
            return delegate.getNumberOfNodes();
        }

        @Override
        public CGNode getFakeWorldClinitNode() {
            return delegate.getFakeWorldClinitNode();
        }

        @Override
        public void removeNodeAndEdges(CGNode n) throws UnsupportedOperationException {
            delegate.removeNodeAndEdges(n);
        }

        @Override
        public void addNode(CGNode n) {
            delegate.addNode(n);
        }

        @Override
        public void removeNode(CGNode n) throws UnsupportedOperationException {
            delegate.removeNode(n);
        }

        @Override
        public Collection<CGNode> getEntrypointNodes() {
            return delegate.getEntrypointNodes();
        }

        @Override
        public Iterator<CGNode> getSuccNodes(CGNode n) {
            Iterator<CGNode> iSucc = delegate.getSuccNodes(n);
            Set<CGNode> succNodes = new HashSet<>();
            Set<CGNode> removedSuccNodes = outgoingRemovedEdge.get(n);

            if(removedSuccNodes == null)
                removedSuccNodes = new HashSet<>();

            for(; iSucc.hasNext();){
                CGNode succ = iSucc.next();
                if(!removedSuccNodes.contains(succ))
                    succNodes.add(succ);
            }

            return succNodes.iterator();
        }

        @Override
        public boolean containsNode(CGNode n) {
            return delegate.containsNode(n);
        }

        @Override
        public CGNode getNode(IMethod method, Context C) {
            return delegate.getNode(method, C);
        }

        @Override
        public int getSuccNodeCount(CGNode N) {
            return this.getSuccNodeNumbers(N).size();
        }

        @Override
        public void addEdge(CGNode src, CGNode dst) {
            delegate.addEdge(src, dst);
        }

        @Override
        public void removeEdge(CGNode src, CGNode dst) throws UnsupportedOperationException {
            delegate.removeEdge(src, dst);
        }

        @Override
        public Set<CGNode> getNodes(MethodReference m) {
            return delegate.getNodes(m);
        }

        @Override
        public void removeAllIncidentEdges(CGNode node) throws UnsupportedOperationException {
            delegate.removeAllIncidentEdges(node);
        }

        @Override
        public void removeIncomingEdges(CGNode node) throws UnsupportedOperationException {
            delegate.removeIncomingEdges(node);
        }

        @Override
        public IClassHierarchy getClassHierarchy() {
            return delegate.getClassHierarchy();
        }

        @Override
        public void removeOutgoingEdges(CGNode node) throws UnsupportedOperationException {
            delegate.removeOutgoingEdges(node);
        }

        @Override
        public boolean hasEdge(CGNode src, CGNode dst) {
            return delegate.hasEdge(src, dst);
        }

        @Override
        public Set<CGNode> getPossibleTargets(CGNode node, CallSiteReference site) {
            Set<CGNode> targets = new HashSet<>(delegate.getPossibleTargets(node, site));

            Pair newP = Pair.make(node, site);
            Set<CGNode> deleted = removedTargets.get(newP);

            if(deleted != null) {
                for (CGNode n : deleted) {
                    targets.remove(n);
                }
            }

            return targets;
        }

        @Override
        public int getNumberOfTargets(CGNode node, CallSiteReference site) {
            return this.getPossibleTargets(node, site).size();
        }

        @Override
        public Iterator<CallSiteReference> getPossibleSites(CGNode src, CGNode target) {
            Iterator<CallSiteReference> iSites = delegate.getPossibleSites(src, target);
            Set<CallSiteReference> sites = new HashSet<>();

            while(iSites.hasNext()){
                sites.add(iSites.next());
            }

            Pair newP = Pair.make(src, target);

            Set<CallSiteReference> deleted = removedSites.get(newP);

            if(deleted != null) {
                for (CallSiteReference csr : deleted) {
                    sites.remove(csr);
                }
            }

            return sites.iterator();
        }
    }

    class Block {
        private List<SSAInstruction> insts;

        public Block(List insts){
            this.insts = insts;
        }

        public List<SSAInstruction> getInsts(){
            return this.insts;
        }

        @Override
        public String toString(){
            String res = "";

            for(int i=0; i<insts.size(); i++){
                res += "(" + insts.get(i).iindex + ") " + insts.get(i);
                if(i != insts.size()-1)
                    res += "\n";
            }

            return res;
        }
    }
}
