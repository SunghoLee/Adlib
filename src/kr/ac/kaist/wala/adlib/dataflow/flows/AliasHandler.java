package kr.ac.kaist.wala.adlib.dataflow.flows;

import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 28/12/2017.
 */
public class AliasHandler {

    public Set<PointerKey> getAlias(PointerKey pk){
        Set<PointerKey> res = new HashSet<>();

        return res;
    }

    public Set<BasicBlockInContext> getAliasingPoint(PointerKey dst, PointerKey src){
        Set<BasicBlockInContext> res = new HashSet<>();

        return res;
    }
}
