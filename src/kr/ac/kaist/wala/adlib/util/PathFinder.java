package kr.ac.kaist.wala.adlib.util;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.traverse.BFSPathFinder;
import kr.ac.kaist.wala.adlib.analysis.malicious.MaliciousPatternChecker;
import kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.flows.PropagateFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.ifds.*;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.NoneField;

import java.io.IOException;
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

    public Set<List<PropagationPoint>> findPaths(PropagationPoint seed, MaliciousPatternChecker.MaliciousPattern mp){
        Set<List<PropagationPoint>> res = new HashSet<>();

        List<PropagationPoint> initPath = new ArrayList<>();
        initPath.add(seed);
        res.add(initPath);

        for(MaliciousPatternChecker.MaliciousPoint mmp : mp.getPoints()){
            Set<List<PropagationPoint>> newS = new HashSet<>();
            for(List<PropagationPoint> prevPath : res){
                for(PropagationPoint pp : convertMPtoPPs(mmp)){
                    PropagationPoint prev = prevPath.get(prevPath.size()-1);

                    BFSPathFinder<PropagationPoint> pf = new BFSPathFinder<>(pg, prev, pp);
                    List<PropagationPoint> path = pf.find();
                    System.out.println("PREV: " + prev);
                    System.out.println("PP: " + pp);
                    System.out.println("RES: " + path);
                    System.out.println();
                    if(path != null){
                        System.out.println("PREV: " + prev);
                        System.out.println("PP: " + pp);
                        System.out.println("RES: " + path);
                        printPath(path);
                        System.out.println();

                        try {
                            System.in.read();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        List<PropagationPoint> nPath = copy(prevPath);
                        nPath.addAll(path);
                        newS.add(nPath);
                    }
                }
            }
            res.clear();
            res.addAll(newS);
        }

        for(List<PropagationPoint> p : res){
            printPath(p);
        }
        return res;
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
                        fact = new LocalDataFact(pred.getNode(), invokeInst.getUse(inVarIndex-1), NoneField.getInstance());
                        break;
                }

                res.add(PropagationPoint.make(pred, fact));
            }
        }

        System.out.println("MP: " + mp);
        for(PropagationPoint pp : res){
            System.out.println("\t => " + pp);
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return res;
    }

//    private List<PropagationPoint> findShortestPath(PropagationPoint from, PropagationPoint to){
//        List<MaliciousPatternChecker.MaliciousPoint> mpList = mp.getPoints();
//        //TODO: need to calculate the pathes from a seed to a last point? Now, to show an existence of a pattern, I just check whether the last point is reachable.
////        mpList = Lists.reverse(mpList);
////
////        for(MaliciousPoint point : mpList){
////
////        }
//        MaliciousPatternChecker.MaliciousPoint lastPoint = mpList.get(mpList.size()-1);
//        return isReachable(lastPoint, edges);
//    }

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
}
