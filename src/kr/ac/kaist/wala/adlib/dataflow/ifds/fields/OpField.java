package kr.ac.kaist.wala.adlib.dataflow.ifds.fields;

/**
 * Created by leesh on 27/02/2018.
 */
public class OpField implements Field {
    public enum Operator {
        PLUS("+"),
        STAR("-");

        private String name;

        Operator(String name){
            this.name = name;
        }

        @Override
        public String toString(){
            return name;
        }
    }

    private final OpField.Operator op;
    private final FieldSeq f;

    public OpField(OpField.Operator op, FieldSeq f){
        this.op = op;
        this.f = f;
    }

    public Operator getOp(){
        return this.op;
    }

    public FieldSeq getField(){
        return this.f;
    }

    @Override
    public int hashCode(){
        return op.hashCode() + f.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof OpField){
            OpField of = (OpField) o;
            if(of.op.equals(this.op) && of.f.equals(this.f))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "( " + f + " )" + op;
    }
}
