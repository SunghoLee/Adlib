package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.SingleField;

/**
 * Created by leesh on 27/02/2018.
 */
public class LocalDataFact implements DataFact {

    private final CGNode n;
    private final int v;
    private final Field f;

    public LocalDataFact(CGNode n, int v, Field f){
        if(f instanceof SingleField)
            Assertions.UNREACHABLE("Single field cannot be assigned itself: " + f);
        this.n = n;
        this.v = v;
        this.f = f;
    }

    public CGNode getNode(){
        return this.n;
    }

    public int getVar(){
        return this.v;
    }

    public Field getField(){
        return this.f;
    }

    @Override
    public boolean accept(IDataFactFilter filter) {
        return filter.accept(this);
    }

    @Override
    public int hashCode(){
        return n.hashCode() + v + f.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof LocalDataFact){
            LocalDataFact ldf = (LocalDataFact) o;
            if(ldf.n.equals(this.n) && ldf.f.equals(this.f) && ldf.v == this.v)
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return n + " [ " + v + " ] F( " + f + " )";
    }
}
