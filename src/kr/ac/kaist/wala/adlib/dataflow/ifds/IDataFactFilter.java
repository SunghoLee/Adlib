package kr.ac.kaist.wala.adlib.dataflow.ifds;

/**
 * Created by leesh on 27/02/2018.
 */
public interface IDataFactFilter {
    public boolean accept(DefaultDataFact defaultFact);
    public boolean accept(LocalDataFact localFact);
    public boolean accept(GlobalDataFact globalFact);
}
