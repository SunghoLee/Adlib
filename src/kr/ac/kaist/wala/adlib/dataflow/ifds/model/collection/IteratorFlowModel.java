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
public class IteratorFlowModel extends ClassFlowModel{

    public static String ITER_NEXT = "@INEXT";

    // for single-ton object
    private static IteratorFlowModel instance;
    private static IClassHierarchy cha;

    private static final Selector[] selectors = {
        Selector.make("forEachRemaining(Ljava/util/Consumer;)V"), // 0
            Selector.make("hasNext()B"), // 1
            Selector.make("next()Ljava/lang/Object;"), // 2
            Selector.make("remove()V"), // 3
    };

    public static IteratorFlowModel getInstance(IClassHierarchy cha){
        if(cha == null)
            Assertions.UNREACHABLE("Class hierarchy must be not null!");

        if(instance == null) {
            IteratorFlowModel.cha = cha;
            instance = new IteratorFlowModel();
        }

        return instance;
    }

    private IteratorFlowModel(){

    }

    @Override
    protected void init() {
        ref = TypeReference.find(ClassLoaderReference.Primordial, "Ljava/util/Iterator");

        Set<MethodFlowModel> mList = new HashSet<>();

        for(IClass c : IteratorFlowModel.cha.getImplementors(ref)){
            for(IMethod m : c.getAllMethods()){

                Selector s = m.getSelector();
                if(s.equals(selectors[0])){ // forEachRemaining(Ljava/util/Consumer;)V
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{})); // TODO: implement this!
                }else if(s.equals(selectors[1])){ // hasNext()B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[2])){ // next()Ljava/lang/Object;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + IteratorFlowModel.ITER_NEXT));
                }else if(s.equals(selectors[3])){ // remove()V
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }
            }
        }




        methods = mList.toArray(new MethodFlowModel[0]);
    }
}
