package kr.ac.kaist.wala.adlib.dataflow.ifds.fields;

/**
 * Created by leesh on 27/02/2018.
 */
public class FieldSeq implements Field {
    private String fst;
    private Field rest;

    public FieldSeq(String fst, Field rest){
        this.fst = fst;
        this.rest = rest;
    }

    public String getFirst(){
        return this.fst;
    }

    public Field getRest(){
        return this.rest;
    }

    @Override
    public int hashCode(){
        return this.fst.hashCode() + this.rest.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof FieldSeq){
            FieldSeq fs = (FieldSeq) o;
            if(fs.fst.equals(this.fst) && fs.rest.equals(this.rest))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return fst + "." + rest;
    }
}
