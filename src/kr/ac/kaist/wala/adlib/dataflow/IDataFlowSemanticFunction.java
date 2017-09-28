package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.ssa.*;

import java.util.Set;

/**
 * Created by leesh on 19/09/2017.
 */
public interface IDataFlowSemanticFunction {
        public Set<DataFlowAnalysis.DataWithWork> visitGoto(DataFlowAnalysis.NodeWithCS block, SSAGotoInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitArrayLoad(DataFlowAnalysis.NodeWithCS block, SSAArrayLoadInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitArrayStore(DataFlowAnalysis.NodeWithCS block, SSAArrayStoreInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitBinaryOp(DataFlowAnalysis.NodeWithCS block, SSABinaryOpInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitUnaryOp(DataFlowAnalysis.NodeWithCS block, SSAUnaryOpInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitConversion(DataFlowAnalysis.NodeWithCS block, SSAConversionInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitComparison(DataFlowAnalysis.NodeWithCS block, SSAComparisonInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitConditionalBranch(DataFlowAnalysis.NodeWithCS block, SSAConditionalBranchInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitSwitch(DataFlowAnalysis.NodeWithCS block, SSASwitchInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitReturn(DataFlowAnalysis.NodeWithCS block, SSAReturnInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitGet(DataFlowAnalysis.NodeWithCS block, SSAGetInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitPut(DataFlowAnalysis.NodeWithCS block, SSAPutInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitInvoke(DataFlowAnalysis.NodeWithCS block, SSAInvokeInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitNew(DataFlowAnalysis.NodeWithCS block, SSANewInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitArrayLength(DataFlowAnalysis.NodeWithCS block, SSAArrayLengthInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitThrow(DataFlowAnalysis.NodeWithCS block, SSAThrowInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitMonitor(DataFlowAnalysis.NodeWithCS block, SSAMonitorInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitCheckCast(DataFlowAnalysis.NodeWithCS block, SSACheckCastInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitInstanceof(DataFlowAnalysis.NodeWithCS block, SSAInstanceofInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitPhi(DataFlowAnalysis.NodeWithCS block, SSAPhiInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitPi(DataFlowAnalysis.NodeWithCS block, SSAPiInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitGetCaughtException(DataFlowAnalysis.NodeWithCS block, SSAGetCaughtExceptionInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitLoadMetadata(DataFlowAnalysis.NodeWithCS block, SSALoadMetadataInstruction instruction, DataFlowAnalysis.DataWithWork data);
}
