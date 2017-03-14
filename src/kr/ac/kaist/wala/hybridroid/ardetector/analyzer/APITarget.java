package kr.ac.kaist.wala.hybridroid.ardetector.analyzer;

import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;

/**
 * Created by leesh on 08/03/2017.
 */
public final class APITarget {
    private TypeName tn;
    private Selector s;

    public APITarget(TypeName tn, Selector s){
        this.tn = tn;
        this.s = s;
    }

    public TypeName getTypeName() {
        return this.tn;
    }

    public Selector getSelector() {
        return this.s;
    }

    @Override
    public int hashCode(){
        return tn.hashCode() + s.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof APITarget){
            APITarget t = (APITarget) o;
            if(t.tn.equals(tn) && t.s.equals(s))
                return true;
        }
        return false;
    }
}
