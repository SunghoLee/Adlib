package kr.ac.kaist.wala.adlib.dataflow.ifds.model;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.ifds.DataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.DefaultDataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.LocalDataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.FieldSeq;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.NoneField;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.collection.MapFlowModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 12/03/2018.
 */
public class FlowModelHandler {
    protected ClassFlowModel[] models;

    public FlowModelHandler(IClassHierarchy cha){
        init(cha);
    }

    private void init(IClassHierarchy cha){
        models = new ClassFlowModel[]{
                StringFlowModel.getInstance(),
                StringBufferFlowModel.getInstance(),
                StringBuilderFlowModel.getInstance(),
                UrlDecoderFlowModel.getInstance(),
                MapFlowModel.getInstance(cha),
        };
    }

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

    public Set<DataFact> matchDataFact(CGNode curNode, CGNode target, SSAAbstractInvokeInstruction invokeInst, DataFact dfact){
        //TODO: improve the matching performance
        Set<DataFact> res = new HashSet<>();
        MethodFlowModel mfm = getMethodModel(target);

        if(invokeInst.toString().contains("put(Ljava"))
            System.out.println("^^^^^^^^");
        if(dfact instanceof LocalDataFact) {
            LocalDataFact fact = (LocalDataFact) dfact;
            int index = -100;
            for (int i = 0; i < invokeInst.getNumberOfUses(); i++) {
                if (invokeInst.getUse(i) == fact.getVar()) {
                    if (!invokeInst.isStatic() && i == 0)
                        index = MethodFlowModel.RECEIVERV;
                    else
                        index = i;
                    break;
                }
            }

            // if the fact is not used in this invoke instruction, just pass none.
            // it is possible, when this instruction is at a return site.
            if (index == -100)
                return Collections.emptySet();

            for (int i : mfm.matchFlow(index)) {
                System.out.println("=== ");
                System.out.println("INST: " + invokeInst);
                System.out.println("FROM: " + invokeInst.getUse(index));
                System.out.println("TO: " + ((i == MethodFlowModel.RETV) ? invokeInst.getDef() : invokeInst.getUse(i)));
                System.out.println("=== ");
                Field f = fact.getField();
                Field newF = mfm.matchField(f);

                // intentionally cut infeasible paths at this point!
                if(newF == null)
                    return Collections.emptySet();

                if (i == MethodFlowModel.RETV) {
                    if(mfm.getReference().getReturnType().isArrayType())
                        res.add(new LocalDataFact(curNode, invokeInst.getDef(), new FieldSeq("[", newF)));
                    else
                        res.add(new LocalDataFact(curNode, invokeInst.getDef(), newF));
                }
                else if (i == MethodFlowModel.RECEIVERV)
                    res.add(new LocalDataFact(curNode, invokeInst.getUse(0), newF));
                else
                    res.add(new LocalDataFact(curNode, invokeInst.getUse(i), newF));
            }
        }else if(dfact instanceof DefaultDataFact){
            if(isIn(mfm.getFrom(), MethodFlowModel.ANY)) {

                Field f = dfact.getField();
                Field newF = mfm.matchField(f);

                // intentionally cut infeasible paths at this point!
                if(newF == null)
                    return Collections.emptySet();

                for (int i : mfm.getTo()) {
                    if (i == MethodFlowModel.RETV) {
                        if (mfm.getReference().getReturnType().isArrayType())
                            res.add(new LocalDataFact(curNode, invokeInst.getDef(), new FieldSeq("[", newF)));
                        else
                            res.add(new LocalDataFact(curNode, invokeInst.getDef(), NoneField.getInstance()));
                    } else if (i == MethodFlowModel.RECEIVERV)
                        res.add(new LocalDataFact(curNode, invokeInst.getUse(0), NoneField.getInstance()));
                    else
                        res.add(new LocalDataFact(curNode, invokeInst.getUse(i), NoneField.getInstance()));
                }
            }
        }else
            Assertions.UNREACHABLE("Local or Default data facts are only possible to match with modeling flows: " + dfact);
        return res;
    }

    private boolean isIn(int[] arr, int i){
        for(int j : arr){
            if(i == j)
                return true;
        }

        return false;
    }
}
