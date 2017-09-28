package kr.ac.kaist.wala.adlib.analyzer;

import com.ibm.wala.classLoader.CallSiteReference;
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
import com.ibm.wala.ssa.SSAInvokeInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.callgraph.HybridSDKModel;
import kr.ac.kaist.wala.adlib.dataflow.*;
import kr.ac.kaist.wala.adlib.model.ARModeling;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by leesh on 14/04/2017.
 */
public class MaliciousPatternChecker {
    private ReachableAPIFlowGraph graph;
    private final IClassHierarchy cha;
    private final Set<MaliciousPattern> mps = new HashSet<>();
    private final Set<MaliciousPatternWarning> warns = new HashSet<>();
    private ICFGSupergraph icfg;
    private CallGraph cg;
    private int maxStackSize = 0;

    public MaliciousPatternChecker(ReachableAPIFlowGraph graph, IClassHierarchy cha){
        this.graph = graph;
        this.cha = cha;
    }

    public MaliciousPatternChecker(IClassHierarchy cha){
        this.cha = cha;
    }

    public int getMaxStackSize(){
        return this.maxStackSize;
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

    class NodeWithCallStack {
        private final BasicBlockInContext<IExplodedBasicBlock> block;
//        private final APICallNode acn;
        private final Stack<CGNode> callStack;

        public NodeWithCallStack(BasicBlockInContext<IExplodedBasicBlock> block, Stack<CGNode> callStack){
            this.block = block;
            this.callStack = (Stack<CGNode>) callStack.clone();
            maxStackSize = Integer.max(maxStackSize, callStack.size());
        }

        public BasicBlockInContext<IExplodedBasicBlock> getBlock(){
            return this.block;
        }

        public Stack<CGNode> getCallStack(){
            return this.callStack;
        }

        @Override
        public int hashCode(){
            return block.hashCode() * callStack.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof NodeWithCallStack){
                NodeWithCallStack n = (NodeWithCallStack) o;
                if(n.block.equals(this.block) && n.callStack.size() == this.callStack.size()){
                    for(int i=0; i<callStack.size(); i++){
                        if(!n.callStack.get(i).equals(this.callStack.get(i)))
                            return false;
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString(){
            return this.block.toString();
        }
    }

    public Set<MaliciousPatternWarning> checkPatterns(CallGraph cg, PointerAnalysis<InstanceKey> pa){
        this.cg = cg;
        this.icfg = ICFGSupergraph.make(cg, new AnalysisCache());

        Set<IMethod> entries = HybridSDKModel.getBridgeEntries();
        DataFlowAnalysis dfa = new DataFlowAnalysis(icfg);


        for(IMethod entry : entries) {
            Set<LocalDataPointer> seeds = new HashSet<>();

            if(entry.getNumberOfParameters() < 2){
                for(CGNode n : cg.getNodes(entry.getReference())){
                    seeds.add(new LocalDataPointer(n, IFlowFunction.ANY));
                }
            }else{
                for(int i = 2; i <= entry.getNumberOfParameters(); i++) {
                    for(CGNode n : cg.getNodes(entry.getReference())){
                        seeds.add(new LocalDataPointer(n, i));
                    }
                }
            }

            MaliciousPatternFlowSemanticsFunction semFun = new MaliciousPatternFlowSemanticsFunction(cg, cg.getClassHierarchy(), pa, clonePatterns(), seeds);
            dfa.analyze(seeds, semFun, new IDataFlowFilter() {
                @Override
                public boolean apply(BasicBlockInContext nextBlock, IDataPointer dp) {
                    return true;
                }
            });
        }

        return warns;
    }

    private static int entryNum = 0;

    class MaliciousPatternFlowSemanticsFunction extends DefaultDataFlowSemanticFunction{
        private final Set<MaliciousPattern> mps;
        private final Set<LocalDataPointer> seeds;

        public MaliciousPatternFlowSemanticsFunction(CallGraph cg, IClassHierarchy cha, PointerAnalysis<InstanceKey> pa, Set<MaliciousPattern> mps, Set<LocalDataPointer> seeds) {
            super(cg, cha, pa);
            this.mps = mps;
            this.seeds = seeds;
            entryNum++;
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitInvoke(DataFlowAnalysis.NodeWithCS block, SSAInvokeInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

            res.add(data);

            boolean matched = false;

            for(MaliciousPattern mp : mps) {
                MaliciousPoint point = mp.getNextWithoutRemove();

                if(point == null)
                    continue;

                if (isMaliciousPoint(block.getBlock(), instruction.getCallSite(), point)){
                    matched = true;

                    PropagateFlowFunction f = point.getFlowFunction();

                    IDataPointer dp = data.getData();

                    if(dp instanceof LocalDataPointer){
                        LocalDataPointer ldp = (LocalDataPointer) dp;

                        if(ldp.getNode().equals(block.getBlock().getNode())){
                            if(f.getFrom() == IFlowFunction.ANY || instruction.getUse(f.getFrom()-1) == ldp.getVar()){
                                int v = f.getTo();
                                if(v == IFlowFunction.TERMINATE){
                                    IMethod m = seeds.iterator().next().getNode().getMethod();
                                    warns.add(new MaliciousPatternWarning(new APICallNode(m.getDeclaringClass().getName(), m.getSelector(), entryNum), mp.toString()));
                                }else if(v == IFlowFunction.RETURN_VARIABLE){
                                    res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(block.getBlock().getNode(), instruction.getDef()), data.getWork()));
                                }else{
                                    res.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(block.getBlock().getNode(), v), data.getWork()));
                                }

                                for(DataFlowAnalysis.DataWithWork dww : res){
                                    System.out.println("\t\t" + dww.getData());
                                }
                                mp.getNext();
                            }
                        }
                    }

                }
            }

            if(!matched)
                res.addAll(super.visitInvoke(block, instruction, data));

            return res;
        }
    }
//    public Set<MaliciousPatternWarning> checkPatterns(CallGraph cg){
//        this.cg = cg;
//        this.icfg = ICFGSupergraph.make(cg, new AnalysisCache());
//
//        Set<IMethod> entries = HybridSDKModel.getBridgeEntries();
//
//        int entryNum = 1;
//        for(IMethod entry : entries){
////            System.out.println("#ENTRY: " + entry);
//            Set<MaliciousPattern> mps = clonePatterns();
//
//            for(MaliciousPattern mp : mps){
////                System.out.println("\t#MP: " + mp);
//                Set<NodeWithCallStack> curNodes = new HashSet<>();
//                Set<NodeWithCallStack> nextNodes = new HashSet<>();
//
//                BasicBlockInContext<IExplodedBasicBlock> entryBlock = getBridgeEntryBlock(entry);
//                if(entryBlock == null)
//                    continue;
//
//                Stack entryStack = new Stack();
//                entryStack.push(entryBlock.getNode());
//
//                curNodes.add(new NodeWithCallStack(entryBlock, entryStack));
//
//                MaliciousPoint nextPoint = null;
//                try {
//                    nextPoint = mp.getNextWithoutRemove();
//                }catch(Exception e){
//                    System.out.println("Why? " + e.toString());
//                    continue;
//                }
//
//                while(nextPoint != null){
//
//                    for(NodeWithCallStack n : curNodes)
//                        nextNodes.addAll(findReachableNodes(n, nextPoint));
//
////                    System.out.println("#PT: " + mp);
////                    System.out.println("\t\t#NEXT: " + nextNodes.size());
//
//                    if(nextNodes.isEmpty())
//                        break;
//
//                    mp.removeNext();
//                    nextPoint = mp.getNextWithoutRemove();
//
//                    curNodes.clear();
//                    curNodes.addAll(nextNodes);
//                    nextNodes.clear();
//                }
//
//                if(mp.isDone()){
//
//                    warns.add(new MaliciousPatternWarning(new APICallNode(entry.getDeclaringClass().getName(), entry.getSelector(), entryNum), mp.toString()));
//
//                }
//            }
//
//            entryNum++;
//        }
//
//        return warns;
//    }

    private BasicBlockInContext<IExplodedBasicBlock> getBridgeEntryBlock(IMethod m){
        Iterator<CallSiteReference> icsr = cg.getFakeRootNode().getIR().iterateCallSites();

        while(icsr.hasNext()){
            CallSiteReference csr = icsr.next();
            for(CGNode n : cg.getPossibleTargets(cg.getFakeRootNode(), csr)){
                if(n.getMethod().equals(m))
                    return icfg.getEntriesForProcedure(n)[0];
            }
        }
        System.out.println("====");
        System.out.println(cg.getFakeRootNode().getIR());
        System.out.println("====");
        Assertions.UNREACHABLE("Cannot find the bridge entry: " + m);
//        System.err.println("Cannot find the bridge entry:  + m");
        return null;
    }

    private boolean isMaliciousPoint(BasicBlockInContext<IExplodedBasicBlock> bb, CallSiteReference site, MaliciousPoint mp){
        CGNode n = bb.getNode();

        if (cg.getPossibleTargets(n, site).isEmpty()) {

            APICallNode target = new APICallNode(site.getDeclaredTarget().getDeclaringClass().getName(), site.getDeclaredTarget().getSelector(), 0);

            if (mp.isSame(target, cg.getClassHierarchy()))
                return true;

        } else {
            for (CGNode succ : cg.getPossibleTargets(n, site)) {
                TypeName tn = succ.getMethod().getDeclaringClass().getName();
                Selector selector = succ.getMethod().getSelector();
                APICallNode target = new APICallNode(tn, selector, 0);

                if (mp.isSame(target, cg.getClassHierarchy())) {
                    return true;
                }
            }
        }
        return false;
    }

//    public Set<NodeWithCallStack> findReachableNodes(NodeWithCallStack start, MaliciousPoint mp){
//        Set<NodeWithCallStack> res = new HashSet<>();
//
//        Queue<NodeWithCallStack> queue = new LinkedBlockingQueue<>();
//        Set<BasicBlockInContext<IExplodedBasicBlock>> visited = new HashSet<>();
//
//        queue.add(start);
//
//        while(!queue.isEmpty()){
//            NodeWithCallStack n = queue.poll();
//            visited.add(n.getBlock());
//
//            Iterator<BasicBlockInContext<IExplodedBasicBlock>> iSucc = icfg.getSuccNodes(n.getBlock());
//
//            while(iSucc.hasNext()) {
//                BasicBlockInContext<IExplodedBasicBlock> succ = iSucc.next();
//
//                if(!visited.contains(succ)) {
//
//                    if (icfg.isCall(n.getBlock())) {
//                        if(isMaliciousPoint(n.getBlock(), ((SSAAbstractInvokeInstruction)n.getBlock().getLastInstruction()).getCallSite(), mp)){
//                            res.add(new NodeWithCallStack(n.getBlock(), n.getCallStack()));
//                            System.out.println("== " + mp);
//                            System.out.println("### Stack ###");
//                            Stack<CGNode> cst = n.getCallStack();
//                            for(CGNode cstn : cst)
//                                System.out.println(cstn);
//                            System.out.println("#############");
//                        }else if (icfg.isEntry(succ) && !isPrimitive(succ)) {
//
//                            Stack cst = (Stack) n.callStack.clone();
//                            cst.push(succ.getNode());
//                            queue.add(new NodeWithCallStack(succ, cst));
//
//                        } else if (!icfg.isEntry(succ) && !visited.contains(succ)) {
//
//                            queue.add(new NodeWithCallStack(succ, n.callStack));
//
//                        }
//
//                    } else if (icfg.isExit(n.getBlock())) {
//                        if(callStackPairMatching(n.getCallStack(), succ.getNode())){
//                            Stack cst = (Stack) n.callStack.clone();
//                            cst.pop();
//
//                            queue.add(new NodeWithCallStack(succ, cst));
//                        }
//                    }else{
//                        queue.add(new NodeWithCallStack(succ, n.getCallStack()));
//                    }
//                }
//            }
////            System.out.println("VISITNUM: " + visited.size());
////            System.out.println("QUEUENUM: " + queue.size());
////            System.out.println("QUEUE: " + queue);
////            System.out.println("totalNUM: " + icfg.getNumberOfNodes());
//        }
//
////        System.out.println("\t\t#VISTED SIZE: " + visited.size());
//        return res;
//    }

    private boolean callStackPairMatching(Stack<CGNode> stack, CGNode n){
        if(stack.size() == 1)
            return false;
        else if(stack.get(stack.size() - 2).equals(n))
            return true;

        return false;
    }

    private boolean isPrimitive(BasicBlockInContext<IExplodedBasicBlock> n) {
        if (n.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
            return !ARModeling.isModelingMethod(icfg.getClassHierarchy(), n.getNode().getMethod());

        return false;
    }

    public Set<MaliciousPatternWarning> checkPatterns(){

        for(APICallNode entry : graph.getEntries()){
            Set<MaliciousPattern> mps = clonePatterns();

            for(MaliciousPattern mp : mps){
                APICallNode curNode = entry;
                MaliciousPoint nextPoint = mp.getNextWithoutRemove();
                while(nextPoint != null){
                    curNode = reachableFromMPtoCALLNODE(curNode, nextPoint);
                    if(curNode == null)
                        break;

                    mp.removeNext();
                    nextPoint = mp.getNextWithoutRemove();
                }

                if(mp.isDone()){
                    warns.add(new MaliciousPatternWarning(entry, mp.toString()));
                }
            }
        }

        return warns;
    }

    public APICallNode reachableFromMPtoCALLNODE(APICallNode start, MaliciousPoint mp){
        Queue<APICallNode> queue = new LinkedBlockingQueue<>();
        Set<APICallNode> visited = new HashSet<>();

        queue.add(start);

        while(!queue.isEmpty()){
            APICallNode n = queue.poll();
            visited.add(n);

            Iterator<APICallNode> iSucc = graph.getSuccNodes(n);

            while(iSucc.hasNext()){
                APICallNode succ = iSucc.next();

                if(mp.isSame(succ, cha))
                    return succ;

                if(!visited.contains(succ)){
                    queue.add(succ);
                }
            }
        }

        return null;
    }

    private Set<MaliciousPattern> clonePatterns(){
        Set<MaliciousPattern> clonedPatterns = new HashSet<>();

        for(MaliciousPattern mp : mps){
            clonedPatterns.add(mp.clone());
        }

        return clonedPatterns;
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

        public Set<MaliciousPoint> getPoints(){
            Iterator<MaliciousPoint> iPoint = pattern.iterator();
            Set<MaliciousPoint> mpSet = new HashSet<>();

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


class PathRecorder {
    private Map<Integer, Path> pathMap;
    private ICFGSupergraph superGraph;

    public PathRecorder(ICFGSupergraph superGraph){
        pathMap = new HashMap<>();
        this.superGraph = superGraph;
    }

    public void addNewPath(SSACFG.BasicBlock from, SSACFG.BasicBlock to, boolean mustFlag){
        if(mustFlag || isNeededToRecord(to)){
            pathMap.get(from.getNumber()).addPath(makePathNode(to));
        }


    }

    private boolean isNeededToRecord(SSACFG.BasicBlock bb){
        if(bb.isEntryBlock() || bb.isExitBlock() || (bb.getLastInstruction() != null && bb.getLastInstruction() instanceof SSAAbstractInvokeInstruction))
            return true;
        return false;
    }

    private PathNode makePathNode(SSACFG.BasicBlock bb){
        return new PathNode(bb.getMethod(), bb);
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