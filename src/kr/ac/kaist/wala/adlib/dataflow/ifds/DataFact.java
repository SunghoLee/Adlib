package kr.ac.kaist.wala.adlib.dataflow.ifds;

import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;

/**
 * Created by leesh on 27/02/2018.
 */
public interface DataFact {
    public final static DataFact DEFAULT_FACT = new DefaultDataFact();

    public Field getField();

    public boolean accept(IDataFactFilter filter);
}
