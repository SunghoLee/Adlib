package kr.ac.kaist.wala.adlib.dataflow.ifds.fields;

import java.util.List;
import java.util.Set;

/**
 * Created by leesh on 27/02/2018.
 */
public interface Field {
    @Override
    public String toString();
    public boolean isMatched(String f);
    public boolean isMatched(Field f);
    public Set<Field> pop(String f);
    public List<String> toSimpleList();
    public int length();
}
