package kr.ac.kaist.wala.adlib.dataflow.ifds;

import com.ibm.wala.types.FieldReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.SingleField;

/**
 * Created by leesh on 27/02/2018.
 */
public class GlobalDataFact implements DataFact {

    private final FieldReference f;
    private final Field regf;
    public GlobalDataFact(FieldReference f, Field regf){
        if(regf instanceof SingleField)
            Assertions.UNREACHABLE("Single field cannot be assigned itself: " + f);
        this.f = f;
        this.regf = regf;
    }

    public FieldReference getGlobalFact(){
        return this.f;
    }

    @Override
    public Field getField() {
        return this.regf;
    }

    @Override
    public boolean accept(IDataFactFilter filter) {
        return filter.accept(this);
    }

    @Override
    public int hashCode(){
        return f.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof GlobalDataFact){
            GlobalDataFact gdf = (GlobalDataFact) o;
            if(gdf.f.equals(this.f))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return " [ " + f + " ] F( " + regf + " )";
    }
}
