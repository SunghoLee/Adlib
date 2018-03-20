package kr.ac.kaist.wala.adlib.callgraph;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import kr.ac.kaist.wala.adlib.model.AbstractModelClass;
import kr.ac.kaist.wala.adlib.model.components.AndroidAlertDialogBuilderModelClass;
import kr.ac.kaist.wala.adlib.model.components.AndroidHandlerModelClass;
import kr.ac.kaist.wala.adlib.model.context.AndroidContextWrapperModelClass;
import kr.ac.kaist.wala.adlib.model.message.AndroidMessageModelClass;
import kr.ac.kaist.wala.adlib.model.string.JavaStringBufferModelClass;
import kr.ac.kaist.wala.adlib.model.string.JavaStringBuilderModelClass;
import kr.ac.kaist.wala.adlib.model.string.JavaStringModelClass;
import kr.ac.kaist.wala.adlib.model.thread.*;

/**
 * Created by leesh on 20/03/2018.
 */
public class ModelingMethodTargetSelector implements MethodTargetSelector {
    private AbstractModelClass[] models;
    private final MethodTargetSelector base;
    private final IClassHierarchy cha;

    public ModelingMethodTargetSelector(MethodTargetSelector base, IClassHierarchy cha){
        this.base = base;
        this.cha = cha;
        init(cha);
    }

    private void init(IClassHierarchy cha){
        models = new AbstractModelClass[]{
                AndroidContextWrapperModelClass.getInstance(cha),
                AndroidAlertDialogBuilderModelClass.getInstance(cha),
                AndroidHandlerModelClass.getInstance(cha),
                JavaReferenceModelClass.getInstance(cha),
                JavaTimerModelClass.getInstance(cha),
                JavaThreadModelClass.getInstance(cha),
                JavaThreadPoolExecutorModelClass.getInstance(cha),
                AndroidHandlerModelClass.getInstance(cha),
                AndroidAsyncTaskModelClass.getInstance(cha),
                JavaExecutorModelClass.getInstance(cha),
                AndroidViewModelClass.getInstance(cha),
                AndroidMessageModelClass.getInstance(cha),
                JavaStringModelClass.getInstance(cha),
                JavaStringBufferModelClass.getInstance(cha),
                JavaStringBuilderModelClass.getInstance(cha),
        };
    }

    @Override
    public IMethod getCalleeTarget(CGNode caller, CallSiteReference site, IClass receiver) {
        IMethod baseM = base.getCalleeTarget(caller, site, receiver);

        if(baseM != null) {
            boolean isStatic = baseM.isStatic();

            for (AbstractModelClass c : models) {
                if (isStatic) {
                    if (baseM.getDeclaringClass().getName().equals(c.getName())) {
                        for (IMethod m : c.getAllMethods()) {
                            if (m.getSelector().equals(baseM.getSelector())) {
                                return m;
                            }
                        }
                    }
                } else {
                    if (baseM.getDeclaringClass().getName().equals(c.getName()))
                        for (IMethod m : c.getAllMethods()) {
                            if (m.getSelector().equals(baseM.getSelector())) {
                                return m;
                            }
                        }
                }
            }
        }

        return baseM;
    }
}
