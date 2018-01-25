package kr.ac.kaist.wala.adlib.dataflow.flows;

import com.ibm.wala.ssa.*;
import kr.ac.kaist.wala.adlib.dataflow.DataFlowAnalysis;
import kr.ac.kaist.wala.adlib.dataflow.Node;

import java.util.Set;

/**
 * Created by leesh on 19/09/2017.
 */
public interface IDataFlowSemanticFunction {
        public Set<DataFlowAnalysis.DataWithWork> visitGoto(Node block, SSAGotoInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitArrayLoad(Node block, SSAArrayLoadInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitArrayStore(Node block, SSAArrayStoreInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitBinaryOp(Node block, SSABinaryOpInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitUnaryOp(Node block, SSAUnaryOpInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitConversion(Node block, SSAConversionInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitComparison(Node block, SSAComparisonInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitConditionalBranch(Node block, SSAConditionalBranchInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitSwitch(Node block, SSASwitchInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitReturn(Node block, SSAReturnInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitGet(Node block, SSAGetInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitPut(Node block, SSAPutInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitInvoke(Node block, SSAInvokeInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitNew(Node block, SSANewInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitArrayLength(Node block, SSAArrayLengthInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitThrow(Node block, SSAThrowInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitMonitor(Node block, SSAMonitorInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitCheckCast(Node block, SSACheckCastInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitInstanceof(Node block, SSAInstanceofInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitPhi(Node block, SSAPhiInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitPi(Node block, SSAPiInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitGetCaughtException(Node block, SSAGetCaughtExceptionInstruction instruction, DataFlowAnalysis.DataWithWork data);

        public Set<DataFlowAnalysis.DataWithWork> visitLoadMetadata(Node block, SSALoadMetadataInstruction instruction, DataFlowAnalysis.DataWithWork data);
}
