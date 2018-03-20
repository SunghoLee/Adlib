package kr.ac.kaist.wala.adlib.callgraph;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.ClassTargetSelector;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import kr.ac.kaist.wala.adlib.model.AbstractModelClass;
import kr.ac.kaist.wala.adlib.model.message.AndroidMessageModelClass;

/**
 * Created by leesh on 20/03/2018.
 */
public class ModelingClassTargetSelector implements ClassTargetSelector {
    private final ClassTargetSelector base;
    private AbstractModelClass[] models;

    public ModelingClassTargetSelector(ClassTargetSelector base, IClassHierarchy cha){
        this.base = base;

        init(cha);
    }

    private void init(IClassHierarchy cha){
        models = new AbstractModelClass[]{
                AndroidMessageModelClass.getInstance(cha),
        };
    }

    @Override
    public IClass getAllocatedTarget(CGNode caller, NewSiteReference site) {
        IClass c = base.getAllocatedTarget(caller, site);

        if(c != null){
            for(IClass mc : models){
                if(c.getName().equals(mc.getName()))
                    return mc;
            }
        }

        return c;
    }
}
