package kr.ac.kaist.wala.adlib.dataflow.ifds.model;

import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Created by leesh on 11/10/2017.
 */
public class MethodFlowModel {
    public final static int RECEIVERV = 0;
    public final static int RETV = -1;
    public final static int NONE = -2;
    public final static int ANY = -99;

    private final int[] from;
    private final int[] to;
    private final MethodReference ref;
    private boolean isMutable = false;

    // 0 is for receivers, and other positive number represents indexes of arguments.
    public static MethodFlowModel make(MethodReference ref, int[] from, int[] to){
        for(int f : from)
            if(ref.getNumberOfParameters() < f)
                Assertions.UNREACHABLE("Unexpected flow FROM argument number: " + f + " in " + ref);
        for(int t : to)
            if(ref.getNumberOfParameters() < t)
                Assertions.UNREACHABLE("Unexpected flow FROM argument number: " + t + " in " + ref);

        return new MethodFlowModel(ref, from, to);
    }

    // 0 is for receivers, and other positive number represents indexes of arguments.
    public static MethodFlowModel make(MethodReference ref, int[] from, int[] to, boolean isMutable){
        for(int f : from)
            if(ref.getNumberOfParameters() < f)
                Assertions.UNREACHABLE("Unexpected flow FROM argument number: " + f + " in " + ref);
        for(int t : to)
            if(ref.getNumberOfParameters() < t)
                Assertions.UNREACHABLE("Unexpected flow FROM argument number: " + t + " in " + ref);

        if(isMutable) {
            boolean hasReceiver = false;

            for (int f : to) {
                if (f == 0)
                    hasReceiver = true;
            }
            if(!hasReceiver){
                Assertions.UNREACHABLE("Mutable method model must have a receiver as to : " + ref);
            }
        }
        return new MethodFlowModel(ref, from, to, isMutable);
    }

    MethodFlowModel(MethodReference ref, int[] from, int[] to){
        this.ref = ref;
        this.from = from;
        this.to = to;
    }

    MethodFlowModel(MethodReference ref, int[] from, int[] to, boolean isMutable){
        this(ref, from, to);
        this.isMutable = isMutable;
    }

    public MethodReference getReference(){
        return ref;
    }

    public int[] matchFlow(int data){
        for(int i=0; i<from.length; i++){
            if(from[i] == data)
                return to;
        }
        return new int[]{};
    }

    @Override
    public int hashCode(){
        return ref.hashCode() + from.hashCode() + to.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof MethodFlowModel){
            MethodFlowModel mfm = (MethodFlowModel) o;
            if(mfm.ref.equals(this.ref) && Arrays.equals(mfm.from, this.from) && Arrays.equals(mfm.to, this.to))
                return true;
        }
        return false;
    }

    public Set<Field> matchField(Field f){
        return Collections.singleton(f);
    }

    public int[] getFrom(){
        return this.from;
    }

    public int[] getTo(){
        return this.to;
    }

    @Override
    public String toString(){
        return "[FlowModel] " + ref;
    }

    public boolean isMutable(){
        return this.isMutable;
    }
}
