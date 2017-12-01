package kr.ac.kaist.wala.adlib.dataflow.flowmodel;

import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;

/**
 * Created by leesh on 11/10/2017.
 */
public abstract class ClassFlowModel {
    protected TypeReference ref;
    protected MethodFlowModel[] methods;

    protected ClassFlowModel(){
        init();
        if(ref == null || methods == null)
            Assertions.UNREACHABLE("Should initialize ref and methods fields in the subclasses of ClassFlowModel: " + this.getClass().getName());
    }

    public TypeReference getReference(){
        return ref;
    }

    public MethodFlowModel[] getMethods(){
        return methods;
    }

    public int[] matchFlow(MethodReference target, int data){
        for(int i=0; i<methods.length; i++){
            if(methods[i].getReference().equals(target)) {
                return methods[i].matchFlow(data);
            }
        }
        return new int[]{};
    }

    /**
     * initialize a Class type reference and Method flow models for each class modeling.
     */
    protected abstract void init();
}
