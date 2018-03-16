package kr.ac.kaist.wala.adlib.dataflow.ifds.model;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeReference;

/**
 * Created by leesh on 16/03/2018.
 */
public class BuiltinFlowPropagationModel {
    private static TypeReference[] allowedClasses = {
        TypeReference.find(ClassLoaderReference.Primordial, "Landroid/os/Message"),
    };

    public boolean isAllowed(CGNode n){
        TypeReference tr = n.getMethod().getDeclaringClass().getReference();

        for(TypeReference allowedClass : allowedClasses){
            if(tr.equals(allowedClass))
                return true;
        }

        if(n.getMethod().isSynthetic())
            return true;

        if(n.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application))
            return true;

        return false;
    }
}
