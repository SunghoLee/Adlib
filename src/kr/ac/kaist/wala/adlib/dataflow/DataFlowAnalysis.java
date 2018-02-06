package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.*;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.context.CallStackContext;
import kr.ac.kaist.wala.adlib.dataflow.context.Context;
import kr.ac.kaist.wala.adlib.dataflow.context.EverywhereContext;
import kr.ac.kaist.wala.adlib.dataflow.flows.IDataFlowSemanticFunction;
import kr.ac.kaist.wala.adlib.dataflow.pointer.IDataPointer;
import kr.ac.kaist.wala.adlib.dataflow.pointer.LocalDataPointer;
import kr.ac.kaist.wala.adlib.dataflow.works.Work;
import kr.ac.kaist.wala.adlib.model.ARModeling;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by leesh on 14/09/2017.
 */
public class DataFlowAnalysis {
    private ICFGSupergraph supergraph;
    private DataFlowHeapModel heapModel;
    private static boolean DEBUG = true;
    private TypeName rootTN = TypeName.findOrCreate("Lcom/ibm/wala/FakeRootClass");
    private Selector fakerootSelector = Selector.make("fakeRootMethod()V");

    public enum CONTEXT{
        FLOW_INSENSITIVE,
        CALLSTACK,
        CALLSTRING,
    }

    private CONTEXT ctxt;

    public DataFlowAnalysis(ICFGSupergraph supergraph, CONTEXT ctxt){
        this.supergraph = supergraph;
        this.ctxt = ctxt;
        heapModel = new DataFlowHeapModel();
    }

    private boolean isPrimitive(BasicBlockInContext<IExplodedBasicBlock> n) {
        if (n.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
            return !ARModeling.isModelingMethod(supergraph.getClassHierarchy(), n.getNode().getMethod());

        return false;
    }

    public boolean analyze(Set<DataWithWork> seeds, IDataFlowSemanticFunction semFun){
        CGNode seedNode = ((LocalDataPointer)seeds.iterator().next().getData()).getNode();
        WorkList worklist = new WorkList();
        heapModel.clear();

        for(DataWithWork seed : seeds) {
            Node seedBlock = null;

            switch(ctxt) {
                case FLOW_INSENSITIVE:
                    seedBlock = new Node(getEntryForProcedure(seedNode), EverywhereContext.getInstance());
                    break;
                case CALLSTACK:
                    seedBlock = new Node(getEntryForProcedure(seedNode), new CallStackContext(supergraph));
                    break;
                case CALLSTRING:
//                    seedBlock = makeNodeWithCS(getEntryForProcedure(seedNode), new CallStack());
                    break;
            }
            heapModel.weekUpdate(seedBlock, seed);
            worklist.addWork(seedBlock);
        }

        while(!worklist.isEmpty()){
            Node nextWork = worklist.getNextWork();
            Set<DataWithWork> nextDataSet = heapModel.getData(nextWork);

            Set<DataWithWork> res = new HashSet<>();
            for(DataWithWork dww : nextDataSet){
                res.addAll(applySemanticFunction(nextWork, dww, nextWork.getInstruction(), semFun));
            }

            for(Node nwcs : getNextBlocks(nextWork)){
                IMethod m = nwcs.getBB().getMethod();

                if(m.getDeclaringClass().getName().equals(rootTN) && m.getSelector().equals(fakerootSelector))
                    continue;

                boolean changed = false;

                for(DataWithWork dww : res) {
                     boolean localChanged = heapModel.weekUpdate(nwcs, dww);
                    if(localChanged) {
//                        System.out.println("\t\tnextblock: " + nwcs.getBlock() + "\t" + nwcs.getBlock().getLastInstruction());
                        IDataPointer dp = dww.getData();
                        if(dp instanceof LocalDataPointer) {
                            LocalDataPointer ldp = (LocalDataPointer) dp;
                            if(ldp.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application))
                                System.out.println("\t\t\tUpdated: " + dww.getData() + "\n\t\t\t\tWork: " + dww.getWork());
                        }else
                            System.out.println("\t\t\tUpdated: " + dww.getData());}

                    changed |= localChanged;
                }

                if(changed)
                    worklist.addWork(nwcs);
            }
        }

        return false;
    }

    int i = 0;
    private Set<DataWithWork> applySemanticFunction(Node block, DataWithWork data, SSAInstruction inst, IDataFlowSemanticFunction semFun){
        if(DEBUG)
            System.out.println("\tSKIP");

        if(inst == null)
            return Collections.singleton(data);

        // todo: optimize data flows
        if(data.getData() instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) data.getData();
            if(!ldp.getNode().equals(block.getBB().getNode()))
                return Collections.emptySet();
//                return Collections.singleton(data);
        }

        if(DEBUG) {
            System.out.println("#B: " + block);
            System.out.println("\t#I: " + inst);
            System.out.println("\t#D: " + data + "\n");
//            try {
//                System.in.read();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
        }

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

    private Set<DataWithWork> getDataForPoint(Node n){
        return heapModel.getData(n);
    }

    private BasicBlockInContext getEntryForProcedure(CGNode n){
        return supergraph.getEntriesForProcedure(n)[0];
    }

    private Set<Node> getNextBlocks(Node node){
        BasicBlockInContext bb = node.getBB();
        Set<Node> res = new HashSet<>();
        Context ctxt = node.getContext();

        if(supergraph.isCall(bb)){
            Stream<BasicBlockInContext> entryStream = getNextCalleeBlocks(bb).stream().filter((succ) -> (!isPrimitive(succ))? true  :false );
            res.addAll(ctxt.getNext(node, entryStream.collect(Collectors.toSet()), Context.TYPE.CALL));

            //if there is no callee
//            if(res.isEmpty()){
                res.addAll(ctxt.getNext(node, getNextNormalBlocks(bb), Context.TYPE.NORMAL));
//            }
        }else if(supergraph.isExit(bb)){
            res.addAll(ctxt.getNext(node, getNextReturnBlocks(bb), Context.TYPE.EXIT));
        }else {
            res.addAll(ctxt.getNext(node, getNextNormalBlocks(bb), Context.TYPE.NORMAL));
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

    private Set<BasicBlockInContext> getNextReturnBlocks(BasicBlockInContext bb){
        if(!bb.isExitBlock())
            return Collections.emptySet();

        Set<BasicBlockInContext> res = new HashSet<>();

        Iterator<BasicBlockInContext> iSucc = supergraph.getSuccNodes(bb);
        while(iSucc.hasNext()){
            res.add(iSucc.next());
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

        @Override
        public String toString(){
            return p.toString();
        }
    }

    class DataFlowHeapModel {
        private HashMap<Node, Set<DataWithWork>> pointDataMap;

        public DataFlowHeapModel(){
            pointDataMap = new HashMap<>();
        }

        public boolean weekUpdate(Node point, DataWithWork data){
            if(!pointDataMap.containsKey(point)){
                pointDataMap.put(point, new HashSet<>());
            }

            return pointDataMap.get(point).add(data);
        }

        public Set<DataWithWork> getData(Node point){
            if(!pointDataMap.containsKey(point))
                pointDataMap.put(point, new HashSet<>());

            return pointDataMap.get(point);
        }

        public void clear(){
            pointDataMap.clear();
        }
    }


    class WorkList {
        private Queue<Node> works = new LinkedBlockingQueue<>();

        public Node getNextWork(){
            return works.poll();
        }

        public void addWork(Node nwcs){
            works.add(nwcs);
        }

        public boolean isEmpty(){
            return works.isEmpty();
        }
    }

    class ScopeFilter implements Predicate<DataWithWork>{
        private IMethod m;

        public ScopeFilter(IMethod m){
            this.m = m;
        }

        @Override
        public Predicate<DataWithWork> and(Predicate<? super DataWithWork> other) {
            return dww -> test(dww) && other.test(dww);
        }

        @Override
        public Predicate<DataWithWork> negate() {
            return dww -> !test(dww);
        }

        @Override
        public Predicate<DataWithWork> or(Predicate<? super DataWithWork> other) {
            return dww -> test(dww) || other.test(dww);
        }

        @Override
        public boolean test(DataWithWork dataWithWork) {
            IDataPointer dp = dataWithWork.getData();

            // if a data fact is a local variable
            if(dp instanceof LocalDataPointer){
                LocalDataPointer ldp = (LocalDataPointer) dp;
                IMethod m = ldp.getNode().getMethod();
                if(this.m.equals(m))
                    return true;
                else
                    return false;
            }

            // if a data fact is a static field
            return true;
        }
    }
}
