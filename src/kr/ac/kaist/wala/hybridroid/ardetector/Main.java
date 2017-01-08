package kr.ac.kaist.wala.hybridroid.ardetector;

import com.ibm.wala.ipa.callgraph.CallGraphBuilderCancelException;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import kr.ac.kaist.wala.hybridroid.ardetector.bridge.BridgeClass;
import kr.ac.kaist.wala.hybridroid.ardetector.hybridroid.HybriDroidDriver;

import java.util.Set;

/**
 * Created by leesh on 05/01/2017.
 */
public class Main {
    public static void main(String[] args) throws CallGraphBuilderCancelException, ClassHierarchyException {
        String prop = args[0];
        String sdk = args[1];


        HybriDroidDriver driver = new HybriDroidDriver(sdk, prop);
        Set<BridgeClass> bridges = driver.getBridgeClassesViaAnn();


    }
}
