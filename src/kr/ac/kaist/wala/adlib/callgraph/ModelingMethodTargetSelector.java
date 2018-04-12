package kr.ac.kaist.wala.adlib.callgraph;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.MethodTargetSelector;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.Selector;
import kr.ac.kaist.wala.adlib.model.ModelClass;
import kr.ac.kaist.wala.adlib.model.TypeBasedBuiltinModeler;
import kr.ac.kaist.wala.adlib.model.components.AndroidAlertDialogBuilderModelClass;
import kr.ac.kaist.wala.adlib.model.components.AndroidHandlerModelClass;
import kr.ac.kaist.wala.adlib.model.context.AndroidContextWrapperModelClass;
import kr.ac.kaist.wala.adlib.model.message.AndroidMessageModelClass;
import kr.ac.kaist.wala.adlib.model.string.JavaAbstractStringBuilderModelClass;
import kr.ac.kaist.wala.adlib.model.string.JavaStringBuilderModelClass;
import kr.ac.kaist.wala.adlib.model.thread.*;

/**
 * Created by leesh on 20/03/2018.
 */
public class ModelingMethodTargetSelector implements MethodTargetSelector {
    private ModelClass[] models;
    private final MethodTargetSelector base;
    private final IClassHierarchy cha;
    private final TypeBasedBuiltinModeler modeler;
    public ModelingMethodTargetSelector(MethodTargetSelector base, IClassHierarchy cha){
        this.base = base;
        this.cha = cha;
        this.modeler = new TypeBasedBuiltinModeler();
        init(cha);
    }

    private void init(IClassHierarchy cha){
        modeler.model(cha);
        models = new ModelClass[]{
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
                AndroidActivityModelClass.getInstance(cha),
//                JavaStringModelClass.getInstance(cha),
//                JavaStringBufferModelClass.getInstance(cha),
                JavaStringBuilderModelClass.getInstance(cha),
                JavaAbstractStringBuilderModelClass.getInstance(cha),
        };
    }

    @Override
    public IMethod getCalleeTarget(CGNode caller, CallSiteReference site, IClass receiver) {
        Selector ss = site.getDeclaredTarget().getSelector();
        IMethod baseM = base.getCalleeTarget(caller, site, receiver);

        if(baseM != null) {
            boolean isStatic = baseM.isStatic();
            for (ModelClass c : models) {
                if (isStatic) {
                    if (baseM.getDeclaringClass().getName().equals(c.getName())) {
                        for (IMethod m : c.getModeledMethods()) {
                            if (m.getSelector().equals(baseM.getSelector()) && m.isStatic()) {
                                return m;
                            }
                        }
                    }
                } else {
                    if (baseM.getDeclaringClass().getName().equals(c.getName())) {
                        for (IMethod m : c.getModeledMethods()) {
                            if (m.getSelector().equals(baseM.getSelector())) {
                                return m;
                            }
                        }
                    }
                }
            }

            if(receiver != null){
                ModelClass modelClass = modeler.getClass(baseM.getDeclaringClass());
                if(modelClass != null) {
                    IMethod m = modelClass.getMethod(site.getDeclaredTarget().getSelector());

                    while(m == null && modelClass != null && modelClass.getSuperclass() != null) {
                        modelClass = modeler.getClass(modelClass.getSuperclass());
                        m = modelClass.getMethod(site.getDeclaredTarget().getSelector());
                    }

                    if(m != null) {
                        return modelClass.getMethod(site.getDeclaredTarget().getSelector());
                    }
                }
            }else if(isStatic){
                IClass staticKlass = cha.lookupClass(site.getDeclaredTarget().getDeclaringClass());
                ModelClass mc = modeler.getClass(staticKlass);
                if(mc != null) {
                    for (IMethod m : mc.getDeclaredMethods()) {
                        if (m.isStatic() && m.getSelector().equals(site.getDeclaredTarget().getSelector())) {
                            return m;
                        }
                    }
                }
            }
        }

        return baseM;
    }
}
