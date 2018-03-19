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
public class ListFlowModel extends ClassFlowModel{
    // for single-ton object
    private static ListFlowModel instance;
    private static IClassHierarchy cha;

    private static final Selector[] selectors = {
        Selector.make("add(Ljava/lang/Object;)B"), // 0
            Selector.make("add(ILjava/lang/Object;)V"), // 1
            Selector.make("addAll(Ljava/util/Collection;)B"), // 2
            Selector.make("clear()V"), // 3
            Selector.make("contains(Ljava/lang/Object;)B"),  // 4
            Selector.make("containsAll(Ljava/util/Collection;)B"), // 5
            Selector.make("equals(Ljava/lang/Object;)B"), // 6
            Selector.make("get(I)Ljava/lang/Object;"), // 7
            Selector.make("hashCode()I"), // 8
            Selector.make("indexOf(Ljava/lang/Object;)I"), // 9
            Selector.make("isEmpty()B"), // 10
            Selector.make("iterator()Ljava/util/Iterator;"), // 11
            Selector.make("remove(I)Ljava/lang/Object;"), // 12
            Selector.make("remove(Ljava/lang/Object;)B"), // 13
            Selector.make("removeAll(Ljava/util/Collection;)B"), // 14
            Selector.make("set(ILjava/lang/Object;)Ljava/lang/Object;"), // 15
            Selector.make("size()I"), // 16
            Selector.make("sort(Ljava/util/Comparator;)V"), // 17
            Selector.make("spliterator()Ljava/util/Spliterator;"), // 18
            Selector.make("subList(II)Ljava/util/List;"), // 19
            Selector.make("toArray()[Ljava/lang/Object;"), // 20
            Selector.make("toArray([Ljava/lang/Object;)[Ljava/lang/Object;"), // 21
    };

    public static ListFlowModel getInstance(IClassHierarchy cha){
        if(cha == null)
            Assertions.UNREACHABLE("Class hierarchy must be not null!");

        if(instance == null) {
            ListFlowModel.cha = cha;
            instance = new ListFlowModel();
        }

        return instance;
    }

    private ListFlowModel(){

    }

    @Override
    protected void init() {
        ref = TypeReference.find(ClassLoaderReference.Primordial, "Ljava/util/List");

        Set<MethodFlowModel> mList = new HashSet<>();

        for(IClass c : ListFlowModel.cha.getImplementors(ref)){
            for(IMethod m : c.getAllMethods()){

                Selector s = m.getSelector();
                if(s.equals(selectors[0])){ // add(Ljava/lang/Object;)B
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, CollectionFlowModel.COLLECTION_GET));
                }else if(s.equals(selectors[1])){ // add(ILjava/lang/Object;)V
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV}, CollectionFlowModel.COLLECTION_GET));
                }else if(s.equals(selectors[2])){ // addAll(Ljava/util/Collection;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}));
                }else if(s.equals(selectors[3])){ // clear()V
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[4])){ // contains(Ljava/lang/Object;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[5])){ // containsAll(Ljava/util/Collection;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[6])){ // equals(Ljava/lang/Object;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[7])){ // get(I)Ljava/lang/Object;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + CollectionFlowModel.COLLECTION_GET));
                }else if(s.equals(selectors[8])){ // hashCode()I
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RECEIVERV}));
                }else if(s.equals(selectors[9])){ // indexOf(Ljava/lang/Object;)I
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[10])){ // isEmpty()B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[11])){ // iterator()Ljava/util/Iterator;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + CollectionFlowModel.COLLECTION_GET + "." + IteratorFlowModel.ITER_NEXT));
                }else if(s.equals(selectors[12])){ // remove(I)Ljava/lang/Object;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + CollectionFlowModel.COLLECTION_GET));
                }else if(s.equals(selectors[13])){ // remove(Ljava/lang/Object;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[14])){ // removeAll(Ljava/util/Collection;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[15])){ // set(ILjava/lang/Object;)Ljava/lang/Object;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RETV}, CollectionFlowModel.COLLECTION_GET));
                }else if(s.equals(selectors[16])){ // size()I
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[17])){ // sort(Ljava/util/Comparator;)V
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[18])){ // spliterator()Ljava/util/Spliterator;
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{})); // TODO: implement this!
                }else if(s.equals(selectors[19])){ // subList(II)Ljava/util/List;
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}));
                }else if(s.equals(selectors[20])){ // toArray()[Ljava/lang/Object;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + CollectionFlowModel.COLLECTION_GET + "." + "["));
                }else if(s.equals(selectors[21])){ // toArray([Ljava/lang/Object;)[Ljava/lang/Object;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + CollectionFlowModel.COLLECTION_GET + "." + "["));
                }


                //toArray([Ljava/lang/Object;)[Ljava/lang/Object;
            }
        }




        methods = mList.toArray(new MethodFlowModel[0]);
    }
}
