package kr.ac.kaist.wala.adlib.dataflow.ifds.fields;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by leesh on 22/03/2018.
 */
public class TopField implements Field {
    private static TopField instance;

    public static Field getInstance(){
        if(instance == null)
            instance = new TopField();
        return instance;
    }
    private TopField(){}

    @Override
    public boolean isMatched(String f) {
        return true;
    }

    @Override
    public boolean isMatched(Field f) {
        return true;
    }

    @Override
    public Set<Field> pop(String f) {
        return Collections.singleton(this);
    }

    @Override
    public List<String> toSimpleList() {
        return Collections.emptyList();
    }

    @Override
    public int length() {
        return 9999;
    }

    @Override
    public boolean isArrayType() {
        return true;
    }

    @Override
    public int hashCode(){
        return 124923;
    }

    @Override
    public String toString(){
        return "TOP";
    }
}
