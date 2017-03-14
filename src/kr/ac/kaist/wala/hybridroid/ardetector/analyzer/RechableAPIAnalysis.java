package kr.ac.kaist.wala.hybridroid.ardetector.analyzer;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CallGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 08/03/2017.
 */
public class RechableAPIAnalysis {
    private CallGraph cg;
    private Set<IMethod> entries;
    private Set<APITarget> targets;

    public RechableAPIAnalysis(CallGraph cg, Set<IMethod> entries){
        this.cg = cg;
        this.entries = entries;
        targets = new HashSet<APITarget>();
    }

    public void addAPITarget(APITarget target){
        targets.add(target);
    }
}
