package kr.ac.kaist.wala.adlib.dataflow.ifds;

import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.NoneField;

/**
 * Created by leesh on 22/02/2018.
 */
public class DefaultDataFact implements DataFact {

    @Override
    public Field getField() {
        return NoneField.getInstance();
    }

    @Override
    public boolean accept(IDataFactFilter filter) {
        return filter.accept(this);
    }

    @Override
    public String toString(){
        return "0";
    }
}
