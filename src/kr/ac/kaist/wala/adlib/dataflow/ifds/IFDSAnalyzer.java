package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.types.ClassLoaderReference;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.FlowModelHandler;
import kr.ac.kaist.wala.hybridroid.util.data.Pair;

import java.util.Set;

/**
 * Created by leesh on 22/02/2018.
 */
public class IFDSAnalyzer {
    private final static boolean DEBUG = true;

    private final ICFGSupergraph supergraph;
    private final PointerAnalysis<InstanceKey> pa;
    private final AliasHandler ah;
    private final PathEdgeManager peManager;
    private final SummaryEdgeManager seManager;
    private final GraphDataFlowManager graphManager;
    private final WorkList workList = new WorkList();

    private final PropagationGraphBuilder recorder;

    public IFDSAnalyzer(ICFGSupergraph supergraph, PointerAnalysis<InstanceKey> pa){
        this.supergraph = supergraph;
        this.pa = pa;
        ah = new AliasHandler(supergraph, pa);
        peManager = new PathEdgeManager();
        this.recorder = new PropagationGraphBuilder();
        seManager = new SummaryEdgeManager();
        this.graphManager = new GraphDataFlowManager(supergraph, pa, seManager);
    }


    public void setModelHandler(FlowModelHandler handler){
        graphManager.setModelHandler(handler);
    }

    private void propagate(PathEdge<BasicBlockInContext, DataFact> pre, PathEdge<BasicBlockInContext, DataFact> pe){
        if(DEBUG){
//            if((pe.getFromFact() instanceof DefaultDataFact) == false)
                System.out.println("\t=> " + pe);
        }
        if(!peManager.contains(pe)){
            peManager.propagate(pe);
            workList.put(pe);
        }
        if(pre != null)
            recorder.addEdge(pre, pe);
    }

    public Set<PathEdge> analyze(BasicBlockInContext entry, DataFact seed) throws InfeasiblePathException {
        //TODO: need to put seed as an initial data fact
        PathEdge<BasicBlockInContext, DataFact> initialEdge = new PathEdge<>(entry, DataFact.DEFAULT_FACT, entry, DataFact.DEFAULT_FACT);
        propagate(null, initialEdge);
        recorder.addSeed(initialEdge);

        if(seed != null) {
            PathEdge<BasicBlockInContext, DataFact> seedEdge = new PathEdge<>(entry, seed, entry, seed);
            propagate(null, seedEdge);
            recorder.addSeed(seedEdge);
        }

        while(!workList.isEmpty()){
            PathEdge<BasicBlockInContext, DataFact> pe = workList.poll();
            if(DEBUG) {
//                if((pe.getFromFact() instanceof DefaultDataFact) == false)
                    System.out.println("# " + pe);
            }
            BasicBlockInContext fromNode = pe.getFromNode();
            BasicBlockInContext toNode = pe.getToNode();
            DataFact fromFact = pe.getFromFact();
            DataFact toFact = pe.getToFact();

            if(supergraph.isCall(toNode)){
                for(Pair<BasicBlockInContext, DataFact> p : graphManager.getCalleeNexts(toNode, toFact)){
                    propagate(pe, new PathEdge(p.fst(), p.snd(), p.fst(), p.snd()));
                }
                for(Pair<BasicBlockInContext, DataFact> p : graphManager.getCallToReturnNexts(toNode, toFact, pe)){
                    propagate(pe, new PathEdge(fromNode, fromFact, p.fst(), p.snd()));
                }
                for(Pair<BasicBlockInContext, DataFact> p : seManager.getSummaryFrom(toNode, toFact)){
                    propagate(pe, new PathEdge(fromNode, fromFact, p.fst(), p.snd()));
                }
            }else if(supergraph.isExit(toNode)){
                for(Pair<BasicBlockInContext, DataFact> callerP : graphManager.getCallerInfo(fromNode, fromFact)){
                    for(Pair<BasicBlockInContext, DataFact> retP : graphManager.getRetInfo(callerP.fst(), toNode, toFact)){
                        PathEdge nEdge = new PathEdge(callerP.fst(), callerP.snd(), retP.fst(), retP.snd());
                        if(!seManager.contains(nEdge)){
                            seManager.add(nEdge);
                            for(PathEdge callerPE : peManager.findLocalEdgeTo(callerP.fst(), callerP.snd())){
                                propagate(pe, new PathEdge(callerPE.getFromNode(), callerPE.getFromFact(), retP.fst(), retP.snd()));
                            }
                        }
                    }
                }
            }else{
                for(Pair<BasicBlockInContext, DataFact> p : graphManager.getNormalNexts(toNode, toFact)){
                    propagate(pe, new PathEdge(fromNode, fromFact, p.fst(), p.snd()));
                }
            }
            if(DEBUG && fromNode.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)) {
//                try {
//                    System.in.read();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
            if(DEBUG)
                System.out.println();
        }

        System.out.println("#PATHSIZE: " + peManager.size());
        return peManager.getEdges();
    }

    public void clear(){
    }

    public void clearPE(){
        peManager.clear();
    }

    public void clearSE(){
        seManager.clear();
    }

    public PropagationGraph getPropagationGraph(){
        return this.recorder.getGraph();
    }
}
