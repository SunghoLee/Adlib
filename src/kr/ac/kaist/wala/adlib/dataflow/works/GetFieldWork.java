package kr.ac.kaist.wala.adlib.dataflow.works;

import com.ibm.wala.classLoader.IField;
import com.ibm.wala.ssa.SSAGetInstruction;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by leesh on 25/09/2017.
 */
public class GetFieldWork extends AbstractWork {

    private static Map<IField, GetFieldWork> instanceMap = new HashMap<>();
    private IField f;

    public static GetFieldWork getInstance(IField f, Work w){
        if(!instanceMap.containsKey(f))
            instanceMap.put(f, new GetFieldWork(f, w));

        return new GetFieldWork(f, w);
    }

    public static GetFieldWork getInstance(IField f){
        if(!instanceMap.containsKey(f))
            instanceMap.put(f, new GetFieldWork(f, NoMoreWork.getInstance()));

        return instanceMap.get(f);
    }

    private GetFieldWork(IField f, Work w){ super(w); this.f = f; }

    @Override
    public Work execute(Object o) {

        if(o instanceof SSAGetInstruction){
            SSAGetInstruction getInst = (SSAGetInstruction) o;

            if(getInst.getDeclaredField().getName().equals(f.getReference().getName())){
                return super.nextWork();
            }
        }

        return this;
    }

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public int hashCode(){
        return f.hashCode() + super.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof GetFieldWork) {
            GetFieldWork gfw = (GetFieldWork) o;
            if(gfw.f.equals(f) && nextWork().equals(gfw.nextWork()))
                return true;
        }

        return false;
    }

    @Override
    public String toString(){
        return "[W: GetField@" + f + "]";
    }
}
