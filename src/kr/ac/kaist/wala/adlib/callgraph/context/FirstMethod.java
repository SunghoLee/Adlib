package kr.ac.kaist.wala.adlib.callgraph.context;

import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.ContextItem;

/**
 * Created by leesh on 12/04/2017.
 */
class FirstMethod implements ContextItem {
    public static final FirstMethod DUMMY_FIRST_METHOD = new FirstMethod(null);
    private final IMethod m;

    public FirstMethod(IMethod m){
        this.m = m;
    }

    @Override
    public int hashCode() {
        return 17326912;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof FirstMethod){
            FirstMethod fm = (FirstMethod) obj;
            if(this.m == null && fm.m == null)
                return true;

            if(fm.m != null && this.m != null && fm.m.equals(this.m))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        if(m != null)
            return "[First: " + m + "]";
        else
            return "[First: DUMMY]";
    }

    public IMethod getMethod(){
        return m;
    }
}

