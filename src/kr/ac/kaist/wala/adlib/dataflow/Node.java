package kr.ac.kaist.wala.adlib.dataflow;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInstruction;
import kr.ac.kaist.wala.adlib.dataflow.context.Context;

/**
 * Created by leesh on 15/12/2017.
 */
public class Node {
    private final BasicBlockInContext bb;
    private final Context ctxt;
    private static int NUM = 1;
    private final int nodeNum;

    public Node(BasicBlockInContext bb, Context ctxt){
        this.bb = bb;
        this.ctxt = ctxt;
        nodeNum = NUM++;
    }

    public boolean isEntry(){
        return bb.isEntryBlock();
    }

    public boolean isExit(){
        return bb.isExitBlock();
    }

    @Override
    public int hashCode(){
        return bb.hashCode() + ctxt.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Node){
            Node n = (Node) o;
            if(n.bb.equals(this.bb) && n.ctxt.equals(this.ctxt))
                return true;
        }
        return false;
    }

    public Context getContext(){
        return this.ctxt;
    }

    public int getNodeNum(){
        return this.nodeNum;
    }

    public BasicBlockInContext getBB(){
        return this.bb;
    }

    public SSAInstruction getInstruction(){
        return this.bb.getLastInstruction();
    }

    @Override
    public String toString(){
        String res = "";
        res += bb.getNode() + ",\tI: " + bb.getLastInstruction();
        return res;
    }
}
