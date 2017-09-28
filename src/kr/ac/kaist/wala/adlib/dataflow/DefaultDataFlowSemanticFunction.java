package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.model.ARModeling;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by leesh on 25/09/2017.
 */
public class DefaultDataFlowSemanticFunction implements IDataFlowSemanticFunction {
    private IClassHierarchy cha;
    private PointerAnalysis<InstanceKey> pa;
    private CallGraph cg;

    public DefaultDataFlowSemanticFunction(CallGraph cg, IClassHierarchy cha, PointerAnalysis<InstanceKey> pa){
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
    public Set<DataFlowAnalysis.DataWithWork> visitGoto(DataFlowAnalysis.NodeWithCS block, SSAGotoInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitArrayLoad(DataFlowAnalysis.NodeWithCS block, SSAArrayLoadInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBlock().getNode()) && instruction.getArrayRef() == ldp.getVar()){
                //over-approximation for arrays
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitArrayStore(DataFlowAnalysis.NodeWithCS block, SSAArrayStoreInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBlock().getNode()) && instruction.getUse(1) == ldp.getVar()){
                //over-approximation for arrays
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getArrayRef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitBinaryOp(DataFlowAnalysis.NodeWithCS block, SSABinaryOpInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBlock().getNode()) && (instruction.getUse(0) == ldp.getVar() || instruction.getUse(1) == ldp.getVar())){
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitUnaryOp(DataFlowAnalysis.NodeWithCS block, SSAUnaryOpInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBlock().getNode()) && instruction.getUse(0) == ldp.getVar()){
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitConversion(DataFlowAnalysis.NodeWithCS block, SSAConversionInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBlock().getNode()) && instruction.getUse(0) == ldp.getVar()){
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitComparison(DataFlowAnalysis.NodeWithCS block, SSAComparisonInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBlock().getNode()) && (instruction.getUse(0) == ldp.getVar() || instruction.getUse(1) == ldp.getVar())){
                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitConditionalBranch(DataFlowAnalysis.NodeWithCS block, SSAConditionalBranchInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitSwitch(DataFlowAnalysis.NodeWithCS block, SSASwitchInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitReturn(DataFlowAnalysis.NodeWithCS block, SSAReturnInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBlock().getNode()) && instruction.getUse(0) == ldp.getVar()){
                CGNode caller = block.getCallStack().peek().getNode();
                int defVar = block.getCallStack().peek().getCallBlock().getLastInstruction().getDef();

                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(caller, defVar), data.getWork()));
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitGet(DataFlowAnalysis.NodeWithCS block, SSAGetInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();
        res.add(data);

        if(instruction.isStatic()){
            if(dp instanceof StaticFieldDataPointer){
                StaticFieldDataPointer sfdp = (StaticFieldDataPointer) dp;

                if(sfdp.getField().getReference().equals(instruction.getDeclaredField())){
                    res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(block.getBlock().getNode(), instruction.getDef()), data.getWork()));
                }
            }
        }else {
            if (dp instanceof LocalDataPointer) {
                LocalDataPointer ldp = (LocalDataPointer) dp;
                if (ldp.getNode().equals(block.getBlock().getNode()) && instruction.getRef() == ldp.getVar()) {
                    Work curWork = data.getWork();
                    if (curWork instanceof NoMoreWork) {
                        res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(ldp.getNode(), instruction.getDef()), curWork));
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
    public Set<DataFlowAnalysis.DataWithWork> visitPut(DataFlowAnalysis.NodeWithCS block, SSAPutInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();
        res.add(data);

        if(instruction.isStatic()){
            if(dp instanceof LocalDataPointer) {
                LocalDataPointer ldp = (LocalDataPointer) dp;
                if(ldp.getNode().equals(block.getBlock().getNode()) && instruction.getVal() == ldp.getVar()){
                    res.add(new DataFlowAnalysis.DataWithWork(new StaticFieldDataPointer(cha.resolveField(instruction.getDeclaredField())), data.getWork()));
                }
            }
        }else {
            if (dp instanceof LocalDataPointer) {
                LocalDataPointer ldp = (LocalDataPointer) dp;
                if (ldp.getNode().equals(block.getBlock().getNode()) && instruction.getVal() == ldp.getVar()) {
                    LocalDataPointer objPointer = new LocalDataPointer(ldp.getNode(), instruction.getRef());
                    res.add(new DataFlowAnalysis.DataWithWork(objPointer, GetFieldWork.getInstance(cha.resolveField(instruction.getDeclaredField()), data.getWork())));

                    //TODO: handle alias!
                    PointerKey objPointerKey = objPointer.getPointerKey(pa);
                    res.addAll(getAliasDataPoint(objPointerKey, data.getWork()));
                }
            }
        }

        return res;
    }

    private Set<DataFlowAnalysis.DataWithWork> getAliasDataPoint(PointerKey pk, Work w){
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        for(InstanceKey ik : pa.getPointsToSet(pk)){
            Iterator iPredPointerKey = pa.getHeapGraph().getPredNodes(ik);
            while(iPredPointerKey.hasNext()){
                PointerKey predPointerKey = (PointerKey) iPredPointerKey.next();

                if(predPointerKey instanceof LocalPointerKey){
                    LocalPointerKey lpk = (LocalPointerKey) predPointerKey;
                    CGNode n = lpk.getNode();
                    int v = lpk.getValueNumber();

                    res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(n, v), w));
                }else if(predPointerKey instanceof InstanceFieldKey){
                    InstanceFieldKey ifpk = (InstanceFieldKey) predPointerKey;
                    IField field = ifpk.getField();
                    Iterator iFieldPredPointerKey = pa.getHeapGraph().getPredNodes(ifpk.getInstanceKey());

                    while(iFieldPredPointerKey.hasNext()){
                        PointerKey fieldPredPointerKey = (PointerKey)iFieldPredPointerKey.next();
                        res.addAll(getAliasDataPoint(fieldPredPointerKey, GetFieldWork.getInstance(field, w)));
                    }
                }else if(predPointerKey instanceof StaticFieldKey){
                    StaticFieldKey sfk = (StaticFieldKey) predPointerKey;
                    IField field = sfk.getField();

                    res.add(new DataFlowAnalysis.DataWithWork(new StaticFieldDataPointer(field), w));
                }else{
                    Assertions.UNREACHABLE("We did not handle with the pointer key: " + predPointerKey.getClass().getName());
                }
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitInvoke(DataFlowAnalysis.NodeWithCS block, SSAInvokeInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBlock().getNode())){
                int index = 0;
                for(;index < instruction.getNumberOfUses(); index++) {
                    if(instruction.getUse(index) == ldp.getVar()) {
                        Iterator<CGNode> iSucc = cg.getSuccNodes(block.getBlock().getNode());

                        while(iSucc.hasNext()) {
                            CGNode succ = iSucc.next();
                            if(isPrimitive(succ)){ //over-approximation for primordial methods
                                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(succ, index+1), data.getWork()));
                            }else
                                res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(succ, index+1), data.getWork()));
                        }
                    }
                }

                if(ldp.getVar() == IFlowFunction.ANY){
                    Iterator<CGNode> iSucc = cg.getSuccNodes(block.getBlock().getNode());

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
    public Set<DataFlowAnalysis.DataWithWork> visitNew(DataFlowAnalysis.NodeWithCS block, SSANewInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitArrayLength(DataFlowAnalysis.NodeWithCS block, SSAArrayLengthInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitThrow(DataFlowAnalysis.NodeWithCS block, SSAThrowInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitMonitor(DataFlowAnalysis.NodeWithCS block, SSAMonitorInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitCheckCast(DataFlowAnalysis.NodeWithCS block, SSACheckCastInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitInstanceof(DataFlowAnalysis.NodeWithCS block, SSAInstanceofInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitPhi(DataFlowAnalysis.NodeWithCS block, SSAPhiInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        IDataPointer dp = data.getData();
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        res.add(data);

        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(block.getBlock().getNode())){
                int index = 0;
                for(;index < instruction.getNumberOfUses(); index++) {
                    if(instruction.getUse(index) == ldp.getVar()) {
                        res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(block.getBlock().getNode(), instruction.getDef()), data.getWork()));
                    }
                }
            }
        }

        return res;
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitPi(DataFlowAnalysis.NodeWithCS block, SSAPiInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitGetCaughtException(DataFlowAnalysis.NodeWithCS block, SSAGetCaughtExceptionInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }

    @Override
    public Set<DataFlowAnalysis.DataWithWork> visitLoadMetadata(DataFlowAnalysis.NodeWithCS block, SSALoadMetadataInstruction instruction, DataFlowAnalysis.DataWithWork data) {
        return Collections.singleton(data);
    }
}
