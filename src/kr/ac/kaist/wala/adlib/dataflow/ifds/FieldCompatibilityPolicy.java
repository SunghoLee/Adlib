package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.TypeReference;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.*;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.collection.*;

/**
 * Created by leesh on 09/04/2018.
 */
public class FieldCompatibilityPolicy {
    private final IClassHierarchy cha;
    private final IClass collection;
    private final IClass iter;
    private final IClass jarray;
    private final IClass jtoken;
    private final IClass jobj;
    private final IClass map;

    public FieldCompatibilityPolicy(IClassHierarchy cha){
        this.cha = cha;
        this.collection = cha.lookupClass(CollectionFlowModel.getInstance(cha).getReference());
        this.iter = cha.lookupClass(IteratorFlowModel.getInstance(cha).getReference());
        this.jarray = cha.lookupClass(JSONArrayFlowModel.getInstance(cha).getReference());
        this.jtoken = cha.lookupClass(JSONTokenerFlowModel.getInstance(cha).getReference());
        this.jobj = cha.lookupClass(JSONObjectFlowModel.getInstance(cha).getReference());
        this.map = cha.lookupClass(MapFlowModel.getInstance(cha).getReference());
    }

    public boolean isCompatible(Field field, TypeReference tr){
        if(field.equals(NoneField.getInstance()) || field.equals(TopField.getInstance()))
            return true;
        IClass c = cha.lookupClass(tr);

        // handle array field
        if(tr.isArrayType()) {
            if (field instanceof FieldSeq) {
                FieldSeq fs = (FieldSeq) field;
                Field fst = fs.getFirst();

                if (fst instanceof SingleField) {
                    SingleField sf = (SingleField) fst;
                    if(sf.toString().equals("["))
                        return true;
                } else if (fst instanceof StarField) {
                    StarField stf = (StarField) fst;
                    if(stf.isMatched("["))
                            return true;
                }
            }
        }

        // handle special field for modeling collections
        if(c.equals(collection) || cha.implementsInterface(c, collection)){
            if(field.isMatched(CollectionFlowModel.COLLECTION_GET))
                return true;
        }else if(c.equals(iter) || cha.implementsInterface(c, iter)){
            if(field.isMatched(IteratorFlowModel.ITER_NEXT))
                return true;
        }else if(c.equals(jarray) || cha.isSubclassOf(c, jarray)){
            if(field.isMatched(JSONArrayFlowModel.JSON_ARRAY_GET))
                return true;
        }else if(c.equals(map) || cha.implementsInterface(c, map)){
            if(field.isMatched(MapFlowModel.MAP_GET))
                return true;
        }else if(c.equals(jtoken) || cha.isSubclassOf(c, jtoken)){
            if(field.isMatched(JSONTokenerFlowModel.JSON_TOKEN_NEXT))
                return true;
        }else if(c.equals(jobj) || cha.isSubclassOf(c, jobj)){
            if(field.isMatched(JSONObjectFlowModel.JSON_OBJ_GET))
                return true;
        }

        boolean isInterface = false;

        // for the class corresponding to tr.
        if(!tr.isArrayType()) {
            if(!(isInterface = c.isInterface())) {
                for (IField f : c.getAllInstanceFields()) {
                    if (field.isMatched(f.getName().toString())) {
                        return true;
                    }
                }
            }
        }

        // for all sub classes of tr.
        if(isInterface){
            for (IClass sc : cha.getImplementors(tr)) {
                if (!sc.isArrayClass()) {
                    for (IField f : sc.getDeclaredInstanceFields()) {
                        if (field.isMatched(f.getName().toString())) {
                            return true;
                        }
                    }
                }
            }
        }else {
            for (IClass sc : cha.computeSubClasses(tr)) {
                if (!sc.isArrayClass()) {
                    for (IField f : sc.getDeclaredInstanceFields()) {
                        if (field.isMatched(f.getName().toString())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}
