package kr.ac.kaist.wala.adlib.analysis.malicious;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSACFG;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.analysis.APICallNode;
import kr.ac.kaist.wala.adlib.dataflow.DataFlowAnalysis;
import kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.flows.PropagateFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.ifds.*;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.NoneField;
import kr.ac.kaist.wala.adlib.dataflow.pointer.IDataPointer;
import kr.ac.kaist.wala.adlib.dataflow.works.Work;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;


/**
 * Created by leesh on 14/04/2017.
 */
public class MaliciousPatternChecker {
    private final Set<MaliciousPattern> mps = new HashSet<>();
    private final Set<MaliciousPatternWarning> warns = new HashSet<>();
    private final ICFGSupergraph icfg;
    private final IClassHierarchy cha;
    private final IFDSAnalyzer ifds;
    private final Set<Pair<CGNode, Integer>> seeds = new HashSet<>();
    private final CallGraph cg;

    public MaliciousPatternChecker(CallGraph cg, PointerAnalysis<InstanceKey> pa){
        this.cg = cg;
        icfg = ICFGSupergraph.make(cg, new AnalysisCache());
        ifds = new IFDSAnalyzer(icfg, pa);
        cha = cg.getClassHierarchy();
    }

    public void addMaliciousPattern(MaliciousPattern mp){
        mp.setClassHierrachy(cha);
        mps.add(mp);
    }

    public void addMaliciousPatterns(MaliciousPattern... mps){
        for(MaliciousPattern mp : mps) {
            mp.setClassHierrachy(cha);
            this.mps.add(mp);
        }
    }

    /**
     * Add a point at where this analysis starts and a variable tracked by this analysis
     * @param n A point at where this analysis starts
     * @param var A seed tracked by this analysis
     */
    public void addSeed(CGNode n, int var){
        seeds.add(Pair.make(n, var));
    }



    ///
    private TypeReference makeClassTypeRef(TypeName tn){
        TypeReference tr = TypeReference.find(ClassLoaderReference.Primordial, tn);
        if(tr == null)
            tr = TypeReference.find(ClassLoaderReference.Application, tn);

        if(tr == null)
            Assertions.UNREACHABLE("A TypeReference of the TypeName does not exist: " + tn);

        return tr;
    }

    private Set<TypeReference> getAllSubClass(TypeReference tr, IClassHierarchy cha){
        Set<TypeReference> res = new HashSet<>();
        getAllSubClass(res, tr, cha);
        return res;
    }

    private Set<TypeReference> getAllSubClass(Set<TypeReference> res, TypeReference tr, IClassHierarchy cha){
//        System.out.println("#?? " + tr + "\t Contained? " + res.contains(tr));
        if(cha.isInterface(tr)){
            for (IClass c : cha.getImplementors(tr)) {
                if(!res.contains(c.getReference())) {
                    res.add(c.getReference());
                    res.addAll(getAllSubClass(res, c.getReference(), cha));
                }
            }
        }else {
            for (IClass c : cha.computeSubClasses(tr)) {
                if(!res.contains(c.getReference())) {
                    res.add(c.getReference());
                    res.addAll(getAllSubClass(res, c.getReference(), cha));
                }
            }
        }
        return res;
    }
    ///
    private boolean matchPattern(MaliciousPattern mp, Set<PathEdge> edges){
        List<MaliciousPoint> mpList = mp.getPoints();
        //TODO: need to calculate the pathes from a seed to a last point? Now, to show an existence of a pattern, I just check whether the last point is reachable.
//        mpList = Lists.reverse(mpList);
//
//        for(MaliciousPoint point : mpList){
//
//        }
        MaliciousPoint lastPoint = mpList.get(mpList.size()-1);
        return isReachable(lastPoint, edges);
    }

    private boolean isReachable(BasicBlockInContext bb, DataFact fact, Set<PathEdge> edges){
        for(PathEdge pe : edges){
//            if(pe.toString().contains("startActivity")) {
//                System.out.println("======");
//                System.out.println(pe);
//                System.out.println("======");
//            }
            if(pe.getToNode().equals(bb) && pe.getToFact().equals(fact))
                return true;
        }
        return false;
    }

    private boolean isReachable(MaliciousPoint mp, Set<PathEdge> edges){
        PropagateFlowFunction ff = mp.getFlowFunction();

        if(ff.getTo() != IFlowFunction.TERMINATE){
            Assertions.UNREACHABLE("This pattern cannot be terminated: " + mp);
        }

        int inVarIndex = ff.getFrom();

        TypeName tn = mp.getTypeName();
        Selector s = mp.getSelector();

        Set<TypeReference> trs = new HashSet<>();
        Set<CGNode> posTarget = new HashSet<>();

        TypeReference ori = makeClassTypeRef(tn);
        trs.add(ori);
        trs.addAll(getAllSubClass(ori, cha));

        for(TypeReference tr : trs){
            IClass c = cha.lookupClass(tr);
            for(IMethod m : c.getAllMethods()){
                if(m.getSelector().equals(s)) {
                    posTarget.addAll(cg.getNodes(m.getReference()));
                }
            }
        }

        for(CGNode target : posTarget){
            // all methods must have only one entry.
            BasicBlockInContext entry = icfg.getEntriesForProcedure(target)[0];
            Iterator<BasicBlockInContext> iPred = icfg.getPredNodes(entry);
            while(iPred.hasNext()){
                BasicBlockInContext pred = iPred.next();
                SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) pred.getLastInstruction();
                if(invokeInst == null)
                    Assertions.UNREACHABLE("The previous block of an entry of any procedure must have a invoke instruction: " + pred);

                DataFact fact = null;
                switch(inVarIndex){
                    case IFlowFunction.ANY:
                        fact = DefaultDataFact.DEFAULT_FACT;
                        break;
                    case IFlowFunction.RETURN_VARIABLE:
                        fact = new LocalDataFact(pred.getNode(), invokeInst.getDef(), NoneField.getInstance());
                        break;
                    case IFlowFunction.TERMINATE:
                        Assertions.UNREACHABLE("How come the variable index of 'from' at a malicious point is terminate? " + mp);
                        break;
                    default:
                        fact = new LocalDataFact(pred.getNode(), invokeInst.getUse(inVarIndex-1), NoneField.getInstance());
                        break;
                }
                if(isReachable(pred, fact, edges))
                    return true;
            }
        }

        return false;
    }

    private Set<MaliciousPattern> findPatterns(Set<PathEdge> edges){
        Set<MaliciousPattern> res = new HashSet<>();

        for(MaliciousPattern mp : mps){
            if(matchPattern(mp, edges))
                res.add(mp);
        }
        return res;
    }

    /**
     * Check whether malicious patterns exist in the super graph.
     * @return possible malicious patterns that exist in the super graph
     */
    public Set<MaliciousPatternWarning> checkPatterns(){
        MaliciousFlowModelHandler mfmh = new MaliciousFlowModelHandler(mps, cha);
        ifds.setModelHandler(mfmh);
        for(Pair p : seeds){
            CGNode n = (CGNode) p.fst;
            int var = (Integer) p.snd;

            Set<PathEdge> res = new HashSet<>();

            try {
                BasicBlockInContext entry = icfg.getEntriesForProcedure(n)[0];

                if(var == kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction.ANY) {
                    res.addAll(ifds.analyze(entry, null));
                }else{
                    res.addAll(ifds.analyze(entry, new LocalDataFact(n, var, NoneField.getInstance())));
                }
                System.out.println("======");
                System.out.println("SEED: " + n + " [ " + var + " ]");
                for(MaliciousPattern mp : findPatterns(res)){
                    System.out.println("[Found] " + mp);
                }
                System.out.println("======");
                ifds.clear();
            } catch (InfeasiblePathException e) {
                e.printStackTrace();
            }
        }

        return warns;
    }


    public static class MaliciousPoint {
        private final TypeName tn;
        private final Selector s;
        private final PropagateFlowFunction f;
        public MaliciousPoint(TypeName tn, Selector s, PropagateFlowFunction f){
            this.tn = tn;
            this.s = s;
            this.f = f;
        }

        public TypeName getTypeName(){
            return this.tn;
        }

        public Selector getSelector(){
            return this.s;
        }

        public PropagateFlowFunction getFlowFunction(){ return this.f; }

        @Override
        public String toString(){
            return tn.toString() + " . " + s.toString();
        }

        public boolean isSame(APICallNode n, IClassHierarchy cha){
            TypeReference tr1 = TypeReference.find(ClassLoaderReference.Primordial, tn);
            if(tr1 == null)
                tr1 = TypeReference.find(ClassLoaderReference.Application, tn);

            TypeReference tr2 = TypeReference.find(ClassLoaderReference.Primordial, n.getReceiverType());
            if(tr2 == null)
                tr2 = TypeReference.find(ClassLoaderReference.Application, n.getReceiverType());

            if(tr1 == null || tr2 == null)
                return tn.equals(n.getReceiverType()) && s.equals(n.getSelector());

            IClass c1 = cha.lookupClass(tr1);
            IClass c2 = cha.lookupClass(tr2);

            if(c1 == null){
                if(tr1.getClassLoader().equals(ClassLoaderReference.Primordial))
                    c1 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Application, tn));
                else
                    c1 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, tn));
            }

            if(c2 == null){
                if(tr2.getClassLoader().equals(ClassLoaderReference.Primordial))
                    c2 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Application, n.getReceiverType()));
                else
                    c2 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, n.getReceiverType()));
            }

            if(tn.equals(n.getReceiverType()) || (c1 != null && c2 != null && cha.isSubclassOf(c2, c1))){
                if(s.equals(n.getSelector()))
                    return true;
            }

            return false;
        }
    }

    public static class MaliciousPattern {
        private final Queue<MaliciousPoint> pattern = new LinkedBlockingQueue<>();
        private IClassHierarchy cha;
        private final String patternName;

        public MaliciousPattern(String name, MaliciousPoint... points){
            this.patternName = name;

            for(int i=0; i<points.length; i++){
                pattern.add(points[i]);
            }
        }

        private MaliciousPattern(String name, Queue<MaliciousPoint> queue){
            this.patternName = name;
            this.pattern.addAll(queue);
        }

        public boolean isDone(){
            return pattern.isEmpty();
        }

        public boolean isMatched(APICallNode n){
            return pattern.peek().isSame(n, cha);
        }

        private void setClassHierrachy(IClassHierarchy cha){
            this.cha = cha;
        }

        public boolean match(APICallNode n){
            if(isMatched(n)){
                pattern.poll();
                return true;
            }
            return false;
        }

        @Override
        public String toString(){
            return "[MaliciousPattern] " + this.patternName;
        }

        @Override
        public int hashCode(){
            return patternName.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof MaliciousPattern){
                MaliciousPattern mp = (MaliciousPattern) o;
                if(mp.patternName.equals(this.patternName))
                    return true;
            }
            return false;
        }

        public MaliciousPattern clone(){
            return new MaliciousPattern(patternName, pattern);
        }

        public List<MaliciousPoint> getPoints(){
            Iterator<MaliciousPoint> iPoint = pattern.iterator();
            List<MaliciousPoint> mpSet = new ArrayList<>();

            while(iPoint.hasNext()){
                MaliciousPoint mp = iPoint.next();
                mpSet.add(mp);
            }

            return mpSet;
        }

        private MaliciousPoint getNext(){
            if(pattern.isEmpty())
                return null;
            return pattern.poll();
        }

        private MaliciousPoint getNextWithoutRemove(){
            if(pattern.isEmpty())
                return null;
            return pattern.peek();
        }

        private void removeNext(){
            pattern.poll();
        }
    }

    public static class MaliciousPatternWarning{
        private final APICallNode startPoint;
        private final String pattern;

        public MaliciousPatternWarning(APICallNode s, String pn){
            this.startPoint = s;
            this.pattern = pn;
        }

        @Override
        public String toString(){
            return pattern + " in a flow started from " + startPoint;
        }

        @Override
        public int hashCode(){
            return startPoint.hashCode() + pattern.hashCode();
        }

        public APICallNode getStartPoint(){
            return this.startPoint;
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof MaliciousPatternWarning){
                MaliciousPatternWarning mpw = (MaliciousPatternWarning) o;
                if(mpw.startPoint.equals(this.startPoint) && mpw.pattern.equals(this.pattern))
                    return true;
            }
            return false;
        }
    }

    class MPData extends DataFlowAnalysis.DataWithWork{
        private Set<MaliciousPattern> mpSet;

        public MPData(IDataPointer p, Work w, Set<MaliciousPattern> mpSet) {
            super(p, w);
            this.mpSet = mpSet;
        }

        public Set<MaliciousPattern> getMaliciousPatterns(){
            return  this.mpSet;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return super.equals(o);
        }
    }
}

class Path {
    private List<PathNode> pathNodeList;

    public Path(){
        pathNodeList = new ArrayList<>();
    }

    private Path(List<PathNode> pl){
        pathNodeList = pl;
    }

    public void addPath(PathNode pn){
        pathNodeList.add(pn);
    }

    @Override
    public int hashCode(){
        return pathNodeList.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Path){
            Path p = (Path) o;

            if(p.pathNodeList.size() == pathNodeList.size()){
                for(int i=0; i<pathNodeList.size(); i++)
                    if(!p.pathNodeList.get(i).equals(pathNodeList.get(i)))
                        return false;
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString(){
        String res = "";

        for(int i=0; i<pathNodeList.size(); i++) {
            res += pathNodeList.get(i).toString();
            if (i != pathNodeList.size() - 1)
                res += "\n";
        }

        return res;
    }

    public Path clone(){
        List<PathNode> pl = new ArrayList<>();
        pl.addAll(pathNodeList);
        return new Path(pl);
    }
}

class PathNode {
    private IMethod node;
    private SSACFG.BasicBlock block;

    public PathNode(IMethod node, SSACFG.BasicBlock block){
        this.node = node;
        this.block = block;
    }

    @Override
    public int hashCode(){
        return node.hashCode() + block.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof PathNode){
            PathNode pn = (PathNode) o;
            if(pn.node.equals(node) && pn.block.getNumber() == block.getNumber())
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return "[ " + node + "] (" + block.getLastInstructionIndex() + ") " + block.getLastInstruction();
    }
}