package kr.ac.kaist.wala.adlib.dataflow.ifds.model;

import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.FieldSeq;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.NoneField;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.SingleField;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by leesh on 16/03/2018.
 */
public final class FineGrainedMethodFlowModel extends MethodFlowModel {

    private final String field;

    // 0 is for receivers, and other positive number represents indexes of arguments.
    public static FineGrainedMethodFlowModel make(MethodReference ref, int[] from, int[] to, String field){
        for(int f : from)
            if(ref.getNumberOfParameters() < f)
                Assertions.UNREACHABLE("Unexpected flow FROM argument number: " + f + " in " + ref);
        for(int t : to)
            if(ref.getNumberOfParameters() < t)
                Assertions.UNREACHABLE("Unexpected flow FROM argument number: " + t + " in " + ref);

        return new FineGrainedMethodFlowModel(ref, from, to, field);
    }

    private FineGrainedMethodFlowModel(MethodReference ref, int[] from, int[] to, String field) {
        super(ref, from, to);
        this.field = field;
    }

    @Override
    public Set<Field> matchField(Field f){
        StringTokenizer tokenizer = new StringTokenizer(field, ".");
        Set<Field> res = new HashSet<>();

//        Field newF = f;
        res.add(f);

        while(tokenizer.hasMoreTokens()){
            String newField = tokenizer.nextToken();
            Set<Field> nexts = new HashSet<>();

            for(Field newF : res) {
                if (newField.startsWith("-")) {
                    newField = newField.substring(1);
                    if (newF.isMatched(newField)) {
                        nexts.addAll(newF.pop(newField));
                    } else if(newField.equals("*")){
                        nexts.add(NoneField.getInstance());
                    } else{
                        // no-op: this field is not possible to be matched with the model's field.
//                    Assertions.UNREACHABLE("The field is not matched with this F: \n\t @ Field: " + newF + "\n\t @ minField: " + field);
//                        return null;
                    }
                } else {
                    nexts.add(FieldSeq.make(SingleField.make(newField), newF));
                }
            }
            res.clear();;
            res.addAll(nexts);
        }

        return res;
    }
}
