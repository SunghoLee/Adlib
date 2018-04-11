package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 02/03/2018.
 */
public class AliasAwareFlowFunction implements IFlowFunction {
    private final ICFGSupergraph supergraph;
    private final PointerAnalysis<InstanceKey> pa;
    private final HeapGraph<InstanceKey> hg;
    private final AliasHandler aliasHandler;
    private final FieldCompatibilityPolicy fcHandler;

    public AliasAwareFlowFunction(ICFGSupergraph supergraph, PointerAnalysis<InstanceKey> pa){
        this.supergraph = supergraph;
        this.pa = pa;
        this.hg = pa.getHeapGraph();
        this.fcHandler = new FieldCompatibilityPolicy(supergraph.getClassHierarchy());
        aliasHandler = new AliasHandler(supergraph, pa);
    }

    public boolean isCompatible(Field field, TypeReference tr){
        return fcHandler.isCompatible(field, tr);
    }

    @Override
    public Set<DataFact> visitGoto(CGNode n, SSAGotoInstruction instruction, DataFact fact) {
        return Collections.singleton(fact);
    }

    private Set<DataFact> getField(CGNode n, String instField, Field factField, int def){
        if(factField.isMatched(instField)){
            Set<DataFact> res=  new HashSet<>();
            for(Field f : factField.pop(instField)) {
                res.add(new LocalDataFact(n, def, f));
            }
            return res;
        }
        Assertions.UNREACHABLE("Cannot reach here! : " + factField);
        return Collections.emptySet();
    }

    @Override
    public Set<DataFact> visitArrayLoad(CGNode n, SSAArrayLoadInstruction instruction, DataFact fact) {
        //TODO: implement this!
        Set<DataFact> res = new HashSet<>();
        res.add(fact);

        /**
         * [Condition]
         * 1. if the data fact is a local data fact,
         * 1-1. the instruction is not a static get, and
         * 1-2. the get instruction has a same object reference with the data fact.
         *
         * 2. if the data fact is a global data fact,
         * 2-1. the instruction is a static get, and
         * 2-2. the get instruction has a same field reference with the data fact.
         */
        if(fact instanceof LocalDataFact &&
                ((LocalDataFact) fact).getVar() == instruction.getArrayRef()) {
            res.addAll(getField(n, "[", fact.getField(), instruction.getDef()));

        }else if(fact instanceof GlobalDataFact)
            return res;

        return res;
    }

    @Override
    public Set<DataFact> visitArrayStore(CGNode n, SSAArrayStoreInstruction instruction, DataFact fact) {
        //TODO: implement this!
        Set<DataFact> res = new HashSet<>();
        res.add(fact);

        if(fact instanceof LocalDataFact &&
            ((LocalDataFact) fact).getVar() == instruction.getValue()) {

            //TODO: handle aliases
            DataFact newFact = new LocalDataFact(n, instruction.getArrayRef(), FieldSeq.make(SingleField.make("["), fact.getField()));
            res.add(newFact);

            for(DataFact aliasFact : aliasHandler.findAlias(n, newFact)){
                res.add(aliasFact);
            }
        } else if(fact instanceof GlobalDataFact)
            return res;

        return res;
    }

    @Override
    public Set<DataFact> visitBinaryOp(CGNode n, SSABinaryOpInstruction instruction, DataFact fact) {
        if(fact instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) fact;

            if(instruction.getUse(0) != ldf.getVar() && instruction.getUse(1) != ldf.getVar())
                return Collections.emptySet();

            if(!ldf.getField().equals(NoneField.getInstance()) &&!ldf.getField().equals(TopField.getInstance()))
                Assertions.UNREACHABLE("A local data fact flowed to a binary operation must not have a field.\n\t Inst: " + instruction + "\n\t Fact: " + fact);

            Set<DataFact> res = new HashSet<>();
            res.add(fact);
            res.add(new LocalDataFact(n, instruction.getDef(), NoneField.getInstance()));
            return res;
        }

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitUnaryOp(CGNode n, SSAUnaryOpInstruction instruction, DataFact fact) {
        if(fact instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) fact;

            if(instruction.getUse(0) != ldf.getVar())
                return Collections.emptySet();

            if(!ldf.getField().equals(NoneField.getInstance()) && !ldf.getField().equals(TopField.getInstance()))
                Assertions.UNREACHABLE("A local data fact flowed to a unary operation must not have a field.\n\t Inst: " + instruction + "\n\t Fact: " + fact);

            Set<DataFact> res = new HashSet<>();
            res.add(fact);
            res.add(new LocalDataFact(n, instruction.getDef(), NoneField.getInstance()));
            return res;
        }

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitConversion(CGNode n, SSAConversionInstruction instruction, DataFact fact) {
        if(fact instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) fact;

            // this case is for return-sites
            if(instruction.getUse(0) != ldf.getVar())
                return Collections.singleton(fact);
//                Assertions.UNREACHABLE("A local data fact flowed to a conversion operation must be used in the instruction.\n\t Inst: " + instruction + "\n\t Fact: " + fact);

            Set<DataFact> res = new HashSet<>();
            res.add(fact);
            res.add(new LocalDataFact(n, instruction.getDef(), fact.getField()));
            return res;
        }

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitComparison(CGNode n, SSAComparisonInstruction instruction, DataFact fact) {
        // TODO: do we need to handle comparison operations?

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitConditionalBranch(CGNode n, SSAConditionalBranchInstruction instruction, DataFact fact) {
        // TODO: do we need to handle conditional operations?

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitSwitch(CGNode n, SSASwitchInstruction instruction, DataFact fact) {
        // TODO: do we need to handle switch operations?

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitReturn(CGNode n, SSAReturnInstruction instruction, DataFact fact) {
        // TODO: no-op because we handle "return" instructions in exit nodes.

        return Collections.singleton(fact);
    }

    private Set<DataFact> getField(CGNode n, FieldReference instField, Field factField, int def){
        if(factField.isMatched(instField.getName().toString())){
            Set<DataFact> res = new HashSet<>();
            for(Field f : factField.pop(instField.getName().toString())) {
                res.add(new LocalDataFact(n, def, f));
            }
            return res;
        }else if(factField instanceof NoneField){
            return Collections.singleton(new LocalDataFact(n, def, factField));
        }
        // it is possible if the data fact is the receiver object.
//        Assertions.UNREACHABLE("Cannot reach here!");
        return Collections.emptySet();
    }


    @Override
    public Set<DataFact> visitGet(CGNode n, SSAGetInstruction instruction, DataFact fact) {
        Set<DataFact> res = new HashSet<>();
        res.add(fact);

        /**
         * [Condition]
         * 1. if the data fact is a local data fact,
         * 1-1. the instruction is not a static get, and
         * 1-2. the get instruction has a same object reference with the data fact.
         *
         * 2. if the data fact is a global data fact,
         * 2-1. the instruction is a static get, and
         * 2-2. the get instruction has a same field reference with the data fact.
         */
        if(fact instanceof LocalDataFact &&
            !instruction.isStatic() &&
            ((LocalDataFact) fact).getVar() == instruction.getRef()) {

            res.addAll(getField(n, instruction.getDeclaredField(), fact.getField(), instruction.getDef()));
        }else if(fact instanceof GlobalDataFact &&
                instruction.isStatic() &&
                ((GlobalDataFact) fact).getGlobalFact().equals(instruction.getDeclaredField())){
            res.add(new LocalDataFact(n, instruction.getDef(), fact.getField()));
        }

        return res;
    }

    public static boolean DDD = false;

    @Override
    public Set<DataFact> visitPut(CGNode n, SSAPutInstruction instruction, DataFact fact) {
        Set<DataFact> res = new HashSet<>();
        res.add(fact);
//        boolean debug = false;
//
//        if(instruction.toString().contains("putfield 1.< Primordial, Ljava/lang/Thread, target, <Primordial,Ljava/lang/Runnable> > = 2") && fact.getField().toString().equals("url.$"))
//            debug = true;

        if(instruction.toString().contains("putfield 1.< Application, Lcom/suyduu/guzqhq288899/VpaidLayout$VPaidWebView$a$5$1, a, <Application,Lcom/suyduu/guzqhq288899/VpaidLayout$VPaidWebView$a$5> > = 2"))
            DDD = true;
        if(fact instanceof LocalDataFact &&
                ((LocalDataFact) fact).getVar() == instruction.getVal()) {

            if(instruction.isStatic()){
                res.add(new GlobalDataFact(instruction.getDeclaredField(), fact.getField()));
            }else{
                //TODO: handle aliases
                DataFact newFact = new LocalDataFact(n, instruction.getRef(), FieldSeq.make(SingleField.make(instruction.getDeclaredField().getName().toString()), fact.getField()));
                res.add(newFact);

                for(DataFact aliasFact : aliasHandler.findAlias(n, newFact)){
                    res.add(aliasFact);
                }
            }
        }
        DDD = false;
        return res;
    }

    @Override
    public Set<DataFact> visitInvoke(CGNode n, SSAInvokeInstruction instruction, DataFact fact) {
        // TODO: no-op because we handle "call" instructions in call nodes.
        if(fact instanceof LocalDataFact)
            Assertions.UNREACHABLE("A local data fact cannot be reached to a invoke instruction.\n\t Inst: " + instruction + "\n\t Fact: " + fact);
        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitNew(CGNode n, SSANewInstruction instruction, DataFact fact) {
        // TODO: no-op.

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitArrayLength(CGNode n, SSAArrayLengthInstruction instruction, DataFact fact) {
        // TODO: do we need to handle array length operations?

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitThrow(CGNode n, SSAThrowInstruction instruction, DataFact fact) {
        // TODO: do we need to handle exceptions?

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitMonitor(CGNode n, SSAMonitorInstruction instruction, DataFact fact) {
        // TODO: do we need to handle monitor operations?

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitCheckCast(CGNode n, SSACheckCastInstruction instruction, DataFact fact) {
        if(fact instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) fact;

            if(instruction.getUse(0) != ldf.getVar())
                return Collections.singleton(fact);

            Field field = ldf.getField();

            if(!(field instanceof NoneField)) {
                boolean isCompatible = false;

                for (TypeReference tr : instruction.getDeclaredResultTypes()) {
                    if(isCompatible(field, tr)) {
                        isCompatible = true;
                        break;
                    }
                }

                //cut this spurious flow!
                if (!isCompatible)
                    return Collections.emptySet();
            }
            Set<DataFact> res = new HashSet<>();
            res.add(fact);
            res.add(new LocalDataFact(n, instruction.getDef(), fact.getField()));
            return res;
        }

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitInstanceof(CGNode n, SSAInstanceofInstruction instruction, DataFact fact) {
        // TODO: do we need to handle instanceof operations?

        return Collections.singleton(fact);
    }

    private boolean isContained(SSAPhiInstruction inst, int var){
        for(int i=0; i<inst.getNumberOfUses(); i++)
            if(inst.getUse(i) == var)
                return true;
        return false;
    }

    @Override
    public Set<DataFact> visitPhi(CGNode n, SSAPhiInstruction instruction, DataFact fact) {
        if(fact instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) fact;

            if(!isContained(instruction, ldf.getVar()))
                Assertions.UNREACHABLE("A local data fact flowed to a phi operation must be used in the instruction.\n\t Inst: " + instruction + "\n\t Fact: " + fact);

            Set<DataFact> res = new HashSet<>();
            res.add(fact);
            res.add(new LocalDataFact(n, instruction.getDef(), fact.getField()));
            return res;
        }

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitPi(CGNode n, SSAPiInstruction instruction, DataFact fact) {
        if(fact instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) fact;

            if(instruction.getVal() != ldf.getVar())
                Assertions.UNREACHABLE("A local data fact flowed to a pi operation must be used in the instruction.\n\t Inst: " + instruction + "\n\t Fact: " + fact);

            Set<DataFact> res = new HashSet<>();
            res.add(fact);
            res.add(new LocalDataFact(n, instruction.getDef(), fact.getField()));
            return res;
        }

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitGetCaughtException(CGNode n, SSAGetCaughtExceptionInstruction instruction, DataFact fact) {
        // TODO: do we need to handle instanceof operations?

        return Collections.singleton(fact);
    }

    @Override
    public Set<DataFact> visitLoadMetadata(CGNode n, SSALoadMetadataInstruction instruction, DataFact fact) {
        // TODO: do we need to handle meta data operations?

        return Collections.singleton(fact);
    }

    public Set<DataFact> getLocalAliasOfCaller(CGNode n, LocalDataFact fact){
        Set<DataFact> res = new HashSet<>();

        for(DataFact aliasFact : aliasHandler.findAlias(n, fact)){
            res.add(aliasFact);
        }

        return res;
    }
}
