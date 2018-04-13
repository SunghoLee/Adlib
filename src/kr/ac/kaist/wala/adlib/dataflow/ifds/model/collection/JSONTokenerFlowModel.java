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
public class JSONTokenerFlowModel extends ClassFlowModel{

    public static String JSON_TOKEN_NEXT = "@JNEXT";

    // for single-ton object
    private static JSONTokenerFlowModel instance;
    private static IClassHierarchy cha;

    private static final Selector[] selectors = {
            Selector.make("<init>(Ljava/lang/String;)V"), // 0
            Selector.make("back()V"), // 1
            Selector.make("dehexchar(C)I"), // 2
            Selector.make("more()Z"), // 3
            Selector.make("next()C"), // 4
            Selector.make("next(C)C"), // 5
            Selector.make("next(I)Ljava/lang/String;"), // 6
            Selector.make("nextClean()C"), // 7
            Selector.make("nextString(C)Ljava/lang/String;"), // 8
            Selector.make("nextTo(Ljava/lang/String;)Ljava/lang/String;"), // 9
            Selector.make("nextTo(C)Ljava/lang/String;"), // 10
            Selector.make("nextValue()Ljava/lang/Object;"), // 11
            Selector.make("skipPast(Ljava/lang/String;)V"), // 12
            Selector.make("syntaxError(Ljava/lang/String;)Lorg/json/JSONException;"), // 13
            Selector.make("toString()Ljava/lang/String;"), // 14
    };

    public static JSONTokenerFlowModel getInstance(IClassHierarchy cha){
        if(cha == null)
            Assertions.UNREACHABLE("Class hierarchy must be not null!");

        if(instance == null) {
            JSONTokenerFlowModel.cha = cha;
            instance = new JSONTokenerFlowModel();
        }

        return instance;
    }

    private JSONTokenerFlowModel(){

    }

    @Override
    protected void init() {
        ref = TypeReference.find(ClassLoaderReference.Primordial, "Lorg/json/JSONTokener");

        Set<MethodFlowModel> mList = new HashSet<>();

        IClass c = JSONTokenerFlowModel.cha.lookupClass(ref);
        for(IMethod m : c.getAllMethods()){

            Selector s = m.getSelector();
            if(s.equals(selectors[0])){ // <init>(Ljava/lang/String;)V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}, JSON_TOKEN_NEXT));
            }else if(s.equals(selectors[1])){ // back()V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[2])){ // dehexchar(C)I
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_TOKEN_NEXT));
            }else if(s.equals(selectors[3])){ // more()Z
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[4])){ // next()C
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_TOKEN_NEXT));
            }else if(s.equals(selectors[5])){ // next(C)C
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_TOKEN_NEXT));
            }else if(s.equals(selectors[6])){ // next(I)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_TOKEN_NEXT));
            }else if(s.equals(selectors[7])){ // nextClean()C
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_TOKEN_NEXT));
            }else if(s.equals(selectors[8])){ // nextString(C)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_TOKEN_NEXT));
            }else if(s.equals(selectors[9])){ // nextTo(Ljava/lang/String;)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_TOKEN_NEXT));
            }else if(s.equals(selectors[10])){ // nextTo(C)Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_TOKEN_NEXT));
            }else if(s.equals(selectors[11])){ // nextValue()Ljava/lang/Object;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-" + JSON_TOKEN_NEXT));
            }else if(s.equals(selectors[12])){ // skipPast(Ljava/lang/String;)V
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[13])){ // syntaxError(Ljava/lang/String;)Lorg/json/JSONException;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{}));
            }else if(s.equals(selectors[14])){ // toString()Ljava/lang/String;
                mList.add(FineGrainedMethodFlowModel.make(MethodReference.findOrCreate(c.getReference(), s), new int[]{0}, new int[]{MethodFlowModel.RETV}, "-*"));
            }
        }

        methods = mList.toArray(new MethodFlowModel[0]);
    }
}
