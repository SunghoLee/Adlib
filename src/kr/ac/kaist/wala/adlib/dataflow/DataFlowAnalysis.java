package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.*;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.model.ARModeling;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by leesh on 14/09/2017.
 */
public class DataFlowAnalysis {
    private ICFGSupergraph supergraph;
    private DataFlowHeapModel heapModel;

    public DataFlowAnalysis(ICFGSupergraph supergraph){
        this.supergraph = supergraph;
        heapModel = new DataFlowHeapModel();
    }

    private boolean isPrimitive(BasicBlockInContext<IExplodedBasicBlock> n) {
        if (n.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
            return !ARModeling.isModelingMethod(supergraph.getClassHierarchy(), n.getNode().getMethod());

        return false;
    }

    public boolean analyze(Set<DataWithWork> seeds, IDataFlowSemanticFunction semFun, IDataFlowFilter filter){
        CGNode seedNode = ((LocalDataPointer)seeds.iterator().next().getData()).getNode();
        WorkList worklist = new WorkList();

        for(DataWithWork seed : seeds) {
            NodeWithCS seedBlock = makeNodeWithCS(getEntryForProcedure(seedNode), new CallStack());
            heapModel.weekUpdate(seedBlock, seed);
            worklist.addWork(seedBlock);
        }

        while(!worklist.isEmpty()){
            NodeWithCS nextWork = worklist.getNextWork();
            Set<DataWithWork> nextDataSet = heapModel.getData(nextWork);

            Set<DataWithWork> res = new HashSet<>();
            for(DataWithWork dww : nextDataSet){
                res.addAll(applySemanticFunction(nextWork, dww, nextWork.bb.getLastInstruction(), semFun));
            }

//            boolean debug = false;
//            if(nextWork.getBlock().getLastInstruction() != null && nextWork.getBlock().getLastInstruction().toString().contains("invokevirtual < Application, Landroid/os/Message, sendToTarget()V > 15 @41 exception:16"))
//                debug = true;

            for(NodeWithCS nwcs : getNextBlocks(nextWork)){
//                if(debug)
//                    System.out.println("NEXT : " + nwcs.getBlock().getLastInstruction());
                boolean changed = false;

                for(DataWithWork dww : res) {
//                    if(filter.apply(nwcs.getBlock(), dww.getData()))
                        changed |= heapModel.weekUpdate(nwcs, dww);
                }

                if(changed)
                    worklist.addWork(nwcs);
            }
        }

        return false;
    }

    private Set<DataWithWork> applySemanticFunction(NodeWithCS block, DataWithWork data, SSAInstruction inst, IDataFlowSemanticFunction semFun){
        if(inst == null)
            return Collections.singleton(data);

//        System.out.println("N: " + block.getBlock().getNode() + "\n\tI: " + inst + "\n\tD: " + data.getData());
//        if(block.getBlock().getNode().toString().contains("Node: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/MHandler, getLocation()V >") && !data.getData().toString().contains("-3")){
//            System.out.println("I: " + inst + " !!!!! " + data.getData());
//        }

        if(inst instanceof SSAGotoInstruction){
            return semFun.visitGoto(block, (SSAGotoInstruction)inst, data);
        }else if(inst instanceof SSAArrayLoadInstruction){
            return semFun.visitArrayLoad(block, (SSAArrayLoadInstruction)inst, data);
        }else if(inst instanceof SSAArrayStoreInstruction){
            return semFun.visitArrayStore(block, (SSAArrayStoreInstruction)inst, data);
        }else if(inst instanceof SSABinaryOpInstruction){
            return semFun.visitBinaryOp(block, (SSABinaryOpInstruction)inst, data);
        }else if(inst instanceof SSAUnaryOpInstruction){
            return semFun.visitUnaryOp(block, (SSAUnaryOpInstruction)inst, data);
        }else if(inst instanceof SSAConversionInstruction){
            return semFun.visitConversion(block, (SSAConversionInstruction)inst, data);
        }else if(inst instanceof SSAComparisonInstruction){
            return semFun.visitComparison(block, (SSAComparisonInstruction)inst, data);
        }else if(inst instanceof SSAConditionalBranchInstruction){
            return semFun.visitConditionalBranch(block, (SSAConditionalBranchInstruction)inst, data);
        }else if(inst instanceof SSASwitchInstruction){
            return semFun.visitSwitch(block, (SSASwitchInstruction)inst, data);
        }else if(inst instanceof SSAReturnInstruction){
            return semFun.visitReturn(block, (SSAReturnInstruction)inst, data);
        }else if(inst instanceof SSAGetInstruction){
            return semFun.visitGet(block, (SSAGetInstruction)inst, data);
        }else if(inst instanceof SSAPutInstruction){
            return semFun.visitPut(block, (SSAPutInstruction)inst, data);
        }else if(inst instanceof SSAInvokeInstruction){
            return semFun.visitInvoke(block, (SSAInvokeInstruction)inst, data);
        }else if(inst instanceof SSANewInstruction){
            return semFun.visitNew(block, (SSANewInstruction)inst, data);
        }else if(inst instanceof SSAArrayLengthInstruction){
            return semFun.visitArrayLength(block, (SSAArrayLengthInstruction)inst, data);
        }else if(inst instanceof SSAThrowInstruction){
            return semFun.visitThrow(block, (SSAThrowInstruction)inst, data);
        }else if(inst instanceof SSAMonitorInstruction){
            return semFun.visitMonitor(block, (SSAMonitorInstruction)inst, data);
        }else if(inst instanceof SSACheckCastInstruction){
            return semFun.visitCheckCast(block, (SSACheckCastInstruction)inst, data);
        }else if(inst instanceof SSAInstanceofInstruction){
            return semFun.visitInstanceof(block, (SSAInstanceofInstruction)inst, data);
        }else if(inst instanceof SSAPhiInstruction){
            return semFun.visitPhi(block, (SSAPhiInstruction)inst, data);
        }else if(inst instanceof SSAPiInstruction){
            return semFun.visitPi(block, (SSAPiInstruction)inst, data);
        }else if(inst instanceof SSAGetCaughtExceptionInstruction){
            return semFun.visitGetCaughtException(block, (SSAGetCaughtExceptionInstruction)inst, data);
        }else if(inst instanceof SSALoadMetadataInstruction){
            return semFun.visitLoadMetadata(block, (SSALoadMetadataInstruction)inst, data);
        }else{
            Assertions.UNREACHABLE("Impossible: " + inst.getClass().getName());
        }
        return null;
    }

    private Set<DataWithWork> getDataForPoint(NodeWithCS n){
        return heapModel.getData(n);
    }

    private NodeWithCS makeNodeWithCS(BasicBlockInContext bb, CallStack cs){
        return new NodeWithCS(bb, cs);
    }

    private BasicBlockInContext getEntryForProcedure(CGNode n){
        return supergraph.getEntriesForProcedure(n)[0];
    }

    private Set<NodeWithCS> getNextBlocks(NodeWithCS node){
        BasicBlockInContext bb = node.getBlock();
        Set<NodeWithCS> res = new HashSet<>();

        if(supergraph.isCall(bb)){
            boolean handled = false;

            for(BasicBlockInContext entry : getNextCalleeBlocks(bb)){
                if(!isPrimitive(entry)) {
                    handled = true;
                    res.add(new NodeWithCS(entry, node.getCallStack().clone().push(new CallSite(node.bb.getNode(), supergraph.getLocalBlockNumber(bb)))));
                }
            }

            //if there is no callee
            if(!handled){
                for(BasicBlockInContext next : getNextNormalBlocks(bb)){
                    res.add(new NodeWithCS(next, node.getCallStack().clone()));
                }
            }
        }else if(supergraph.isExit(bb)){
            CallSite cs = node.cs.pop();
            if(cs != null) {
                for(BasicBlockInContext next : getNextNormalBlocks(cs.getCallBlock())){
                    res.add(new NodeWithCS(next, node.getCallStack().clone()));
                }
            }else
                return Collections.emptySet();
        }else {
            for(BasicBlockInContext next : getNextNormalBlocks(bb)){
                res.add(new NodeWithCS(next, node.getCallStack().clone()));
            }
        }

        return res;
    }

    private Set<BasicBlockInContext> getNextCalleeBlocks(BasicBlockInContext bb){
        Set<BasicBlockInContext> res = new HashSet<>();

        if(supergraph.isCall(bb)){
            Iterator<BasicBlockInContext> iSucc = supergraph.getSuccNodes(bb);
            while(iSucc.hasNext()){
                BasicBlockInContext succ = iSucc.next();
                if(succ.isEntryBlock())
                    res.add(succ);
            }
        }
        return res;
    }

    private Set<BasicBlockInContext> getNextNormalBlocks(BasicBlockInContext bb){
        Set<BasicBlockInContext> res = new HashSet<>();

        boolean isRet = (bb.getLastInstruction() != null && bb.getLastInstruction() instanceof SSAReturnInstruction)? true : false;
        boolean isCall = (bb.getLastInstruction() != null && bb.getLastInstruction() instanceof SSAAbstractInvokeInstruction)? true : false;

        Iterator<BasicBlockInContext> iSucc = supergraph.getSuccNodes(bb);
        while(iSucc.hasNext()){
            BasicBlockInContext succ = iSucc.next();

            if(isRet && succ.isExitBlock())
                res.add(succ);
            else if(isCall && !succ.isEntryBlock())
                res.add(succ);
            else if(!succ.isEntryBlock() && !succ.isExitBlock())
                res.add(succ);
        }

        return res;
    }


    class CallSite {
        private CGNode n;
        private int nodeNum;

        public CallSite(CGNode n, int nodeNum){
            this.n = n;
            this.nodeNum = nodeNum;
        }

        public CGNode getNode(){
            return n;
        }

        public int getNodeNum(){
            return nodeNum;
        }

        public BasicBlockInContext getCallBlock(){
            return supergraph.getLocalBlock(n, nodeNum);
        }

        @Override
        public int hashCode(){
            return n.hashCode() + nodeNum;
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof CallSite) {
                CallSite cs = (CallSite) o;
                if(cs.n.equals(n) && cs.nodeNum == nodeNum)
                    return true;
            }
            return false;
        }
    }

    class CallStack {
        private Stack<CallSite> callSites = new Stack<>();

        public CallSite peek(){
            if(callSites.size() != 0)
                return callSites.peek();
            return null;
        }

        public CallSite pop(){
            if(callSites.size() != 0)
                return callSites.pop();
            return null;
        }

        public CallStack push(CallSite cs){
            callSites.add(cs);
            return this;
        }

        private CallStack addAll(Stack<CallSite> cs){
            callSites.addAll(cs);
            return this;
        }

        public CallStack clone(){
            return (new CallStack()).addAll(callSites);
        }

        @Override
        public int hashCode(){
            return callSites.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof CallStack){
                CallStack cs = (CallStack) o;
                if(cs.callSites.size() == callSites.size()){
                    for(int i = 0; i < callSites.size(); i++){
                        if(!cs.callSites.get(i).equals(callSites.get(i)))
                            return false;
                    }
                    return true;
                }
            }
            return false;
        }
    }

    public static class NodeWithCS{
        private BasicBlockInContext bb;
        private CallStack cs;

        public NodeWithCS(BasicBlockInContext bb, CallStack cs){
            this.bb = bb;
            this.cs = cs;
        }

        public boolean isEntry(){
            return bb.isEntryBlock();
        }

        public boolean isExit(){
            return bb.isExitBlock();
        }

        public BasicBlockInContext getBlock(){
            return this.bb;
        }

        public CallStack getCallStack(){
            return this.cs;
        }

        @Override
        public int hashCode(){
            return bb.hashCode() + cs.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof NodeWithCS){
                NodeWithCS nwcs = (NodeWithCS) o;
                if(nwcs.bb.getNumber() == bb.getNumber() && nwcs.cs.equals(cs))
                    return true;
            }
            return false;
        }
    }

    public static class DataWithWork {
        private IDataPointer p;
        private Work w;

        public DataWithWork(IDataPointer p, Work w){
            this.p = p;
            this.w = w;
        }

        public IDataPointer getData(){
            return this.p;
        }

        public Work getWork(){
            return this.w;
        }

        @Override
        public int hashCode(){
            return p.hashCode() + w.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof DataWithWork){
                DataWithWork dww = (DataWithWork) o;

                if(dww.p.equals(p) && dww.w.equals(w))
                    return true;
            }
            return false;
        }
    }

    class DataFlowHeapModel {
        private HashMap<NodeWithCS, Set<DataWithWork>> pointDataMap;

        public DataFlowHeapModel(){
            pointDataMap = new HashMap<>();
        }

        public boolean weekUpdate(NodeWithCS point, DataWithWork data){
            if(!pointDataMap.containsKey(point)){
                pointDataMap.put(point, new HashSet<>());
            }

            return pointDataMap.get(point).add(data);
        }

        public Set<DataWithWork> getData(NodeWithCS point){
            if(!pointDataMap.containsKey(point))
                pointDataMap.put(point, new HashSet<>());

            return pointDataMap.get(point);
        }
    }

    class WorkList {
        private Queue<NodeWithCS> works = new LinkedBlockingQueue<>();

        public NodeWithCS getNextWork(){
            return works.poll();
        }

        public void addWork(NodeWithCS nwcs){
            works.add(nwcs);
        }

        public boolean isEmpty(){
            return works.isEmpty();
        }
    }
}

abstract class AbstractWork implements Work{
    private Work superWork;

    protected AbstractWork(Work w){
        this.superWork = w;
    }

    @Override
    public Work nextWork() {
        return superWork;
    }
}

