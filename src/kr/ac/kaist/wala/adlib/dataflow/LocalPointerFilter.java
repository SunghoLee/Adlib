package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;

/**
 * Created by leesh on 25/09/2017.
 */
public class LocalPointerFilter implements IDataFlowFilter {

    @Override
    public boolean apply(BasicBlockInContext nextBlock, IDataPointer dp) {
        if(dp instanceof LocalDataPointer){
            LocalDataPointer ldp = (LocalDataPointer) dp;
            if(ldp.getNode().equals(nextBlock.getNode()))
                return true;
            else if(nextBlock.isExitBlock())
                return true;
        }else if(dp instanceof StaticFieldDataPointer)
            return true;
        return false;
    }
}
