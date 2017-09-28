package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;

/**
 * Created by leesh on 25/09/2017.
 */
public interface IDataFlowFilter {

    public boolean apply(BasicBlockInContext nextBlock, IDataPointer dp);
}
