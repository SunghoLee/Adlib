package kr.ac.kaist.wala.adlib.dataflow.ifds.fields;

import com.ibm.wala.util.debug.Assertions;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    @Override
    public boolean isMatched(String f){
        return false;
    }

    @Override
    public List<String> toSimpleList() {
        return Collections.emptyList();
    }

    @Override
    public int length() {
        return 0;
    }

    @Override
    public boolean isMatched(Field f){
        return false;
    }

    private NoneField(){}

    @Override
    public Set<Field> pop(String f) {
        Assertions.UNREACHABLE("Cannot pop any field from a NoneField: " + f);
        return null;
    }

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
