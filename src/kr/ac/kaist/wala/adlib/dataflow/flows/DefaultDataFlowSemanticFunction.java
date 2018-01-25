package kr.ac.kaist.wala.adlib.dataflow.flows;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ExceptionReturnValueKey;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.DataFlowAnalysis;
import kr.ac.kaist.wala.adlib.dataflow.Node;
import kr.ac.kaist.wala.adlib.dataflow.flowmodel.BuiltinFlowHandler;
import kr.ac.kaist.wala.adlib.dataflow.flowmodel.MethodFlowModel;
import kr.ac.kaist.wala.adlib.dataflow.pointer.IDataPointer;
import kr.ac.kaist.wala.adlib.dataflow.pointer.LocalDataPointer;
import kr.ac.kaist.wala.adlib.dataflow.pointer.StaticFieldDataPointer;
import kr.ac.kaist.wala.adlib.dataflow.works.GetFieldWork;
import kr.ac.kaist.wala.adlib.dataflow.works.NoMoreWork;
import kr.ac.kaist.wala.adlib.dataflow.works.Work;
import kr.ac.kaist.wala.adlib.model.ARModeling;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Created by leesh on 25/09/2017.
 */
public class DefaultDataFlowSemanticFunction implements IDataFlowSemanticFunction {
    private final IClassHierarchy cha;
    private final PointerAnalysis<InstanceKey> pa;
    private final CallGraph cg;
    private final BuiltinFlowHandler builtinHandler = BuiltinFlowHandler.getInstance();

    public DefaultDataFlowSemanticFunction(CallGraph cg, IClassHierarchy cha, PointerAnalysis<InstanceKey> pa){
        System.out.println("#Dataflow start!");
        this.cha = cha;
        this.pa = pa;
        this.cg = cg;
    }

    private boolean isPrimitive(CGNode n) {
        if (n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
            return !ARModeling.isModelingMethod(cg.getClassHierarchy(), n.getMethod());

        return false;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitGoto(Node block, SSAGotoInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitArrayLoad(Node block, SSAArrayLoadInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBB().getNode()) && instruction.getArrayRef() == ldp.getVar()){
                //over-approximation for arrays
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitArrayStore(Node block, SSAArrayStoreInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBB().getNode()) && instruction.getUse(1) == ldp.getVar()){
                //over-approximation for arrays
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getArrayRef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitBinaryOp(Node block, SSABinaryOpInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBB().getNode()) && (instruction.getUse(0) == ldp.getVar() || instruction.getUse(1) == ldp.getVar())){
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitUnaryOp(Node block, SSAUnaryOpInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBB().getNode()) && instruction.getUse(0) == ldp.getVar()){
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitConversion(Node block, SSAConversionInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBB().getNode()) && instruction.getUse(0) == ldp.getVar()){
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitComparison(Node block, SSAComparisonInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBB().getNode()) && (instruction.getUse(0) == ldp.getVar() || instruction.getUse(1) == ldp.getVar())){
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitConditionalBranch(Node block, SSAConditionalBranchInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitSwitch(Node block, SSASwitchInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitReturn(Node block, SSAReturnInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBB().getNode()) && instruction.getUse(0) == ldp.getVar()){
                Iterator<CGNode> iPred = cg.getPredNodes(block.getBB().getNode());
                while(iPred.hasNext()){
                    CGNode pred = iPred.next();
                    Iterator<CallSiteReference> iCsr = cg.getPossibleSites(pred, block.getBB().getNode());
                    while(iCsr.hasNext()){
                        CallSiteReference csr = iCsr.next();
                        for(SSAAbstractInvokeInstruction invoke : pred.getIR().getCalls(csr)){
                            if(block.getContext().isFeasibleReturn(pred, invoke))
                                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(pred, invoke.getDef()), data.getWork()));
                        }
                    }
                }
//                CGNode caller = block.getCallStack().peek().getNode();
//                int defVar = block.getCallStack().peek().getCallBlock().getLastInstruction().getDef();
//
//                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(caller, defVar), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitGet(Node block, SSAGetInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();
        res.add(data);

        if(instruction.isStatic()){
            if(dp instanceof StaticFieldDataPointer){
                StaticFieldDataPointer sfdp = (StaticFieldDataPointer) dp;

                if(sfdp.getField().getReference().equals(instruction.getDeclaredField())){
                    res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(block.getBB().getNode(), instruction.getDef()), data.getWork()));
                }
            }
        }else {
            if (dp instanceof LocalDataPointer) {
                LocalDataPointer ldp = (LocalDataPointer) dp;
                if (ldp.getNode().equals(block.getBB().getNode()) && instruction.getRef() == ldp.getVar()) {
                    Work curWork = data.getWork();

                    if (curWork instanceof NoMoreWork) {
                        //todo: over-approximation?
//                        res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), curWork));
                    } else {
                        Work nextWork = data.getWork().execute(instruction);
                        if (!nextWork.equals(curWork)) {
                            res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), nextWork));
                        }
                    }
                }
            }
        }
        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitPut(Node block, SSAPutInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();
        res.add(data);

        if(instruction.isStatic()){
            if(dp instanceof LocalDataPointer) {
                LocalDataPointer ldp = (LocalDataPointer) dp;
                if(ldp.getNode().equals(block.getBB().getNode()) && instruction.getVal() == ldp.getVar()){
                    res.add(new DataFlowAnalysis.DataWithWork(new StaticFieldDataPointer(cha.resolveField(instruction.getDeclaredField())), data.getWork()));
                }
            }
        }else {
            if (dp instanceof LocalDataPointer) {
                LocalDataPointer ldp = (LocalDataPointer) dp;
                if (ldp.getNode().equals(block.getBB().getNode()) && instruction.getVal() == ldp.getVar()) {
                    LocalDataPointer objPointer = new LocalDataPointer(ldp.getNode(), instruction.getRef());
                    res.add(new DataFlowAnalysis.DataWithWork(objPointer, GetFieldWork.getInstance(cha.resolveField(instruction.getDeclaredField()), data.getWork())));

                    //TODO: handle alias!
                    PointerKey objPointerKey = objPointer.getPointerKey(pa);
                    long START = System.currentTimeMillis();
                    System.out.println("#Alias Start!:  " + dp);
                    System.out.println("\t in" + instruction);
                    res.addAll(getAliasDataPoint(objPointerKey, GetFieldWork.getInstance(cha.resolveField(instruction.getDeclaredField()), data.getWork())));
                    System.out.println("#Alias time: " + (System.currentTimeMillis() - START));
                }
            }
        }

        return res;
    }

    private Iterator<PointerKey> getPredNodes(InstanceKey ik){
        Set<PointerKey> pks = new HashSet<>();
        long start = System.currentTimeMillis();

        pa.getPointerKeys().forEach((pk -> {
            if(pa.getPointsToSet(pk).contains(ik)){
                pks.add(pk);
            }
        }));
        System.out.println("\tFOUND PREDS: " + (System.currentTimeMillis() - start));
        return pks.iterator();
    }

    private Set<PointerKey> handled = new HashSet<>();

    private void handlePointerKeyforAlias(Set<DataFlowAnalysis.DataWithWork> s, PointerKey pk, Work w){
        if(handled.contains(pk))
            return;

        handled.add(pk);

        if(pk instanceof LocalPointerKey){
            LocalPointerKey lpk = (LocalPointerKey) pk;
            CGNode n = lpk.getNode();

            // Ban primordial alias
            if(n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
                return;

            int v = lpk.getValueNumber();

            s.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(n, v), w));
        }else if(pk instanceof InstanceFieldKey){
            //no-op
        }else if(pk instanceof StaticFieldKey){
            StaticFieldKey sfk = (StaticFieldKey) pk;

            // Ban primordial alias
            if(sfk.getField().getReference().getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Primordial))
                return;

            IField field = sfk.getField();

            s.add(new DataFlowAnalysis.DataWithWork(new StaticFieldDataPointer(field), w));
        }else if(pk instanceof ExceptionReturnValueKey){
            // no-op
        }else if(pk instanceof PropagationCallGraphBuilder.TypedPointerKey){
            PropagationCallGraphBuilder.TypedPointerKey tpk = (PropagationCallGraphBuilder.TypedPointerKey) pk;
            handlePointerKeyforAlias(s, tpk.getBase(), w);
        }else if(pk instanceof ArrayContentsKey){
            // no-op
        }else if(pk instanceof ReturnValueKey){
            //todo: need to handle all def values for this node call sites?
            // no-op
        }else {
            Assertions.UNREACHABLE("We did not handle with the pointer key: " + pk.getClass().getName());
        }
    }

    private Set<DataFlowAnalysis.DataWithWork> getAliasDataPoint(PointerKey pk, Work w){
        Set<DataFlowAnalysis.DataWithWork> res = handleAliasDataPoint(pk, w);

        handled.clear();

        return res;
    }

    private Set<DataFlowAnalysis.DataWithWork> handleAliasDataPoint(PointerKey pk, Work w){
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        for(InstanceKey ik : pa.getPointsToSet(pk)){
            Iterator iPredPointerKey = getPredNodes(ik);
            while(iPredPointerKey.hasNext()){
                PointerKey predPointerKey = (PointerKey) iPredPointerKey.next();
                handlePointerKeyforAlias(res, predPointerKey, w);
            }
        }

        handled.clear();

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitInvoke(Node block, SSAInvokeInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBB().getNode())){
                int index = 0;
                for(;index < instruction.getNumberOfUses(); index++) {
                    if(instruction.getUse(index) == ldp.getVar()) {
                        final int succData = index;

                        cg.getPossibleTargets(block.getBB().getNode(), instruction.getCallSite()).forEach(new Consumer<CGNode>() {
                            @Override
                            public void accept(CGNode succ) {
                                if(isPrimitive(succ)){ //over-approximation for primordial methods
                                    int[] rets = builtinHandler.matchFlow(succ, succData);
                                    IntStream is = Arrays.stream(rets);
                                    is.forEach(
                                            (value -> {
                                                switch(value) {
                                                    case MethodFlowModel.RETV:
                                                        res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(block.getBB().getNode(), instruction.getDef()), data.getWork()));
                                                        break;
                                                    case MethodFlowModel.NONE:
                                                        break;
                                                    default:
                                                        res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(block.getBB().getNode(), instruction.getUse(value)), data.getWork()));
                                                        break;
                                                }
                                            })
                                    );
                                }else
                                    res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(succ, succData + 1), data.getWork()));
                            }
                        });
                    }
                }

                if(ldp.getVar() == IFlowFunction.ANY){
                    Iterator<CGNode> iSucc = cg.getSuccNodes(block.getBB().getNode());

                    while(iSucc.hasNext()) {
                        CGNode succ = iSucc.next();
                        if(isPrimitive(succ)){ //over-approximation for primordial methods
                            res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(succ, IFlowFunction.ANY), data.getWork()));
                        }else {
                            res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(succ, IFlowFunction.ANY), data.getWork()));
                        }
                    }
                }
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitNew(Node block, SSANewInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitArrayLength(Node block, SSAArrayLengthInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitThrow(Node block, SSAThrowInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitMonitor(Node block, SSAMonitorInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitCheckCast(Node block, SSACheckCastInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBB().getNode()) && instruction.getUse(0) == ldp.getVar()){
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitInstanceof(Node block, SSAInstanceofInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitPhi(Node block, SSAPhiInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBB().getNode())){
                int index = 0;
                for(;index < instruction.getNumberOfUses(); index++) {
                    if(instruction.getUse(index) == ldp.getVar()) {
                        res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(block.getBB().getNode(), instruction.getDef()), data.getWork()));
                    }
                }
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitPi(Node block, SSAPiInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitGetCaughtException(Node block, SSAGetCaughtExceptionInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitLoadMetadata(Node block, SSALoadMetadataInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }
}
