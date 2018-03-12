package kr.ac.kaist.wala.adlib.dataflow.ifds.model;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import kr.ac.kaist.wala.adlib.dataflow.ifds.DataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.LocalDataFact;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 12/03/2018.
 */
public class FlowModelHandler {

    private ClassFlowModel[] models = {
            StringFlowModel.getInstance(),
            StringBufferFlowModel.getInstance(),
            StringBuilderFlowModel.getInstance()
    };

    public boolean isModeled(CGNode target){
        //TODO: improve the matching performance
        if(getMethodModel(target) != null)
            return true;

        return false;
    }

    private MethodFlowModel getMethodModel(CGNode n){
        for(ClassFlowModel cfm : models){
            for(MethodFlowModel mfm : cfm.getMethods())
                if(mfm.getReference().equals(n.getMethod().getReference()))
                    return mfm;
        }
        return null;
    }

    public Set<DataFact> matchDataFact(CGNode target, SSAAbstractInvokeInstruction invokeInst, LocalDataFact fact){
        //TODO: improve the matching performance
        Set<DataFact> res = new HashSet<>();
        MethodFlowModel mfm = getMethodModel(target);

        int index = -100;
        for(int i=0; i<invokeInst.getNumberOfUses(); i++){
            if(invokeInst.getUse(i) == fact.getVar()) {
                if(!invokeInst.isStatic() && i == 0)
                    index = MethodFlowModel.RECEIVERV;
                else
                    index = i;
                break;
            }
        }

        // if the fact is not used in this invoke instruction, just pass none.
        // it is possible, when this instruction is at a return site.
        if(index == -100)
            return Collections.emptySet();

        for(int i : mfm.matchFlow(index)){
            System.out.println("=== ");
            System.out.println("INST: " + invokeInst);
            System.out.println("FROM: " + invokeInst.getUse(index));
            System.out.println("TO: " + ((i == MethodFlowModel.RETV)? invokeInst.getDef() : invokeInst.getUse(i)));
            System.out.println("=== ");
            if(i == MethodFlowModel.RETV)
                res.add(new LocalDataFact(fact.getNode(), invokeInst.getDef(), fact.getField()));
            else if(i == MethodFlowModel.RECEIVERV)
                res.add(new LocalDataFact(fact.getNode(), invokeInst.getUse(0), fact.getField()));
            else
                res.add(new LocalDataFact(fact.getNode(), invokeInst.getUse(i), fact.getField()));
        }

        return res;
    }
}
