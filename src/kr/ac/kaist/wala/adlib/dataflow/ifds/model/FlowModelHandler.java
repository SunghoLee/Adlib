package kr.ac.kaist.wala.adlib.dataflow.ifds.model;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;
import kr.ac.kaist.wala.adlib.dataflow.ifds.AliasHandler;
import kr.ac.kaist.wala.adlib.dataflow.ifds.DataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.DefaultDataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.LocalDataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.Field;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.FieldSeq;
import kr.ac.kaist.wala.adlib.dataflow.ifds.fields.SingleField;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.collection.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by leesh on 12/03/2018.
 */
public class FlowModelHandler {
    private static boolean DEBUG = false;
    protected ClassFlowModel[] models;
    private static boolean SUB_CLASS_FLAG = true;
    private final IClassHierarchy cha;
    private final AliasHandler aliasHandler;

    public FlowModelHandler(IClassHierarchy cha, AliasHandler aliasHandler){
        init(cha);
        this.cha = cha;
        this.aliasHandler = aliasHandler;
    }

    private void init(IClassHierarchy cha){
        models = new ClassFlowModel[]{
                StringFlowModel.getInstance(),
                StringBufferFlowModel.getInstance(),
                StringBuilderFlowModel.getInstance(),
                UrlDecoderFlowModel.getInstance(),
                MapFlowModel.getInstance(cha),
                ListFlowModel.getInstance(cha),
                SetFlowModel.getInstance(cha),
                IteratorFlowModel.getInstance(cha),
                CollectionFlowModel.getInstance(cha),
                UriFlowModel.getInstance(),
                JSONTokenerFlowModel.getInstance(cha),
                JSONObjectFlowModel.getInstance(cha),
                JSONArrayFlowModel.getInstance(cha),
                ArraysFlowModel.getInstance(cha),
        };
    }

    public boolean isModeled(CGNode target){
        //TODO: improve the matching performance
        if(getMethodModel(target).size() != 0)
            return true;

        return false;
    }

    private Set<MethodFlowModel> getMethodModel(CGNode n){
        Set<MethodFlowModel> mfms = new HashSet<>();

        for(ClassFlowModel cfm : models){
            IClass sc = cha.lookupClass(cfm.getReference());
            boolean further = (sc != null ) && cha.isSubclassOf(n.getMethod().getDeclaringClass(), sc) && SUB_CLASS_FLAG;

            for(MethodFlowModel mfm : cfm.getMethods()) {
                if (mfm.getReference().equals(n.getMethod().getReference())) {
                    mfms.add(mfm);
                } else if (further && n.getMethod().getSelector().equals(mfm.getReference().getSelector())) {
                    mfms.add(mfm);
                }
            }
        }

        return mfms;
    }

    private boolean isArrayType(MethodReference mr, int index){
        if(index == MethodFlowModel.RETV)
            return mr.getReturnType().isArrayType();
        else{
            IClass k = cha.lookupClass(mr.getDeclaringClass());
            IMethod m = k.getMethod(mr.getSelector());
            if(m.isStatic()){
                return mr.getParameterType(index).isArrayType();
            }else{
                if(index == 0)
                    return false;
                else
                    return mr.getParameterType(index-1).isArrayType();
            }
        }
    }

    public Set<DataFact> matchDataFact(CGNode curNode, CGNode target, SSAAbstractInvokeInstruction invokeInst, DataFact dfact){
        //TODO: improve the matching performance
        Set<DataFact> res = new HashSet<>();
        Set<MethodFlowModel> mfms = getMethodModel(target);

        res.add(dfact);

        if(dfact instanceof LocalDataFact) {
            LocalDataFact fact = (LocalDataFact) dfact;
            int index = -100;
            for (int i = 0; i < invokeInst.getNumberOfUses(); i++) {
                if (invokeInst.getUse(i) == fact.getVar()) {
                    if (!invokeInst.isStatic() && i == 0)
                        index = MethodFlowModel.RECEIVERV;
                    else
                        index = i;
                    break;
                }
            }

            // if the fact is not used in this invoke instruction, just pass none.
            // it is possible, when this instruction is at a return site.
            if (index == -100)
                return Collections.emptySet();

            for(MethodFlowModel mfm : mfms) {
                for (int i : mfm.matchFlow(index)) {
                    if (DEBUG) {
                        System.out.println("=== ");
                        System.out.println("MODEL: " + mfm);
                        System.out.println("INST: " + invokeInst);
                        System.out.println("FROM: " + invokeInst.getUse(index));
                        System.out.println("I: " + i);
                        System.out.println("TO: " + ((i == MethodFlowModel.RETV) ? invokeInst.getDef() : invokeInst.getUse(i)));
                        System.out.println("=== ");
                    }
                    Field f = fact.getField();
                    Set<Field> newFs = mfm.matchField(f);
                    // intentionally cut infeasible paths at this point!
                    if (newFs.isEmpty())
                        return Collections.emptySet();

                    int var = -100;

                    if(i == MethodFlowModel.RETV)
                        var = invokeInst.getDef();
                    else if(i == MethodFlowModel.RECEIVERV)
                        var = invokeInst.getUse(0);
                    else
                        var = invokeInst.getUse(i);

                    for(Field newF : handleField(mfm.getReference(), i, newFs)) {
                        DataFact newFact = new LocalDataFact(curNode, var, newF);
                        res.add(newFact);

                        if(mfm.isMutable() && i == MethodFlowModel.RECEIVERV){
                            res.addAll(aliasHandler.findAlias(curNode, newFact));
                        }
                    }
                }
            }
        }else if(dfact instanceof DefaultDataFact){
            for(MethodFlowModel mfm : mfms) {
                if (isIn(mfm.getFrom(), MethodFlowModel.ANY)) {

                    Field f = dfact.getField();
                    Set<Field> newFs = mfm.matchField(f);

                    // intentionally cut infeasible paths at this point!
                    if (newFs.isEmpty())
                        return Collections.emptySet();

                    for (int i : mfm.getTo()) {
                        int var = -100;

                        if(i == MethodFlowModel.RETV)
                            var = invokeInst.getDef();
                        else if(i == MethodFlowModel.RECEIVERV)
                            var = invokeInst.getUse(0);
                        else
                            var = invokeInst.getUse(i);

                        for(Field newF : handleField(mfm.getReference(), i, newFs)) {
                            DataFact newFact = new LocalDataFact(curNode, var, newF);
                            res.add(newFact);

                            if(mfm.isMutable() && i == MethodFlowModel.RECEIVERV){
                                res.addAll(aliasHandler.findAlias(curNode, newFact));
                            }
                        }
                    }
                }
            }
        }else
            Assertions.UNREACHABLE("Local or Default data facts are only possible to match with modeling flows: " + dfact);
        return res;
    }

    private IClass collection;
    private IClass iter;
    private IClass jarray;
    private IClass jtoken;
    private IClass jobj;
    private IClass map;

    private String getSpecialF(MethodReference mr, int index){
        TypeReference tr = null;
        if(index == MethodFlowModel.RETV)
            tr = mr.getReturnType();
        else{
            IClass k = cha.lookupClass(mr.getDeclaringClass());
            IMethod m = k.getMethod(mr.getSelector());
            if(m.isStatic()){
                tr = mr.getParameterType(index);
            }else{
                if(index == 0)
                    return null;
                else
                    tr = mr.getParameterType(index-1);
            }
        }

        if(tr.isPrimitiveType())
            return null;

        IClass c = cha.lookupClass(tr);

        if(collection == null)
            this.collection = cha.lookupClass(CollectionFlowModel.getInstance(cha).getReference());
        if(iter == null)
            this.iter = cha.lookupClass(IteratorFlowModel.getInstance(cha).getReference());
        if(jarray == null)
            this.jarray = cha.lookupClass(JSONArrayFlowModel.getInstance(cha).getReference());
        if(jtoken == null)
            this.jtoken = cha.lookupClass(JSONTokenerFlowModel.getInstance(cha).getReference());
        if(jobj == null)
            this.jobj = cha.lookupClass(JSONObjectFlowModel.getInstance(cha).getReference());
        if(map == null)
            this.map = cha.lookupClass(MapFlowModel.getInstance(cha).getReference());

        if(c.equals(collection) || cha.implementsInterface(c, collection)){
            return CollectionFlowModel.COLLECTION_GET;
        }else if(c.equals(iter) || cha.implementsInterface(c, iter)){
            return IteratorFlowModel.ITER_NEXT;
        }else if(c.equals(jarray) || cha.isSubclassOf(c, jarray)){
            return JSONArrayFlowModel.JSON_ARRAY_GET;
        }else if(c.equals(map) || cha.implementsInterface(c, map)){
            return MapFlowModel.MAP_GET;
        }else if(c.equals(jtoken) || cha.isSubclassOf(c, jtoken)){
            return JSONTokenerFlowModel.JSON_TOKEN_NEXT;
        }else if(c.equals(jobj) || cha.isSubclassOf(c, jobj)){
            return JSONObjectFlowModel.JSON_OBJ_GET;
        }

        return null;
    }

    private Set<Field> handleField(MethodReference mr, int index, Set<Field> fs){
        Set<Field> res = new HashSet<>();

        for(Field f : fs){
            if (isArrayType(mr, index)) {
                if(f.isArrayType()){
                    res.add(f);
                }else{
                    res.add(FieldSeq.make(SingleField.make("["), f));
                }
            } else {
                String spF = getSpecialF(mr, index);
                if(spF == null) {
                    if (f.isArrayType()) {
                        for (Field nnF : f.pop("["))
                            res.add(nnF);
                    } else {
                        res.add(f);
                    }
                }else{
                    if(f.isMatched(spF)){
                        res.add(f);
                    }else{
                        res.add(FieldSeq.make(SingleField.make(spF), f));
                    }
                }
            }
        }
        return res;
    }

    private boolean isIn(int[] arr, int i){
        for(int j : arr){
            if(i == j)
                return true;
        }

        return false;
    }
}
