package kr.ac.kaist.wala.adlib.analyzer;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.AnalysisCache;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.model.ARModeling;
import kr.ac.kaist.wala.hybridroid.util.data.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by leesh on 08/03/2017.
 */
public final class ReachableAPIAnalysis {

    private final Set<IMethod> entries;
    private final Set<APITarget> targets;
    private final ICFGSupergraph icfg;
    private final ReachableAPIFlowGraph flowGraph;
    private final CallGraph cg;
    private final Map<BasicBlockInContext<IExplodedBasicBlock>, APICallNode> blockNodeMap;

    private int blockIndex = 1;

    private static boolean DEBUG = false;
    private BufferedWriter debugBW;

    public ReachableAPIAnalysis(CallGraph cg, Set<IMethod> entries){
        APITarget.set(cg.getClassHierarchy());
        this.cg = cg;
        this.icfg = ICFGSupergraph.make(cg, new AnalysisCache());
        this.entries = entries;
        targets = new HashSet<>();
        flowGraph = new ReachableAPIFlowGraph();
        blockNodeMap = new HashMap<>();

        if(DEBUG) {
            try {
                debugBW = new BufferedWriter(new FileWriter("debug"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void finalize(){
        if(DEBUG) {
            try {
                debugBW.flush();
                debugBW.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addAPITarget(APITarget target){
        targets.add(target);
    }

    public void addAPITargets(Set<APITarget> targets){
        this.targets.addAll(Lists.newArrayList(targets));
    }

    private APITarget getTargetIfItIs(BasicBlockInContext<IExplodedBasicBlock> bb, CallSiteReference site){
        CGNode n = bb.getNode();

        if(cg.getPossibleTargets(n, site).isEmpty()){
            APITarget target = new APITarget(site.getDeclaredTarget().getDeclaringClass().getName(), site.getDeclaredTarget().getSelector());
//            return target;
            for (APITarget t : targets) {
                if (APITarget.isContains(t, target))
                    return target;
            }
        }else {
            //we need to check others?
            for (CGNode succ : cg.getPossibleTargets(n, site)) {
                TypeName tn = succ.getMethod().getDeclaringClass().getName();
                Selector selector = succ.getMethod().getSelector();
                APITarget target = new APITarget(tn, selector);

                for (APITarget t : targets) {
                    if (APITarget.isContains(t, target))
                        return target;
                }
            }
        }
        return null;
    }

    private void depthFirstTraversalIter(Stack<Pair<BasicBlockInContext<IExplodedBasicBlock>, Stack<BasicBlockInContext<IExplodedBasicBlock>>>> stack, Set<BasicBlockInContext<IExplodedBasicBlock>> exitPoints) throws IOException {
        Set<BasicBlockInContext<IExplodedBasicBlock>> visited = new HashSet<>();

        while(!stack.isEmpty()){
            Pair<BasicBlockInContext<IExplodedBasicBlock>, Stack<BasicBlockInContext<IExplodedBasicBlock>>> p = stack.pop();
            BasicBlockInContext<IExplodedBasicBlock> bb = p.fst();

            visited.add(bb);

            if(exitPoints.contains(bb)) {
                continue;
            }

            if(icfg.isCall(bb)){
                SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) bb.getLastInstruction();
                APITarget target = getTargetIfItIs(bb, invokeInst.getCallSite());
                if(target != null){
                    APICallNode cn = recordTrace(p.snd(), bb, target);
                    if(DEBUG) {
                        debugBW.write("%%%%%%RECORD : " + cn);
                        debugBW.newLine();
                    }
                }
            }

            Iterator<BasicBlockInContext<IExplodedBasicBlock>> isucc = icfg.getSuccNodes(bb);
            List<BasicBlockInContext<IExplodedBasicBlock>> succSet = new ArrayList<>();

            while(isucc.hasNext()){
                succSet.add(isucc.next());
            }

            BasicBlockInContext<IExplodedBasicBlock>[] pathArr = p.snd().toArray(new BasicBlockInContext[0]);
            Set<BasicBlockInContext<IExplodedBasicBlock>> pathSet = new HashSet<>(Arrays.asList(pathArr));

            for(BasicBlockInContext<IExplodedBasicBlock> succ : succSet){
                if(exitPoints.contains(succ)) {
                    continue;
                }

                if(icfg.isCall(bb) && isNeedToVisit(bb, visited) && !icfg.isEntry(succ)) {
                    if(DEBUG) {
                        try {
                            debugBW.write("--NOT_TRACKING--");
                            debugBW.newLine();
                            debugBW.write("CUR: " + bb + " : " + bb.getLastInstruction());
                            debugBW.newLine();
                            debugBW.write("SUCC: " + succ + " : " + succ.getLastInstruction());
                            debugBW.newLine();
                            debugBW.write("\tISCALL: " + icfg.isCall(bb));
                            debugBW.newLine();
                            debugBW.write("\tISNEXTPRIMORDIAL: " + isNeedToVisit(bb, visited));
                            debugBW.newLine();
                            debugBW.write("\tISENTRY: " + !icfg.isEntry(succ));
                            debugBW.newLine();
                            debugBW.write("--");
                            debugBW.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    continue;
                }
                else if(icfg.isCall(bb) && isNeedToVisit(bb, visited)){
                    if(DEBUG) {
                        try {
                            debugBW.write("=====GOOGO+=====: PREV: " + findPreviousAPICallNode(p.snd()));
                            debugBW.newLine();
                            debugBW.write("CUR: " + bb + " : " + bb.getLastInstruction());
                            debugBW.newLine();
                            debugBW.write("SUCC: " + succ + " : " + succ.getLastInstruction());
                            debugBW.newLine();
                            debugBW.write("--");
                            debugBW.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }else if(icfg.isExit(bb)){
                    if(DEBUG) {
                        debugBW.write("=====EXIT=====");
                        debugBW.newLine();
                        debugBW.write("CUR: " + bb + " : " + bb.getLastInstruction());
                        debugBW.newLine();
                        debugBW.write("SUCC: " + succ + " : " + succ.getLastInstruction());
                        debugBW.newLine();
                        debugBW.write("--");
                        debugBW.newLine();
                    }
                }
                if(DEBUG) {
                    debugBW.write("BB: " + bb + " : " + bb.getLastInstruction() + " <= " + findPreviousAPICallNode(p.snd()));
                    debugBW.newLine();
                    debugBW.write("\tNEXT: " + succ + " : " + succ.getLastInstruction());
                    debugBW.newLine();
                }
                int stackSize = stack.size();

                Stack newStack = ((Stack)p.snd().clone());
                newStack.push(bb);

                if(!isPrimitive(succ)){
                    if(isPairMatching(newStack, succ, false)) {
                        if (succ.isExitBlock()) {
                            if (isLastInstruction(bb, succ)) {
//                                newStack.push(bb);
                                if(visited.contains(succ)){
                                    newStack.push(succ);
                                    JoinPoint jp = new JoinPoint(succ, findPreviousAPICallNode(newStack), getCallStack(new Stack(), newStack));
                                    jpSet.add(jp);
                                    if(DEBUG) {
                                        debugBW.write(" ===> " + jp);
                                        debugBW.newLine();
                                    }
                                }else
                                    stack.push(Pair.make(succ, newStack));
                            }
                        } else {
//                            newStack.push(bb);
                            if(visited.contains(succ)){
                                newStack.push(succ);
                                JoinPoint jp = new JoinPoint(succ, findPreviousAPICallNode(newStack), getCallStack(new Stack(), newStack));
                                jpSet.add(jp);
                                if(DEBUG) {
                                    debugBW.write(" ===> " + jp);
                                    debugBW.newLine();
                                }
                            }else
                                stack.push(Pair.make(succ, newStack));
                        }
                    }
                }

                if(DEBUG) {
                    debugBW.write("\tADDED? " + (stack.size() != stackSize));
                    debugBW.newLine();
                    if (stack.size() == stackSize) {
                        debugBW.write("\t\tWHY_VISITED? " + visited.contains(succ));
                        debugBW.newLine();
                        debugBW.write("\t\tWHY_PRIMITIVE? " + isPrimitive(succ));
                        debugBW.newLine();
                        debugBW.write("\t\tWHY_PAIRMATCHING? " + !isPairMatching(newStack, succ, false));
                        debugBW.newLine();
                        if (succ.isExitBlock()) {
                            debugBW.write("\t\tWHY_LASTINST? " + !isLastInstruction(bb, succ));
                            debugBW.newLine();
                        }
                    }
                }
            }
        }
    }

    private boolean isNeedToVisit(BasicBlockInContext<IExplodedBasicBlock> bb, Set<BasicBlockInContext<IExplodedBasicBlock>> visited){
        Set<CGNode> callees = cg.getPossibleTargets(bb.getNode(), ((SSAAbstractInvokeInstruction)bb.getLastInstruction()).getCallSite());

        if(callees.size() == 0)
            return false;

        for(CGNode callee : callees){
            if(isPrimitive(callee))
                return false;
        }

        Iterator<BasicBlockInContext<IExplodedBasicBlock>> isucc = icfg.getSuccNodes(bb);
        List<BasicBlockInContext<IExplodedBasicBlock>> succSet = new ArrayList<>();

        while(isucc.hasNext()){
            succSet.add(isucc.next());
        }

        for(BasicBlockInContext<IExplodedBasicBlock> succ : succSet){
            if(icfg.isEntry(succ)){
                if(visited.contains(succ)){
                    return false;
                }
            }
        }

        return true;
    }

    private void depthFirstTraversal(Stack<BasicBlockInContext<IExplodedBasicBlock>> stack, BasicBlockInContext<IExplodedBasicBlock> bb, Set<BasicBlockInContext<IExplodedBasicBlock>> exitPoints){
        if(exitPoints.contains(bb)) {
            stack.pop();
            return;
        }

        if(bb.getLastInstruction() != null && bb.getLastInstruction() instanceof SSAAbstractInvokeInstruction){
            SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) bb.getLastInstruction();
            APITarget target = getTargetIfItIs(bb, invokeInst.getCallSite());

            if(target != null){
                recordTrace(stack, bb, target);
            }
        }

        Iterator<BasicBlockInContext<IExplodedBasicBlock>> isucc = icfg.getSuccNodes(bb);
        List<BasicBlockInContext<IExplodedBasicBlock>> succSet = new ArrayList<>();

        while(isucc.hasNext()){
            succSet.add(isucc.next());
        }

        for(BasicBlockInContext<IExplodedBasicBlock> succ : succSet){
            if(!stack.contains(succ) && !isPrimitive(succ)){
                if(isPairMatching(stack, succ, false)) {
                    if (succ.isExitBlock()) {
                        if (isLastInstruction(bb, succ)) {
                            stack.add(succ);
                            depthFirstTraversal(stack, succ, exitPoints);
                        }
                    } else {
                        stack.add(succ);
                        depthFirstTraversal(stack, succ, exitPoints);
                    }
                }
            }
        }

        stack.pop();
    }

    private Set<APICallNode> findNextCallNodes(Stack<CGNode> baseCallStack, BasicBlockInContext<IExplodedBasicBlock> startBlock, APICallNode callNode) throws IOException {
        Set<APICallNode> res = new HashSet<>();
        Set<BasicBlockInContext<IExplodedBasicBlock>> visited = new HashSet<>();
        Stack<Pair<BasicBlockInContext<IExplodedBasicBlock>, Stack<BasicBlockInContext<IExplodedBasicBlock>>>> stack = new Stack<>();
//        if(DEBUG) {
//            System.out.println("=========");
//            System.out.println("START: " + startBlock + " : " + startBlock.getLastInstruction());
//        }
        stack.push(Pair.make(startBlock, new Stack()));
        while(!stack.isEmpty()){
            Pair<BasicBlockInContext<IExplodedBasicBlock>, Stack<BasicBlockInContext<IExplodedBasicBlock>>> p = stack.pop();
            BasicBlockInContext<IExplodedBasicBlock> bb = p.fst();
//            if(DEBUG){
//                System.out.println("NODE: " + bb + " : " + bb.getLastInstruction());
//            }
            visited.add(bb);

            int stackSize = stack.size();

            /*
            for(BasicBlockInContext<IExplodedBasicBlock> succ : succSet){

             */
            if(blockNodeMap.keySet().contains(bb)){
                res.add(blockNodeMap.get(bb));
            }else {
                Iterator<BasicBlockInContext<IExplodedBasicBlock>> isucc = icfg.getSuccNodes(bb);
                List<BasicBlockInContext<IExplodedBasicBlock>> succSet = new ArrayList<>();

                while (isucc.hasNext()) {
                    succSet.add(isucc.next());
                }

                BasicBlockInContext<IExplodedBasicBlock>[] pathArr = p.snd().toArray(new BasicBlockInContext[0]);
                Set<BasicBlockInContext<IExplodedBasicBlock>> pathSet = new HashSet<>(Arrays.asList(pathArr));

                for (BasicBlockInContext<IExplodedBasicBlock> succ : succSet) {
                    if(icfg.isCall(bb) && isNeedToVisit(bb, visited) && !icfg.isEntry(succ)) {
                        if(DEBUG) {
                            try {
                                debugBW.write("--NOT_TRACKING--");
                                debugBW.newLine();
                                debugBW.write("CUR: " + bb + " : " + bb.getLastInstruction());
                                debugBW.newLine();
                                debugBW.write("SUCC: " + succ + " : " + succ.getLastInstruction());
                                debugBW.newLine();
                                debugBW.write("\tISCALL: " + icfg.isCall(bb));
                                debugBW.newLine();
                                debugBW.write("\tISNEXTPRIMORDIAL: " + isNeedToVisit(bb, visited));
                                debugBW.newLine();
                                debugBW.write("\tISENTRY: " + !icfg.isEntry(succ));
                                debugBW.newLine();
                                debugBW.write("--");
                                debugBW.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        continue;
                    }
                    else if(icfg.isCall(bb) && isNeedToVisit(bb, visited)){
                        if(DEBUG) {
                            try {
                                debugBW.write("=====GOOGO+=====: PREV: " + callNode);
                                debugBW.newLine();
                                debugBW.write("CUR: " + bb + " : " + bb.getLastInstruction());
                                debugBW.newLine();
                                debugBW.write("SUCC: " + succ + " : " + succ.getLastInstruction());
                                debugBW.newLine();
                                debugBW.write("--");
                                debugBW.newLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }else if(icfg.isExit(bb)){
                        if(DEBUG) {
                            debugBW.write("=====EXIT=====");
                            debugBW.newLine();
                            debugBW.write("CUR: " + bb + " : " + bb.getLastInstruction());
                            debugBW.newLine();
                            debugBW.write("SUCC: " + succ + " : " + succ.getLastInstruction());
                            debugBW.newLine();
                            debugBW.write("--");
                            debugBW.newLine();
                        }
                    }
                    if(DEBUG) {
                        debugBW.write("BB: " + bb + " : " + bb.getLastInstruction() + " <= " + callNode);
                        debugBW.newLine();
                        debugBW.write("\tNEXT: " + succ + " : " + succ.getLastInstruction());
                        debugBW.newLine();
                    }
                    Stack newStack = ((Stack) p.snd().clone());
                    newStack.push(bb);
                    if(!visited.contains(succ) && !isPrimitive(succ)){
                        if(isPairMatching(baseCallStack, newStack, succ)) {
                            if (succ.isExitBlock()) {
                                if (isLastInstruction(bb, succ)) {
    //                                newStack.push(bb);
                                    stack.push(Pair.make(succ, newStack));
                                }
                            } else {
//                                newStack.push(bb);
                                stack.push(Pair.make(succ, newStack));
                            }
                        }
                    }

                    if(DEBUG) {
                        debugBW.write("\tADDED? " + (stack.size() != stackSize));
                        debugBW.newLine();
                        if (stack.size() == stackSize) {
                            debugBW.write("\t\tWHY_VISITED? " + visited.contains(succ));
                            debugBW.newLine();
                            debugBW.write("\t\tWHY_PRIMITIVE? " + isPrimitive(succ));
                            debugBW.newLine();
                            debugBW.write("\t\tWHY_PAIRMATCHING? " + !isPairMatching(baseCallStack, newStack, succ));
                            debugBW.newLine();
                            if (succ.isExitBlock()) {
                                debugBW.write("\t\tWHY_LASTINST? " + !isLastInstruction(bb, succ));
                                debugBW.newLine();
                            }
                        }
                    }
                }
            }
        }
//        if(DEBUG)
//        System.out.println("=========");

        return res;
    }

//    private boolean DEBUG = false;

    private void mergeJoinPoints(){
        if(DEBUG) {
            try {
                debugBW.flush();
                debugBW.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int i = 1;

        Set<JoinPoint> newJp = adjustPathForJoinPoint(jpSet);

        for (JoinPoint jp : newJp) {
            String debugFileName = "debug" + (i++);
            if(DEBUG) {
                try {
                    debugBW = new BufferedWriter(new FileWriter(debugFileName));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                System.out.println(debugFileName + " => " + jp.toString());
                if (!jpSet.contains(jp))
                    System.out.println("\t\tTHISTHISTHIS!");
            }
            BasicBlockInContext<IExplodedBasicBlock> bb = jp.getBlock();
            APICallNode previousNode = jp.getPreviousCallNode();
            if(DEBUG) {
                try {
                    debugBW.write(jp.toString());
                    debugBW.newLine();
                    printCallStack(jp.callStack, debugBW);
                    debugBW.write("#PREV: " + previousNode);
                    debugBW.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            if(previousNode.toString().equals("Landroid/location/LocationManager . getLastKnownLocation(Ljava/lang/String;)Landroid/location/Location;")){
//                DEBUG = true;
//            }
            try {
                for (APICallNode next : findNextCallNodes(jp.getCallStack(), bb, jp.getPreviousCallNode())) {
                    flowGraph.addEdge(previousNode, next);
                    if(DEBUG) {
                        try {
                            System.out.println("ADDED FLOW FROM " + jp.getPreviousCallNode() + " to " + next);
                            debugBW.write("\t#NEXT: " + next);
                            debugBW.newLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(DEBUG) {
                try {
                    debugBW.flush();
                    debugBW.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

//    private Set<JoinPoint> findAllPoint(){
//        Set<JoinPoint> jpSet = new HashSet<>();
//        for(BasicBlockInContext<IExplodedBasicBlock> bb : icfg){
//            if(icfg.isCall(bb)){
//                SSAAbstractInvokeInstruction invokeInst = (SSAAbstractInvokeInstruction) bb.getLastInstruction();
//                APITarget target = getTargetIfItIs(bb, invokeInst.getCallSite());
//                if(target != null){
//                    APICallNode curCallNode = flowGraph.makeNewNode(target.getTypeName(), target.getSelector());
//                    blockNodeMap.put(bb, curCallNode);
//                }
//            }
//        }
//    }
    private boolean isSameCallSequence(Stack a, Stack b){
        if(a.size() == b.size()){
            for(int i = 0; i < a.size(); i++){
                if(!a.get(i).equals(b.get(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    public ReachableAPIFlowGraph analyze(){
        for(IMethod entry : entries){
            System.out.println("\tFinding reachable APIs from " + entry);
            flowGraph.newEntry();
            BasicBlockInContext<IExplodedBasicBlock> bb = getBridgeEntryBlock(entry);

            blockNodeMap.put(bb, flowGraph.makeNewNode(entry.getDeclaringClass().getName(), entry.getSelector()));

            BasicBlockInContext<IExplodedBasicBlock>[] exits = getBridgeExitBlock(entry);

            Set<BasicBlockInContext<IExplodedBasicBlock>> exitSet = new HashSet<>(Arrays.asList(exits));

            Stack<Pair<BasicBlockInContext<IExplodedBasicBlock>,Stack<BasicBlockInContext<IExplodedBasicBlock>>>> stack = new Stack();

            stack.add(Pair.make(bb, new Stack()));

            try {
                depthFirstTraversalIter(stack, exitSet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println("\t\tMerging paths for " + entry);

            mergeJoinPoints();
            jpSet.clear();
            blockNodeMap.clear();
        }
        return flowGraph;
    }

    private boolean isPrimitive(BasicBlockInContext<IExplodedBasicBlock> n){
        if(n.getNode().getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
            return !ARModeling.isModelingMethod(icfg.getClassHierarchy(), n.getNode().getMethod());

        return false;
    }

    private boolean isPrimitive(CGNode n){
        if(n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial))
            return !ARModeling.isModelingMethod(icfg.getClassHierarchy(), n.getMethod());

        return false;
    }

    private static int POINT_ID = 1;

    class JoinPoint {
        private final BasicBlockInContext<IExplodedBasicBlock> bb;
        private final APICallNode predCallNode;
        private final Stack<CGNode> callStack;
        private final int pid;
        public JoinPoint(BasicBlockInContext<IExplodedBasicBlock> bb, APICallNode predCallNode, Stack<CGNode> callStack){
            this.bb = bb;
            this.predCallNode = predCallNode;
            this.callStack = callStack;
            pid = POINT_ID++;
        }

        @Override
        public int hashCode(){
            return predCallNode.hashCode() + bb.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof JoinPoint){
                JoinPoint jp = (JoinPoint) o;
                if(jp.bb.equals(this.bb) && jp.predCallNode.equals(this.bb)) {
                    if(isSameCallSequence(jp.callStack, this.callStack))
                        return true;
                }
            }
            return false;
        }

        public BasicBlockInContext<IExplodedBasicBlock> getBlock(){
            return this.bb;
        }

        public APICallNode getPreviousCallNode(){
            return this.predCallNode;
        }

        public Stack<CGNode> getCallStack(){
            return this.callStack;
        }

        public int getPID(){
            return pid;
        }

        @Override
        public String toString(){
            return "[JP " + pid + "] " + this.bb + " : " + bb.getLastInstruction() + " <= " + this.predCallNode;
        }
    }


//    private Set<APICallNode> findAllCallNodesFromJoinPoint(JoinPoint jp){
//
//    }

    private Set<JoinPoint> jpSet = new HashSet<>();
    private Set<JoinPoint> totalJpSet = new HashSet<>();

    private APICallNode findPreviousAPICallNode(Stack<BasicBlockInContext<IExplodedBasicBlock>> stack){
        BasicBlockInContext<IExplodedBasicBlock>[] arr = stack.toArray(new BasicBlockInContext[0]);
        List<BasicBlockInContext<IExplodedBasicBlock>> list = Lists.newArrayList(arr);
        list = Lists.reverse(list);

        for(BasicBlockInContext<IExplodedBasicBlock> bb : list){
            if(blockNodeMap.keySet().contains(bb)){
                return blockNodeMap.get(bb);
            }
        }
        System.out.println("?NODE? ");
        printStack(stack);
        if(!stack.isEmpty())
            Assertions.UNREACHABLE("Every node must have an entry: " + stack);
        return null;
    }

    private APICallNode recordTrace(Stack<BasicBlockInContext<IExplodedBasicBlock>> stack, BasicBlockInContext<IExplodedBasicBlock> current, APITarget target){
        APICallNode previousNode = findPreviousAPICallNode(stack);
        APICallNode curCallNode = flowGraph.makeNewNode(target.getTypeName(), target.getSelector());

        flowGraph.addEdge(previousNode, curCallNode);
//        System.out.println("#########################Edge: " + previousNode + " -> " + curCallNode);
        blockNodeMap.put(current, curCallNode);

        return curCallNode;
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

        Assertions.UNREACHABLE("Cannot find the bridge entry: " + m);
        return null;
    }

    private BasicBlockInContext<IExplodedBasicBlock>[] getBridgeExitBlock(IMethod m){
        Iterator<CallSiteReference> icsr = cg.getFakeRootNode().getIR().iterateCallSites();

        while(icsr.hasNext()){
            CallSiteReference csr = icsr.next();
            for(CGNode n : cg.getPossibleTargets(cg.getFakeRootNode(), csr)){
                if(n.getMethod().equals(m))
                    return icfg.getExitsForProcedure(n);
            }
        }

        Assertions.UNREACHABLE("Cannot find the bridge entry: " + m);
        return null;
    }

    private void printGraphForMethod(IMethod m){
        BasicBlockInContext<IExplodedBasicBlock> entry = getBridgeEntryBlock(m);

        Iterator<BasicBlockInContext<IExplodedBasicBlock>> iSucc = icfg.getSuccNodes(entry);
    }

    private void printStack(Stack stack){
        BasicBlockInContext[] blocks = (BasicBlockInContext[]) stack.toArray(new BasicBlockInContext[0]);
        System.out.println("===");
        for(BasicBlockInContext<IExplodedBasicBlock> i : blocks){
            System.out.println(i + " , INST: " + i.getLastInstruction());
        }
        System.out.println("=== SIZE: " + stack.size());
    }

    private boolean isLastInstruction(BasicBlockInContext<IExplodedBasicBlock> bb, BasicBlockInContext<IExplodedBasicBlock> succ){
        SSAInstruction inst = bb.getLastInstruction();

//        if(true)
//            return true;
        if(bb.getNode().equals(succ.getNode())) {
            if (inst == null)
                return false;

            if (inst instanceof SSAReturnInstruction)
                return true;
            else {
                SSAInstruction[] insts = bb.getNode().getIR().getInstructions();

                for (int i = inst.iindex + 1; i < insts.length; i++) {
                    if (insts[i] != null)
                        return false;
                }

                return true;
            }
        }else{
            CGNode callee = bb.getNode();
            CGNode caller = succ.getNode();

            Iterator<CallSiteReference> iCsr = cg.getPossibleSites(caller, callee);
            SSAInstruction[] insts = caller.getIR().getInstructions();

            while(iCsr.hasNext()){
                CallSiteReference csr = iCsr.next();

                for(SSAAbstractInvokeInstruction invokeInst : caller.getIR().getCalls(csr)){
                    for (int i = invokeInst.iindex + 1; i < insts.length; i++) {
                        if (insts[i] != null)
                            return false;
                    }
                }
            }
            System.out.println("FOUND?");
            System.out.println("BB: " + bb);
            System.out.println("SUCC: " + succ);
            System.out.println("====");
            return true;
        }
    }

    private boolean isPairMatching(Stack<CGNode> base, Stack<BasicBlockInContext<IExplodedBasicBlock>> stack, BasicBlockInContext<IExplodedBasicBlock> succ){
        BasicBlockInContext<IExplodedBasicBlock> cur = stack.peek();
        //cur is entry or exit

//        if(DEBUG) {
//            System.out.println("INITIAL =>  ");
//            printCallStack(base);
//            printStack(stack);
//        }
        if(cur.isExitBlock()){
            Stack<CGNode> nodeStack = (Stack<CGNode>) base.clone();

            BasicBlockInContext[] blocks = stack.toArray(new BasicBlockInContext[0]);
            for(int i=0; i<blocks.length; i++){
                BasicBlockInContext bb = blocks[i];

                if(i == 0 && icfg.isEntry(bb) && !nodeStack.isEmpty() && bb.getNode().equals(nodeStack.peek())){
                    //no-op
                }else if(bb.isEntryBlock())
                    nodeStack.push(bb.getNode());
                else if(bb.isExitBlock()){
                    nodeStack.pop();
                }
            }
//            CGNode top = nodeStack.peek();
            if(nodeStack.size() == 0)
                return false;

            if(nodeStack.pop().equals(succ.getNode())) {
//                nodeStack.push(top);
//                if(DEBUG){
//                    printCallStack(nodeStack);
//                    System.out.println("\tSTACKTOP: " + top);
//                    System.out.println("\tSUCCNode: " + succ.getNode());
//                    System.out.println("\tPAIRMATCHING: TRUE");
//                }
                return true;
            }else {
//                nodeStack.push(top);
//                if(DEBUG){
//                    printCallStack(nodeStack);
//                    System.out.println("\tSTACKTOP: " + top);
//                    System.out.println("\tSUCCNode: " + succ.getNode());
//                    System.out.println("\tPAIRMATCHING: FALSE");
//                }
                return false;
            }
        }

//        if(DEBUG){
//            System.out.print("\tPAIRMATCHING: TRUE");
//        }
        return true;
    }

    private boolean isPairMatching(Stack<BasicBlockInContext<IExplodedBasicBlock>> stack, BasicBlockInContext<IExplodedBasicBlock> succ, boolean debug){
        return isPairMatching(new Stack(), stack, succ);
    }

    private Stack<CGNode> getCallStack(Stack<CGNode> base, Stack<BasicBlockInContext<IExplodedBasicBlock>> stack){
        Stack<CGNode> nodeStack = new Stack<>();

//        printStack(stack);
        nodeStack.addAll(base);

        BasicBlockInContext[] blocks = stack.toArray(new BasicBlockInContext[0]);
        for(int i=0; i<blocks.length; i++){
            BasicBlockInContext bb = blocks[i];

            if(bb.isEntryBlock())
                nodeStack.push(bb.getNode());
            else if(i != 0 && blocks[i-1].isExitBlock()){
                nodeStack.pop();
            }
        }

//        printCallStack(nodeStack);
//        try {
//            System.in.read();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return nodeStack;
    }

    private void printCallStack(Stack<CGNode> cs){
        CGNode[] nodes = cs.toArray(new CGNode[0]);
        System.out.println("->");
        for(CGNode n : nodes){
            System.out.println("#N : " + n);
        }
        System.out.println("<-");
    }

    private void printCallStack(Stack<CGNode> cs, BufferedWriter bw) throws IOException {
        CGNode[] nodes = cs.toArray(new CGNode[0]);
        bw.write("->");
        bw.newLine();
        for(CGNode n : nodes){
            bw.write("#N : " + n);
            bw.newLine();
        }
        bw.write("<-");
        bw.newLine();
    }

    private Set<JoinPoint> adjustPathForJoinPoint(Set<JoinPoint> jp){
        Set<JoinPoint> newJsSet = new HashSet<>(jp);
        Set<JoinPoint> sJp = new HashSet<>(jp);

        if(DEBUG) {
            System.out.println("AdjustJoinPoint: " + jp.size());
        }
        for(JoinPoint a : jp){
            for(JoinPoint b : sJp){
                if(isPathContains(a.getCallStack(), b.getCallStack())){
                    JoinPoint[] newJPs = cloneJoinPointWithSubPath(a, b);

                    newJsSet.add(newJPs[0]);
                    newJsSet.add(newJPs[1]);
                }
            }
        }
        if(DEBUG) {
            System.out.println("\t=> " + newJsSet.size());
        }
        return newJsSet;
    }

    private boolean isPathContains(Stack a, Stack b){
        if(a.size() == 1 || b.size() == 1)
            return false;

        Object ob = b.peek();

        if(a.contains(ob) && !a.peek().equals(ob)){
            int index = a.indexOf(ob);

            for(int i = 0; i < index; i++){
                if(!a.get(i).equals(b.get(i)))
                    return true;
            }
        }

        return false;
    }

    private Stack cloneStackWithSubPath(Stack a, Stack b){
        int index = a.indexOf(b.peek());
        Stack newStack = (Stack) b.clone();

        for(int i=index+1; i<a.size(); i++){
            newStack.push(a.get(i));
        }
        return newStack;
    }

    private JoinPoint[] cloneJoinPointWithSubPath(JoinPoint a, JoinPoint b){
        Stack s1 = cloneStackWithSubPath(a.callStack, b.callStack);
        Stack s2 = (Stack) s1.clone();
        return new JoinPoint[]{new JoinPoint(a.getBlock(), b.predCallNode, s1), new JoinPoint(a.getBlock(), a.predCallNode, s2)};
    }
}


