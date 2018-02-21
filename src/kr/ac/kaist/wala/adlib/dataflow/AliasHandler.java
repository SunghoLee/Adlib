package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.pointer.IDataPointer;
import kr.ac.kaist.wala.adlib.dataflow.pointer.LocalDataPointer;
import kr.ac.kaist.wala.adlib.dataflow.pointer.StaticFieldDataPointer;
import kr.ac.kaist.wala.adlib.dataflow.works.GetFieldWork;
import kr.ac.kaist.wala.adlib.dataflow.works.Work;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by leesh on 2018. 2. 19..
 */
public class AliasHandler {
    private ICFGSupergraph supergraph;
    private PointerAnalysis<InstanceKey> pa;
    private HeapGraph<InstanceKey> hg;
    private InstanceKeyFilter ikf;
    private PointerKeyFilter pkf;

    public AliasHandler(ICFGSupergraph supergraph, PointerAnalysis<InstanceKey> pa){
        if(supergraph == null || pa == null)
            Assertions.UNREACHABLE("Supergraph & PointerAnalysis must not be null.");

        this.supergraph = supergraph;
        this.pa = pa;
        this.hg = pa.getHeapGraph();
    }

    public AliasHandler(ICFGSupergraph supergraph, PointerAnalysis<InstanceKey> pa, InstanceKeyFilter ikf, PointerKeyFilter pkf){
        this(supergraph, pa);
        if(ikf == null)
            this.ikf = new DefaultInstanceKeyFilter();
        if(pkf == null)
            this.pkf = new DefaultPointerKeyFilter();
    }

    private Set<DataFlowAnalysis.DataWithWork> findAlias(InstanceKey ik, Work w){
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        if(ikf.accept(supergraph, ik)) {
            Iterator<Object> iPred = hg.getPredNodes(ik);
            while (iPred.hasNext()) {
                PointerKey predPK = (PointerKey) iPred.next();
                if (pkf.accept(supergraph, predPK)) {
                    if (predPK instanceof StaticFieldKey) {
                        res.add(new DataFlowAnalysis.DataWithWork(new StaticFieldDataPointer(((StaticFieldKey) predPK).getField()), w));
                    } else if (predPK instanceof LocalPointerKey) {
                        res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(((LocalPointerKey) predPK).getNode(), ((LocalPointerKey) predPK).getValueNumber()), w));
                    } else if (predPK instanceof InstanceFieldKey) {
                        InstanceFieldKey ifk = (InstanceFieldKey) predPK;
                        InstanceKey oik = ifk.getInstanceKey();
                        IField f = ifk.getField();

                        res.addAll(findAlias(oik, GetFieldWork.getInstance(f, w)));
                    } else
                        Assertions.UNREACHABLE("Should handle this type pointer key: " + predPK.getClass().getName());
                }
            }
        }

        return res;
    }

    public Set<DataFlowAnalysis.DataWithWork> findAlias(DataFlowAnalysis.DataWithWork dwp){
        Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

        PointerKey pk = dwp.getData().getPointerKey(pa);
        Work w = dwp.getWork();

        for(InstanceKey ik : pa.getPointsToSet(pk)){
            res.addAll(findAlias(ik, w));
        }

        return res;
    }

    public static interface InstanceKeyFilter {
        public boolean accept(ICFGSupergraph supergraph, InstanceKey ik);
    }

    public static interface PointerKeyFilter {
        public boolean accept(ICFGSupergraph supergraph, PointerKey pk);
    }

    class DefaultInstanceKeyFilter implements InstanceKeyFilter{
        @Override
        public boolean accept(ICFGSupergraph supergraph, InstanceKey ik) {
            return true;
        }
    }

    class DefaultPointerKeyFilter implements PointerKeyFilter{
        @Override
        public boolean accept(ICFGSupergraph supergraph, PointerKey pk) {
            return true;
        }
    }
}
