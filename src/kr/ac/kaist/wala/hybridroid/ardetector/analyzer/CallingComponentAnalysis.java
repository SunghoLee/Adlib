package kr.ac.kaist.wala.hybridroid.ardetector.analyzer;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.debug.UnimplementedError;
import kr.ac.kaist.wala.hybridroid.ardetector.types.Intent;
import sun.jvm.hotspot.types.WrongTypeException;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static com.ibm.wala.util.debug.Assertions.UNREACHABLE;

/**
 * Created by leesh on 01/02/2017.
 */
public class CallingComponentAnalysis {
    private final CallGraph cg;
    public final static TypeReference CONTEXT_WRAPPER = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/content/ContextWrapper");
    public final static TypeReference INTENT = TypeReference.findOrCreate(ClassLoaderReference.Application, "Landroid/content/Intent");

    public enum CallingMethod{
        START_ACTIVITY1("startActivity(Landroid/content/Intent;)V"),
        START_ACTIVITY2("startActivity(Landroid/content/Intent;Landroid/os/Bundle;)V"),
        START_ACTIVITY3("startActivities([Landroid/content/Intent;)V"),
        START_ACTIVITY4("startActivities([Landroid/content/Intent;Landroid/os/Bundle;)V"),
        START_SERVICE("startService(Landroid/content/Intent;)V"),
        ;

        private final Selector selector;

        CallingMethod(String s){
            this.selector = Selector.make(s);
        }

        public Selector getSelector(){
            return selector;
        }
    }

    public CallingComponentAnalysis(CallGraph cg){
        this.cg = cg;
    }

    private Set<CGNode> findCallingComponentNode(CallGraph cg){
        Set<CGNode> res = new HashSet<CGNode>();

        for(CallingMethod cm : CallingMethod.values()){
            res.addAll(cg.getNodes(MethodReference.findOrCreate(CONTEXT_WRAPPER, cm.getSelector())));
        }
        return res;
    }

    private Set<CGNode> getPreds(CallGraph cg, CGNode n){
        Iterator<CGNode> iNode = cg.getPredNodes(n);
        Set<CGNode> nodes = new HashSet<CGNode>();

        while(iNode.hasNext())
            nodes.add(iNode.next());

        return nodes;
    }

    class ComponentCallingContext {
        private final String action;
        private final String category;
        private final Set<String> flags = new HashSet<>();

        public ComponentCallingContext(){
            this.action = "Unknown";
            this.category = "Unknown";
        }

        public ComponentCallingContext(String action){
            this.action = action;
            this.category = "Unknown";
        }

        public ComponentCallingContext(String action, String category){
            this.action = action;
            this.category = category;
        }


        public void addFlag(String s){
            flags.add(s);
        }

        @Override
        public String toString(){
            return "Intent[ action: " + action + ", category: " + category + ", flags: " + flags + " ]";
        }
    }

    private Set<ComponentCallingContext> getCallingContexts(CGNode n, int intentVar){
        Set<ComponentCallingContext> res = new HashSet<>();

        /*
        Only three cases possible
        1. def by argument
        2. def by new
        3. def by return of method call
         */

        //def by argument
        if(intentVar < n.getMethod().getNumberOfParameters() + 1){
            for(CGNode pred: getPreds(cg, n)){ // for each caller node for this node
                Iterator<CallSiteReference> iCallSite = cg.getPossibleSites(pred, n);
                while(iCallSite.hasNext()){ // for each callsites for this node in the caller node
                    CallSiteReference csRef = iCallSite.next();
                    for(SSAAbstractInvokeInstruction callInst : pred.getIR().getCalls(csRef)){ // for each call instruction for this node in caller node
                        res.addAll(getCallingContexts(pred, callInst.getUse(intentVar-1))); // 1 is a second argument that denotes intent object
                    }
                }
            }
        }else{//def by 1 or 2
            DefUse du = n.getDU();
            SSAInstruction defInst = du.getDef(intentVar);
            IntentCreationTrackingVisitor visitor = new IntentCreationTrackingVisitor(n);
            defInst.visit(visitor);
            res.addAll(visitor.getResult());
        }

        addFlags(n, intentVar, res);
        return res;
    }

    private Set<ComponentCallingContext> addFlags(CGNode n, int intentVar, Set<ComponentCallingContext> res){
        DefUse du = n.getDU();
        Iterator<SSAInstruction> iInst = du.getUses(intentVar);
        while(iInst.hasNext()){
            SSAInstruction useInst = iInst.next();
            System.out.println("#I: " + useInst);
            if(useInst instanceof SSAAbstractInvokeInstruction){
                for(CGNode callee : cg.getPossibleTargets(n, ((SSAAbstractInvokeInstruction)useInst).getCallSite())){
                    MethodReference calleeRef = callee.getMethod().getReference();
                    if(calleeRef.getDeclaringClass().getName().equals(Intent.INTENT_TYPE)){
                        if(calleeRef.getSelector().equals(Intent.AddFlagsSelector.ADD_FLAGS.getSelector())){
                            int flagVar = useInst.getUse(Intent.AddFlagsSelector.ADD_FLAGS.getFlagIndex());
                            SymbolTable symTab = n.getIR().getSymbolTable();
                            if(symTab.isIntegerConstant(flagVar)){
                                addFlagToAllIntent(Intent.Flag.matchFlag(symTab.getIntValue(flagVar)).getName(), res);
                            }else
                                throw new WrongTypeException("Intent flag must be Integer constant: " + flagVar + " in " + n);
                        }
                    }else{
                        int i=0;
                        for(; i < useInst.getNumberOfUses(); i++){
                            if(useInst.getUse(i) == intentVar)
                                break;
                        }
                        res = addFlags(callee, ++i, res);
                    }
                }
            }else if(useInst instanceof SSAReturnInstruction){
                //no-op
            }else{
                Assertions.UNREACHABLE("Intent object can be used in invoke instruction or return instruction only: " + useInst);
            }
        }
        return res;
    }

    private Set<ComponentCallingContext> addFlagToAllIntent(String s, Set<ComponentCallingContext> intents){
        for(ComponentCallingContext ccc: intents)
            ccc.addFlag(s);
        return intents;
    }

    public Set<ComponentCallingContext> getCallingContexts(){
        Set<CGNode> nodes = findCallingComponentNode(cg);
        Set<ComponentCallingContext> res = new HashSet<>();

        for(CGNode n : nodes){ // for each startActivity node
            for(CGNode pred: getPreds(cg, n)){ // for each caller node for startActivity
                Iterator<CallSiteReference> iCallSite = cg.getPossibleSites(pred, n);
                while(iCallSite.hasNext()){ // for each callsites for startActivity in the caller node
                    CallSiteReference csRef = iCallSite.next();
                    for(SSAAbstractInvokeInstruction callInst : pred.getIR().getCalls(csRef)){ // for each call instruction for startActivity in caller node
                        res.addAll(getCallingContexts(pred, callInst.getUse(1))); // 1 is a second argument that denotes intent object
                    }
                }
            }
        }
        for(ComponentCallingContext ccc : res)
            System.out.println("CCC: " + ccc);
        return res;
    }

    class IntentCreationTrackingVisitor implements SSAInstruction.IVisitor{
        private final Set<ComponentCallingContext> cccSet;
        private final CGNode n;

        public IntentCreationTrackingVisitor(CGNode n){
            this.cccSet = new HashSet<>();
            this.n = n;
        }

        @Override
        public void visitGoto(SSAGotoInstruction instruction) {
            UNREACHABLE("Intent can not created by Goto instruction.");
        }

        @Override
        public void visitArrayLoad(SSAArrayLoadInstruction instruction) {
            UNREACHABLE("Intent can not created by Array load instruction.");
        }

        @Override
        public void visitArrayStore(SSAArrayStoreInstruction instruction) {
            UNREACHABLE("Intent can not created by Array store instruction.");
        }

        @Override
        public void visitBinaryOp(SSABinaryOpInstruction instruction) {
            UNREACHABLE("Intent can not created by Binary operation instruction.");
        }

        @Override
        public void visitUnaryOp(SSAUnaryOpInstruction instruction) {
            UNREACHABLE("Intent can not created by Unary operation instruction.");
        }

        @Override
        public void visitConversion(SSAConversionInstruction instruction) {
            UNREACHABLE("Intent can not created by Conversion instruction.");
        }

        @Override
        public void visitComparison(SSAComparisonInstruction instruction) {
            UNREACHABLE("Intent can not created by Comparison instruction.");
        }

        @Override
        public void visitConditionalBranch(SSAConditionalBranchInstruction instruction) {
            UNREACHABLE("Intent can not created by Conditional branch instruction.");
        }

        @Override
        public void visitSwitch(SSASwitchInstruction instruction) {
            UNREACHABLE("Intent can not created by Switch instruction.");
        }

        @Override
        public void visitReturn(SSAReturnInstruction instruction) {
            UNREACHABLE("Intent can not created by Return instruction.");
        }

        @Override
        public void visitGet(SSAGetInstruction instruction) {
            UNREACHABLE("Intent can not created by Get instruction.");
        }

        @Override
        public void visitPut(SSAPutInstruction instruction) {
            UNREACHABLE("Intent can not created by Put instruction.");
        }

        @Override
        public void visitInvoke(SSAInvokeInstruction instruction) {
            for(CGNode callee : cg.getPossibleTargets(n, instruction.getCallSite())){
                SSACFG cfg = callee.getIR().getControlFlowGraph();
                SSACFG.BasicBlock exitBlock = callee.getIR().getExitBlock();
                Iterator<ISSABasicBlock> iReturn = cfg.getPredNodes(exitBlock);
                while(iReturn.hasNext()){
                    ISSABasicBlock retBB = iReturn.next();
                    SSAInstruction inst = retBB.getLastInstruction();
                    if(inst instanceof SSAReturnInstruction){
                        SSAReturnInstruction retInst = (SSAReturnInstruction) inst;
                        if(retInst.getNumberOfUses() == 0)
                            UNREACHABLE("Return block must have a return value: " + inst);
                        int retVar = retInst.getUse(0);
                        cccSet.addAll(getCallingContexts(callee, retVar));
                    }else
                        UNREACHABLE("All return blocks must have return instruction: " + inst);
                }

            }
        }

        @Override
        public void visitNew(SSANewInstruction instruction) {
            cccSet.add(findComponentCallingContext(instruction));
        }

        @Override
        public void visitArrayLength(SSAArrayLengthInstruction instruction) {
            UNREACHABLE("Intent can not created by Array length instruction.");
        }

        @Override
        public void visitThrow(SSAThrowInstruction instruction) {
            UNREACHABLE("Intent can not created by Throw instruction.");
        }

        @Override
        public void visitMonitor(SSAMonitorInstruction instruction) {
            UNREACHABLE("Intent can not created by Monitor instruction.");
        }

        @Override
        public void visitCheckCast(SSACheckCastInstruction instruction) {
            UNREACHABLE("Intent can not created by Check cast instruction.");
        }

        @Override
        public void visitInstanceof(SSAInstanceofInstruction instruction) {
            UNREACHABLE("Intent can not created by Instance of instruction.");
        }

        @Override
        public void visitPhi(SSAPhiInstruction instruction) {
            UNREACHABLE("Intent can not created by Phi instruction.");
        }

        @Override
        public void visitPi(SSAPiInstruction instruction) {
            UNREACHABLE("Intent can not created by Pi instruction.");
        }

        @Override
        public void visitGetCaughtException(SSAGetCaughtExceptionInstruction instruction) {
            UNREACHABLE("Intent can not created by Get caught instruction.");
        }

        @Override
        public void visitLoadMetadata(SSALoadMetadataInstruction instruction) {
            UNREACHABLE("Intent can not created by Load metadata instruction.");
        }

        public Set<ComponentCallingContext> getResult(){
            return cccSet;
        }

        private ComponentCallingContext findComponentCallingContext(SSANewInstruction newInst){
            SSAAbstractInvokeInstruction invokeInst = findInitInstOfNew(newInst);
            return getComponentCallingContextFromInvoke(invokeInst);
        }

        private SSAAbstractInvokeInstruction findInitInstOfNew(SSANewInstruction newInst){
            int newIndex = newInst.iindex;
            SSAInstruction[] insts = n.getIR().getInstructions();
            for(int i=newIndex+1; i < insts.length; i++){
                SSAInstruction inst = insts[i];
                if(inst instanceof SSAAbstractInvokeInstruction && inst.getUse(0) == newInst.getDef()){
                    return (SSAAbstractInvokeInstruction)inst;
                }
            }

            throw new UnimplementedError("Do not consider about non-initiated object yet: " + newInst);
        }

        private ComponentCallingContext getComponentCallingContextFromInvoke(SSAAbstractInvokeInstruction invokeInst){
            MethodReference ref = invokeInst.getDeclaredTarget();

            if(ref.getDeclaringClass().getName().equals(Intent.INTENT_TYPE)){
                Intent.InitSelector s = Intent.InitSelector.matchInit(ref.getSelector());
                if(s != null){
                    switch(s){
                        case INIT_INTENT1:
                            break;
                        case INIT_INTENT2:
                            break;
                        case INIT_INTENT3:
                            break;
                        case INIT_INTENT4:
                            break;
                        case INIT_INTENT5:
                            break;
                        case INIT_INTENT6:
                        case INIT_INTENT7:
                        case INIT_INTENT8:
                            int actionVar = invokeInst.getUse(1);
                            SymbolTable symTab = n.getIR().getSymbolTable();

                            if(!symTab.isStringConstant(actionVar))
                                return new ComponentCallingContext();

                            return new ComponentCallingContext(symTab.getStringValue(actionVar));
                    }
                }
            }
            throw new UnimplementedError("Now, do not consider about non-action based intent initialization: " + invokeInst);
        }
    }
}
