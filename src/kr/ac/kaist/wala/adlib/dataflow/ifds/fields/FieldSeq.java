package kr.ac.kaist.wala.adlib.dataflow.ifds.fields;

import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;

import java.util.*;

import static kr.ac.kaist.wala.adlib.dataflow.ifds.AliasAwareFlowFunction.DDD;

/**
 * Created by leesh on 27/02/2018.
 */
public class FieldSeq implements Field {
    public static int LTHRESH = 5;
    private Field fst;
    private Field rest;

    @Override
    public int length() {
        return fst.length() + rest.length();
    }

    @Override
    public boolean isArrayType() {
        return fst.isArrayType();
    }

    public static Field make(Field fst, Field rest){
        if(fst instanceof NoneField)
            Assertions.UNREACHABLE("NoneField cannot be the first field of this sequence:  " + rest);

        if(fst.length() + rest.length() > LTHRESH)
            return TopField.getInstance();

        if(rest instanceof SingleField) // only for the split case.
            return new FieldSeq(fst, rest);

        if(rest instanceof NoneField)
            return new FieldSeq(fst, rest);
        else if(fst instanceof SingleField && rest instanceof FieldSeq){
            return makeConsiderReg(fst, rest);
        }else if(rest instanceof StarField){
            if(rest.isMatched(fst))
                return rest;
            return new FieldSeq(fst, rest);
        }else if(fst instanceof StarField){
            return new FieldSeq(fst, rest);
        }else
            Assertions.UNREACHABLE("Field must be categorized in None, Seq, or Op. F: " + fst + "[ " + fst.getClass().getName() + " ]\tR: " + rest +"[ " + rest.getClass().getName() + " ]");

        return null;
    }

    private static Pair<Field, Field> split(Field f, int s){
        Field rf = f;
        Stack<Field> fl = new Stack<>();

        while(s != 0){
            if(!(rf instanceof FieldSeq))
                Assertions.UNREACHABLE("The field f must be a FieldSeq: " + f);

            FieldSeq fs = (FieldSeq) rf;
            s--;
            Field fst = fs.getFirst();
            fl.add(fst);

            rf = fs.getRest();
        }

        Field ff = null;
        ff = SingleField.make(fl.pop().toString());

        while(!fl.isEmpty()){
            ff = (FieldSeq) FieldSeq.make(fl.pop(), ff);
        }

        return Pair.make(ff, rf);
    }

    private static FieldSeq makeConsiderReg(Field f, Field r){
        List<String> fl = new ArrayList<>();
        List<String> ll = new ArrayList<>();
        //f: a  r: a.$
        fl.addAll(f.toSimpleList());
        ll.addAll(r.toSimpleList());
        //fl: [a]
        //ll: [a, $]

        for(int i=0; i < Math.min(fl.size(), ll.size()); i++){
            int marker = 0;
            for(int j=0; j < fl.size(); j++){
                if(ll.get(j).equals("*")) { // a == *
                    marker = -2;
                    break;
                }
                else if(!ll.get(j).equals(fl.get(j))) { // a != a
                    marker = -1;
                    break;
                }
            }

            if(marker == -2){ // no match case
                if(DDD){
                    System.out.println("NO MATCH!: " + new FieldSeq(f, r));
                }
                return new FieldSeq(f, r);
            }else if(marker == -1){ // further search  thiscase
                fl.add(ll.get(0)); // fl: [obj, data]
                ll = ll.subList(1, ll.size()); //ll: [$]
                if(DDD){
                    System.out.println("FUTHERFUTHER! FL: " + fl + "\t LL: " + ll);
                }
            }else if(marker == 0 && ll.size() != 0){ // match case
                Pair<Field, Field> p = split(r, fl.size());
                if(DDD){
                    System.out.println("MATCHMATCH! SQ: " + new FieldSeq(StarField.make(p.fst), p.snd));
                    System.out.println("\tF: " + f + "\t R: " + r);
                    System.out.println("\tFL: " + fl + "\t LL: " + ll);
                }
                return new FieldSeq(StarField.make(p.fst), p.snd);
            }
        }

        return new FieldSeq(f, r);
    }

    @Override
    public List<String> toSimpleList() {
        List<String> l = new ArrayList<>();
        l.addAll(fst.toSimpleList());
        l.addAll(rest.toSimpleList());
        return l;
    }

    private Field merge(Field n, Field rest){
        if(n instanceof NoneField)
            return rest;
        else if(n instanceof StarField)
            return FieldSeq.make(n, rest);
        else if(n instanceof FieldSeq){
            FieldSeq nn = (FieldSeq) n;
            return FieldSeq.make(nn.getFirst(), merge(nn.getRest(), rest));
        }
        Assertions.UNREACHABLE("No other field type exist in Field sequence: " + n);
        return null;
    }

    @Override
    public Set<Field> pop(String f) {
        if(fst.isMatched(f)){
            Set<Field> res = new HashSet<>();

            if(fst instanceof StarField && rest.isMatched(f)){
                res.addAll(rest.pop(f));
            }
            for(Field nf : fst.pop(f)){
                res.add(merge(nf, rest));
            }
            return res;
        }else if(fst instanceof StarField){
            return rest.pop(f);
        }
        Assertions.UNREACHABLE("The field f is not matched with this FieldSeq. f: " + f +"\tfs: " + this);
        return null;
    }

    @Override
    public boolean isMatched(Field f){
        if(fst.isMatched(f))
            return true;

        if(fst instanceof StarField){
            return rest.isMatched(f);
        }

        return false;
    }

    @Override
    public boolean isMatched(String f){
        return isMatched(SingleField.make(f));
    }

    private FieldSeq(Field fst, Field rest){
        this.fst = fst;
        this.rest = rest;
    }

    public Field getFirst(){
        return this.fst;
    }

    public Field getRest(){
        return this.rest;
    }

    @Override
    public int hashCode(){
        return this.fst.hashCode() + this.rest.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof FieldSeq){
            FieldSeq fs = (FieldSeq) o;
            if(fs.fst.equals(this.fst) && fs.rest.equals(this.rest))
                return true;
        }
        return false;
    }

    @Override
    public String toString(){
        return fst + "." + rest;
    }
}
