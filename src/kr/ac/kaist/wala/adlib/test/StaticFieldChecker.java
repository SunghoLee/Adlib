package kr.ac.kaist.wala.adlib.test;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.ipa.callgraph.impl.Everywhere;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Created by leesh on 23/01/2018.
 */
public class StaticFieldChecker {

    public static void check(IClassHierarchy cha){
        Map<String, Set<String>> mapToStaticField = new HashMap<String, Set<String>>();

        cha.forEach(new Consumer<IClass>() {
            @Override
            public void accept(IClass iClass) {
                iClass.getAllMethods().stream().forEach(new Consumer<IMethod>() {
                    @Override
                    public void accept(IMethod iMethod) {
                        if(iMethod.isNative() || !isApplication(iMethod.getDeclaringClass()) || iMethod.isAbstract())
                            return;
                        IR ir = null;
                        try {
                            ir = makeIR(iMethod);
                        }catch(NullPointerException e){
                            System.out.println("EX: " + iMethod + " ?"  + iMethod.isNative() + " ?" + iMethod.isAbstract());
                            e.printStackTrace();
                        }

                        for(SSAInstruction inst : ir.getInstructions()){
                            if(inst instanceof SSAGetInstruction){
                                SSAGetInstruction getInst = (SSAGetInstruction) inst;
                                if(getInst.isStatic() && !isThePackage(getInst.getDeclaredField())){
                                    String field = getInst.getDeclaredField().toString();
                                    field = "[Get] " + field;
                                    add(mapToStaticField, iMethod.toString(), field);
                                }
                            }else if(inst instanceof SSAPutInstruction){
                                SSAPutInstruction getInst = (SSAPutInstruction) inst;
                                if(getInst.isStatic() && !isThePackage(getInst.getDeclaredField())){
                                    String field = getInst.getDeclaredField().toString();
                                    field = "[Put] " + field;
                                    add(mapToStaticField, iMethod.toString(), field);
                                }
                            }
                        }
                    }
                });
            }
        });


        print(mapToStaticField);
    }

    private static boolean isThePackage(FieldReference f){
        if(f.getDeclaringClass().getName().toString().startsWith("Lcom/chartboost/"))
            return true;
        return false;
    }

    private static void print(Map<String, Set<String>> m){
        for(String method : m.keySet()){
            System.out.println("#M: " + method);
            for(String field : m.get(method)){
                System.out.println("\t" + field);
            }
            System.out.println();
        }
    }

    private static void add(Map<String, Set<String>> m, String method, String field){
        if(!m.containsKey(method))
            m.put(method, new HashSet<String>());
        m.get(method).add(field);
    }
    private static IR makeIR(IMethod m){
        IRFactory irFactory = new DexIRFactory();
        return irFactory.makeIR(m, Everywhere.EVERYWHERE, new SSAOptions());
    }

    private static boolean isApplication(IClass c){
        if(c.getReference().getClassLoader().equals(ClassLoaderReference.Application))
            return true;
        return false;
    }
}
