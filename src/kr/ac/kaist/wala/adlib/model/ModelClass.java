package kr.ac.kaist.wala.adlib.model;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.SyntheticClass;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.strings.Atom;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by leesh on 08/12/2017.
 */
public class ModelClass extends SyntheticClass{
    protected final Map<Selector, IMethod> methods = HashMapFactory.make();
    protected final Map<Atom, IField> fields = HashMapFactory.make();

    protected final IClass base;
    private IClass superClass;

    public ModelClass(TypeReference T, IClassHierarchy cha) {
        super(T, cha);
        this.base = cha.lookupClass(T);
        this.superClass = base.getSuperclass();
    }

    public void addMethod(IMethod m){
        Selector s = m.getSelector();
        if(methods.containsKey(s))
            Assertions.UNREACHABLE("The class, " + getReference() + ", already has the method: " + m);
        methods.put(s, m);
    }

    public void addField(IField f){
        Atom name = f.getName();
        if(fields.containsKey(name))
            Assertions.UNREACHABLE("The class, " + getReference() + ", already has the field: " + f);
        fields.put(name, f);
    }

    public void setSuperClass(IClass c){
        this.superClass = c;
    }
    @Override
    public boolean isPublic() {
        return base.isPublic();
    }

    @Override
    public boolean isPrivate() {
        return base.isPrivate();
    }

    @Override
    public int getModifiers() throws UnsupportedOperationException {
        return base.getModifiers();
    }

    @Override
    public IClass getSuperclass() {
        return (this.superClass == null)? base.getSuperclass() : this.superClass;
    }

    @Override
    public Collection<? extends IClass> getDirectInterfaces() {
        return base.getDirectInterfaces();
    }

    @Override
    public Collection<IClass> getAllImplementedInterfaces() {
        return base.getAllImplementedInterfaces();
    }

    @Override
    public IMethod getMethod(Selector selector) {
        if(methods.containsKey(selector)) {
            return methods.get(selector);
        } else {
            return base.getMethod(selector);
        }
    }

    @Override
    public IField getField(Atom name) {
        if(fields.containsKey(name))
            return fields.get(name);
        else
            return base.getField(name);
    }

    @Override
    public IMethod getClassInitializer() {
        return base.getClassInitializer();
    }

    @Override
    public Collection<IMethod> getDeclaredMethods() {
        Set<IMethod> res = new HashSet<>();
        res.addAll(methods.values());
        res.addAll(base.getDeclaredMethods().stream().filter(new Predicate<IMethod>() {
            @Override
            public boolean test(IMethod iMethod) {
                if(methods.containsKey(iMethod.getSelector()))
                    return false;
                return true;
            }
        }).collect(Collectors.toSet()));

        return res;
    }

    @Override
    public Collection<IField> getAllInstanceFields() {
        Set<IField> res = new HashSet<>();
        res.addAll(fields.values().stream().filter(new Predicate<IField>() {
            @Override
            public boolean test(IField iField) {
                if(!iField.isStatic())
                    return true;
                return false;
            }
        }).collect(Collectors.toSet()));
        res.addAll(base.getAllInstanceFields().stream().filter(new Predicate<IField>() {
            @Override
            public boolean test(IField f) {
                if(!fields.containsKey(f.getName()))
                    return true;
                return false;
            }
        }).collect(Collectors.toSet()));

        return res;
    }

    @Override
    public Collection<IField> getAllStaticFields() {
        Set<IField> res = new HashSet<>();
        res.addAll(fields.values().stream().filter(new Predicate<IField>() {
            @Override
            public boolean test(IField iField) {
                if(iField.isStatic())
                    return true;
                return false;
            }
        }).collect(Collectors.toSet()));
        res.addAll(base.getAllStaticFields().stream().filter(new Predicate<IField>() {
            @Override
            public boolean test(IField f) {
                if(!fields.containsKey(f.getName()))
                    return true;
                return false;
            }
        }).collect(Collectors.toSet()));

        return res;
    }

    @Override
    public Collection<IField> getAllFields() {
        Set<IField> res = new HashSet<>();
        res.addAll(fields.values());
        res.addAll(base.getAllFields().stream().filter(new Predicate<IField>() {
            @Override
            public boolean test(IField f) {
                if(!fields.containsKey(f.getName()))
                    return true;
                return false;
            }
        }).collect(Collectors.toSet()));

        return res;
    }

    @Override
    public Collection<IMethod> getAllMethods() {
        Set<IMethod> res = new HashSet<>();
        res.addAll(methods.values());
        res.addAll(base.getAllMethods().stream().filter(new Predicate<IMethod>() {
            @Override
            public boolean test(IMethod iMethod) {
                if(methods.containsKey(iMethod.getSelector()))
                    return false;
                return true;
            }
        }).collect(Collectors.toSet()));

        return res;
    }

    @Override
    public Collection<IField> getDeclaredInstanceFields() {
        Set<IField> res = new HashSet<>();
        res.addAll(fields.values().stream().filter(new Predicate<IField>() {
            @Override
            public boolean test(IField iField) {
                if(!iField.isStatic())
                    return true;
                return false;
            }
        }).collect(Collectors.toSet()));
        res.addAll(base.getDeclaredInstanceFields().stream().filter(new Predicate<IField>() {
            @Override
            public boolean test(IField f) {
                if(!fields.containsKey(f.getName()))
                    return true;
                return false;
            }
        }).collect(Collectors.toSet()));

        return res;
    }

    @Override
    public Collection<IField> getDeclaredStaticFields() {
        Set<IField> res = new HashSet<>();
        res.addAll(fields.values().stream().filter(new Predicate<IField>() {
            @Override
            public boolean test(IField iField) {
                if(iField.isStatic())
                    return true;
                return false;
            }
        }).collect(Collectors.toSet()));
        res.addAll(base.getDeclaredStaticFields().stream().filter(new Predicate<IField>() {
            @Override
            public boolean test(IField f) {
                if(!fields.containsKey(f.getName()))
                    return true;
                return false;
            }
        }).collect(Collectors.toSet()));

        return res;
    }


    @Override
    public boolean isReferenceType() {
        return base.isReferenceType();
    }

    @Override
    public String toString(){
        return "[Model] " + base.toString();
    }

    @Override
    public int hashCode(){
        return base.hashCode();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof ModelClass){
            ModelClass mc = (ModelClass) o;
            if(mc.base.equals(this.base))
                return true;
        }
        return false;
    }
}
