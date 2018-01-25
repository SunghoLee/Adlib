package kr.ac.kaist.wala.adlib.model;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.SyntheticClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashMapFactory;

import java.util.Map;

/**
 * Created by leesh on 08/12/2017.
 */
public abstract class AbstractModelClass extends SyntheticClass{
    protected Map<Selector, IMethod> methods = HashMapFactory.make();

    public AbstractModelClass(TypeReference T, IClassHierarchy cha) {
        super(T, cha);
    }

    public IMethod match(TypeReference t, Selector s){
        if(t.getName().equals(getReference().getName()) && methods.containsKey(s))
            return methods.get(s);

        return null;
    }
}
