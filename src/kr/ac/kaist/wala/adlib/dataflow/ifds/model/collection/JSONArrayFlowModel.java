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
public class JSONArrayFlowModel extends ClassFlowModel{

    public static String JSON_ARRAY_GET = "@JAGET";

    // for single-ton object
    private static JSONArrayFlowModel instance;
    private static IClassHierarchy cha;

    private static final Selector[] selectors = {
            Selector.make("<init>(Ljava/util/Collection;)V"), // 0
            Selector.make("<init>(Lorg/json/JSONTokener;)V"), // 1
            Selector.make("<init>(Ljava/lang/String;)V"), // 2
            Selector.make("<init>(Lorg/json/JSONObject;)V"), // 3
            Selector.make("equals(Ljava/lang/Object;)Z"), // 4
            Selector.make("get(I)Ljava/lang/Object;"), // 5
            Selector.make("getBoolean(I)Z"), // 6
            Selector.make("getDouble(I)D"), // 7
            Selector.make("getInt(I)I"), // 8
            Selector.make("getJSONArray(I)Lorg/json/JSONArray;"), // 9
            Selector.make("getJSONObject(I)Lorg/json/JSONObject;"), // 10
            Selector.make("getLong(I)J"), // 11
            Selector.make("getString(I)Ljava/lang/String;"), // 12
            Selector.make("hashCode()I"), // 13
            Selector.make("isNull(I)Z"), // 14
            Selector.make("join(Ljava/lang/String;)Ljava/lang/String;"), // 15
            Selector.make("length()I"), // 16
            Selector.make("opt(I)Ljava/lang/Object;"), // 17
            Selector.make("optBoolean(IZ)Z"), // 18
            Selector.make("optBoolean(I)Z"), // 19
            Selector.make("optDouble(ID)D"), // 20
            Selector.make("optDouble(I)D"), // 21
            Selector.make("optInt(II)I"), // 22
            Selector.make("optInt(I)I"), // 23
            Selector.make("optJSONArray(I)Lorg/json/JSONArray;"), // 24
            Selector.make("optJSONObject(I)Lorg/json/JSONObject;"), // 25
            Selector.make("optLong(IJ)J"), // 26
            Selector.make("optLong(I)J"), // 27
            Selector.make("optString(ILjava/lang/String;)Ljava/lang/String;"), // 28
            Selector.make("optString(I)Ljava/lang/String;"), // 29
            Selector.make("put(I)Lorg/json/JSONArray;"), // 30
            Selector.make("put(J)Lorg/json/JSONArray;"), // 31
            Selector.make("put(IZ)Lorg/json/JSONArray;"), // 32
            Selector.make("put(D)Lorg/json/JSONArray;"), // 33
            Selector.make("put(ILjava/lang/Object;)Lorg/json/JSONArray;"), // 34
            Selector.make("put(IJ)Lorg/json/JSONArray;"), // 35
            Selector.make("put(II)Lorg/json/JSONArray;"), // 36
            Selector.make("put(Z)Lorg/json/JSONArray;"), // 37
            Selector.make("put(ID)Lorg/json/JSONArray;"), // 38
            Selector.make("put(IJ)Lorg/json/JSONArray;"), // 39
            Selector.make("put(Ljava/lang/Object;)Lorg/json/JSONArray;"), // 40
            Selector.make("remove(I)Ljava/lang/Object;"), // 41
            Selector.make("toJSONObject(Lorg/json/JSONArray;)Lorg/json/JSONObject;"), // 42
            Selector.make("toString()Ljava/lang/String;"), // 43
            Selector.make("toString(I)Ljava/lang/String;"), // 44

    };

    public static JSONArrayFlowModel getInstance(IClassHierarchy cha){
        if(cha == null)
            Assertions.UNREACHABLE("Class hierarchy must be not null!");

        if(instance == null) {
            JSONArrayFlowModel.cha = cha;
            instance = new JSONArrayFlowModel();
        }

        return instance;
    }

    private JSONArrayFlowModel(){

    }

    @Override
    protected void init() {
        ref = TypeReference.find(ClassLoaderReference.Primordial, "Lorg/json/JSONArray");

        Set<MethodFlowModel> mList = new HashSet<>();

        IClass c = JSONArrayFlowModel.cha.lookupClass(ref);
        for(IMethod m : c.getAllMethods()){

            Selector s = m.getSelector();
            if(s.equals(selectors[0])){ // <init>(Ljava/util/Collection;)V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, "-" + CollectionFlowModel.COLLECTION_GET + "." + JSON_ARRAY_GET));
            }else if(s.equals(selectors[1])){ // <init>(Lorg/json/JSONTokener;)V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, "-" + JSONTokenerFlowModel.JSON_TOKEN_NEXT + "." + JSON_ARRAY_GET));
            }else if(s.equals(selectors[2])){ // <init>(Ljava/lang/String;)V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[3])){ // <init>(Lorg/json/JSONObject;)V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[4])){ // equals(Ljava/lang/Object;)Z
                mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[5])){ // get(I)Ljava/lang/Object;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[6])){ // getBoolean(I)Z
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[7])){ // getDouble(I)D
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[8])){ // getInt(I)I
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[9])){ // getJSONArray(I)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[10])){ // getJSONObject(I)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[11])){ // getLong(I)S
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[12])){ // getString(I)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[13])){ // hashCode()I
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[14])){ // isNull(I)Z
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[15])){ // join(Ljava/lang/String;)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[16])){ // length()I
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[17])){ // opt(I)Ljava/lang/Object;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[18])){ // optBoolean(IZ)Z
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[19])){ // optBoolean(I)Z
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[20])){ // optDouble(ID)D
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[21])){ // optDouble(I)D
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[22])){ // optInt(II)I
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[23])){ // optInt(I)I
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[24])){ // optJSONArray(I)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[25])){ // optJSONObject(I)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[26])){ // optLong(IS)S
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[27])){ // optLong(I)S
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[28])){ // optString(ILjava/lang/String;)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[29])){ // optString(I)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[30])){ // put(I)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV, MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[31])){ // put(S)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV, MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[32])){ // put(IZ)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[33])){ // put(D)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[34])){ // put(ILjava/lang/Object;)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[35])){ // put(IS)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[36])){ // put(II)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[37])){ // put(Z)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[38])){ // put(ID)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[39])){ // put(IS)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[40])){ // put(Ljava/lang/Object;)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_ARRAY_GET));
            }else if(s.equals(selectors[41])){ // remove(I)Ljava/lang/Object;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[42])){ // toJSONObject(Lorg/json/JSONArray;)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET + "." + JSONObjectFlowModel.JSON_OBJ_GET));
            }else if(s.equals(selectors[43])){ // toString()Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }else if(s.equals(selectors[44])){ // toString(I)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_ARRAY_GET));
            }
        }

        methods = mList.toArray(new MethodFlowModel[0]);
    }
}
