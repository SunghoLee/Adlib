package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.*;
import com.ibm.wala.util.debug.Assertions;

import java.util.Set;

/**
 * Created by leesh on 02/03/2018.
 */
public interface IFlowFunction {
    Set<DataFact> visitGoto(CGNode n, SSAGotoInstruction instruction, DataFact fact);

    Set<DataFact> visitArrayLoad(CGNode n, SSAArrayLoadInstruction instruction, DataFact fact);

    Set<DataFact> visitArrayStore(CGNode n, SSAArrayStoreInstruction instruction, DataFact fact);

    Set<DataFact> visitBinaryOp(CGNode n, SSABinaryOpInstruction instruction, DataFact fact);

    Set<DataFact> visitUnaryOp(CGNode n, SSAUnaryOpInstruction instruction, DataFact fact);

    Set<DataFact> visitConversion(CGNode n, SSAConversionInstruction instruction, DataFact fact);

    Set<DataFact> visitComparison(CGNode n, SSAComparisonInstruction instruction, DataFact fact);

    Set<DataFact> visitConditionalBranch(CGNode n, SSAConditionalBranchInstruction instruction, DataFact fact);

    Set<DataFact> visitSwitch(CGNode n, SSASwitchInstruction instruction, DataFact fact);

    Set<DataFact> visitReturn(CGNode n, SSAReturnInstruction instruction, DataFact fact);

    Set<DataFact> visitGet(CGNode n, SSAGetInstruction instruction, DataFact fact);

    Set<DataFact> visitPut(CGNode n, SSAPutInstruction instruction, DataFact fact);

    Set<DataFact> visitInvoke(CGNode n, SSAInvokeInstruction instruction, DataFact fact);

    Set<DataFact> visitNew(CGNode n, SSANewInstruction instruction, DataFact fact);

    Set<DataFact> visitArrayLength(CGNode n, SSAArrayLengthInstruction instruction, DataFact fact);

    Set<DataFact> visitThrow(CGNode n, SSAThrowInstruction instruction, DataFact fact);

    Set<DataFact> visitMonitor(CGNode n, SSAMonitorInstruction instruction, DataFact fact);

    Set<DataFact> visitCheckCast(CGNode n, SSACheckCastInstruction instruction, DataFact fact);

    Set<DataFact> visitInstanceof(CGNode n, SSAInstanceofInstruction instruction, DataFact fact);

    Set<DataFact> visitPhi(CGNode n, SSAPhiInstruction instruction, DataFact fact);

    Set<DataFact> visitPi(CGNode n, SSAPiInstruction instruction, DataFact fact);

    Set<DataFact> visitGetCaughtException(CGNode n, SSAGetCaughtExceptionInstruction instruction, DataFact fact);

    Set<DataFact> visitLoadMetadata(CGNode n, SSALoadMetadataInstruction instruction, DataFact fact);

    default Set<DataFact> visit(CGNode n, SSAInstruction inst, DataFact fact){
        if(inst instanceof SSAGotoInstruction)
            return this.visitGoto(n, (SSAGotoInstruction) inst, fact);

        else if(inst instanceof SSAArrayLoadInstruction)
            return this.visitArrayLoad(n, (SSAArrayLoadInstruction) inst, fact);

        else if(inst instanceof SSAArrayStoreInstruction)
            return this.visitArrayStore(n, (SSAArrayStoreInstruction) inst, fact);

        else if(inst instanceof SSABinaryOpInstruction)
            return this.visitBinaryOp(n, (SSABinaryOpInstruction) inst, fact);

        else if(inst instanceof SSAUnaryOpInstruction)
            return this.visitUnaryOp(n, (SSAUnaryOpInstruction) inst, fact);

        else if(inst instanceof SSAConversionInstruction)
            return this.visitConversion(n, (SSAConversionInstruction) inst, fact);

        else if(inst instanceof SSAComparisonInstruction)
            return this.visitComparison(n, (SSAComparisonInstruction) inst, fact);

        else if(inst instanceof SSAConditionalBranchInstruction)
            return this.visitConditionalBranch(n, (SSAConditionalBranchInstruction) inst, fact);

        else if(inst instanceof SSASwitchInstruction)
            return this.visitSwitch(n, (SSASwitchInstruction) inst, fact);

        else if(inst instanceof SSAReturnInstruction)
            return this.visitReturn(n, (SSAReturnInstruction) inst, fact);

        else if(inst instanceof SSAGetInstruction)
            return this.visitGet(n, (SSAGetInstruction) inst, fact);

        else if(inst instanceof SSAPutInstruction)
            return this.visitPut(n, (SSAPutInstruction) inst, fact);

        else if(inst instanceof SSAInvokeInstruction)
            return this.visitInvoke(n, (SSAInvokeInstruction) inst, fact);

        else if(inst instanceof SSANewInstruction)
            return this.visitNew(n, (SSANewInstruction) inst, fact);

        else if(inst instanceof SSAArrayLengthInstruction)
            return this.visitArrayLength(n, (SSAArrayLengthInstruction) inst, fact);

        else if(inst instanceof SSAThrowInstruction)
            return this.visitThrow(n, (SSAThrowInstruction) inst, fact);

        else if(inst instanceof SSAMonitorInstruction)
            return this.visitMonitor(n, (SSAMonitorInstruction) inst, fact);

        else if(inst instanceof SSACheckCastInstruction)
            return this.visitCheckCast(n, (SSACheckCastInstruction) inst, fact);

        else if(inst instanceof SSAInstanceofInstruction)
            return this.visitInstanceof(n, (SSAInstanceofInstruction) inst, fact);

        else if(inst instanceof SSAPhiInstruction)
            return this.visitPhi(n, (SSAPhiInstruction) inst, fact);

        else if(inst instanceof SSAPiInstruction)
            return this.visitPi(n, (SSAPiInstruction) inst, fact);

        else if(inst instanceof SSAGetCaughtExceptionInstruction)
            return this.visitGetCaughtException(n, (SSAGetCaughtExceptionInstruction) inst, fact);

        else if(inst instanceof SSALoadMetadataInstruction)
            return this.visitLoadMetadata(n, (SSALoadMetadataInstruction) inst, fact);

        Assertions.UNREACHABLE("We did not handle this instruction: " + inst.getClass().getName());
        return null;
    }
}
