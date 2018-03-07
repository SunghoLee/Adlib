package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.FieldSeq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by leesh on 2018. 2. 19..
 */
public class AliasHandler {
    private final static boolean DEBUG = false;
    private final ICFGSupergraph supergraph;
    private final PointerAnalysis<InstanceKey> pa;
    private final HeapGraph<InstanceKey> hg;

    public AliasHandler(ICFGSupergraph supergraph, PointerAnalysis<InstanceKey> pa){
        if(supergraph == null || pa == null)
            Assertions.UNREACHABLE("Supergraph & PointerAnalysis must not be null.");

        this.supergraph = supergraph;
        this.pa = pa;
        this.hg = pa.getHeapGraph();
    }

    private Set<InstanceKey> visited = new HashSet<>();
    private Set<DataFact> findAlias(InstanceKey ik, Field f, PointerKeyFilter pkf, InstanceKeyFilter ikf){
        Set<DataFact> res = new HashSet<>();

        if(ikf.accept(supergraph, ik) && !visited.contains(ik)) {
            visited.add(ik);
            if(DEBUG) {
                System.out.println("####IK: " + ik);
                System.out.println("####F: " + f);
            }
            Iterator<Object> iPred = hg.getPredNodes(ik);

            while (iPred.hasNext()) {
                PointerKey predPK = (PointerKey) iPred.next();
                if (pkf.accept(supergraph, predPK)) {
                    if(DEBUG)
                        System.out.println("\tPRED_PK: " + predPK);
                    if (predPK instanceof StaticFieldKey) {
                        res.add(new GlobalDataFact(((StaticFieldKey)predPK).getField().getReference(), f));
                    } else if (predPK instanceof LocalPointerKey) {
                        res.add(new LocalDataFact(((LocalPointerKey) predPK).getNode(), ((LocalPointerKey) predPK).getValueNumber(), f));
                    } else if (predPK instanceof InstanceFieldKey) {
                        InstanceFieldKey ifk = (InstanceFieldKey) predPK;
                        InstanceKey oik = ifk.getInstanceKey();
                        IField insF = ifk.getField();

                        res.addAll(findAlias(oik, new FieldSeq(insF.getName().toString(), f), pkf, ikf));
                    } else if (predPK instanceof ReturnValueKey){
                      //TODO: no-op
                    } else if (predPK instanceof ArrayContentsKey){
                        //TODO: implement this!
                    } else
                        Assertions.UNREACHABLE("Should handle this type pointer key: " + predPK.getClass().getName());
                }
            }
        }

        return res;
    }

    public Set<DataFact> findAlias(CGNode n, DataFact fact, PointerKeyFilter pkf, InstanceKeyFilter ikf){
        Set<DataFact> res = new HashSet<>();

        PointerKey pk = null;

        if(fact instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) fact;
            pk = pa.getHeapModel().getPointerKeyForLocal(n, ldf.getVar());
        }else if(fact instanceof GlobalDataFact){
//            GlobalDataFact gdf = (GlobalDataFact) fact;
//            pk = pa.getHeapModel().getPointerKeyForStaticField(gdf.getGlobalFact());
            Assertions.UNREACHABLE("A GloablDataFact cannot be used to find alias: " + fact);
        }else
            Assertions.UNREACHABLE("Only Local and Global data facts can be used to find aliases: " + fact);

        for(InstanceKey lik : pa.getPointsToSet(pk)){
            res.addAll(findAlias(lik, fact.getField(), pkf, ikf));
        }

        visited.clear();

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
