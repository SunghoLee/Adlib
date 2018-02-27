package kr.ac.kaist.wala.adlib.dataflow.ifds.fields;

/**
 * Created by leesh on 27/02/2018.
 */
public class NoneField implements Field {
    private static NoneField instance;

    public static NoneField getInstance(){
        if(instance == null)
            instance = new NoneField();
        return instance;
    }

    private NoneField(){}

    @Override
    public int hashCode(){
        return 1249821;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof NoneField){
            return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "$";
    }
}
