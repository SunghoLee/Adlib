package kr.ac.kaist.wala.adlib.analysis;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;

/**
 * Created by leesh on 08/03/2017.
 */
public final class APITarget {
    private static IClassHierarchy cha;

    public static void set(IClassHierarchy cha){
        APITarget.cha = cha;
    }

    private TypeName tn;
    private Selector s;
    private String alltn;
    private String alls;

    public APITarget(TypeName tn, Selector s){
        this.tn = tn;
        this.s = s;
    }

    public APITarget(TypeName tn){
        this.tn = tn;
        this.alls = "*";
    }

    public APITarget(String alltn){
        this.alltn = alltn;
        this.alls = "*";
    }

    public TypeName getTypeName() {
        return this.tn;
    }

    public Selector getSelector() {
        return this.s;
    }

    @Override
    public int hashCode(){

        if(tn != null && s != null)
            return tn.hashCode() + s.hashCode();
        else if(tn != null)
            return tn.hashCode();
        else
            return alltn.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof APITarget){
            APITarget t = (APITarget) o;
            if(t.s.equals(s)){
                TypeReference tr1 = TypeReference.find(ClassLoaderReference.Primordial, this.getTypeName());
                if(tr1 == null)
                    tr1 = TypeReference.find(ClassLoaderReference.Application, this.getTypeName());

                TypeReference tr2 = TypeReference.find(ClassLoaderReference.Primordial, t.getTypeName());
                if(tr2 == null)
                    tr2 = TypeReference.find(ClassLoaderReference.Application, t.getTypeName());

                if(tr1 == null || tr2 == null)
                    return this.tn.equals(t.tn);

                IClass c1 = cha.lookupClass(tr1);
                IClass c2 = cha.lookupClass(tr2);

                if(c1 == null){
                    if(tr1.getClassLoader().equals(ClassLoaderReference.Primordial))
                        c1 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Application, this.getTypeName()));
                    else
                        c1 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, this.getTypeName()));
                }

                if(c2 == null){
                    if(tr2.getClassLoader().equals(ClassLoaderReference.Primordial))
                        c2 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Application, t.getTypeName()));
                    else
                        c2 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, t.getTypeName()));
                }

                if(t.tn.equals(tn) || (c1 != null && c2 != null && cha.isSubclassOf(c2, c1)))
                    return true;
            }
//            return t.tn.equals(tn) && t.s.equals(s);
        }
        return false;
    }

    @Override
    public String toString(){
        if(tn != null && s != null)
            return tn.toString() + " . " + s.toString();
        else if(tn != null)
            return tn.toString() + " . " + alls;
        else
            return alltn + " . " + alls;
    }

    public static boolean isContains(APITarget a, APITarget b){
        if(b.getSelector().toString().contains("<cinit>") ||
                b.getSelector().toString().contains("<init>") ||
                b.getSelector().toString().contains("toString()Ljava/lang/String;") ||
                b.getSelector().toString().contains("equals(Ljava/lang/Object;)Z"))
            return false;

        if(a.equals(b))
            return true;

        if(a.s == null && a.tn != null){
            TypeReference tr1 = TypeReference.find(ClassLoaderReference.Primordial, a.getTypeName());
            if(tr1 == null)
                tr1 = TypeReference.find(ClassLoaderReference.Application, a.getTypeName());

            TypeReference tr2 = TypeReference.find(ClassLoaderReference.Primordial, b.getTypeName());
            if(tr2 == null)
                tr2 = TypeReference.find(ClassLoaderReference.Application, b.getTypeName());

            if(tr1 == null || tr2 == null)
                return a.tn.equals(b.tn);

            IClass c1 = cha.lookupClass(tr1);
            IClass c2 = cha.lookupClass(tr2);

            if(c1 == null){
                if(tr1.getClassLoader().equals(ClassLoaderReference.Primordial))
                    c1 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Application, a.getTypeName()));
                else
                    c1 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, a.getTypeName()));
            }

            if(c2 == null){
                if(tr2.getClassLoader().equals(ClassLoaderReference.Primordial))
                    c2 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Application, b.getTypeName()));
                else
                    c2 = cha.lookupClass(TypeReference.find(ClassLoaderReference.Primordial, b.getTypeName()));
            }

            if(a.tn.equals(b.tn) || (c1 != null && c2 != null && cha.isSubclassOf(c2, c1)))
                return true;

        }else if(a.s == null && a.tn == null){
            String path = a.alltn;
            String btype = b.tn.toString();
            path = path.substring(0, path.lastIndexOf("/"));
            return btype.startsWith(path);
        }

        return false;
    }
}
