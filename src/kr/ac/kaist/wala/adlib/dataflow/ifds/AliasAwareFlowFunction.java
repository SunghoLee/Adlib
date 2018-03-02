package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ssa.*;

import java.util.Collections;
import java.util.Set;

/**
 * Created by leesh on 02/03/2018.
 */
public class AliasAwareFlowFunction implements IFlowFunction {
    private final ICFGSupergraph supergraph;
    private final PointerAnalysis<InstanceKey> pa;
    private final HeapGraph<InstanceKey> hg;

    public AliasAwareFlowFunction(ICFGSupergraph supergraph, PointerAnalysis<InstanceKey> pa){
        this.supergraph = supergraph;
        this.pa = pa;
        this.hg = pa.getHeapGraph();
    }

    @Override
    public Set<DataFact> visitGoto(SSAGotoInstruction instruction, DataFact fact) {
        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitArrayLoad(SSAArrayLoadInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitArrayStore(SSAArrayStoreInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitBinaryOp(SSABinaryOpInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitUnaryOp(SSAUnaryOpInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitConversion(SSAConversionInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitComparison(SSAComparisonInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitConditionalBranch(SSAConditionalBranchInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitSwitch(SSASwitchInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitReturn(SSAReturnInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitGet(SSAGetInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitPut(SSAPutInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitInvoke(SSAInvokeInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitNew(SSANewInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitArrayLength(SSAArrayLengthInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitThrow(SSAThrowInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitMonitor(SSAMonitorInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitCheckCast(SSACheckCastInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitInstanceof(SSAInstanceofInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitPhi(SSAPhiInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitPi(SSAPiInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction, DataFact fact) {
        return null;
    }

    @Override
    public Set<DataFact> visitLoadMetadata(SSALoadMetadataInstruction instruction, DataFact fact) {
        return null;
    }
}
