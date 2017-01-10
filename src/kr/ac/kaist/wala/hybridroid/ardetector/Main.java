package kr.ac.kaist.wala.hybridroid.ardetector;

import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import kr.ac.kaist.hybridroid.utils.VisualizeCGTest;
import kr.ac.kaist.wala.hybridroid.ardetector.callgraph.CallGraphBuilderForHybridSDK;

/**
 * Created by leesh on 05/01/2017.
 */
public class Main {
    public static void main(String[] args) throws CallGraphBuilderCancelException, ClassHierarchyException {
        String prop = args[0];
        String sdk = args[1];

        CallGraphBuilderForHybridSDK builder = new CallGraphBuilderForHybridSDK(prop, sdk);
        CallGraph cg = builder.makeCallGraph();
        VisualizeCGTest.visualizeCallGraph(cg, "cfg_out", false);
    }
}
