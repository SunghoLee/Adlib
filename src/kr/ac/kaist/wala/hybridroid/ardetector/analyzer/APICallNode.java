package kr.ac.kaist.wala.hybridroid.ardetector.analyzer;

import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;

import java.io.Serializable;

/**
 * Rerpesent an API call as a node in ReachableAPIFlowGraph.
 * Created by leesh on 07/04/2017.
 */
public final class APICallNode implements Serializable{

    //TODO: How to represent the callee precisely?

    private static int NODE_NUMBER = 1;
    private static int getNewNumber(){
        return NODE_NUMBER++;
    }

    private final TypeName tn;
    private final Selector selector;
    private int nodeNumber;
    private final int entryNum;

    public APICallNode(TypeName tn, Selector selector, int entryNum) {
        this.tn = tn;
        this.selector = selector;
        this.entryNum = entryNum;
    }

    public void setNumber(int n){
        this.nodeNumber = n;
    }

    public int getNodeNumber(){
        return this.nodeNumber;
    }

    public TypeName getReceiverType(){
        return tn;
    }

    public Selector getSelector(){
        return selector;
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof APICallNode){
            APICallNode acn = (APICallNode) o;
//            if(this.tn.toString().equals("Ljava/io/File") && acn.tn.toString().equals("Ljava/io/File") && this.selector.toString().equals("delete()Z") && acn.selector.toString().equals("delete()Z")){
//                System.out.println("TNEQ? " + acn.tn.equals(this.tn));
//                System.out.println("STEQ? " + acn.selector.equals(this.selector));
//            }
            if(acn.tn.equals(this.tn) && acn.selector.equals(this.selector) && acn.entryNum == this.entryNum)
                return true;
        }
        return false;
    }

    @Override
    public int hashCode(){
        return this.tn.hashCode() + this.selector.hashCode() + this.entryNum;
    }

    @Override
    public String toString(){ return this.tn.toString() + " . " + this.selector.toString();}

}
