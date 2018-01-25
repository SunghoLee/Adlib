package kr.ac.kaist.wala.adlib.dataflow.works;

/**
 * Created by leesh on 19/12/2017.
 */
public abstract class AbstractWork implements Work{
    private Work superWork;

    protected AbstractWork(Work w){
        this.superWork = w;
    }

    @Override
    public Work nextWork() {
        return superWork;
    }
}