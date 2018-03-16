package kr.ac.kaist.wala.adlib.callgraph.context;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.intset.IntSet;
import kr.ac.kaist.wala.adlib.callgraph.HybridSDKModel;
import kr.ac.kaist.wala.adlib.model.ARModeling;

/**
 * Created by leesh on 12/04/2017.
 */
public class FirstMethodContextSelector implements ContextSelector{

    public static final ContextKey FIRST_METHOD = new ContextKey(){
        @Override
        public String toString() {
            return "FIRST_METHOD_KEY";
        }
    };

    private final ContextSelector base;

    public FirstMethodContextSelector(ContextSelector base) {
        this.base = base;
    }

    private FirstMethod getFirstMethod(CGNode caller, IMethod target){
        if(caller.getMethod().toString().contains("FakeRootClass") && caller.getMethod().toString().contains("fakeRootMethod")){
            if(HybridSDKModel.getBridgeEntries().contains(target)){
                return new FirstMethod(target);
            }else
                return FirstMethod.DUMMY_FIRST_METHOD;
        }else{
            if(target.getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Primordial) && !ARModeling.isModelingMethod(caller.getClassHierarchy(), target) &&
                    !target.getDeclaringClass().getName().toString().equals("Landroid/os/Handler") &&
                    !target.getDeclaringClass().getName().toString().equals("Landroid/os/Message"))
                return FirstMethod.DUMMY_FIRST_METHOD;

            return ((FirstMethodContextPair)caller.getContext()).getFirstMethod();
        }
    }

    @Override
    public Context getCalleeTarget(CGNode caller, CallSiteReference site, IMethod callee, InstanceKey[] receiver) {
        Context baseCtxt = base.getCalleeTarget(caller, site, callee, receiver);
        FirstMethod fm = getFirstMethod(caller, callee);

        return new FirstMethodContextPair(fm, baseCtxt);
    }

    @Override
    public IntSet getRelevantParameters(CGNode caller, CallSiteReference site) {
        return base.getRelevantParameters(caller, site);
    }

    public static class FirstMethodContextPair implements Context{
        private final Context base;
        private final FirstMethod fm;

        public FirstMethodContextPair(FirstMethod fm, Context base){
            this.base = base;
            this.fm = fm;
        }

        @Override
        public int hashCode() {
            return this.base.hashCode() + fm.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof FirstMethodContextPair){
                FirstMethodContextPair p = (FirstMethodContextPair) obj;

                if(p.fm.equals(this.fm) && p.base.equals(this.base))
                    return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return "FirstMethodContextPair: " + fm.toString() + " : " + base.toString();
        }

        @Override
        public ContextItem get(ContextKey name) {
            if(FIRST_METHOD.equals(name))
                return fm;
            else
                return base.get(name);
        }

        public Context getBaseContext(){
            return this.base;
        }

        public FirstMethod getFirstMethod(){
            return this.fm;
        }
    }
}
