package kr.ac.kaist.wala.adlib.dataflow.context;

import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.Node;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

/**
 * Created by leesh on 18/12/2017.
 */
public class CallStackContext implements Context {
    private final CallStack cs;
    private final ICFGSupergraph supergraph;

    public CallStackContext(ICFGSupergraph supergraph){
        this.supergraph = supergraph;
        this.cs = new CallStack();
    }

    public CallStackContext(ICFGSupergraph supergraph, CallStack cs){
        this.supergraph = supergraph;
        this.cs = cs.clone();
    }

    @Override
    public int hashCode() {
        return cs.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return cs.equals(obj);
    }

    @Override
    public Set<Node> getNext(Node curNode, Set<BasicBlockInContext> nexts, TYPE type) {
        Set<Node> res = new HashSet<>();

        switch(type){
            case CALL:
                CallStack newcs = this.cs.clone();
                newcs.push(new CallSite(curNode.getBB().getNode(), supergraph.getLocalBlockNumber(curNode.getBB())));
                for(BasicBlockInContext bb : nexts)
                    res.add(new Node(bb, new CallStackContext(this.supergraph, newcs.clone())));
                break;
            case EXIT:
                if(!this.cs.isEmpty()) {
                    CallSite cs = this.cs.peek();
                    CallStack newCS = this.cs.clone();

                    Iterator<BasicBlockInContext> iSucc = supergraph.getSuccNodes(cs.getCallBlock());
                    Set<Integer> succSet = new HashSet<>();

                    while(iSucc.hasNext()){
                        BasicBlockInContext succ = iSucc.next();
                        if(!succ.isEntryBlock() && !succ.isExitBlock()) {
                            succSet.add(supergraph.getLocalBlockNumber(succ));
                        }
                    }

                    for (BasicBlockInContext bb : nexts) {
                        if (cs.getNode().equals(bb.getNode()) && succSet.contains(supergraph.getLocalBlockNumber(bb)))
                            res.add(new Node(bb, new CallStackContext(this.supergraph, newCS.clone())));
                    }
                }
                break;
            case NORMAL:
                for(BasicBlockInContext bb : nexts)
                    res.add(new Node(bb, new CallStackContext(this.supergraph, this.cs)));
                break;
        }
        return res;
    }

    @Override
    public boolean isFeasibleReturn(CGNode n, SSAAbstractInvokeInstruction invokeInst) {
        CallSite cs = this.cs.peek();
        SSAInstruction inst = cs.getCallBlock().getLastInstruction();
        CGNode caller = cs.getNode();

        if(inst == null)
            Assertions.UNREACHABLE("CallSite must always have a call instruction.");

        if(caller.equals(n) && inst.iindex == invokeInst.iindex)
            return true;

        return false;
    }

    class CallStack {
        private Stack<CallSite> callSites = new Stack<>();

        public CallSite peek(){
            if(callSites.size() != 0)
                return callSites.peek();
            return null;
        }

        public CallSite pop(){
            if(callSites.size() != 0)
                return callSites.pop();
            return null;
        }

        public CallStack push(CallSite cs){
            callSites.add(cs);
            return this;
        }

        private CallStack addAll(Stack<CallSite> cs){
            callSites.addAll(cs);
            return this;
        }

        public CallStack clone(){
            return (new CallStack()).addAll(callSites);
        }

        @Override
        public int hashCode(){
            return callSites.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof CallStack){
                CallStack cs = (CallStack) o;
                if(cs.callSites.size() == callSites.size()){
                    for(int i = 0; i < callSites.size(); i++){
                        if(!cs.callSites.get(i).equals(callSites.get(i)))
                            return false;
                    }
                    return true;
                }
            }
            return false;
        }

        public boolean isEmpty(){
            return callSites.isEmpty();
        }
    }

    class CallSite {
        private CGNode n;
        private int nodeNum;

        public CallSite(CGNode n, int nodeNum){
            this.n = n;
            this.nodeNum = nodeNum;
        }

        public CGNode getNode(){
            return n;
        }

        public int getNodeNum(){
            return nodeNum;
        }

        public BasicBlockInContext getCallBlock(){
            return supergraph.getLocalBlock(n, nodeNum);
        }

        @Override
        public int hashCode(){
            return n.hashCode() + nodeNum;
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof CallSite) {
                CallSite cs = (CallSite) o;
                if(cs.n.equals(n) && cs.nodeNum == nodeNum)
                    return true;
            }
            return false;
        }

        @Override
        public String toString(){
            String res = n + "\t" + getCallBlock().getLastInstruction();
            return res;
        }
    }
}
