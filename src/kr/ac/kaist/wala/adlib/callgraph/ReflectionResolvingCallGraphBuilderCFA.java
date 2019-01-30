package kr.ac.kaist.wala.adlib.callgraph;

import com.google.common.collect.Lists;
import com.ibm.wala.classLoader.*;
import com.ibm.wala.core.tests.callGraph.CallGraphTestUtil;
import com.ibm.wala.dalvik.classLoader.DexFileModule;
import com.ibm.wala.dalvik.classLoader.DexIRFactory;
import com.ibm.wala.dalvik.ipa.callgraph.impl.AndroidEntryPoint;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.*;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ipa.slicer.Slicer;
import com.ibm.wala.ipa.summaries.LambdaMethodTargetSelector;
import com.ibm.wala.ssa.*;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeName;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.types.annotations.Annotation;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.Predicate;
import com.ibm.wala.util.collections.ComposedIterator;
import com.ibm.wala.util.collections.FilterIterator;
import com.ibm.wala.util.collections.MapIterator;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.config.FileOfClasses;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.functions.Function;
import com.ibm.wala.util.strings.Atom;
import kr.ac.kaist.wala.adlib.InitInstsParser;
import kr.ac.kaist.wala.adlib.callgraph.context.FirstMethodContextSelector;
import kr.ac.kaist.wala.adlib.model.entries.RecursiveParamDefFakeRootMethod;
import kr.ac.kaist.wala.hybridroid.models.AndroidHybridAppModel;
import kr.ac.kaist.wala.hybridroid.utils.LocalFileReader;

import java.io.*;
import java.util.*;
import java.util.jar.JarFile;

public class ReflectionResolvingCallGraphBuilderCFA extends nCFABuilder {
    public static TypeName CLASS_TYPE = TypeName.findOrCreate("Ljava/lang/Class");
    public static Selector GET_METHOD_SELECTOR = Selector.make("getMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
    public static TypeName METHOD_TYPE = TypeName.findOrCreate("Ljava/lang/reflect/Method");
    public static Selector INVOKE_SELECTOR = Selector.make("invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;");
    public static Selector FORNAME_SELECTOR1 = Selector.make("forName(Ljava/lang/String;)Ljava/lang/Class;");
    public static Selector FORNAME_SELECTOR2 = Selector.make("forName(Ljava/lang/String;ZLjava/lang/ClassLoader;)Ljava/lang/Class;");
    private static TypeReference ANY_PARAM_TYPE = TypeReference.findOrCreate(ClassLoaderReference.Extension, "Lardetector/AnyParam");

    private Set<Pair<CGNode, Integer>> reflectionCallingPoint = new HashSet<>();

    public ReflectionResolvingCallGraphBuilderCFA(int n, IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache){
        super(n, cha, options, cache, null, null);
    }

    private InstanceKey getLocalCreateInstanceForImplicitPointerKey(CGNode n, PointerKey pk){
        for(SSAInstruction inst : n.getIR().getInstructions()){
            if(inst == null)
                continue;

            if(inst instanceof SSANewInstruction){
                SSANewInstruction newInst = (SSANewInstruction) inst;
                if(pk.equals(getPointerKeyForLocal(n, newInst.getDef()))){
                    return getInstanceKeyForAllocation(n, newInst.getNewSite());
                }
            }
        }

        return null;
    }

    @Override
    protected void processCallingConstraints(CGNode caller, SSAAbstractInvokeInstruction instruction, CGNode target, InstanceKey[][] constParams, PointerKey uniqueCatchKey) {
        if(caller.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application) && !target.getMethod().getDeclaringClass().getName().equals(METHOD_TYPE) && !target.getMethod().getSelector().equals(INVOKE_SELECTOR)) {
            if (instruction.getDeclaredTarget().getDeclaringClass().getName().equals(METHOD_TYPE) && instruction.getDeclaredTarget().getSelector().equals(INVOKE_SELECTOR)) {

                List<PointerKey> params = findParams(caller, instruction.iindex, instruction.getUse(2));

                if (params == null) {
                    System.err.println("[Warning] cannot solving reflection: " + instruction + " in " + caller);
                    PointerKey formal = getTargetPointerKey(target, 0);
                    PointerKey actual = getPointerKeyForLocal(caller, instruction.getUse(1));

                    if (formal instanceof FilteredPointerKey) {
                        system.newConstraint(formal, filterOperator, actual);
                    } else {
                        system.newConstraint(formal, assignOperator, actual);
                    }
                } else {
                    for (int i = 0; i < target.getMethod().getNumberOfParameters(); i++) {
                        if (target.getMethod().getParameterType(i).isReferenceType()) {
                            PointerKey formal = getTargetPointerKey(target, i);
                            PointerKey actual = null;

                            if (i == 0)
                                actual = getPointerKeyForLocal(caller, instruction.getUse(1));
                            else {
                                actual = params.get(i - 1);
                            }

                            if (formal instanceof FilteredPointerKey) {
                                if (!system.isImplicit(actual))
                                    system.newConstraint(formal, filterOperator, actual);
                                else {
                                    if (actual instanceof LocalPointerKey && caller.getIR().getSymbolTable().isConstant(((LocalPointerKey) actual).getValueNumber())) {
                                        int constant = ((LocalPointerKey) actual).getValueNumber();
                                        Object v = caller.getIR().getSymbolTable().getConstantValue(constant);

                                        system.newConstraint(formal, this.getInstanceKeyForConstant(getTypeReferenceForConstant(caller, constant), v));
                                    } else
                                        system.newConstraint(formal, getLocalCreateInstanceForImplicitPointerKey(caller, actual));
                                }
                            } else {
                                system.newConstraint(formal, assignOperator, actual);
                            }
                        }
                    }
                }
                // generate contraints from return value.
                if (instruction.hasDef() && instruction.getDeclaredResultType().isReferenceType()) {
                    PointerKey result = getPointerKeyForLocal(caller, instruction.getDef());
                    PointerKey ret = getPointerKeyForReturnValue(target);
                    system.newConstraint(result, assignOperator, ret);
                }
                // generate constraints from exception return value.
                PointerKey e = getPointerKeyForLocal(caller, instruction.getException());
                PointerKey er = getPointerKeyForExceptionalReturnValue(target);
                if (SHORT_CIRCUIT_SINGLE_USES && uniqueCatchKey != null) {
                    // e has exactly one use. so, represent e implicitly
                    system.newConstraint(uniqueCatchKey, assignOperator, er);
                } else {
                    system.newConstraint(e, assignOperator, er);
                }
            } else {
                super.processCallingConstraints(caller, instruction, target, constParams, uniqueCatchKey);
            }
        }else
            super.processCallingConstraints(caller, instruction, target, constParams, uniqueCatchKey);
    }

    private TypeReference getTypeReferenceForConstant(CGNode n, int var){
        SymbolTable symTab = n.getIR().getSymbolTable();

        if(symTab.isStringConstant(var)){
            return TypeReference.JavaLangString;
        }else if(symTab.isBooleanConstant(var)){
            return TypeReference.Boolean;
        }else if(symTab.isDoubleConstant(var)){
            return TypeReference.Double;
        }else if(symTab.isFloatConstant(var)){
            return TypeReference.Float;
        }else if(symTab.isIntegerConstant(var)){
            return TypeReference.Int;
        }else if(symTab.isLongConstant(var)){
            return TypeReference.Long;
        }else if(symTab.isNullConstant(var)){
            return TypeReference.Null;
        }
        Assertions.UNREACHABLE("This variable is not constant: " + var + " in " + n);
        return null;
    }

    private List<PointerKey> findParams(CGNode n, int iindex, int v){
        List<PointerKey> params = new ArrayList<>();

        SSAInstruction[] insts = n.getIR().getInstructions();

        for(int i = iindex; i > -1 ; i--){
            SSAInstruction inst = insts[i];
            if(inst == null)
                continue;

            if(inst instanceof SSAArrayStoreInstruction && inst.getUse(0) == v){
                params.add(getPointerKeyForLocal(n, inst.getUse(2)));
            }else if(inst instanceof SSANewInstruction && inst.getDef() == v){
                return Lists.reverse(params);
            }
        }
        return null;
    }

    @Override
    protected CGNode getTargetForCall(CGNode caller, CallSiteReference site, IClass recv, InstanceKey[] iKey) {
        if(caller.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)){
        if (recv != null && CLASS_TYPE.equals(site.getDeclaredTarget().getDeclaringClass().getName()) &&
                GET_METHOD_SELECTOR.equals(site.getDeclaredTarget().getSelector())) {
            Set<IMethod> targets = new HashSet<>();

            String methodName = findMethodName(caller, site);
            TypeReference[] types = findParamTypes(caller, site);

            //if method name is constant string value, only one method is the target.
            InstanceKey ik = iKey[0];
            if (ik != null && ik instanceof ConstantKey) {
                if (methodName != null) {
                    for (IMethod m : ((IClass) ((ConstantKey) ik).getValue()).getDeclaredMethods()) {
                        if (m.getName().toString().equals(methodName)) {
                            if (types == null)
                                targets.add(m);
                            else {
                                if (m.getNumberOfParameters() - 1 == types.length) {
                                    int i = 0;
                                    for (; i < m.getNumberOfParameters() - 1; i++) {
                                        if (!types[i].equals(ANY_PARAM_TYPE) && !m.getParameterType(i + 1).equals(types[i]))
                                            break;
                                    }

                                    if (i == m.getNumberOfParameters() - 1) {
                                        targets.add(m);
                                    }
                                }
                            }
                        }
                    }
                } else { // else, all methods from the class are the targets.
                    for (IMethod m : ((IClass) ((ConstantKey) ik).getValue()).getDeclaredMethods()) {
                        if (types == null)
                            targets.add(m);
                        else {
                            if (m.getNumberOfParameters() - 1 == types.length) {
                                int i = 0;
                                for (; i < m.getNumberOfParameters() - 1; i++) {
                                    if (!types[i].equals(ANY_PARAM_TYPE) && !m.getParameterType(i + 1).equals(types[i]))
                                        break;
                                }
                                if (i == m.getNumberOfParameters() - 1) {
                                    targets.add(m);
                                }
                            }
                        }
                    }
                }
            }
            assignRelectingMethodToDefValue(caller, site, targets);

        } else if (recv == null && CLASS_TYPE.equals(site.getDeclaredTarget().getDeclaringClass().getName()) && (FORNAME_SELECTOR1.equals(site.getDeclaredTarget().getSelector()) || FORNAME_SELECTOR2.equals(site.getDeclaredTarget().getSelector()))) {
//                CGNode caller, CallSiteReference site, IClass recv, InstanceKey[] iKey
            IR ir = caller.getIR();

            if (ir != null) {
                SymbolTable symTab = caller.getIR().getSymbolTable();

                SSAAbstractInvokeInstruction[] invokeInsts = ir.getCalls(site);

                // Currently, we did not treat multiple calls for a call site
                if (invokeInsts.length == 1) {
                    SSAAbstractInvokeInstruction invokeInst = invokeInsts[0];
                    int classVar = invokeInst.getUse(0);

                    //Currently, we only treat constant string only for reflection
                    if (symTab.isStringConstant(classVar)) {
                        String className = symTab.getStringValue(classVar);
                        className = className.replace(".", "/");
                        className = "L" + className;
                        TypeReference targetClassTR = TypeReference.findOrCreate(ClassLoaderReference.Application, className);

                        if (cha.lookupClass(targetClassTR) == null) {
                            targetClassTR = TypeReference.findOrCreate(ClassLoaderReference.Primordial, className);
                        }

                        if (cha.lookupClass(targetClassTR) != null) {
                            int defVar = invokeInst.getDef();
                            TypeReference classTR = TypeReference.findOrCreate(ClassLoaderReference.Application, "Ljava/lang/Class");
                            IClass klass = cha.lookupClass(classTR);

                            InstanceKey ik = this.getInstanceKeyForMetadataObject(targetClassTR, targetClassTR);
                            PointerKey defPK = getPointerKeyForLocal(caller, defVar);

                            this.getSystem().findOrCreateIndexForInstanceKey(ik);
                            this.getSystem().newConstraint(defPK, ik);
                        }
                    }
                }
            }
        } else if (recv != null && METHOD_TYPE.equals(site.getDeclaredTarget().getDeclaringClass().getName()) && INVOKE_SELECTOR.equals(site.getDeclaredTarget().getSelector())) {
            InstanceKey ik = iKey[0];

            if (ik != null && ik instanceof ReflectingMethodKey) {
                IMethod targetMethod = ((ReflectingMethodClass) ((ReflectingMethodKey) ik).getConcreteType()).getTargetMethod();
                Context targetContext = contextSelector.getCalleeTarget(caller, site, targetMethod, iKey);
                try {
                    return getCallGraph().findOrCreateNode(targetMethod, targetContext);
                } catch (CancelException e) {
                    e.printStackTrace();
                }
            }
        }
    }
        return super.getTargetForCall(caller, site, recv, iKey);
    }

    private void assignRelectingClassToDefValue(CGNode caller, CallSiteReference site, Set<IMethod> targets){
        IR ir = caller.getIR();
        if(ir.getCalls(site).length > 1){
            Assertions.UNREACHABLE("Currently, we do not deal with multiple reflections in a method");
        }

        for(SSAAbstractInvokeInstruction invoke : ir.getCalls(site)){
            int def = invoke.getDef();
            PointerKey defPK = this.getPointerKeyForLocal(caller, def);
            for(IMethod m : targets) {
                InstanceKey ik = new ReflectingMethodKey(new ReflectingMethodClass(cha, m));
                this.getSystem().findOrCreateIndexForInstanceKey(ik);
                this.getSystem().newConstraint(defPK, ik);
            }
        }
    }

    private void assignRelectingMethodToDefValue(CGNode caller, CallSiteReference site, Set<IMethod> targets){
        IR ir = caller.getIR();
        if(ir.getCalls(site).length > 1){
            Assertions.UNREACHABLE("Currently, we do not deal with multiple reflections in a method");
        }

        for(SSAAbstractInvokeInstruction invoke : ir.getCalls(site)){
            int def = invoke.getDef();
            PointerKey defPK = this.getPointerKeyForLocal(caller, def);
            for(IMethod m : targets) {
                InstanceKey ik = new ReflectingMethodKey(new ReflectingMethodClass(cha, m));
                this.getSystem().findOrCreateIndexForInstanceKey(ik);
                this.getSystem().newConstraint(defPK, ik);
            }
        }
    }

    private TypeReference findParamType(SSAInstruction[] insts, int iindex, int v){
        for(int i = iindex -1; i > -1; i--){
            SSAInstruction inst = insts[i];

            if(inst == null)
                continue;

            if(inst instanceof SSALoadMetadataInstruction){
                SSALoadMetadataInstruction metaLoadInst = (SSALoadMetadataInstruction) inst;
                return (TypeReference) metaLoadInst.getToken();
            }
        }

        return ANY_PARAM_TYPE;
    }

    private String findMethodName(CGNode caller, CallSiteReference site){
        List<TypeReference> params = new ArrayList<>();
        IR ir = caller.getIR();
        SSAInstruction[] insts = ir.getInstructions();

        if(ir.getCalls(site).length > 1){
            Assertions.UNREACHABLE("Currently, we do not deal with multiple reflections in a method");
        }

        for(SSAAbstractInvokeInstruction invoke : ir.getCalls(site)){
            int nameVar = invoke.getUse(1);
            if(ir.getSymbolTable().isStringConstant(nameVar))
                return ir.getSymbolTable().getStringValue(nameVar);

        }

        return null;
    }

    private TypeReference[] findParamTypes(CGNode caller, CallSiteReference site){
        List<TypeReference> params = new ArrayList<>();
        IR ir = caller.getIR();
        SSAInstruction[] insts = ir.getInstructions();

        if(ir.getCalls(site).length > 1){
            Assertions.UNREACHABLE("Currently, we do not deal with multiple reflections in a method");
        }

        for(SSAAbstractInvokeInstruction invoke : ir.getCalls(site)){
            int paramVar = invoke.getUse(2);

            for(int i = invoke.iindex-1 ; i > -1; i--){
                SSAInstruction inst = insts[i];

                if(inst == null)
                    continue;

                if(inst instanceof SSAArrayStoreInstruction && inst.getUse(0) == paramVar){
                    params.add(findParamType(insts, inst.iindex, inst.getUse(2)));
                }else if(inst instanceof SSANewInstruction && inst.getDef() == paramVar)
                    return Lists.reverse(params).toArray(new TypeReference[0]);
            }
        }

        return null;
    }

    static final class ReflectingMethodClass implements IClass {
        private IClass delegate;
        private IMethod m;

        public ReflectingMethodClass(IClassHierarchy cha, IMethod m){
            delegate = cha.lookupClass(TypeReference.findOrCreate(ClassLoaderReference.Primordial, "Ljava/lang/reflect/Method"));
            this.m = m;
        }

        @Override
        public IClassHierarchy getClassHierarchy() {
            return delegate.getClassHierarchy();
        }

        @Override
        public IClassLoader getClassLoader() {
            return delegate.getClassLoader();
        }

        @Override
        public boolean isInterface() {
            return delegate.isInterface();
        }

        @Override
        public boolean isAbstract() {
            return delegate.isAbstract();
        }

        @Override
        public boolean isPublic() {
            return delegate.isPublic();
        }

        @Override
        public boolean isPrivate() {
            return delegate.isPrivate();
        }

        @Override
        public int getModifiers() throws UnsupportedOperationException {
            return delegate.getModifiers();
        }

        @Override
        public IClass getSuperclass() {
            return delegate.getSuperclass();
        }

        @Override
        public Collection<? extends IClass> getDirectInterfaces() {
            return delegate.getDirectInterfaces();
        }

        @Override
        public Collection<IClass> getAllImplementedInterfaces() {
            return delegate.getAllImplementedInterfaces();
        }

        @Override
        public IMethod getMethod(Selector selector) {
            return delegate.getMethod(selector);
        }

        @Override
        public IField getField(Atom name) {
            return delegate.getField(name);
        }

        @Override
        public IField getField(Atom name, TypeName type) {
            return delegate.getField(name, type);
        }

        @Override
        public TypeReference getReference() {
            return delegate.getReference();
        }

        @Override
        public String getSourceFileName() throws NoSuchElementException {
            return delegate.getSourceFileName();
        }

        @Override
        public Reader getSource() throws NoSuchElementException {
            return delegate.getSource();
        }

        @Override
        public IMethod getClassInitializer() {
            return delegate.getClassInitializer();
        }

        @Override
        public boolean isArrayClass() {
            return delegate.isArrayClass();
        }

        @Override
        public Collection<IMethod> getDeclaredMethods() {
            return delegate.getDeclaredMethods();
        }

        @Override
        public Collection<IField> getAllInstanceFields() {
            return delegate.getAllInstanceFields();
        }

        @Override
        public Collection<IField> getAllStaticFields() {
            return delegate.getAllStaticFields();
        }

        @Override
        public Collection<IField> getAllFields() {
            return delegate.getAllFields();
        }

        @Override
        public Collection<IMethod> getAllMethods() {
            return delegate.getAllMethods();
        }

        @Override
        public Collection<IField> getDeclaredInstanceFields() {
            return delegate.getDeclaredInstanceFields();
        }

        @Override
        public Collection<IField> getDeclaredStaticFields() {
            return delegate.getDeclaredStaticFields();
        }

        @Override
        public TypeName getName() {
            return delegate.getName();
        }

        @Override
        public boolean isReferenceType() {
            return delegate.isReferenceType();
        }

        @Override
        public Collection<Annotation> getAnnotations() {
            return delegate.getAnnotations();
        }

        public IMethod getTargetMethod(){
            return this.m;
        }
    }

    static final class ReflectingMethodKey implements InstanceKey {
        private ReflectingMethodClass klass;

        public ReflectingMethodKey(ReflectingMethodClass klass){
            this.klass = klass;
        }

        @Override
        public IClass getConcreteType() {
            return this.klass;
        }

        public Iterator<Pair<CGNode, NewSiteReference>> getCreationSites(CallGraph CG) {
            return new ComposedIterator<CGNode, Pair<CGNode, NewSiteReference>>(CG.iterator()) {
                @Override
                public Iterator<? extends Pair<CGNode, NewSiteReference>> makeInner(final CGNode outer) {
                    return new MapIterator<NewSiteReference, Pair<CGNode, NewSiteReference>>(
                            new FilterIterator<NewSiteReference>(
                                    outer.iterateNewSites(),
                                    new Predicate<NewSiteReference>() {
                                        @Override public boolean test(NewSiteReference o) {
                                            return o.getDeclaredType().equals(klass.getReference());
                                        }
                                    }
                            ),
                            new Function<NewSiteReference, Pair<CGNode, NewSiteReference>>() {
                                @Override
                                public Pair<CGNode, NewSiteReference> apply(NewSiteReference object) {
                                    return Pair.make(outer, object);
                                }
                            });
                }
            };
        }

        @Override
        public String toString(){
            return "[InstanceKey for Reflection Method] " + klass.getTargetMethod();
        }
    }
}
