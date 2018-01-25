package kr.ac.kaist.wala.adlib.dataflow.works;

import com.ibm.wala.ssa.SSAArrayLoadInstruction;

/**
 * Created by leesh on 07/12/2017.
 */
public class GetElementOfArrayWork extends AbstractWork {
    private static GetElementOfArrayWork instance;
    public static GetElementOfArrayWork getInstance(Work w){
        if(instance == null)
            instance = new GetElementOfArrayWork(w);
        return instance;
    }

    private GetElementOfArrayWork(Work w) {
        super(w);
    }

    @Override
    public Work execute(Object o) {
        if(o instanceof SSAArrayLoadInstruction){
            return super.nextWork();
        }
        return this;
    }

    @Override
    public boolean isLeft() {
        return true;
    }
}
