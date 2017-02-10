package kr.ac.kaist.wala.hybridroid.ardetector;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import kr.ac.kaist.hybridroid.utils.VisualizeCGTest;
import kr.ac.kaist.wala.hybridroid.ardetector.analyzer.CallingComponentAnalysis;
import kr.ac.kaist.wala.hybridroid.ardetector.callgraph.CallGraphBuilderForHybridSDK;

/**
 * Created by leesh on 05/01/2017.
 */
public class Main {
    public static void main(String[] args) throws CallGraphBuilderCancelException, ClassHierarchyException {
        String prop = args[0];
        String sdk = args[1];

        long start = System.currentTimeMillis();
        CallGraphBuilderForHybridSDK builder = new CallGraphBuilderForHybridSDK(prop, sdk);
        CallGraph cg = builder.makeCallGraph();
        CallingComponentAnalysis cca = new CallingComponentAnalysis(cg);
        cca.getCallingContexts();
        for(String w : cca.getWarnings()){
            System.out.println("W: " + w);
        }
        VisualizeCGTest.visualizeCallGraph(cg, "cfg_out", false);
        VisualizeCGTest.printIR(cg, "ir");
        long end = System.currentTimeMillis();
        System.out.println("#AnalysisTime: " + ((end - start)/1000d) + "s");
    }
}
