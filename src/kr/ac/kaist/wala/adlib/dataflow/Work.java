package kr.ac.kaist.wala.adlib.dataflow;

/**
 * Created by leesh on 29/11/2017.
 */
public interface Work {
    public Work execute(Object o);
    public boolean isLeft();
    public Work nextWork();
}
