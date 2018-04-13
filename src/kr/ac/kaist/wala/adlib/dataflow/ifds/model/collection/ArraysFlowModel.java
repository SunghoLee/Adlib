package kr.ac.kaist.wala.adlib.dataflow.ifds.model.collection;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.ClassFlowModel;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.FineGrainedMethodFlowModel;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.MethodFlowModel;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 11/10/2017.
 */
public class ArraysFlowModel extends ClassFlowModel{

    // for single-ton object
    private static ArraysFlowModel instance;
    private static IClassHierarchy cha;

    private static final Selector[] selectors = {
            Selector.make("asList([Ljava/lang/Object;)Ljava/util/List;"), // 0
    };

    public static ArraysFlowModel getInstance(IClassHierarchy cha){
        if(cha == null)
            Assertions.UNREACHABLE("Class hierarchy must be not null!");

        if(instance == null) {
            ArraysFlowModel.cha = cha;
            instance = new ArraysFlowModel();
        }

        return instance;
    }

    private ArraysFlowModel(){

    }

    @Override
    protected void init() {
        ref = TypeReference.find(ClassLoaderReference.Primordial, "Ljava/util/Arrays");

        Set<MethodFlowModel> mList = new HashSet<>();

        IClass c = ArraysFlowModel.cha.lookupClass(ref);
        for(IMethod m : c.getAllMethods()){

            Selector s = m.getSelector();
            if(s.equals(selectors[0])){ // asList([Ljava/lang/Object;)Ljava/util/List;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-[" + "." + CollectionFlowModel.COLLECTION_GET));
            }
        }

        methods = mList.toArray(new MethodFlowModel[0]);
    }
}
