package kr.ac.kaist.wala.adlib.dataflow.ifds.fields;

import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.debug.Assertions;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by leesh on 22/03/2018.
 */
public class SingleField implements Field {
    private static Map<String, SingleField> cache = HashMapFactory.make();

    public static SingleField make(String f){
        if(!cache.containsKey(f))
            cache.put(f, new SingleField(f));
        return cache.get(f);
    }

    private String f;

    private SingleField(String f){
        this.f = f;
    }

    @Override
    public int length() {
        return 1;
    }

    @Override
    public boolean isArrayType() {
        return f.equals("[");
    }

    @Override
    public List<String> toSimpleList() {
        return Collections.singletonList(f);
    }

    @Override
    public boolean isMatched(String f) {
        return this.f.equals(f);
    }

    @Override
    public Set<Field> pop(String f) {
        if(isMatched(f))
            return Collections.singleton(NoneField.getInstance());
        Assertions.UNREACHABLE("The field f is not matched with this Single Field. f:" + f + "\tsf: " + this);
        return null;
    }

    @Override
    public boolean isMatched(Field f) {
        return this.equals(f);
    }

    @Override
    public int hashCode(){
        return f.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof SingleField){
            SingleField s = (SingleField) o;
            if(s.f.equals(this.f))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return f;
    }
}
