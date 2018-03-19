package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.analysis.pointers.HeapGraph;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.graph.traverse.BFSPathFinder;
import com.ibm.wala.util.intset.OrdinalSet;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.FieldSeq;

import java.util.*;

/**
 * Created by leesh on 2018. 2. 19..
 */
public class AliasHandler {
    private final static boolean DEBUG = true;
    private final ICFGSupergraph supergraph;
    private final PointerAnalysis<InstanceKey> pa;
    private final HeapGraph<InstanceKey> hg;
    private final Map<CGNode, Set<PointerKey>> localPKMap = new HashMap<>();
    private final Set<StaticFieldKey> statics = new HashSet<>();

    public AliasHandler(ICFGSupergraph supergraph, PointerAnalysis<InstanceKey> pa){
        if(supergraph == null || pa == null)
            Assertions.UNREACHABLE("Supergraph & PointerAnalysis must not be null.");

        this.supergraph = supergraph;
        this.pa = pa;
        this.hg = pa.getHeapGraph();
        init();
    }

    private void init(){
        for(PointerKey pk : pa.getPointerKeys()){
            if(pk instanceof LocalPointerKey){
                LocalPointerKey lpk = (LocalPointerKey) pk;
                put(lpk.getNode(), pk);
            }else if(pk instanceof StaticFieldKey){
                StaticFieldKey sfk = (StaticFieldKey) pk;
                if(sfk.getField().getDeclaringClass().getReference().getClassLoader().equals(ClassLoaderReference.Application))
                    statics.add(sfk);
            }
        }
    }

    private void put(CGNode n, PointerKey pk){
        if(!localPKMap.containsKey(n))
            localPKMap.put(n, new HashSet<>());
        localPKMap.get(n).add(pk);
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
                        //TODO: no-op! why? before using this, the field must be assigned another variable and the variable would already choosed by alias finder.
                    } else if (predPK instanceof ReturnValueKey){
                      //TODO: no-op
                    } else if (predPK instanceof ArrayContentsKey){
                        //TODO: implement this!
                    } else if (predPK instanceof PropagationCallGraphBuilder.TypedPointerKey){
                        //TODO: implement this!
                    } else
                        Assertions.UNREACHABLE("Should handle this type pointer key: " + predPK.getClass().getName());
                }
            }
        }

        return res;
    }

//    public Set<DataFact> findAlias(CGNode n, DataFact fact, PointerKeyFilter pkf, InstanceKeyFilter ikf){
//        Set<DataFact> res = new HashSet<>();
//
//        PointerKey pk = null;
//
//        if(fact instanceof LocalDataFact){
//            LocalDataFact ldf = (LocalDataFact) fact;
//            pk = pa.getHeapModel().getPointerKeyForLocal(n, ldf.getVar());
//        }else if(fact instanceof GlobalDataFact){
////            GlobalDataFact gdf = (GlobalDataFact) fact;
////            pk = pa.getHeapModel().getPointerKeyForStaticField(gdf.getGlobalFact());
//            Assertions.UNREACHABLE("A GloablDataFact cannot be used to find alias: " + fact);
//        }else
//            Assertions.UNREACHABLE("Only Local and Global data facts can be used to find aliases: " + fact);
//
//        for(InstanceKey lik : pa.getPointsToSet(pk)){
//            res.addAll(findAlias(lik, fact.getField(), pkf, ikf));
//        }
//
//        visited.clear();
//
//        return res;
//    }

    public Set<DataFact> findAlias(CGNode n, DataFact fact){
        Set<DataFact> res = new HashSet<>();

        if(fact instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) fact;
            final PointerKey targetPK = pa.getHeapModel().getPointerKeyForLocal(ldf.getNode(), ldf.getVar());

            Set<PointerKey> localPKs = localPKMap.get(n);
//            localPKs.addAll(statics);
            Collection targets = OrdinalSet.toCollection(pa.getPointsToSet(targetPK));

            Field field = fact.getField();

            Set<InstanceKey> realTargets = new HashSet<>();
            if(field instanceof FieldSeq){
                String fst = ((FieldSeq) field).getFirst();
                if(fst.equals("this$0")){
                    field = ((FieldSeq) field).getRest();
                    for(Object target : targets){
                        Iterator<Object> iSucc = hg.getSuccNodes(target);
                        while(iSucc.hasNext()){
                            PointerKey pk = (PointerKey) iSucc.next();
                            if(pk instanceof InstanceFieldKey){
                                InstanceFieldKey ifk = (InstanceFieldKey) pk;
                                if(ifk.getField().getName().toString().equals("this$0")){
                                    realTargets.addAll(OrdinalSet.toCollection(pa.getPointsToSet(ifk)));
                                }
                            }
                        }
                    }
                }else
                    realTargets.addAll(targets);
            }

            for(PointerKey pk : localPKs){
                BFSPathFinder pathFinder = new BFSPathFinder((Graph) hg, pk, new Predicate() {
                    @Override
                    public boolean test(Object o) {
                        if(realTargets.contains(o))
                            return true;
                        return false;
                    }
                });

                List path = pathFinder.find();
                Field newField = field;

                if(path != null){
                    for(Object o : path){
                        if(o instanceof PointerKey){
                            if(o instanceof InstanceFieldKey){
                                InstanceFieldKey ifk = (InstanceFieldKey) o;
                                String fieldName = ifk.getField().getName().toString();
                                newField = new FieldSeq(fieldName, newField);
                            }else if(o instanceof LocalPointerKey){
                                LocalPointerKey lpk = (LocalPointerKey) o;
                                res.add(new LocalDataFact(n, lpk.getValueNumber(), newField));
                            }else if(o instanceof StaticFieldKey){
                                StaticFieldKey sfk = (StaticFieldKey) o;
                                res.add(new GlobalDataFact(sfk.getField().getReference(), newField));
                            }
                        }
                    }
                }
            }
        }else if(fact instanceof GlobalDataFact){
//            GlobalDataFact gdf = (GlobalDataFact) fact;
//            pk = pa.getHeapModel().getPointerKeyForStaticField(gdf.getGlobalFact());
            Assertions.UNREACHABLE("A GloablDataFact cannot be used to find alias: " + fact);
        }else
            Assertions.UNREACHABLE("Only Local and Global data facts can be used to find aliases: " + fact);

        if(DEBUG){
            System.out.println("***** ALIAS *****");
            System.out.println("FROM: " + fact);
            for(DataFact df : res)
                System.out.println("\t=> " + df);
            System.out.println("*****************");

        }

        return res;
    }

//    private Set<PointerKey> findLocalPointerKeys(CGNode n){
//        Set<PointerKey> locals = new HashSet<>();
//
//        for(PointerKey pk : pa.getPointerKeys()){
//            if(pk instanceof LocalPointerKey){
//                LocalPointerKey lpk = (LocalPointerKey) pk;
//                if(lpk.getNode().equals(n))
//                    locals.add(lpk);
//            }
//        }
//        return locals;
//    }
//
//    private Set<PointerKey> findAppStaticPointerKey(){
//        Set<PointerKey> statics = new HashSet<>();
//
//        for(PointerKey pk : pa.getPointerKeys()){
//            if(pk instanceof StaticFieldKey){
//                StaticFieldKey sfk = (StaticFieldKey) pk;
//                if(sfk.getField().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application))
//                    statics.add(sfk);
//            }
//        }
//        return statics;
//    }
//
//    private Set<DataFact> findAliasF(PointerKey pk, InstanceKey ik, Field f){
//        Iterator<Object> iSucc = hg.getSuccNodes(pk);
//
//        while(iSucc.hasNext()){
//
//        }
//    }
//
//    private Set<DataFact> findAliasF(CGNode n, InstanceKey lik, Field f){
//        Set<PointerKey> posAlias = findLocalPointerKeys(n);
//        posAlias.addAll(findAppStaticPointerKey());
//
//
//    }

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
