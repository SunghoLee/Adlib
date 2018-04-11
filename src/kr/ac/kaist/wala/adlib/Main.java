package kr.ac.kaist.wala.adlib;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAInstruction;
import com.ibm.wala.ssa.SSAOptions;
import com.ibm.wala.types.ClassLoaderReference;
import kr.ac.kaist.wala.adlib.analysis.APITarget;
import kr.ac.kaist.wala.adlib.analysis.malicious.MaliciousPatternChecker;
import kr.ac.kaist.wala.adlib.analysis.malicious.MaliciousPatternRepo;
import kr.ac.kaist.wala.adlib.callgraph.CallGraphBuilderForHybridSDK;
import kr.ac.kaist.wala.adlib.callgraph.HybridSDKModel;
import kr.ac.kaist.wala.adlib.callgraph.context.FirstMethod;
import kr.ac.kaist.wala.adlib.callgraph.context.FirstMethodContextSelector;
import kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction;
import kr.ac.kaist.wala.adlib.model.ARModeling;
import kr.ac.kaist.wala.hybridroid.util.debug.PointerAnalysisCommandlineDebugger;
import kr.ac.kaist.wala.hybridroid.util.print.IRPrinter;
import org.json.simple.parser.ParseException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by leesh on 05/01/2017.
 */
public class Main {
    private static boolean DEBUG = true;

    private static Set<APITarget> targetAPIs = new HashSet<>();

//runOnUiThread(Runnable action) android.app.Activity
    public static void main(String[] args) throws CallGraphBuilderCancelException, ClassHierarchyException, ParseException, IOException {
//        printMP();
//        System.exit(-1);
        String prop = args[0];
        String sdk = args[1];
        String initInstFile = args[2];

        System.out.println("##################### INIT Fs #####################");
        System.out.println("#PROP: " + prop);
        System.out.println("#AdSDK: " + sdk);
        System.out.println("#INIT: " + initInstFile);
        System.out.println("###################################################");

        long start = System.currentTimeMillis();
        long cgStart = System.currentTimeMillis();

        CallGraphBuilderForHybridSDK builder = new CallGraphBuilderForHybridSDK(prop, sdk, InitInstsParser.parse(initInstFile));
        CallGraph cg = builder.makeCallGraph();
        PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();

        long cgEnd = System.currentTimeMillis();

        System.out.println("Finish to build a callgraph: " + ((cgEnd - cgStart)/1000d) + "s");

        long fgStart = System.currentTimeMillis();

//        ReachableAPIAnalysis apiAnalysis = new ReachableAPIAnalysis(cg, HybridSDKModel.getBridgeEntries());
//
//        apiAnalysis.addAPITargets(targetAPIs);
//        ReachableAPIFlowGraph apiGraph = apiAnalysis.analyze();

//        if(DEBUG)
//            GraphPrinter.print(apiGraph);

//        long fgEnd = System.currentTimeMillis();
//
//        System.err.println("Finish to construct a reachableAPIFlowGraph: " + ((fgEnd - fgStart)/1000d) + "s");

//        long pcStart = System.currentTimeMillis();

//        BranchSlicerForConstant slicer = new BranchSlicerForConstant(cg, pa);
//        cg = slicer.prune();

//        ICFGSupergraph supergraph = ICFGSupergraph.make(cg,new AnalysisCache());
//        IFDSAnalyzer ifds = new IFDSAnalyzer(supergraph, pa);
//        try {
//            for(CGNode n : cg) {
//                if(n.getMethod().getReference().toString().contains("< Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, deleteFile(Ljava/lang/String;)V >")) {
//                    System.out.println("# SEED_MATCH: " + n);
//                    ifds.analyze(supergraph.getEntriesForProcedure(n)[0], new LocalDataFact(n, 2, NoneField.getInstance()));
//                }
//            }
//        } catch (InfeasiblePathException e) {
//            e.printStackTrace();
//        }

        if(DEBUG) {
            String name = "ir_test";
            final CallGraph fcg = cg;
            IRPrinter.printIR(fcg, name, new IRPrinter.Filter(){
                @Override
                public boolean filter(CGNode n) {
                    if(n.getMethod().toString().contains("fakeRootMethod"))
                        return true;
                    if(n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial)){
                        if(!ARModeling.isModelingMethod(fcg.getClassHierarchy(), n.getMethod()))
                            return false;
                    }
                    return true;
                }
            });

            printIR(cg, name);

            for(PointerKey pk : pa.getPointerKeys()){
                if(pk.toString().contains("[Node: < Application, Lcom/nativex/monetization/mraid/MRAIDAsyncManager, run()V > Context: FirstMethodContextPair: [First: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice, storePicture(Ljava/lang/String;)V >] : Everywhere, v7]")){
                    System.out.println("#PK: " + pk);
                    for(InstanceKey ik : pa.getPointsToSet(pk)){
                        System.out.println("\t#IK: " + ik);
//                        for(IField f: ik.getConcreteType().getAllFields()) {
//                            PointerKey fpk = pa.getHeapModel().getPointerKeyForInstanceField(ik, f);
//                            System.out.println("\t\t#FPK: " + fpk);
//                            for(InstanceKey fik : pa.getPointsToSet(fpk)){
//                                System.out.println("\t\t\t#FIK: " + fik);
//                            }
//                        }
                    }
                }
            }
//System.exit(-1);
            PointerAnalysisCommandlineDebugger padebugger = new PointerAnalysisCommandlineDebugger(cg, pa);
//            padebugger.debug();
        }

        APITarget.set(cg.getClassHierarchy());

        MaliciousPatternChecker.MaliciousPattern[] maliciousPatterns = MaliciousPatternRepo.patterns;

        for(int i=0; i<maliciousPatterns.length; i++){
            MaliciousPatternChecker.MaliciousPattern p = maliciousPatterns[i];
            for(MaliciousPatternChecker.MaliciousPoint mp : p.getPoints()){
                targetAPIs.add(new APITarget(mp.getTypeName(), mp.getSelector()));
            }
        }

        MaliciousPatternChecker mpc = new MaliciousPatternChecker(cg, pa);
        mpc.addMaliciousPatterns(maliciousPatterns);

        for(IMethod entry : HybridSDKModel.getBridgeEntries()) {
            CGNode entryNode = cg.getNode(entry, new FirstMethodContextSelector.FirstMethodContextPair(new FirstMethod(entry), Everywhere.EVERYWHERE));
//                if(!entryNode.toString().contains("vibration"))
//                    continue;
            if(entry.getNumberOfParameters() == 1){
                System.out.println("# SEED_MATCH: " + entryNode + " [ ANY ]");
                mpc.addSeed(entryNode, IFlowFunction.ANY);
            }
            for(int i=1; i<entry.getNumberOfParameters(); i++){
                System.out.println("# SEED_MATCH: " + entryNode + " [ " + (i+1) + " ]");
                mpc.addSeed(entryNode, (i+1));
            }
        }
        System.out.println("SEED: " + mpc.getSeeds().size());
        mpc.checkPatterns();
        /*
        // Malicious Pattern Checking
        MaliciousPatternChecker pc = new MaliciousPatternChecker(cg.getClassHierarchy());
        pc.addMaliciousPatterns(maliciousPatterns);
        Set<MaliciousPatternChecker.MaliciousPatternWarning> mpwSet = pc.checkPatterns(cg, pa);

        List<MaliciousPatternChecker.MaliciousPatternWarning> warns = Lists.newArrayList(mpwSet);
        Collections.sort(warns, new Comparator<MaliciousPatternChecker.MaliciousPatternWarning>() {
            @Override
            public int compare(MaliciousPatternChecker.MaliciousPatternWarning o1, MaliciousPatternChecker.MaliciousPatternWarning o2) {
                if(o1.getStartPoint().toString().compareTo(o2.getStartPoint().toString()) > 0)
                    return 1;
                return -1;
            }
        });

        for(MaliciousPatternChecker.MaliciousPatternWarning mpw : warns){
            System.out.println(mpw.toString());
        }

        long pcEnd = System.currentTimeMillis();

        System.out.println("Finish to detect malicious patterns: " + ((pcEnd - fgStart)/1000d) + "s");
        System.out.println("#Max Stack Size: " + pc.getMaxStackSize());

//        CallingComponentAnalysis cca = new CallingComponentAnalysis(cg, builder.getPointerAnalysis());
//        cca.getCallingContexts();

//        for(String w : cca.getWarnings()){
//            System.out.println("W: " + w);
//        }
*/

//            Visualizer vis = Visualizer.getInstance();
//            vis.setType(Visualizer.GraphType.Digraph);
//            Set<CGNode> visited = new HashSet<>();
//            Queue<CGNode> queue = new LinkedBlockingQueue<>();
//
//            for (CGNode n : cg) {
//                if (n.toString().contains("Application, Lcom/millennialmedia/internal/JSBridge$JSBridgeMMJS, vibrate(Ljava/lang/String;)V")) {
//                    queue.add(n);
//                    break;
//                }
//            }
//
//            while (!queue.isEmpty()) {
//                CGNode n = queue.poll();
//                visited.add(n);
//
//                Iterator<CGNode> iSucc = cg.getSuccNodes(n);
//                while (iSucc.hasNext()) {
//                    CGNode succ = iSucc.next();
//                    if (!visited.contains(succ)) {
//                        if (succ.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial)) {
//                            if (!ARModeling.isModelingMethod(cg.getClassHierarchy(), succ.getMethod()))
//                                continue;
//                        }
//                        vis.fromAtoB(n, succ);
//                        queue.add(succ);
//                    }
//                }
//            }
//
//            vis.printGraph("test.dot");
//            vis.clear();

//            for(CGNode n : cg){
//                if(n.toString().contains("Node: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice$JSIAdToDeviceInnerHandler, handleMessage(Landroid/os/Message;)V > Context: FirstMethodContextPair: [First: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice, playVideo(Ljava/lang/String;)V >] : Everywhere")){
//                    System.out.println("#N: " + n);
//                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, 4);
//                    System.out.println("\t#PK: " + pk);
//                    for(InstanceKey ik : pa.getPointsToSet(pk)){
//                        System.out.println("\t\t#IK: " + ik);
//                    }
//                }
//
//                if(n.toString().contains("Node: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, getLocation()V > Context: FirstMethodContextPair: [First: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, getLocation()V >] : Everywhere")){
//                    System.out.println("#N: " + n);
//                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, 5);
//                    System.out.println("\t#PK: " + pk);
//                    for(InstanceKey ik : pa.getPointsToSet(pk)){
//                        PointerKey fpk = pa.getHeapModel().getPointerKeyForInstanceField(ik, ik.getConcreteType().getField(Atom.findOrCreateAsciiAtom("what")));
//                        System.out.println("\t\t#IK: " + ik);
//                        System.out.println("\t\t\t#FPK: " + fpk);
//                        for(InstanceKey fik : pa.getPointsToSet(fpk)){
//                            System.out.println("\t\t\t\t#FIK: " + fik);
//                        }
//                    }
//                }
//
//                if(n.toString().contains("Node: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, getLocation()V > Context: FirstMethodContextPair: [First: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, getLocation()V >] : Everywhere")){
//                    System.out.println("#N: " + n);
//                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, 5);
//                    System.out.println("\t#PK: " + pk);
//                    for(InstanceKey ik : pa.getPointsToSet(pk)){
//                        PointerKey fpk = pa.getHeapModel().getPointerKeyForInstanceField(ik, ik.getConcreteType().getField(Atom.findOrCreateAsciiAtom("what")));
//                        System.out.println("\t\t#IK: " + ik);
//                        System.out.println("\t\t\t#FPK: " + fpk);
//                        for(InstanceKey fik : pa.getPointsToSet(fpk)){
//                            System.out.println("\t\t\t\t#FIK: " + fik);
//                        }
//                    }
//                }

                //

//                if(n.toString().contains("Node: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/MHandler, handleMessage(Landroid/os/Message;)V > Context: FirstMethodContextPair: [First: < Application, Lkr/ac/kaist/wala/hybridroid/branchsample/JSBridge, vibration()V >] : Everywhere")){
//                    int var = 4;
//                    System.out.println("#N: " + n);
//                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, var);
//
//
//                    System.out.println("\t#PK: " + pk);
////                    System.out.println("\t\tV: " + n.getIR().getSymbolTable().getConstantValue(var));
//                    for(InstanceKey ik : pa.getPointsToSet(pk)){
//                        System.out.println("\t\t#IK: " + ik);
//                    }
//                }
//
//
//                if(n.toString().contains("Node: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice, adConverted()V > Context: FirstMethodContextPair: [First: < Application, Lcom/nativex/monetization/mraid/JSIAdToDevice, adConverted()V >] : Everywhere")){
//                    int var = 9;
//                    System.out.println("#N: " + n);
//                    PointerKey pk = pa.getHeapModel().getPointerKeyForLocal(n, var);
//
//                    System.out.println("\t#PK: " + pk);
//                    for(InstanceKey ik : pa.getPointsToSet(pk)){
//                        System.out.println("\t\t#IK: " + ik);
//                        IField f = ik.getConcreteType().getField(Atom.findOrCreateAsciiAtom("what"));
//                        System.out.println("\t\t\t#F: " + f);
//                        PointerKey fPK = pa.getHeapModel().getPointerKeyForInstanceField(ik, f);
//                        for(InstanceKey fik : pa.getPointsToSet(fPK)){
//                            System.out.println("\t\t\t\t#FIK: " + fik);
//                        }
//                    }
//                }
//            }

        long end = System.currentTimeMillis();
        System.out.println("#AnalysisTime: " + ((end - start)/1000d) + "s");
    }

    public static void printIR(CallGraph cg, String out) {
        File outFile = new File(out);

        try {
            BufferedWriter e = new BufferedWriter(new FileWriter(outFile));
            Iterator var4 = cg.iterator();

            while(var4.hasNext()) {
                CGNode n = (CGNode)var4.next();
                e.write("N" + n.toString() + "\n");
                e.write("=======================================\n");
                IR ir = makeIR(n);
                if(ir != null) {
                    SSAInstruction[] insts = ir.getInstructions();
                    int index = 1;

                    for(int iSucc = 0; iSucc < insts.length; ++iSucc) {
                        SSAInstruction succ = insts[iSucc];
                        if(succ != null) {
                            e.write("( " + succ.iindex + " ) " + succ + "\n");
                        }
                    }

                    e.write("[Succ]=================================\n");
                    Iterator var13 = cg.getSuccNodes(n);

                    while(var13.hasNext()) {
                        CGNode var12 = (CGNode)var13.next();
                        e.write("\t" + var12 + "\n");
                    }
                }

                e.newLine();
                e.newLine();
            }

            e.flush();
            e.close();
        } catch (IOException var11) {
            var11.printStackTrace();
        }

    }

    private static IR makeIR(CGNode n) {
        IR ir = n.getIR();
        if(ir == null) {
            DexIRFactory irFactory = new DexIRFactory();

            try {
                ir = irFactory.makeIR(n.getMethod(), Everywhere.EVERYWHERE, SSAOptions.defaultOptions());
            } catch (NullPointerException var4) {
                return null;
            }
        }

        return ir;
    }
}
