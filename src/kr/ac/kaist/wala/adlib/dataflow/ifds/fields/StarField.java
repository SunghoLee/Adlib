package kr.ac.kaist.wala.adlib.dataflow.ifds.fields;

import com.ibm.wala.util.debug.Assertions;

import java.util.*;

/**
 * Created by leesh on 27/02/2018.
 */
public class StarField implements Field {

    public static StarField make(Field f){
        if(f instanceof NoneField)
            Assertions.UNREACHABLE("Cannot make a regular expression for NoneField. sf:" + f + "\tf: " + f);
        else if(f instanceof StarField)
            Assertions.UNREACHABLE("Cannot duplicate regular expressions over twice directly. sf:" + f + "\tf: " + f);

        return new StarField(f);
    }

    private final Field f;

    private StarField(Field f){
        this.f = f;
    }

    @Override
    public boolean isMatched(String field){
        return isMatched(SingleField.make(field));
    }

    @Override
    public Set<Field> pop(String field) {
        if(isMatched(field)){
            if(f instanceof SingleField){
                SingleField sf = (SingleField) f;
                return Collections.singleton(this);
            }else if(f instanceof FieldSeq){
                Set<Field> res = new HashSet<>();

                for(Field nf : f.pop(field)){
                    res.add(FieldSeq.make(nf, this));
                }
                return res;
            }else
                Assertions.UNREACHABLE("The Star field only can have Single or Seq. sf:" + this);
        }
        Assertions.UNREACHABLE("The field f is not matched with this Star field. f: " + f + "\tsf:" + this);
        return null;
    }

    @Override
    public int length() {
        return f.length();
    }

    @Override
    public List<String> toSimpleList() {
        List<String> l = new ArrayList<>();
        l.add("*");
        l.addAll(f.toSimpleList());
        l.add("*");
        return l;
    }

    @Override
    public boolean isMatched(Field field){
        return f.isMatched(field);
    }

    public Field getField(){
        return this.f;
    }

    @Override
    public int hashCode(){
        return f.hashCode() + 7;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof StarField){
            StarField of = (StarField) o;
            if(of.f.equals(this.f))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "( " + f + " )*";
    }
}
