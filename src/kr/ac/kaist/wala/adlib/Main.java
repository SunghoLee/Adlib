package kr.ac.kaist.wala.adlib;

import kr.ac.kaist.wala.hybridroid.util.graph.visualize.Visualizer;
import java.util.Iterator;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ssa.IR;
import com.ibm.wala.ssa.SSAOptions;

import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import kr.ac.kaist.wala.adlib.analysis.APITarget;
import kr.ac.kaist.wala.adlib.analysis.malicious.MaliciousPatternChecker;
import kr.ac.kaist.wala.adlib.analysis.malicious.MaliciousPatternRepo;
import kr.ac.kaist.wala.adlib.callgraph.CallGraphBuilderForHybridSDK;
import kr.ac.kaist.wala.adlib.callgraph.HybridSDKModel;
import kr.ac.kaist.wala.adlib.callgraph.context.FirstMethod;
import kr.ac.kaist.wala.adlib.callgraph.context.FirstMethodContextSelector;
import kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction;

/**
 * Created by leesh on 05/01/2017.
 */
public class Main {
    private static boolean DEBUG = false;

    private static Set<APITarget> targetAPIs = new HashSet<>();

    public static void main(String[] args) throws CallGraphBuilderCancelException, ClassHierarchyException, ParseException, IOException {
        String prop = args[0];
        String sdk = args[1];
        String initInstFile = args[2];

        boolean onlyBridge = ((args.length > 3 && args[3].equals("b"))? true : false);

        System.out.println(Config.describe());
        if(DEBUG) {
            System.out.println("##################### INIT Fs #####################");
            System.out.println("#PROP: " + prop);
            System.out.println("#AdSDK: " + sdk);
            System.out.println("#INIT: " + initInstFile);
            System.out.println("###################################################");
        }
        long start = System.currentTimeMillis();
        long cgStart = System.currentTimeMillis();

        CallGraphBuilderForHybridSDK builder = new CallGraphBuilderForHybridSDK(prop, sdk, InitInstsParser.parse(initInstFile));

        if(onlyBridge)
            System.exit(-1);
        System.out.println("#Analyzing " + sdk + "...");

        System.out.println("Constructing a CallGraph...");
        CallGraph cg = builder.makeCallGraph();
        PointerAnalysis<InstanceKey> pa = builder.getPointerAnalysis();

        long cgEnd = System.currentTimeMillis();

        System.out.println("\tDone: " + ((cgEnd - cgStart)/1000d) + "s");

        System.out.println("\n\nSetting malicious patterns...");

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

        System.out.println("\tDone!");

        System.out.println("\n\nChecking malicious patterns...");
        for(IMethod entry : HybridSDKModel.getBridgeEntries()) {
            for(CGNode entryNode : cg.getNodes(entry.getReference())){
                //CGNode entryNode = cg.getNode(entry, new FirstMethodContextSelector.FirstMethodContextPair(new FirstMethod(entry), Everywhere.EVERYWHERE));
                if(entry.getNumberOfParameters() == 1){
                    mpc.addSeed(entryNode, IFlowFunction.ANY);
                }
                for(int i=1; i<entry.getNumberOfParameters(); i++){
                    mpc.addSeed(entryNode, (i+1));
                }
            }
        }
        List<String> warn = mpc.checkPatterns();
        long pcEnd = System.currentTimeMillis();

        System.out.println("\tDone: " + ((pcEnd - cgEnd)/1000d) + "s");

        System.out.println("\n\n####### RESULT ( " + sdk + " ) #######");
        for(String s : warn){
            System.out.println(s);
        }
        System.out.println("######################");

        System.out.println("\n\n#Total Analysis Time: " + ((pcEnd - start)/1000d) + "s");
        System.out.println("\n\n#Total Program Points: " + mpc.getTotalProgramPoint());
        System.out.println("\n\n#Total Data Facts: " + mpc.getTotalDF().size());

        visualize(cg, "cg.dot");
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

    private static void visualize(CallGraph cg, String out){
        Visualizer vis = Visualizer.getInstance();
        for(CGNode n : cg){
            for(Iterator<CGNode> iSucc = cg.getSuccNodes(n); iSucc.hasNext();) {
                CGNode succNode = iSucc.next();
                vis.fromAtoB(n, succNode);
            }
        }
        vis.printGraph(out);
    }
}
