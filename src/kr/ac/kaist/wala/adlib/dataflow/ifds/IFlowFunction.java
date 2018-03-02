package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.ssa.*;

import java.util.Set;

/**
 * Created by leesh on 02/03/2018.
 */
public interface IFlowFunction {
    Set<DataFact> visitGoto(SSAGotoInstruction instruction, DataFact fact);

    Set<DataFact> visitArrayLoad(SSAArrayLoadInstruction instruction, DataFact fact);

    Set<DataFact> visitArrayStore(SSAArrayStoreInstruction instruction, DataFact fact);

    Set<DataFact> visitBinaryOp(SSABinaryOpInstruction instruction, DataFact fact);

    Set<DataFact> visitUnaryOp(SSAUnaryOpInstruction instruction, DataFact fact);

    Set<DataFact> visitConversion(SSAConversionInstruction instruction, DataFact fact);

    Set<DataFact> visitComparison(SSAComparisonInstruction instruction, DataFact fact);

    Set<DataFact> visitConditionalBranch(SSAConditionalBranchInstruction instruction, DataFact fact);

    Set<DataFact> visitSwitch(SSASwitchInstruction instruction, DataFact fact);

    Set<DataFact> visitReturn(SSAReturnInstruction instruction, DataFact fact);

    Set<DataFact> visitGet(SSAGetInstruction instruction, DataFact fact);

    Set<DataFact> visitPut(SSAPutInstruction instruction, DataFact fact);

    Set<DataFact> visitInvoke(SSAInvokeInstruction instruction, DataFact fact);

    Set<DataFact> visitNew(SSANewInstruction instruction, DataFact fact);

    Set<DataFact> visitArrayLength(SSAArrayLengthInstruction instruction, DataFact fact);

    Set<DataFact> visitThrow(SSAThrowInstruction instruction, DataFact fact);

    Set<DataFact> visitMonitor(SSAMonitorInstruction instruction, DataFact fact);

    Set<DataFact> visitCheckCast(SSACheckCastInstruction instruction, DataFact fact);

    Set<DataFact> visitInstanceof(SSAInstanceofInstruction instruction, DataFact fact);

    Set<DataFact> visitPhi(SSAPhiInstruction instruction, DataFact fact);

    Set<DataFact> visitPi(SSAPiInstruction instruction, DataFact fact);

    Set<DataFact> visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction, DataFact fact);

    Set<DataFact> visitLoadMetadata(SSALoadMetadataInstruction instruction, DataFact fact);
}
