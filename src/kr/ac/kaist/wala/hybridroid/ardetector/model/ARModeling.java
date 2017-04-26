package kr.ac.kaist.wala.hybridroid.ardetector.model;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import kr.ac.kaist.wala.hybridroid.ardetector.model.components.AndroidAlertDialogBuilderModelClass;
import kr.ac.kaist.wala.hybridroid.ardetector.model.components.AndroidHandlerModelClass;
import kr.ac.kaist.wala.hybridroid.ardetector.model.context.AndroidContextWrapperModelClass;
import kr.ac.kaist.wala.hybridroid.ardetector.model.thread.*;

/**
 * Created by leesh on 12/04/2017.
 */
public class ARModeling {

    public static boolean isModelingMethod(IClassHierarchy cha, IMethod m){
        AndroidAlertDialogBuilderModelClass a = AndroidAlertDialogBuilderModelClass.getInstance(cha);
        AndroidHandlerModelClass b = AndroidHandlerModelClass.getInstance(cha);
        AndroidContextWrapperModelClass c = AndroidContextWrapperModelClass.getInstance(cha);
        AndroidAsyncTaskModelClass d = AndroidAsyncTaskModelClass.getInstance(cha);
        AndroidViewModelClass e = AndroidViewModelClass.getInstance(cha);
        JavaReferenceModelClass f = JavaReferenceModelClass.getInstance(cha);
        JavaThreadModelClass g = JavaThreadModelClass.getInstance(cha);
        JavaThreadPoolExecutorModelClass h = JavaThreadPoolExecutorModelClass.getInstance(cha);
        JavaTimerModelClass i = JavaTimerModelClass.getInstance(cha);
        JavaExecutorModelClass j = JavaExecutorModelClass.getInstance(cha);

        if(/*a.getAllMethods().contains(m) || */b.getAllMethods().contains(m) || c.getAllMethods().contains(m) || d.getAllMethods().contains(m) || e.getAllMethods().contains(m) || f.getAllMethods().contains(m) || g.getAllMethods().contains(m) || h.getAllMethods().contains(m) || i.getAllMethods().contains(m) || j.getAllMethods().contains(m)) {
            return true;
        }else {
            IClass actClass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/app/Activity"));
            if(cha.isSubclassOf(m.getDeclaringClass(), actClass) && m.getSelector().equals(Selector.make("runOnUiThread(Ljava/lang/Runnable;)V")))
                return true;

            ////Landroid/os/Message, sendToTarget()V
            IClass msgClass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/os/Message"));
            if(cha.isSubclassOf(m.getDeclaringClass(), msgClass) && m.getSelector().equals(Selector.make("sendToTarget()V")))
                return true;

            IClass hdClass = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Landroid/os/Handler"));
            if(cha.isSubclassOf(m.getDeclaringClass(), hdClass) && m.getSelector().equals(Selector.make("sendEmptyMessage(I)Z")))
                return true;
//            if (m.getDeclaringClass().getName().equals(TypeName.findOrCreate("Landroid/app/Activity")) && m.getSelector().equals(Selector.make("runOnUiThread((Ljava/lang/Runnable;)V")))
//                return true;
        }

        return false;
    }
}
