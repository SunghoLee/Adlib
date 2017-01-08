package kr.ac.kaist.wala.hybridroid.ardetector.callgraph;

import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import kr.ac.kaist.wala.hybridroid.ardetector.bridge.BridgeClass;
import kr.ac.kaist.wala.hybridroid.ardetector.hybridroid.HybriDroidDriver;

import java.util.Set;

/**
 * Created by leesh on 06/01/2017.
 */
public class CallGraphBuilderForHybridSDK {
    private final Set<BridgeClass> bridges;
    private final Iterable<Entrypoint> entries;
    private final AnalysisScope scope;
    private final AnalysisOptions options;
    private final CallGraphBuilder delegate;

    public CallGraphBuilderForHybridSDK(String prop, String sdk) throws CallGraphBuilderCancelException, ClassHierarchyException {
        bridges = collectBridgeInfo(prop, sdk);
        entries = getEntrypoints();
        scope = makeAnalysisScope();
        options = makeAnalysisOptions();
        delegate = getDelegate();
    }

    protected Set<BridgeClass> collectBridgeInfo(String prop, String sdk) throws CallGraphBuilderCancelException, ClassHierarchyException {
        HybriDroidDriver driver = new HybriDroidDriver(prop, sdk);
        return driver.getBridgeClassesViaAnn();
    }

    protected Iterable<Entrypoint> getEntrypoints(){
        return null;
    }

    protected AnalysisScope makeAnalysisScope(){
        return null;
    }

    protected AnalysisOptions makeAnalysisOptions(){
        AnalysisOptions options = new AnalysisOptions();
        return null;
    }

    protected CallGraphBuilder getDelegate(){
        return null;
    }

    public Set<BridgeClass> getBridges(){
        return bridges;
    }

    public CallGraph makeCallGraph() throws CallGraphBuilderCancelException {
        return delegate.makeCallGraph(options, null);
    }
}
