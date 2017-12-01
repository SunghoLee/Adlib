package kr.ac.kaist.wala.adlib.dataflow;

/**
 * Created by leesh on 29/11/2017.
 */
public class NoMoreWork implements Work{
    private static NoMoreWork instance;

    public static NoMoreWork getInstance(){
        if(instance == null)
            instance = new NoMoreWork();

        return instance;
    }

    private NoMoreWork(){}

    @Override
    public Work execute(Object o) {
        return this;
    }

    @Override
    public boolean isLeft() {
        return false;
    }

    @Override
    public Work nextWork() {
        return this;
    }
}