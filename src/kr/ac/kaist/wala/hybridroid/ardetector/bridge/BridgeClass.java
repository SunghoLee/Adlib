package kr.ac.kaist.wala.hybridroid.ardetector.bridge;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import kr.ac.kaist.hybridroid.types.HybriDroidTypes;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by leesh on 05/01/2017.
 */
public class BridgeClass {

    private Set<BridgeMethod> mSet;
    private TypeReference bridgeClassReference;
    public BridgeClass(IClass c){
        this.bridgeClassReference = c.getReference();
        initBridgeMethods(c);
    }

    private void initBridgeMethods(IClass c){
        mSet = new HashSet<BridgeMethod>();

        for(IMethod m : c.getAllMethods()){
            if(HybriDroidTypes.hasJavascriptInterfaceAnnotation(m))
                mSet.add(new BridgeMethod(m.getReference()));
        }
    }

    public TypeReference getReference(){
        return bridgeClassReference;
    }

    public Set<BridgeMethod> getAccessibleMethods(){
        return mSet;
    }

    @Override
    public int hashCode(){
        return bridgeClassReference.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof BridgeClass){
            if(this.bridgeClassReference.equals(((BridgeClass)o).bridgeClassReference))
                return true;
        }
        return false;
    }

    static class BridgeMethod {
        private MethodReference mr;

        public BridgeMethod(MethodReference mr){
            this.mr = mr;
        }

        public String getSignature(){
            return mr.getSignature();
        }

        public Selector getSelector(){
            return mr.getSelector();
        }

        public MethodReference getMethodReference(){
            return mr;
        }

        @Override
        public int hashCode(){
            return mr.hashCode();
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof BridgeMethod){
                if(this.mr.equals(((BridgeMethod)o).mr))
                    return true;
            }
            return false;
        }

        @Override
        public String toString(){
            return mr.toString();
        }
    }

    @Override
    public String toString(){
        String res = "[Bridge] " + bridgeClassReference + "\n";
        Iterator<BridgeMethod> iM = mSet.iterator();
        while(iM.hasNext()){
            BridgeMethod bm = iM.next();
            res += "\t" + bm;
            if(iM.hasNext())
                res += "\n";
        }
        return res;
    }
}
