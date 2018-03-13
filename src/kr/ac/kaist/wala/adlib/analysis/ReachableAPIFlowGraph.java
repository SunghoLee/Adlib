package kr.ac.kaist.wala.adlib.analysis;

import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.util.graph.NumberedGraph;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.IntSetUtil;
import com.ibm.wala.util.intset.MutableIntSet;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;

/**
 * Created by leesh on 07/04/2017.
 */
public class ReachableAPIFlowGraph implements NumberedGraph<APICallNode>, Serializable{
    private final List<APICallNode> nodeList;
    private final Map<Integer, Set<Integer>> forwardEdges;
    private final Map<Integer, Set<Integer>> backwardEdges;

    private int nodeNum = 1;
    private int entryNum = 1;
    public ReachableAPIFlowGraph(){
        this.nodeList = new ArrayList<APICallNode>();
        this.forwardEdges = new HashMap<>();
        this.backwardEdges = new HashMap<>();
    }

    public void newEntry(){
        entryNum++;
    }

    public APICallNode makeNewNode(TypeName tn, Selector s){
        APICallNode newNode = new APICallNode(tn, s, entryNum);
        if(nodeList.contains(newNode))
            return nodeList.get(nodeList.indexOf(newNode));
        return newNode;
    }

    public APICallNode makeNewNode(TypeName tn, Selector s, int entryNum){
        APICallNode newNode = new APICallNode(tn, s, entryNum);
        if(nodeList.contains(newNode))
            return nodeList.get(nodeList.indexOf(newNode));
        return newNode;
    }

    @Override
    public int getNumber(APICallNode N) {
        return N.getNodeNumber();
    }

    @Override
    public APICallNode getNode(int number) {
        return nodeList.get(number);
    }

    @Override
    public IntSet getSuccNodeNumbers(APICallNode node) {
        MutableIntSet intSet = IntSetUtil.make();

        if(forwardEdges.containsKey(node.getNodeNumber()))
            for(int s : forwardEdges.get(node.getNodeNumber())){
                intSet.add(s);
            }

        return intSet;
    }

    @Override
    public int getMaxNumber() {
        return nodeNum;
    }

    @Override
    public IntSet getPredNodeNumbers(APICallNode node) {
        MutableIntSet intSet = IntSetUtil.make();
        if(backwardEdges.containsKey(node.getNodeNumber()))
            for(int b : backwardEdges.get(node.getNodeNumber())){
                intSet.add(b);
            }

        return intSet;
    }

    @Override
    public Iterator<APICallNode> iterateNodes(IntSet s) {
        return nodeList.iterator();
    }

    @Override
    public Iterator<APICallNode> getPredNodes(APICallNode n) {
        Set<APICallNode> preds = new HashSet<>();

        if(backwardEdges.containsKey(n.getNodeNumber()))
            for(int p : backwardEdges.get(n.getNodeNumber())){
                preds.add(getNode(p));
            }
        return preds.iterator();
    }

    @Override
    public Iterator<APICallNode> iterator() {
        return nodeList.iterator();
    }

    @Override
    public int getPredNodeCount(APICallNode n) {
        return backwardEdges.get(n.getNodeNumber()).size();
    }

    @Override
    public int getNumberOfNodes() {
        return nodeList.size();
    }

    @Override
    public void removeNodeAndEdges(APICallNode n) throws UnsupportedOperationException {
        nodeList.remove(n);
        forwardEdges.remove(n.getNodeNumber());
        backwardEdges.remove(n.getNodeNumber());
    }

    @Override
    public void addNode(APICallNode n) {
        if(!nodeList.contains(n)){
            nodeList.add(n);
        }
        n.setNumber(nodeList.indexOf(n));
    }

    @Override
    public void removeNode(APICallNode n) throws UnsupportedOperationException {
        removeNodeAndEdges(n);
    }

    @Override
    public Iterator<APICallNode> getSuccNodes(APICallNode n) {
        Set<APICallNode> succs = new HashSet<>();

        if(forwardEdges.containsKey(n.getNodeNumber()))
            for(int f : forwardEdges.get(n.getNodeNumber())){
                if(f == -1){
                    System.out.println("Is it possible? " + n);
                }
                succs.add(getNode(f));
            }
        return succs.iterator();
    }

    @Override
    public boolean containsNode(APICallNode n) {
        return nodeList.contains(n);
    }

    @Override
    public int getSuccNodeCount(APICallNode n) {
        return forwardEdges.get(n.getNodeNumber()).size();
    }

    @Override
    public void addEdge(APICallNode src, APICallNode dst) {
        addNode(src);
        addNode(dst);

        if(!forwardEdges.containsKey(src.getNodeNumber()))
            forwardEdges.put(src.getNodeNumber(), new HashSet<Integer>());

        forwardEdges.get(src.getNodeNumber()).add(dst.getNodeNumber());

        if(!backwardEdges.containsKey(dst.getNodeNumber()))
            backwardEdges.put(dst.getNodeNumber(), new HashSet<Integer>());

        backwardEdges.get(dst.getNodeNumber()).add(src.getNodeNumber());
    }

    @Override
    public void removeEdge(APICallNode src, APICallNode dst) throws UnsupportedOperationException {
        forwardEdges.get(src.getNodeNumber()).remove(dst.getNodeNumber());
        backwardEdges.get(dst.getNodeNumber()).remove(src.getNodeNumber());
    }

    @Override
    public void removeAllIncidentEdges(APICallNode node) throws UnsupportedOperationException {
        forwardEdges.get(node.getNodeNumber()).clear();
        backwardEdges.get(node.getNodeNumber()).clear();
    }

    @Override
    public void removeIncomingEdges(APICallNode node) throws UnsupportedOperationException {
        for(int p : backwardEdges.get(node.getNodeNumber())){
            forwardEdges.get(p).remove(node.getNodeNumber());
        }

        backwardEdges.get(node.getNodeNumber()).clear();
    }

    @Override
    public void removeOutgoingEdges(APICallNode node) throws UnsupportedOperationException {
        for(int s : forwardEdges.get(node.getNodeNumber())){
            backwardEdges.get(s).remove(node.getNodeNumber());
        }

        forwardEdges.get(node.getNodeNumber()).clear();
    }

    @Override
    public boolean hasEdge(APICallNode src, APICallNode dst) {
        return forwardEdges.get(src.getNodeNumber()).contains(dst.getNodeNumber());
    }

    @Override
    public void forEach(Consumer<? super APICallNode> action) {
        nodeList.forEach(action);
    }

    @Override
    public Spliterator<APICallNode> spliterator() {
        return nodeList.spliterator();
    }

    public Set<APICallNode> getEntries(){
        clearEmpty();

        Set<APICallNode> entries = new HashSet<>();

        Set<Integer> fs = forwardEdges.keySet();
        Set<Integer> bs = backwardEdges.keySet();

        for(Integer i : fs){
            if(!bs.contains(i))
                entries.add(getNode(i));
        }

        return entries;
    }

    private void clearEmpty(){
        Set<Integer> fs = forwardEdges.keySet();
        for(Integer i : fs){
            if(forwardEdges.get(i).size() == 0)
                forwardEdges.remove(i);
        }

        Set<Integer> bs = backwardEdges.keySet();
        for(Integer i : bs){
            if(backwardEdges.get(i).size() == 0)
                backwardEdges.remove(i);
        }
    }
}
