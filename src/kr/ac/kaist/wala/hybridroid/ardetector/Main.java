package kr.ac.kaist.wala.hybridroid.ardetector;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import kr.ac.kaist.wala.hybridroid.ardetector.analyzer.CallingComponentAnalysis;
import kr.ac.kaist.wala.hybridroid.ardetector.callgraph.CallGraphBuilderForHybridSDK;
import kr.ac.kaist.wala.hybridroid.util.print.IRPrinter;
import kr.ac.kaist.wala.hybridroid.utils.VisualizeCGTest;

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
        long end = System.currentTimeMillis();
        System.err.println("Finish to build a callgraph: " + ((end - start)/1000d) + "s");
        CallingComponentAnalysis cca = new CallingComponentAnalysis(cg, builder.getPointerAnalysis());
        cca.getCallingContexts();
        for(String w : cca.getWarnings()){
            System.out.println("W: " + w);
        }
        VisualizeCGTest.visualizeCallGraph(cg, "cfg_out", false);
        IRPrinter.printIR(cg, "ir");
        end = System.currentTimeMillis();
        System.out.println("#AnalysisTime: " + ((end - start)/1000d) + "s");
    }
}
