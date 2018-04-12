package kr.ac.kaist.wala.adlib.util;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.*;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.analysis.malicious.MaliciousPatternChecker;
import kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.flows.PropagateFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.ifds.*;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.FieldSeq;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.NoneField;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.SingleField;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.MethodFlowModel;

import java.util.*;

/**
 * Created by leesh on 06/04/2018.
 */
public class PathFinder {
    private final IClassHierarchy cha;
    private final PropagationGraph pg;
    private final ICFGSupergraph supergraph;
    private final CallGraph cg;

    public PathFinder(CallGraph cg, ICFGSupergraph supergraph, IClassHierarchy cha, PropagationGraph pg){
        this.cg = cg;
        this.supergraph = supergraph;
        this.cha = cha;
        this.pg = pg;
    }

    boolean debug = false;
    boolean pd = false;
    public Set<Path> findPaths(PropagationPoint seed, MaliciousPatternChecker.MaliciousPattern mp){
        Set<List<PropagationPoint>> res = new HashSet<>();
        List<PropagationPoint> initPath = new ArrayList<>();
        initPath.add(seed);
        res.add(initPath);
        for(MaliciousPatternChecker.MaliciousPoint mmp : mp.getPoints()){
            if(debug)
                System.out.println("#TRY? " + mmp);
            Set<List<PropagationPoint>> newS = new HashSet<>();
            for(List<PropagationPoint> prevPath : res){
                for(PropagationPoint pp : convertMPtoPPs(mmp)){
                    PropagationPoint prev = prevPath.get(0);
//                    BFSPathFinder<PropagationPoint> pf = new BFSPathFinder<>(pg, prev, pp);
//                    List<PropagationPoint> path = pf.find();
                    List<PropagationPoint> path = find(prev, pp);
                    if(path != null){
                        if(debug){
                            System.out.println("\t\t#MATCH!!! : " + pp);
                            System.out.println("\t\t\tFrom : " + prev);
                        }
                        pp.setTarget();
                        List<PropagationPoint> nPath = copy(path);
                        nPath.remove(nPath.size()-1);
                        nPath.addAll(prevPath);
                        newS.add(nPath);
                    }
                }
            }
            res.clear();

            if(newS.size() == 0)
                break;
            else
                res.addAll(newS);
        }

        Set<Path> resPath = new HashSet<>();

        for(List<PropagationPoint> path : res){
            resPath.add(new Path(path));
        }
        pd = false;
        debug = false;
        return resPath;
    }

    private void printPath(List<PropagationPoint> path){
        System.out.println("============= PATH =================");
        for(PropagationPoint pp : path){
            System.out.println("=> " + pp);
        }
        System.out.println("");
    }

    private List<PropagationPoint> copy(List<PropagationPoint> old){
        List<PropagationPoint> newL = new ArrayList<>();
        newL.addAll(old);
        return newL;
    }

    private boolean isArrayType(MethodReference mr, int index){
        if(index == MethodFlowModel.RETV)
            return mr.getReturnType().isArrayType();
        else{
            IClass k = cha.lookupClass(mr.getDeclaringClass());
            IMethod m = k.getMethod(mr.getSelector());
            if(m.isStatic()){
                return mr.getParameterType(index).isArrayType();
            }else{
                if(index == 0)
                    return false;
                else
                    return mr.getParameterType(index-1).isArrayType();
            }
        }
    }

    private Set<PropagationPoint> convertMPtoPPs(MaliciousPatternChecker.MaliciousPoint mp){
        Set<PropagationPoint> res = new HashSet<>();

        TypeName mpName = mp.getTypeName();
        TypeReference mpTR = makeClassTypeRef(mpName);

        PropagateFlowFunction ff = mp.getFlowFunction();
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
            BasicBlockInContext entry = supergraph.getEntriesForProcedure(target)[0];
            Iterator<BasicBlockInContext> iPred = supergraph.getPredNodes(entry);
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
                        if(isArrayType(target.getMethod().getReference(), inVarIndex-1))
                            fact = new LocalDataFact(pred.getNode(), invokeInst.getUse(inVarIndex-1), FieldSeq.make(SingleField.make("["), NoneField.getInstance()));
                        else
                            fact = new LocalDataFact(pred.getNode(), invokeInst.getUse(inVarIndex-1), NoneField.getInstance());
                        break;
                }

                res.add(PropagationPoint.make(pred, fact));
            }
        }
        return res;
    }

    private Set<kr.ac.kaist.wala.adlib.dataflow.ifds.PathEdge> isReachable(BasicBlockInContext bb, DataFact fact, Set<kr.ac.kaist.wala.adlib.dataflow.ifds.PathEdge> edges){
        Set<kr.ac.kaist.wala.adlib.dataflow.ifds.PathEdge> res = new HashSet<>();

        for(kr.ac.kaist.wala.adlib.dataflow.ifds.PathEdge pe : edges){
            if(pe.getToNode().equals(bb) && pe.getToFact().equals(fact)) {
                res.add(pe);
            }
        }

        return res;
    }


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

    private boolean isMatched(List<PropagationPoint> path){
        Stack<CGNode> s = new Stack<>();

        for(int i=path.size()-1; i > -1; i--){
            PropagationPoint pp = path.get(i);
            if(pp.getBlock().isEntryBlock()){
                s.add(pp.getBlock().getNode());
            }else if(pp.getBlock().isExitBlock() && i != 0){
                s.pop();
                CGNode n = s.peek();
                PropagationPoint nextPP = path.get(i-1);
                if(!nextPP.getBlock().getNode().equals(n))
                    return false;
            }
        }

        return true;
    }

    public List<PropagationPoint> find(PropagationPoint from, PropagationPoint to) {

        LinkedList<PropagationPoint> Q = new LinkedList<>();
        HashMap<Object, PropagationPoint> history = HashMapFactory.make();

        Q.addLast(from);
        history.put(from, null);

        while (!Q.isEmpty()) {
            PropagationPoint N = Q.removeFirst();

            if (N.equals(to)) { //TODO: need to check whether this path is spurious or not.
                return makePath(N, history);
            }
            Iterator<PropagationPoint> children = pg.getSuccNodes(N);
            while (children.hasNext()) {
                PropagationPoint c = children.next();
                if (!history.containsKey(c)) {
                    if(pd){
                        System.out.println("#FROM: \n\t" + N);
                        System.out.println("#TO: \n\t" + c);
                        System.out.println();
                    }
                    Q.addLast(c);
                    history.put(c, N);
                }
            }
        }

        return null;
    }

    /**
     * @return a List which represents a path in the breadth-first search to Q[i]. Q holds the nodes visited during the BFS, in order.
     */
    private List<PropagationPoint> makePath(PropagationPoint node, Map<Object, PropagationPoint> history) {
        ArrayList<PropagationPoint> result = new ArrayList<>();
        PropagationPoint n = node;
        result.add(n);
        while (true) {
            PropagationPoint parent = history.get(n);
            if (parent == null)
                return result;
            else {
                result.add(parent);
                n = parent;
            }
        }
    }

    public class Path{
        private final List<PropagationPoint> path;
        private final boolean isMatched;

        public Path(List<PropagationPoint> path){
            this.path = path;
            this.isMatched = PathFinder.this.isMatched(path);
//            printPath(path);
//            System.out.println("ISMATCHED? " + isMatched);
        }

        public boolean isMatched(){
            return isMatched;
        }

        public List<PropagationPoint> getPath(){
            return this.path;
        }
    }
}
