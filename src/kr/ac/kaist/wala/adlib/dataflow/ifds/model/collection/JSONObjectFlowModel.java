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
public class JSONObjectFlowModel extends ClassFlowModel{

    public static String JSON_OBJ_GET = "@JOGET";

    // for single-ton object
    private static JSONObjectFlowModel instance;
    private static IClassHierarchy cha;

    private static final Selector[] selectors = {
            Selector.make("<init>(Ljava/util/Map;)V"), // 0
            Selector.make("<init>(Lorg/json/JSONTokener;)V"), // 1
            Selector.make("<init>(Ljava/lang/String;)V"), // 2
            Selector.make("<init>(Lorg/json/JSONObject;[Ljava/lang/String;)V"), // 3
            Selector.make("accumulate(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;"), // 4
            Selector.make("get(Ljava/lang/String;)Ljava/lang/Object;"), // 5
            Selector.make("getBoolean(Ljava/lang/String;)Z"), // 6
            Selector.make("getDouble(Ljava/lang/String;)D"), // 7
            Selector.make("getInt(Ljava/lang/String;)I"), // 8
            Selector.make("getJSONArray(Ljava/lang/String;)Lorg/json/JSONArray;"), // 9
            Selector.make("getJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;"), // 10
            Selector.make("getLong(Ljava/lang/String;)J"), // 11
            Selector.make("getString(Ljava/lang/String;)Ljava/lang/String;"), // 12
            Selector.make("has(Ljava/lang/String;)Z"), // 13
            Selector.make("isNull(Ljava/lang/String;)Z"), // 14
            Selector.make("keys()Ljava/util/Iterator;"), // 15
            Selector.make("length()I"), // 16
            Selector.make("names()Lorg/json/JSONArray;"), // 17
            Selector.make("numberToString(Ljava/lang/Number;)Ljava/lang/String;"), // 18
            Selector.make("opt(Ljava/lang/String;)Ljava/lang/Object;"), // 19
            Selector.make("optBoolean(Ljava/lang/String;Z)Z"), // 20
            Selector.make("optBoolean(Ljava/lang/String;)Z"), // 21
            Selector.make("optDouble(Ljava/lang/String;D)D"), // 22
            Selector.make("optDouble(Ljava/lang/String;)D"), // 23
            Selector.make("optInt(Ljava/lang/String;I)I"), // 24
            Selector.make("optInt(Ljava/lang/String;)I"), // 25
            Selector.make("optJSONArray(Ljava/lang/String;)Lorg/json/JSONArray;"), // 26
            Selector.make("optJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;"), // 27
            Selector.make("optLong(Ljava/lang/String;J)J"), // 28
            Selector.make("optLong(Ljava/lang/String;)J"), // 29
            Selector.make("optString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"), // 30
            Selector.make("optString(Ljava/lang/String;)Ljava/lang/String;"), // 31
            Selector.make("put(Ljava/lang/String;D)Lorg/json/JSONObject;"), // 32
            Selector.make("put(Ljava/lang/String;Z)Lorg/json/JSONObject;"), // 33
            Selector.make("put(Ljava/lang/String;I)Lorg/json/JSONObject;"), // 34
            Selector.make("put(Ljava/lang/String;J)Lorg/json/JSONObject;"), // 35
            Selector.make("put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;"), // 36
            Selector.make("putOpt(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;"), // 37
            Selector.make("quote(Ljava/lang/String;)Ljava/lang/String;"), // 38
            Selector.make("remove(Ljava/lang/String;)Ljava/lang/Object;"), // 39
            Selector.make("toJSONArray(Lorg/json/JSONArray;)Lorg/json/JSONArray;"), // 40
            Selector.make("toString()Ljava/lang/String;"), // 41
            Selector.make("wrap(Ljava/lang/Object;)Ljava/lang/Object;"), // 42

    };

    public static JSONObjectFlowModel getInstance(IClassHierarchy cha){
        if(cha == null)
            Assertions.UNREACHABLE("Class hierarchy must be not null!");

        if(instance == null) {
            JSONObjectFlowModel.cha = cha;
            instance = new JSONObjectFlowModel();
        }

        return instance;
    }

    private JSONObjectFlowModel(){

    }

    @Override
    protected void init() {
        ref = TypeReference.find(ClassLoaderReference.Primordial, "Lorg/json/JSONObject");

        Set<MethodFlowModel> mList = new HashSet<>();

        IClass c = JSONObjectFlowModel.cha.lookupClass(ref);
        for(IMethod m : c.getAllMethods()){

            Selector s = m.getSelector();
            if(s.equals(selectors[0])){ // <init>(Ljava/util/Map;)V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, "-" + MapFlowModel.MAP_GET + "." + JSON_OBJ_GET));
            }else if(s.equals(selectors[1])){ // <init>(Lorg/json/JSONTokener;)V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, "-" + JSONTokenerFlowModel.JSON_TOKEN_NEXT + "." + JSON_OBJ_GET));
            }else if(s.equals(selectors[2])){ // <init>(Ljava/lang/String;)V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, JSON_OBJ_GET));
            }else if(s.equals(selectors[3])){ // <init>(Lorg/json/JSONObject;[Ljava/lang/String;)V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, JSON_OBJ_GET));
            }else if(s.equals(selectors[4])){ // Selector.make("accumulate(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;"), // 4
                mList.add(MethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RECEIVERV, MethodFlowModel.RETV}));
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV, MethodFlowModel.RETV}, JSON_OBJ_GET));
            }else if(s.equals(selectors[5])){ // get(Ljava/lang/String;)Ljava/lang/Object;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[6])){ // getBoolean(Ljava/lang/String;)Z
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[7])){ // getDouble(Ljava/lang/String;)D
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[8])){ // getInt(Ljava/lang/String;)I
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[9])){ // getJSONArray(Ljava/lang/String;)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET + "." +JSONArrayFlowModel.JSON_ARRAY_GET));
            }else if(s.equals(selectors[10])){ // getJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[11])){ // getLong(Ljava/lang/String;)S
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[12])){ // getString(Ljava/lang/String;)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[13])){ // has(Ljava/lang/String;)Z
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[14])){ // isNull(Ljava/lang/String;)Z
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[15])){ // keys()Ljava/util/Iterator;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET + "." + IteratorFlowModel.ITER_NEXT));
            }else if(s.equals(selectors[16])){ // length()I
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[17])){ // names()Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET + "." + JSONArrayFlowModel.JSON_ARRAY_GET));
            }else if(s.equals(selectors[18])){ // numberToString(Ljava/lang/Number;)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}));
            }else if(s.equals(selectors[19])){ // opt(Ljava/lang/String;)Ljava/lang/Object;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[20])){ // optBoolean(Ljava/lang/String;Z)Z
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[21])){ // optBoolean(Ljava/lang/String;)Z
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[22])){ // optDouble(Ljava/lang/String;D)D
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[23])){ // optDouble(Ljava/lang/String;)D
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[24])){ // optInt(Ljava/lang/String;I)I
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[25])){ // optInt(Ljava/lang/String;)I
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[26])){ // optJSONArray(Ljava/lang/String;)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[27])){ // optJSONObject(Ljava/lang/String;)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[28])){ // optLong(Ljava/lang/String;S)S
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[29])){ // optLong(Ljava/lang/String;)S
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[30])){ // optString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[31])){ // optString(Ljava/lang/String;)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[32])){ // put(Ljava/lang/String;D)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_OBJ_GET));
            }else if(s.equals(selectors[33])){ // put(Ljava/lang/String;Z)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_OBJ_GET));
            }else if(s.equals(selectors[34])){ // put(Ljava/lang/String;I)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_OBJ_GET));
            }else if(s.equals(selectors[35])){ // put(Ljava/lang/String;S)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_OBJ_GET));
            }else if(s.equals(selectors[36])){ // put(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_OBJ_GET));
            }else if(s.equals(selectors[37])){ // putOpt(Ljava/lang/String;Ljava/lang/Object;)Lorg/json/JSONObject;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{2}, new int[]{MethodFlowModel.RECEIVERV,MethodFlowModel.RETV}, JSON_OBJ_GET));
            }else if(s.equals(selectors[38])){ // quote(Ljava/lang/String;)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}));
            }else if(s.equals(selectors[39])){ // remove(Ljava/lang/String;)Ljava/lang/Object;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[40])){ // toJSONArray(Lorg/json/JSONArray;)Lorg/json/JSONArray;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET + "." + JSONArrayFlowModel.JSON_ARRAY_GET));
            }else if(s.equals(selectors[41])){ // toString()Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_OBJ_GET));
            }else if(s.equals(selectors[42])){ // wrap(Ljava/lang/Object;)Ljava/lang/Object;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}));
            }
        }

        methods = mList.toArray(new MethodFlowModel[0]);
    }
}
