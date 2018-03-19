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
public class SetFlowModel extends ClassFlowModel{
    // for single-ton object
    private static SetFlowModel instance;
    private static IClassHierarchy cha;

    private static final Selector[] selectors = {
        Selector.make("add(Ljava/lang/Object;)B"), // 0
            Selector.make("addAll(Ljava/util/Collection;)B"), // 1
            Selector.make("clear()V"), // 2
            Selector.make("contains(Ljava/lang/Object;)B"),  // 3
            Selector.make("containsAll(Ljava/util/Collection;)B"), // 4
            Selector.make("equals(Ljava/lang/Object;)B"), // 5
            Selector.make("hashCode()I"), // 6
            Selector.make("isEmpty()B"), // 7
            Selector.make("iterator()Ljava/util/Iterator;"), // 8
            Selector.make("remove(Ljava/lang/Object;)B"), // 9
            Selector.make("removeAll(Ljava/util/Collection;)B"), // 10
            Selector.make("retainAll(Ljava/util/Collection;)B"), // 11
            Selector.make("size()I"), // 12
            Selector.make("toArray()[Ljava/lang/Object;"), // 13
            Selector.make("toArray([Ljava/lang/Object;)[Ljava/lang/Object;"), // 14
    };

    public static SetFlowModel getInstance(IClassHierarchy cha){
        if(cha == null)
            Assertions.UNREACHABLE("Class hierarchy must be not null!");

        if(instance == null) {
            SetFlowModel.cha = cha;
            instance = new SetFlowModel();
        }

        return instance;
    }

    private SetFlowModel(){

    }

    @Override
    protected void init() {
        ref = TypeReference.find(ClassLoaderReference.Primordial, "Ljava/util/Set");

        Set<MethodFlowModel> mList = new HashSet<>();

        for(IClass c : SetFlowModel.cha.getImplementors(ref)){
            for(IMethod m : c.getAllMethods()){

                Selector s = m.getSelector();
                if(s.equals(selectors[0])){ // add(Ljava/lang/Object;)B
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, CollectionFlowModel.COLLECTION_GET));
                }else if(s.equals(selectors[1])){ // addAll(Ljava/util/Collection;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}));
                }else if(s.equals(selectors[2])){ // clear()V
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[3])){ // contains(Ljava/lang/Object;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[4])){ // containsAll(Ljava/util/Collection;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[5])){ // equals(Ljava/lang/Object;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[6])){ // hashCode()I
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RECEIVERV}));
                }else if(s.equals(selectors[7])){ // isEmpty()B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[8])){ // iterator()Ljava/util/Iterator;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + CollectionFlowModel.COLLECTION_GET + "." + IteratorFlowModel.ITER_NEXT));
                }else if(s.equals(selectors[9])){ // remove(Ljava/lang/Object;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[10])){ // removeAll(Ljava/util/Collection;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[11])){ // retainAll(Ljava/util/Collection;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[12])){ // size()I
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[13])){ // toArray()[Ljava/lang/Object;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + CollectionFlowModel.COLLECTION_GET + "." + "["));
                }else if(s.equals(selectors[14])){ // toArray([Ljava/lang/Object;)[Ljava/lang/Object;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + CollectionFlowModel.COLLECTION_GET + "." + "["));
                }
            }
        }




        methods = mList.toArray(new MethodFlowModel[0]);
    }
}
