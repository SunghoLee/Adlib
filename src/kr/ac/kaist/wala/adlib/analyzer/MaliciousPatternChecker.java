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
import com.ibm.wala.ssa.*;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.callgraph.HybridSDKModel;
import kr.ac.kaist.wala.adlib.dataflow.*;

import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

/**
 * Created by leesh on 14/04/2017.
 */
public class MaliciousPatternChecker {
    private final IClassHierarchy cha;
    private final Set<MaliciousPattern> mps = new HashSet<>();
    private final Set<MaliciousPatternWarning> warns = new HashSet<>();
    private ICFGSupergraph icfg;
    private CallGraph cg;
    private int maxStackSize = 0;

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

    public Set<MaliciousPatternWarning> checkPatterns(CallGraph cg, PointerAnalysis<InstanceKey> pa){
        this.cg = cg;
        this.icfg = ICFGSupergraph.make(cg, new AnalysisCache());

        Set<IMethod> entries = HybridSDKModel.getBridgeEntries();
        DataFlowAnalysis dfa = new DataFlowAnalysis(icfg);


        for(IMethod entry : entries) {
            Set<MPData> seeds = new HashSet<>();

            if(entry.getNumberOfParameters() < 2){
                cg.getNodes(entry.getReference()).forEach((n) -> seeds.add(new MPData(new LocalDataPointer(n, IFlowFunction.ANY), NoMoreWork.getInstance(), clonePatterns(mps))));
            }else{
                for(int i = 2; i <= entry.getNumberOfParameters(); i++) {
                    for(CGNode n : cg.getNodes(entry.getReference())){
                        seeds.add(new MPData(new LocalDataPointer(n, i), NoMoreWork.getInstance(), clonePatterns(mps)));
                    }
                }
            }

            MaliciousPatternFlowSemanticsFunction semFun = new MaliciousPatternFlowSemanticsFunction(cg, cg.getClassHierarchy(), pa, clonePatterns(mps), seeds);
            dfa.analyze(Collections.unmodifiableSet(seeds), semFun, (nextBlock, dp) -> true);
        }

        return warns;
    }

    private static int entryNum = 0;

    class MaliciousPatternFlowSemanticsFunction extends DefaultDataFlowSemanticFunction{
        private final Set<MaliciousPattern> mps;
        private final Set<MPData> seeds;

        public MaliciousPatternFlowSemanticsFunction(CallGraph cg, IClassHierarchy cha, PointerAnalysis<InstanceKey> pa, Set<MaliciousPattern> mps, Set<MPData> seeds) {
            super(cg, cha, pa);
            this.mps = mps;
            this.seeds = seeds;
            entryNum++;
        }

        private Set<DataFlowAnalysis.DataWithWork> wrapToMPData(Set<DataFlowAnalysis.DataWithWork> src, DataFlowAnalysis.DataWithWork from) {
            if(!(from instanceof MPData))
                Assertions.UNREACHABLE("Data must be MPData type.");
            MPData mpFrom = (MPData) from;

            return wrapToMPData(src, mpFrom.getMaliciousPatterns());
        }

        private Set<DataFlowAnalysis.DataWithWork> wrapToMPData(Set<DataFlowAnalysis.DataWithWork> src, Set<MaliciousPattern> mps) {
            return src.stream().map(
                    (dataWithWork) -> new MPData(dataWithWork.getData(), dataWithWork.getWork(), clonePatterns(mps))
            ).collect(Collectors.toSet());
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitGoto(DataFlowAnalysis.NodeWithCS block, SSAGotoInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitGoto(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitArrayLoad(DataFlowAnalysis.NodeWithCS block, SSAArrayLoadInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitArrayLoad(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitArrayStore(DataFlowAnalysis.NodeWithCS block, SSAArrayStoreInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitArrayStore(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitBinaryOp(DataFlowAnalysis.NodeWithCS block, SSABinaryOpInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitBinaryOp(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitUnaryOp(DataFlowAnalysis.NodeWithCS block, SSAUnaryOpInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitUnaryOp(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitConversion(DataFlowAnalysis.NodeWithCS block, SSAConversionInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitConversion(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitComparison(DataFlowAnalysis.NodeWithCS block, SSAComparisonInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitComparison(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitConditionalBranch(DataFlowAnalysis.NodeWithCS block, SSAConditionalBranchInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitConditionalBranch(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitSwitch(DataFlowAnalysis.NodeWithCS block, SSASwitchInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitSwitch(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitReturn(DataFlowAnalysis.NodeWithCS block, SSAReturnInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitReturn(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitGet(DataFlowAnalysis.NodeWithCS block, SSAGetInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitGet(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitPut(DataFlowAnalysis.NodeWithCS block, SSAPutInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitPut(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitNew(DataFlowAnalysis.NodeWithCS block, SSANewInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitNew(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitArrayLength(DataFlowAnalysis.NodeWithCS block, SSAArrayLengthInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitArrayLength(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitThrow(DataFlowAnalysis.NodeWithCS block, SSAThrowInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitThrow(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitMonitor(DataFlowAnalysis.NodeWithCS block, SSAMonitorInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitMonitor(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitCheckCast(DataFlowAnalysis.NodeWithCS block, SSACheckCastInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitCheckCast(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitInstanceof(DataFlowAnalysis.NodeWithCS block, SSAInstanceofInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitInstanceof(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitPhi(DataFlowAnalysis.NodeWithCS block, SSAPhiInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitPhi(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitPi(DataFlowAnalysis.NodeWithCS block, SSAPiInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitPi(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitGetCaughtException(DataFlowAnalysis.NodeWithCS block, SSAGetCaughtExceptionInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitGetCaughtException(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitLoadMetadata(DataFlowAnalysis.NodeWithCS block, SSALoadMetadataInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            return wrapToMPData(super.visitLoadMetadata(block, instruction, data), data);
        }

        @Override
        public Set<DataFlowAnalysis.DataWithWork> visitInvoke(DataFlowAnalysis.NodeWithCS block, SSAInvokeInstruction instruction, DataFlowAnalysis.DataWithWork data) {
            Set<DataFlowAnalysis.DataWithWork> res = new HashSet<>();

            res.add(data);

            boolean matched = false;

            Set<MaliciousPattern> mps = (clonePatterns(((MPData) data).getMaliciousPatterns()));
            Set<DataFlowAnalysis.DataWithWork> matchedRes = new HashSet<>();

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
                                    IMethod m = ((LocalDataPointer)seeds.iterator().next().getData()).getNode().getMethod();
                                    warns.add(new MaliciousPatternWarning(new APICallNode(m.getDeclaringClass().getName(), m.getSelector(), entryNum), mp.toString()));
                                }else if(v == IFlowFunction.RETURN_VARIABLE){
                                    matchedRes.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(block.getBlock().getNode(), instruction.getDef()), data.getWork()));
                                }else{
                                    matchedRes.add(new DataFlowAnalysis.DataWithWork(new LocalDataPointer(block.getBlock().getNode(), instruction.getUse(v-1)), data.getWork()));
                                }

                                mp.getNext();
                            }
                        }
                    }

                }
            }

            if(matched){
                res.addAll(wrapToMPData(matchedRes, mps));
            }else {
                res.addAll(wrapToMPData(super.visitInvoke(block, instruction, data), data));
            }

            return res;
        }
    }

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

    private Set<MaliciousPattern> clonePatterns(Set<MaliciousPattern> mps){
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