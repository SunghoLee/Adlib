package kr.ac.kaist.wala.adlib.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.CallGraph;
import com.ibm.wala.ipa.callgraph.propagation.ConstantKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;
import com.ibm.wala.util.collections.Pair;

import java.util.Iterator;

/**
 * Created by leesh on 18/04/2018.
 */
public class IntConstantKey implements InstanceKey{
    private final ConstantKey constantKey;
    private final CGNode n;

    public IntConstantKey(ConstantKey base, CGNode n){
        this.constantKey = base;
        this.n = n;
    }

    public ConstantKey getBase(){
        return this.constantKey;
    }

    public CGNode getNode(){
        return this.n;
    }

    @Override
    public int hashCode(){
        return constantKey.hashCode() + n.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof IntConstantKey){
            IntConstantKey ick = (IntConstantKey) o;
            if(ick.constantKey.equals(this.constantKey) && ick.getNode().equals(this.n))
                return true;

        }
        return false;
    }

    @Override
    public String toString(){
        return getBase().toString() + " IN " + n;
    }

    @Override
    public IClass getConcreteType() {
        return constantKey.getConcreteType();
    }

    @Override
    public Iterator<Pair<CGNode, NewSiteReference>> getCreationSites(CallGraph CG) {
        return constantKey.getCreationSites(CG);
    }
}
