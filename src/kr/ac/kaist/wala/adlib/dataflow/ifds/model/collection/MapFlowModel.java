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
public class MapFlowModel extends ClassFlowModel{

    public static String MAP_GET = "@MGET";

    // for single-ton object
    private static MapFlowModel instance;
    private static IClassHierarchy cha;

    private static final Selector[] selectors = {
            Selector.make("entrySet()Ljava/util/Set;"), // 0
            Selector.make("get(Ljava/lang/Object;)Ljava/lang/Object;"), // 1
            Selector.make("keySet()Ljava/util/Set;"), // 2
            Selector.make("put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"), // 3
            Selector.make("putAll(Ljava/util/Map;)V"),  // 4
            Selector.make("values()Ljava/util/Collection;"), // 5
            Selector.make("clear()V"), // 6
            Selector.make("containsKey(Ljava/lang/Object;)B"), // 7
            Selector.make("containsValue(Ljava/lang/Object;)B"), // 8
            Selector.make("hashCode()I"), // 9
            Selector.make("isEmpty()B"), // 10
            Selector.make("remove(Ljava/lang/Object;)Ljava/lang/Object;"), // 11
            Selector.make("size()I"), // 12
    };

    public static MapFlowModel getInstance(IClassHierarchy cha){
        if(cha == null)
            Assertions.UNREACHABLE("Class hierarchy must be not null!");

        if(instance == null) {
            MapFlowModel.cha = cha;
            instance = new MapFlowModel();
        }

        return instance;
    }

    private MapFlowModel(){

    }

    @Override
    protected void init() {
        ref = TypeReference.find(ClassLoaderReference.Primordial, "Ljava/util/Map");

        Set<MethodFlowModel> mList = new HashSet<>();

        for(IClass c : MapFlowModel.cha.getImplementors(ref)){
            for(IMethod m : c.getAllMethods()){

                Selector s = m.getSelector();
                if(s.equals(selectors[0])){ // entrySet()Ljava/util/Set;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + MapFlowModel.MAP_GET + "." + CollectionFlowModel.COLLECTION_GET));
                }else if(s.equals(selectors[1])){ // get(Ljava/lang/Object;)Ljava/lang/Object;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + MapFlowModel.MAP_GET));
                }else if(s.equals(selectors[2])){ // keySet()Ljava/util/Set;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + MapFlowModel.MAP_GET + "." + CollectionFlowModel.COLLECTION_GET));
                }else if(s.equals(selectors[3])){ // put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1, 2}, new int[]{MethodFlowModel.RECEIVERV}, MapFlowModel.MAP_GET));
                }else if(s.equals(selectors[4])){ // putAll(Ljava/util/Map;)V
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}));
                }else if(s.equals(selectors[5])){ // values()Ljava/util/Collection;
                    mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + MapFlowModel.MAP_GET + "." + CollectionFlowModel.COLLECTION_GET));
                }else if(s.equals(selectors[6])){ // clear()V
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[7])){ // containsKey(Ljava/lang/Object;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[8])){ // containsValue(Ljava/lang/Object;)B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[9])){ // hashCode()I
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[10])){ // isEmpty()B
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[11])){ // remove(Ljava/lang/Object;)Ljava/lang/Object;
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }else if(s.equals(selectors[12])){ // size()I
                    mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
                }
            }
        }




        methods = mList.toArray(new MethodFlowModel[0]);
    }
}
