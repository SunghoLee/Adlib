package kr.ac.kaist.wala.hybridroid.ardetector.analyzer;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 01/02/2017.
 */
public class CallingComponentAnalysis {
    private final CallGraph cg;
    public final static TypeReference CONTEXT_WRAPPER = TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/context/ContextWrapper");

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
}
