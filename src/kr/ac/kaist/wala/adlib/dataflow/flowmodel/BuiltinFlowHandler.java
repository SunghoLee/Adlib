package kr.ac.kaist.wala.adlib.dataflow.flowmodel;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.*;

/**
 * Created by leesh on 11/10/2017.
 */
public class BuiltinFlowHandler {

    //for single-ton
    private static BuiltinFlowHandler instance;

    private ClassFlowModel[] models = {
        StringFlowModel.getInstance(),
            StringBufferFlowModel.getInstance(),
            StringBuilderFlowModel.getInstance()
    };

    public static BuiltinFlowHandler getInstance(){
        if(instance == null)
            instance = new BuiltinFlowHandler();
        return instance;
    }

    protected BuiltinFlowHandler(){}

    public int[] matchFlow(CGNode target, int data){
        TypeReference targetClassRef  = target.getMethod().getDeclaringClass().getReference();
        MethodReference targetMethodRef = target.getMethod().getReference();

        data =  (!target.getMethod().isStatic() && data == 0)? MethodFlowModel.RECEIVERV : data;

        // find the callee model and match the data flow
        for(int i=0; i<models.length; i++){
            if(models[i].getReference().equals(targetClassRef)){
                return models[i].matchFlow(targetMethodRef, data);
            }

        }

        // delegate the data flow handling
        return handleNoneModeledFlow(target.getMethod().isStatic(), targetMethodRef, data);
    }

    protected int[] handleNoneModeledFlow(boolean isStatic, MethodReference target, int data){
        int paramNum = target.getNumberOfParameters();
        boolean hasReturn = !target.getReturnType().equals(TypeReference.Void);

        int[] result = new int[paramNum + ((hasReturn)? 1 : 0) + ((isStatic)? 0 : 1)];

        int i=0;
        // insert parameter indexes
        for(; i<paramNum; i++){
            result[i] = i;
        }

        // insert the return index if it has
        if(hasReturn)
            result[i++] = MethodFlowModel.RETV;

        // insert the receiver index if it has
        if(!isStatic)
            result[i++] = MethodFlowModel.RECEIVERV;

        return result;
    }
}
