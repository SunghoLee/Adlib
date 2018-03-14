package kr.ac.kaist.wala.adlib.analysis.malicious;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.flows.IFlowFunction;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.ClassFlowModel;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.FlowModelHandler;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.MethodFlowModel;

import java.util.*;

/**
 * Created by leesh on 13/03/2018.
 */
public class MaliciousFlowModelHandler extends FlowModelHandler {
    private static TypeReference ModelingClassTR = TypeReference.findOrCreateClass(ClassLoaderReference.Application, "Lmodel","MaliciousFlowModel");

    public MaliciousFlowModelHandler(Set<MaliciousPatternChecker.MaliciousPattern> mps, IClassHierarchy cha) {
        List<ClassFlowModel> pModels =  Arrays.asList(super.models);
        List<ClassFlowModel> nModels = new ArrayList<>();
        nModels.addAll(pModels);
        nModels.add(patternsToModel(mps, cha));
        super.models = nModels.toArray(new ClassFlowModel[0]);
    }

    private ClassFlowModel patternsToModel(Set<MaliciousPatternChecker.MaliciousPattern> mps, IClassHierarchy cha){
        Set<MethodFlowModel> methodModels = new HashSet<>();

        for(MaliciousPatternChecker.MaliciousPattern mp : mps){
            for(MaliciousPatternChecker.MaliciousPoint point : mp.getPoints()){
                Set<TypeReference> classes = new HashSet<>();
                TypeReference mClass = makeClassTypeRef(point.getTypeName());
                classes.add(mClass);
                classes.addAll(getAllSubClass(mClass, cha));
                for(TypeReference tr : classes){
                    IClass clazz = cha.lookupClass(tr);
                    for(IMethod method : clazz.getAllMethods()){
                        if(method.getSelector().equals(point.getSelector()))
                            methodModels.add(MethodFlowModel.make(method.getReference(),
                                    new int[]{(point.getFlowFunction().getFrom() == IFlowFunction.ANY)? MethodFlowModel.ANY : point.getFlowFunction().getFrom() - 1},
                                    (point.getFlowFunction().getTo() == IFlowFunction.TERMINATE)? new int[]{} :
                                            new int[]{(point.getFlowFunction().getTo() == IFlowFunction.RETURN_VARIABLE)? MethodFlowModel.RETV : point.getFlowFunction().getTo() - 1}));
                    }
                }
            }
        }

        return new ClassFlowModel() {
            @Override
            protected void init() {
                ref = ModelingClassTR;
                this.methods = methodModels.toArray(new MethodFlowModel[0]);
            }
        };
    }

    private TypeReference makeClassTypeRef(TypeName tn){
        TypeReference tr = TypeReference.find(ClassLoaderReference.Primordial, tn);
        if(tr == null)
            tr = TypeReference.find(ClassLoaderReference.Application, tn);

        if(tr == null)
            Assertions.UNREACHABLE("A TypeReference of the TypeName does not exist: " + tn);

        return tr;
    }

    private Set<TypeReference> getAllSubClass(TypeReference tr, IClassHierarchy cha){
        Set<TypeReference> res = new HashSet<>();
        getAllSubClass(res, tr, cha);
        return res;
    }

    private Set<TypeReference> getAllSubClass(Set<TypeReference> res, TypeReference tr, IClassHierarchy cha){
//        System.out.println("#?? " + tr + "\t Contained? " + res.contains(tr));
        if(cha.isInterface(tr)){
            for (IClass c : cha.getImplementors(tr)) {
                if(!res.contains(c.getReference())) {
                    res.add(c.getReference());
                    res.addAll(getAllSubClass(res, c.getReference(), cha));
                }
            }
        }else {
            for (IClass c : cha.computeSubClasses(tr)) {
                if(!res.contains(c.getReference())) {
                    res.add(c.getReference());
                    res.addAll(getAllSubClass(res, c.getReference(), cha));
                }
            }
        }
        return res;
    }
}
