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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import kr.ac.kaist.wala.adlib.analysis.APICallNode;
import kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.flows.PropagateFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.ifds.AliasHandler;
import kr.ac.kaist.wala.adlib.dataflow.ifds.DataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.DefaultDataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.IFDSAnalyzer;
import kr.ac.kaist.wala.adlib.dataflow.ifds.InfeasiblePathException;
import kr.ac.kaist.wala.adlib.dataflow.ifds.LocalDataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.PathEdge;
import kr.ac.kaist.wala.adlib.dataflow.ifds.PropagationGraph;
import kr.ac.kaist.wala.adlib.dataflow.ifds.PropagationPoint;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.FieldSeq;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.NoneField;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.SingleField;
import kr.ac.kaist.wala.adlib.util.GraphPrinter;
import kr.ac.kaist.wala.adlib.util.PathFinder;
import kr.ac.kaist.wala.adlib.util.PathOptimizer;


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
    private final PointerAnalysis<InstanceKey> pa;

    public MaliciousPatternChecker(CallGraph cg, PointerAnalysis<InstanceKey> pa){
        this.cg = cg;
        this.pa = pa;
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

    public int getTotalProgramPoint(){
        return icfg.getNumberOfNodes();
    }

    public Set<Pair> getTotalDF(){
        return ifds.getTotalDF();
    }

    public Set<Pair<CGNode, Integer>> getSeeds(){
        return this.seeds;
    }
    /**
     * Add a point at where this analysis starts and a variable tracked by this analysis
     * @param n A point at where this analysis starts
     * @param var A seed tracked by this analysis
     */
    public void addSeed(CGNode n, int var){
        boolean isInit = false;
        if(var == IFlowFunction.ANY)
            var = 0;

        for(Pair p : seeds){
            //System.out.println("SEEDPAIR: " + p);
            if(p.fst.equals(n)){
                isInit = true;
            }
        }

        if(isInit == false)
            seeds.add(Pair.make(n, 0));

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
    private Set<PathEdge> matchPattern(MaliciousPattern mp, Set<PathEdge> edges){
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

    private Set<PathEdge> isReachable(BasicBlockInContext bb, DataFact fact, Set<PathEdge> edges){
        Set<PathEdge> res = new HashSet<>();

        for(PathEdge pe : edges){
            if(pe.getToNode().equals(bb) && pe.getToFact().equals(fact)) {
                res.add(pe);
            }
        }

        return res;
    }

    private Set<PathEdge> isReachable(MaliciousPoint mp, Set<PathEdge> edges){
        Set<PathEdge> res = new HashSet<>();

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
                res.addAll(isReachable(pred, fact, edges));
            }
        }

        return res;
    }

    private Set<Pair<MaliciousPattern, Set<PathEdge>>> findPatterns(Set<PathEdge> edges){
        Set<Pair<MaliciousPattern, Set<PathEdge>>> res = new HashSet<>();

        for(MaliciousPattern mp : mps){
            Set<PathEdge> pe = matchPattern(mp, edges);
            if(!pe.isEmpty())
                res.add(Pair.make(mp, pe));
        }
        return res;
    }

    private String dup(String s, int n){
        String res = "";
        for(int i=0; i<n; i++){
            res += s;
        }
        return res;
    }

    /**
     * Check whether malicious patterns exist in the super graph.
     * @return possible malicious patterns that exist in the super graph
     */
    public List<String> checkPatterns(){
        MaliciousFlowModelHandler mfmh = new MaliciousFlowModelHandler(mps, cha, new AliasHandler(icfg, pa));
        List<String> warn = new ArrayList<>();

        ifds.setModelHandler(mfmh);
        int index = 0;

        //for progress prints
        int numOfSeeds = seeds.size();
        int curi = 0;

        System.out.println();

        for(Pair p : seeds){
            CGNode n = (CGNode) p.fst;
            int var = (Integer) p.snd;
            curi++;

            index++;
            Set<PathEdge> res = new HashSet<>();
            int count = 1;
            //System.out.print(String.format("\033[%dA",count)); // Move up
            System.out.print("\033[2K"); // Erase line content
            //System.err.print(dup(" ", 50)+"\r");
            System.out.print("\t|" + dup("=", curi) + dup(" ", (numOfSeeds-curi)) + "| ( " + curi + " / " + numOfSeeds + " ) \t" + n.getMethod().getName() + " [ " + var + " ]\r");
            try {
                BasicBlockInContext entry = icfg.getEntriesForProcedure(n)[0];
		        boolean isArray = false;

                if(var == 0) {
                    res.addAll(ifds.analyze(entry, null));
                }else{
		            Field f = NoneField.getInstance();
                    if(var > 1) { // except for this
                        if(entry.getNode().getMethod().getParameterType(var - 1).isArrayType()){
                            isArray = true;
                            f = FieldSeq.make(SingleField.make("["), f);
		                }
                    }
                    res.addAll(ifds.analyze(entry, new LocalDataFact(n, var, f)));
                }
                PropagationGraph graph = ifds.getPropagationGraph();

                PathFinder pf = new PathFinder(cg, icfg, cha, graph);

                PropagationPoint seedPP = PropagationPoint.make(icfg.getEntriesForProcedure(n)[0], ((var == 0)? DataFact.DEFAULT_FACT : new LocalDataFact(n, var, ((isArray)? FieldSeq.make(SingleField.make("["), NoneField.getInstance()) : NoneField.getInstance()))));

                for(MaliciousPattern mp : mps) {
                    Set<PathFinder.Path> paths = pf.findPaths(seedPP, mp);
                    if (paths.size() != 0) {
                        for (PathFinder.Path path : paths) {
                            boolean isMatched = path.isMatched();
                            warn.add("SEED: " + n + " [ " + var + " ] + \t ( " + mp.patternName + " )");
                            String fn = mp.patternName + "_I_" + (index++);
                            String dotF = GraphPrinter.print(fn, PathOptimizer.optimize(path.getPath()));
                            //String svgF = GraphUtil.convertDotToSvg(dotF);
                            String svgF = fn;
                            warn.add("\t\t => The flows are printed in " + svgF + "\t( " + isMatched + " )");
                        }
                    }
                }

                //TODO: should we clear?
                ifds.clearPE();
            } catch (InfeasiblePathException e) {
                e.printStackTrace();
            }
        }
        System.err.println();
        System.err.println();
        return warn;
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
            return tn.toString() + " . " + s.toString() + "\t" + f;
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

        public String getName(){
            return this.patternName;
        }

        @Override
        public String toString(){
            return "[MaliciousPattern] " + this.patternName;
        }
        public String toStringAll(){
            String res = toString() + "\n";
            for(MaliciousPoint p : pattern){
                res += "\t" + p.toString()+"\n";
            }

            return res;
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
