package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.dataflow.IFDS.ICFGSupergraph;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.ConstantKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.shrikeBT.IConditionalBranchInstruction;
import com.ibm.wala.ssa.DefUse;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SSAConditionalBranchInstruction;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAPhiInstruction;
import com.ibm.wala.ssa.SSAReturnInstruction;
import com.ibm.wala.ssa.SSASwitchInstruction;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.intset.OrdinalSet;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.NoneField;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.BuiltinFlowPropagationModel;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.FlowModelHandler;
import kr.ac.kaist.wala.hybridroid.util.data.Pair;

/** Created by leesh on 23/02/2018. */
public class GraphDataFlowManager {
  private final ICFGSupergraph supergraph;
  private final InfeasiblePathFilter normalPathFilter;
  private final InfeasiblePathFilter callPathFilter;
  private final InfeasiblePathFilter exitPathFilter;
  private final InfeasibleDataFilter normalDataFilter;
  private final InfeasibleDataFilter callDataFilter;
  private final InfeasibleDataFilter callToRetDataFilter;
  private final SummaryEdgeManager seManager;
  private final PointerAnalysis<InstanceKey> pa;
  private final AliasAwareFlowFunction flowFun;
  private FlowModelHandler modelHandler;
  private final BuiltinFlowPropagationModel builtinPolicy;

  public GraphDataFlowManager(
      ICFGSupergraph supergraph, PointerAnalysis<InstanceKey> pa, SummaryEdgeManager seManager) {
    this.supergraph = supergraph;
    this.normalPathFilter = new DefaultPathFilter();
    this.callPathFilter = new DefaultPathFilter();
    this.exitPathFilter = new DefaultPathFilter();
    this.normalDataFilter = new DefaultDataFilter();
    this.callDataFilter = new DefaultDataFilter();
    this.callToRetDataFilter = new DefaultDataFilter();
    this.seManager = seManager;
    this.pa = pa;
    flowFun = new AliasAwareFlowFunction(supergraph, pa);
    modelHandler = new FlowModelHandler(pa.getClassHierarchy(), flowFun.getAliasHandler());
    builtinPolicy = new BuiltinFlowPropagationModel();
  }

  private Set<LocalDataFact> handlePhi(CGNode n, DataFact df){
    Set<LocalDataFact> res = new HashSet<>();
    handlePhi(n, df, res);
    return res;
  }

  private Set<LocalDataFact> handlePhi(CGNode n, DataFact df, Set<LocalDataFact> res){
    if(df instanceof LocalDataFact){
      LocalDataFact ldf = (LocalDataFact) df;
      int v = ldf.getVar();

      IR ir = n.getIR();
      if(ir == null)
        return Collections.emptySet();

      Iterator<SSAPhiInstruction> iPhi = (Iterator<SSAPhiInstruction>) ir.iteratePhis();
      while(iPhi.hasNext()){
        SSAPhiInstruction phi = iPhi.next();

        for(int i=0; i<phi.getNumberOfUses(); i++){
          if(phi.getUse(i) == v) {
            LocalDataFact nFact = new LocalDataFact(n, phi.getDef(), df.getField());
            if(res.add(nFact)){
              handlePhi(n, nFact, res);
            }
          }
        }
      }

      return res;
    }//else
      //Assertions.UNREACHABLE("Phi is handled by only LocalDataFact: " + df);
    return Collections.emptySet();
  }

  private Set<LocalDataFact> allPhi(CGNode n, LocalDataFact ldf){
    return allPhi(n, ldf, new HashSet<>());
  }

  private Set<LocalDataFact> addAll(CGNode n, Field f, SSAPhiInstruction phi){
    Set<LocalDataFact> res = new HashSet<>();
    for(int i=0; i<phi.getNumberOfUses(); i++){
      if(phi.getUse(i) != -1)
        res.add(new LocalDataFact(n, phi.getUse(i), f));
    }
    res.add(new LocalDataFact(n, phi.getDef(), f));
    return res;
  }

  private Set<LocalDataFact> allPhi(CGNode n, LocalDataFact ldf, Set<LocalDataFact> res){
    int v = ldf.getVar();

    IR ir = n.getIR();
    if(ir == null)
      return Collections.emptySet();

    Iterator<SSAPhiInstruction> iPhi = (Iterator<SSAPhiInstruction>) ir.iteratePhis();
    while(iPhi.hasNext()){
      SSAPhiInstruction phi = iPhi.next();

      for(int i=0; i<phi.getNumberOfUses(); i++){
        if(phi.getUse(i) == v) {
          for(LocalDataFact ndf : addAll(n, ldf.getField(), phi)){
            if(res.add(ndf)){
              allPhi(n, ndf, res);
            }
          }
          break;
        }
      }
    }

    return res;
  }

  public Set<Pair<BasicBlockInContext, DataFact>> getNormalNexts(
      BasicBlockInContext node, DataFact fact) {
    Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

    try {
      if (fact.equals(DataFact.DEFAULT_FACT)) {
        for (BasicBlockInContext succ : getNormalSuccessors(node)) {
          res.add(Pair.make(succ, fact));
        }
      } else {
        // TODO: do we need to consider the dense propagation or only sparse one?
        SSAInstruction inst = node.getLastInstruction();
        Set<DataFact> nextFacts = new HashSet<>();
	
        if (inst == null) {
          nextFacts.add(fact);
        } else {
          Set<DataFact> r = flowFun.visit(node.getNode(), inst, fact);
          if(r != null)
            nextFacts.addAll(r);
        }

        for (DataFact nextFact : nextFacts) {
          Set<BasicBlockInContext> succs = getSparseNormalSuccessors(node, nextFact);

          if(!nextFact.equals(fact)) {
            for(DataFact phi : handlePhi(node.getNode(), nextFact)) {
              for (BasicBlockInContext succ : succs) {
                res.add(Pair.make(succ, phi));
              }
            }
          }
          for (BasicBlockInContext succ : succs) {
            res.add(Pair.make(succ, nextFact));
          }
        }
      }
    } catch (InfeasiblePathException e) {
      e.printStackTrace();
    }
    debug = false;
    return res;
  }

  public void setModelHandler(FlowModelHandler handler) {
    this.modelHandler = handler;
  }

  private boolean debug = false;

  public Set<Pair<BasicBlockInContext, DataFact>> getCalleeNexts(
      BasicBlockInContext node, DataFact fact) throws InfeasiblePathException {
    Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

    SSAAbstractInvokeInstruction invokeInst =
        (SSAAbstractInvokeInstruction) node.getLastInstruction();
    if (invokeInst == null)
      throw new InfeasiblePathException("Call node must have an instruction: " + node);

    for (BasicBlockInContext callee : getCalleeSuccessors(node)) {
	    // cut flows to modeled methods
      if (modelHandler.isModeled(callee.getNode())) {
        continue;
      }

      DataFact calleeDataFact = getCalleeDataFact(callee.getNode(), invokeInst, fact);

      if (calleeDataFact == null) continue;
      // Static fields of application classes do not be used in methods of primordial classes. So, skip the propagation.
      if (fact instanceof GlobalDataFact) {
        GlobalDataFact gdf = (GlobalDataFact) fact;
        if (gdf.getGlobalFact()
                .getDeclaringClass()
                .getClassLoader()
                .equals(ClassLoaderReference.Application)
            && callee
                .getMethod()
                .getDeclaringClass()
                .getClassLoader()
                .getReference()
                .equals(ClassLoaderReference.Primordial)) continue;
      }
      res.add(Pair.make(callee, calleeDataFact));
      if(calleeDataFact instanceof LocalDataFact) {
        for (DataFact phi : handlePhi(callee.getNode(), calleeDataFact)) {
          res.add(Pair.make(callee, phi));
        }
      }
    }

    debug = false;

    return res;
  }

  boolean debugg = false;

  public Set<Pair<BasicBlockInContext, DataFact>> getCallToReturnNexts(
      BasicBlockInContext node, DataFact fact, PathEdge pe) throws InfeasiblePathException {
    Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();
    Set<DataFact> modeledFacts = new HashSet<>();

    SSAAbstractInvokeInstruction invokeInst =
        (SSAAbstractInvokeInstruction) node.getLastInstruction();

    if (invokeInst == null)
      throw new InfeasiblePathException("Call node must have an instruction: " + node);

    // add flows between call sites and return sites regarding to modeled method calls
    if (fact instanceof LocalDataFact || fact instanceof DefaultDataFact) {
      for (BasicBlockInContext callee : getCalleeSuccessors(node)) {
        if (modelHandler.isModeled(callee.getNode())) {
          modeledFacts.addAll(
              modelHandler.matchDataFact(node.getNode(), callee.getNode(), invokeInst, fact));
        }
      }
    }

    for (BasicBlockInContext ret : getCallToReturnSuccessors(node)) {
      // TODO: should we filter out data facts used in call sites?
      if (callToRetDataFilter.accept(node, ret, fact)) res.add(Pair.make(ret, fact));

      for (DataFact modeledFact : modeledFacts) {
        if(!modeledFact.equals(fact)) {
          for (DataFact phi : handlePhi(node.getNode(), modeledFact))
            res.add(Pair.make(ret, phi));
        }
        res.add(Pair.make(ret, modeledFact));
      }
    }

    return res;
  }

  public Set<Pair<BasicBlockInContext, DataFact>> getRetInfo(
      BasicBlockInContext call, BasicBlockInContext exit, DataFact fact)
      throws InfeasiblePathException {
    Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

    SSAAbstractInvokeInstruction invokeInst =
        (SSAAbstractInvokeInstruction) call.getLastInstruction();

    Iterator<BasicBlockInContext> iRetSites = supergraph.getReturnSites(call, exit.getNode());
    while (iRetSites.hasNext()) {
      BasicBlockInContext retSite = iRetSites.next();
      // skip exceptional edges at return sites
      if (retSite.isExitBlock()) continue;

      if (fact instanceof LocalDataFact) {
        LocalDataFact ldf = (LocalDataFact) fact;
        int v = ldf.getVar();

        if (isReturned(exit.getNode().getDU(), v) && invokeInst.hasDef()) {
          DataFact defFact = new LocalDataFact(retSite.getNode(), invokeInst.getDef(), ldf.getField());

          res.add(Pair.make(retSite, defFact));
          for(DataFact phi : handlePhi(retSite.getNode(), defFact))
            res.add(Pair.make(retSite, phi));
        }

        if (!(fact.getField() instanceof NoneField)) {
          for (DataFact aliasFact : flowFun.getLocalAliasOfCaller(retSite.getNode(), ldf)) {
            res.add(Pair.make(retSite, aliasFact));
            for(DataFact phi : handlePhi(retSite.getNode(), aliasFact))
              res.add(Pair.make(retSite, phi));
          }
        }
      } else if (fact instanceof GlobalDataFact) {
        res.add(Pair.make(retSite, fact));
      } else {
        // no-op
        //                Assertions.UNREACHABLE("Only Local and Global data facts are considered in exit nodes: " + fact);
      }
    }
    return res;
  }

  public Set<Pair<BasicBlockInContext, DataFact>> getCallerInfo(
      BasicBlockInContext entry, DataFact fact) {
    Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

    Iterator<BasicBlockInContext> iCallerBlock = supergraph.getPredNodes(entry);

    if (fact instanceof LocalDataFact) {
      LocalDataFact ldf = (LocalDataFact) fact;

      while (iCallerBlock.hasNext()) {
        BasicBlockInContext callerBlock = iCallerBlock.next();
        SSAAbstractInvokeInstruction invokeInst =
            (SSAAbstractInvokeInstruction) callerBlock.getLastInstruction();
        Set<LocalDataFact> df = new HashSet<>();
        df.add(ldf);
        df.addAll(allPhi(entry.getNode(), ldf));

        for(LocalDataFact lldf : df) {
          if(lldf.getVar() <= invokeInst.getNumberOfUses()) {
            int useV = invokeInst.getUse(lldf.getVar() - 1);
            res.add(
                    Pair.make(callerBlock, new LocalDataFact(callerBlock.getNode(), useV, lldf.getField())));
          }
        }
      }

    } else if (fact instanceof DefaultDataFact) {
      while (iCallerBlock.hasNext()) {
        BasicBlockInContext callerBlock = iCallerBlock.next();
        SSAAbstractInvokeInstruction invokeInst =
            (SSAAbstractInvokeInstruction) callerBlock.getLastInstruction();
        res.add(Pair.make(callerBlock, DefaultDataFact.DEFAULT_FACT));
      }
    }
    return res;
  }

  public Set<Pair<BasicBlockInContext, DataFact>> getExitNexts(
      BasicBlockInContext node, DataFact fact) {
    Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

    for (BasicBlockInContext ret : getExitSuccessors(node)) {
      if (callToRetDataFilter.accept(node, ret, fact)) res.add(Pair.make(ret, fact));
    }

    return res;
  }

  private boolean isReturned(DefUse du, int v) {
    Iterator<SSAInstruction> iUse = du.getUses(v);
    while (iUse.hasNext()) {
      SSAInstruction useInst = iUse.next();

      if (useInst instanceof SSAReturnInstruction) {
        if (useInst.getNumberOfUses() > 0 && useInst.getUse(0) == v) return true;
      }
    }
    return false;
  }

  public Set<Pair<BasicBlockInContext, DataFact>> getCallerDataToEntry(
      BasicBlockInContext entry, DataFact fact) throws InfeasiblePathException {
    Set<Pair<BasicBlockInContext, DataFact>> res = new HashSet<>();

    if ((fact instanceof LocalDataFact) == false) return res;

    Iterator<BasicBlockInContext> iPred = supergraph.getPredNodes(entry);

    while (iPred.hasNext()) {
      BasicBlockInContext pred = iPred.next();
      if (pred.getLastInstruction() == null)
        throw new InfeasiblePathException("Call node must have an instruction: " + pred);

      if (fact instanceof GlobalDataFact) {
        res.add(Pair.make(pred, fact));
      } else if (fact instanceof LocalDataFact) {
        SSAAbstractInvokeInstruction invokeInst =
            (SSAAbstractInvokeInstruction) pred.getLastInstruction();
        int index = getArgumentIndex((LocalDataFact) fact);
        LocalDataFact callerFact =
            makeNewLocalDataFact(pred.getNode(), invokeInst.getUse(index), fact.getField());
      }
    }

    return res;
  }

  private int getArgumentIndex(LocalDataFact fact) {
    return fact.getVar() - 1;
  }

  private LocalDataFact makeNewLocalDataFact(CGNode node, int numV, Field f) {
    return new LocalDataFact(node, numV, f);
  }

  protected DataFact getCalleeDataFact(
      CGNode callee, SSAAbstractInvokeInstruction invokeInst, DataFact fact) {
    // TODO: need to calculate a callee data fact regarding to the caller data fact used in the invoke instruction
    if (fact instanceof GlobalDataFact) {
      return fact;
    } else if (fact instanceof LocalDataFact) {
      LocalDataFact ldf = (LocalDataFact) fact;
      int v = ldf.getVar();
      int i = 0;
      for (; i < invokeInst.getNumberOfUses(); i++) {
        if (invokeInst.getUse(i) == v) {
          // cut infeasible path using the type information with the field of this data fact
          if (!callee.getMethod().isStatic() && i == 0)
            return new LocalDataFact(callee, i + 1, fact.getField());
          else if (flowFun.isCompatible(fact.getField(), callee.getMethod().getParameterType(i)))
            return new LocalDataFact(callee, i + 1, fact.getField());
        }
      }
    } else if (fact instanceof DefaultDataFact) return fact;

    return null;
  }

  private Set<BasicBlockInContext> getNextClosestUseBlocks(BasicBlockInContext bb, DefUse du, int v)
      throws InfeasiblePathException {
    Set<BasicBlockInContext> res = new HashSet<>();
    Iterator<SSAInstruction> iUse = du.getUses(v);

    Set<Integer> useIndexSet = new HashSet<>();

    if(debug)
      System.out.println("\t#HN?: " + iUse.hasNext());
    
    while (iUse.hasNext()) {
      SSAInstruction useInst = iUse.next();
      if(debug)
	System.out.println("\t#UI?: " + useInst + "\t" + (useInst.iindex <= bb.getLastInstructionIndex()));
      if (useInst.iindex <= bb.getLastInstructionIndex()) continue;

      useIndexSet.add(useInst.iindex);
    }

    Queue<BasicBlockInContext> bfsQueue = new LinkedBlockingQueue<>();
    bfsQueue.add(bb);

    Set<BasicBlockInContext> visited = new HashSet<>();

    while (!bfsQueue.isEmpty()) {
      BasicBlockInContext succ = bfsQueue.poll();
      if (visited.contains(succ)) continue;
      visited.add(succ);

      for (BasicBlockInContext succOfSucc : getNormalSuccessors(succ)) {
        if (succOfSucc.isExitBlock()) res.add(succOfSucc);
        else if (useIndexSet.contains(succOfSucc.getLastInstructionIndex())) {
           res.add(succOfSucc);
         } else {
          bfsQueue.add(succOfSucc);
        }
      }
    }

    if(debug){
      System.out.println("\t#V?: " + v);
      System.out.println("\t#UseIndexSize: " + useIndexSet.size());
      System.out.println("\t#SUCCSIZE: " + res.size());
    }
    return res;
  }

  protected Set<BasicBlockInContext> getSparseNormalSuccessors(BasicBlockInContext bb, DataFact df)
      throws InfeasiblePathException {
    if (df instanceof GlobalDataFact) {
      return getNormalSuccessors(bb);
    } else if (df instanceof LocalDataFact) {
      LocalDataFact ldf = (LocalDataFact) df;
      CGNode n = ldf.getNode();
      DefUse du = n.getDU();
      return getNextClosestUseBlocks(bb, du, ldf.getVar());
    } else
      throw new InfeasiblePathException(
          "A data fact used to find sparse successors must be either GlobalDataFact or LocalDataFact: "
              + df.getClass().getName());
  }

//  private Set<InstanceKey> calcFeasibleIntSet(OrdinalSet<InstanceKey> iks, BasicBlockInContext curBlock){
//    Set<InstanceKey> res = new HashSet<>();
//    for (InstanceKey ik : iks){
//      if(ik instanceof IntConstantKey){
//        IntConstantKey ick = (IntConstantKey) ik;
//        BFSPathFinder pathFinder = new BFSPathFinder(supergraph, supergraph.getEntriesForProcedure(ick.getNode())[0], curBlock);
//        List<BasicBlockInContext> l = pathFinder.find();
//        if(debug){
//          System.out.println(ik + " \t => \t " + (l != null));
//          if( l != null ){
//            for(BasicBlockInContext bb : l){
//              System.out.println(bb);
//            }
//          }
//        }
//        if(l != null)
//          res.add(ick);
//      }else
//        res.add(ik);
//    }
//    if(debug)
//      try {
//        System.in.read();
//      } catch (IOException e) {
//        e.printStackTrace();
//      }
//    return res;
//  }

  protected Set<BasicBlockInContext> getNormalSuccessors(BasicBlockInContext bb)
      throws InfeasiblePathException {
    Set<BasicBlockInContext> res = new HashSet<>();
    // this method only handles normal nodes. So reject call and exit nodes.
    if (bb.isExitBlock()) return res;

    boolean isRet = false;
    boolean isSwitch = false;
    int[] possibleLabels = null;
    boolean isIf = false;
    Boolean ifTrueCase = null;
    if (bb.getLastInstruction() != null) {
      if (bb.getLastInstruction() instanceof SSAReturnInstruction) isRet = true;
      else if (bb.getLastInstruction() instanceof SSASwitchInstruction) {
        isSwitch = true;
        SSASwitchInstruction switchInst = (SSASwitchInstruction) bb.getLastInstruction();
        if(switchInst.toString().contains("switch 9 [1000->12,1001->18,1002->22,1003->28,1004->34,1005->40,1006->46,1007->50,1008->56,1009->60,1010->66,1011->72,1012->78,1013->122,1014->84,1015->90,1016->94,1017->100,1018->106,1019->112,1020->118]"))
          debug  = true;
        int[] caseLabels = switchInst.getCasesAndLabels();
        int condV = switchInst.getUse(0);
        PointerKey condPK = pa.getHeapModel().getPointerKeyForLocal(bb.getNode(), condV);
        OrdinalSet<InstanceKey> ikSet = pa.getPointsToSet(condPK);
        if (ikSet.size() == 0) isSwitch = false;
        else {
//          Set<InstanceKey> iks = calcFeasibleIntSet(ikSet, bb);
          possibleLabels = new int[ikSet.size()];
          int index = 0;
          for (InstanceKey ik : ikSet) {
            // we only handle switch statements when all possible condition values are constant.
            if (ik instanceof ConstantKey) {
              isSwitch = true;
              int caseValue = (Integer) ((ConstantKey) ik).getValue();
              int beforeCasesSize = index;
              for (int i = 0; i < caseLabels.length - 1; i += 2) {
                if (caseValue == caseLabels[i]) {
                  possibleLabels[index++] = caseLabels[i + 1];
                }
              }
              if (beforeCasesSize == index) possibleLabels[index++] = switchInst.getDefault();
            } else {
              isSwitch = false;
              break;
            }
          }
        }
      } else if (bb.getLastInstruction() instanceof SSAConditionalBranchInstruction) {
        SSAConditionalBranchInstruction ifInst =
            (SSAConditionalBranchInstruction) bb.getLastInstruction();
        if (ifInst.isIntegerComparison()) {
          int condV1 = ifInst.getUse(0);
          int condV2 = ifInst.getUse(1);

          PointerKey condPK1 = pa.getHeapModel().getPointerKeyForLocal(bb.getNode(), condV1);
          PointerKey condPK2 = pa.getHeapModel().getPointerKeyForLocal(bb.getNode(), condV2);

//          Set<InstanceKey> condIK1s = calcFeasibleIntSet(pa.getPointsToSet(condPK1), bb);
//          Set<InstanceKey> condIK2s = calcFeasibleIntSet(pa.getPointsToSet(condPK1), bb);

          for (InstanceKey condIK1 : pa.getPointsToSet(condPK1)) {
            boolean partialRes = true;
            for (InstanceKey condIK2 : pa.getPointsToSet(condPK2)) {
              if (condIK1 instanceof ConstantKey && condIK2 instanceof ConstantKey) {
                int condValue1 = (Integer) ((ConstantKey) condIK1).getValue();
                int condValue2 = (Integer) ((ConstantKey) condIK2).getValue();

                boolean localCond = false;
                IConditionalBranchInstruction.Operator op =
                    (IConditionalBranchInstruction.Operator) ifInst.getOperator();
                switch (op) {
                  case EQ:
                    if (condValue1 == condValue2) localCond = true;
                    else localCond = false;
                    break;
                  case NE:
                    if (condValue1 != condValue2) localCond = true;
                    else localCond = false;
                    break;
                  case LT:
                    if (condValue1 < condValue2) localCond = true;
                    else localCond = false;
                    break;
                  case GE:
                    if (condValue1 >= condValue2) localCond = true;
                    else localCond = false;
                    break;
                  case GT:
                    if (condValue1 > condValue2) localCond = true;
                    else localCond = false;
                    break;
                  case LE:
                    if (condValue1 <= condValue2) localCond = true;
                    else localCond = false;
                    break;
                  default:
                    throw new InfeasiblePathException(
                        "The operator must belong to 6 types: " + ifInst.getOperator());
                }
                if (ifTrueCase == null) ifTrueCase = localCond;
                else if (ifTrueCase != localCond) {
                  isIf = false;
                  partialRes = false;
                  break;
                }
              } else {
                isIf = false;
                partialRes = false;
                break;
              }
            }
            if (partialRes == false) break;
          }
        }
      }
    }
    Iterator<BasicBlockInContext> iSucc = supergraph.getSuccNodes(bb);

    while (iSucc.hasNext()) {
      BasicBlockInContext succ = iSucc.next();

      // exclude exception paths
      if (!isRet && succ.isExitBlock()) continue;
      if (succ.isEntryBlock()) continue;
      else if (isSwitch) { // when we can slice infeasible cases
        int label = succ.getLastInstructionIndex();
        boolean feasibleCheck = false;
        for (int i = 0; i < possibleLabels.length; i++) {
          if (possibleLabels[i] == label) feasibleCheck = true;
        }
        if (!feasibleCheck) continue;
      } else if (isIf) { // when we can slice an infeasible condition

      }

      if (normalPathFilter.accept(bb, succ)) res.add(succ);
    }
    debug = false;
    return res;
  }

  protected Set<BasicBlockInContext> getCalleeSuccessors(BasicBlockInContext bb) {
    Set<BasicBlockInContext> res = new HashSet<>();

    Iterator<BasicBlockInContext> iSucc = supergraph.getCalledNodes(bb);

    while (iSucc.hasNext()) {
      BasicBlockInContext succ = iSucc.next();
      if (callPathFilter.accept(bb, succ)) res.add(succ);
    }
    return res;
  }

  protected Set<BasicBlockInContext> getCallToReturnSuccessors(BasicBlockInContext bb) {
    Set<BasicBlockInContext> res = new HashSet<>();

    // getReturnSites method does not use the second argument. So, we just pass null.
    // TODO: Do we need to check that the return sites include exceptional paths?
    Iterator<BasicBlockInContext> iSucc = supergraph.getReturnSites(bb, null);

    while (iSucc.hasNext()) {
      BasicBlockInContext succ = iSucc.next();
      // exclude exception paths
      if (succ.isExitBlock()) continue;
      if (callPathFilter.accept(bb, succ)) res.add(succ);
    }
    return res;
  }

  protected Set<BasicBlockInContext> getExitSuccessors(BasicBlockInContext bb) {
    Set<BasicBlockInContext> res = new HashSet<>();

    Iterator<BasicBlockInContext> iSucc = supergraph.getCalledNodes(bb);

    while (iSucc.hasNext()) {
      BasicBlockInContext succ = iSucc.next();
      if (exitPathFilter.accept(bb, succ)) res.add(succ);
    }
    return res;
  }

  interface InfeasiblePathFilter {
    public boolean accept(BasicBlockInContext bb, BasicBlockInContext succ);
  }

  class DefaultPathFilter implements InfeasiblePathFilter {
    @Override
    public boolean accept(BasicBlockInContext bb, BasicBlockInContext succ) {
      return true;
    }
  }

  interface InfeasibleDataFilter {
    public boolean accept(BasicBlockInContext bb, BasicBlockInContext succ, DataFact df);
  }

  class DefaultDataFilter implements InfeasibleDataFilter {
    @Override
    public boolean accept(BasicBlockInContext bb, BasicBlockInContext succ, DataFact df) {
      return true;
    }
  }

  class SwitchFilter {
    public boolean accept(
        BasicBlockInContext bb, BasicBlockInContext succ, Set<InstanceKey> ikSet) {
      return true;
    }
  }

  class IfFilter {}
}
