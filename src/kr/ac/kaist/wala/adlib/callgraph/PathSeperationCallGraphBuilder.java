package kr.ac.kaist.wala.adlib.callgraph;

import com.ibm.wala.analysis.typeInference.JavaPrimitiveType;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IField;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.impl.ExplicitCallGraph;
import com.ibm.wala.ipa.callgraph.propagation.*;
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFABuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.IClassHierarchy;
import com.ibm.wala.ssa.SSAAbstractInvokeInstruction;
import com.ibm.wala.ssa.SymbolTable;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.MutableIntSet;
import com.ibm.wala.util.intset.MutableSparseIntSetFactory;
import com.ibm.wala.util.warnings.Warning;
import com.ibm.wala.util.warnings.Warnings;
import kr.ac.kaist.wala.adlib.callgraph.context.FirstMethod;
import kr.ac.kaist.wala.adlib.callgraph.context.FirstMethodContextSelector;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by leesh on 07/02/2018.
 */
public class PathSeperationCallGraphBuilder extends nCFABuilder {
    private final static boolean DEBUG = false;

    public PathSeperationCallGraphBuilder(int n, IClassHierarchy cha, AnalysisOptions options, AnalysisCache cache, ContextSelector appContextSelector, SSAContextInterpreter appContextInterpreter) {
        super(n, cha, options, cache, appContextSelector, appContextInterpreter);
    }

    private List<InstanceKey> getInstances(IntSet set) {
        LinkedList result = new LinkedList();
        IntIterator it = set.intIterator();

        while(it.hasNext()) {
            int j = it.next();
            result.add(system.getInstanceKey(j));
        }

        return result;
    }

    @Override
    protected ConstraintVisitor makeVisitor(CGNode node) {
        return new PathSeparationConstraintVisitor(this, node);
    }

    protected class PathSeparationConstraintVisitor extends ConstraintVisitor {
        private final ExplicitCallGraph callGraph;

        public PathSeparationConstraintVisitor(SSAPropagationCallGraphBuilder builder, CGNode node) {
            super(builder, node);
            this.callGraph = builder.getCallGraph();
        }

        @Override
        protected void visitGetInternal(int lval, int ref, boolean isStatic, FieldReference field) {
            if (DEBUG) {
                System.err.println("visitGet " + field);
            }

            PointerKey def = getPointerKeyForLocal(lval);
            assert def != null;

            IField f = getClassHierarchy().resolveField(field);
            if (f == null && callGraph.getFakeRootNode().getMethod().getDeclaringClass().getReference().equals(field.getDeclaringClass())) {
                f = callGraph.getFakeRootNode().getMethod().getDeclaringClass().getField(field.getName());
            }

            if (f == null) {
                return;
            }

            if(isStatic){
                IClass klass = getClassHierarchy().lookupClass(field.getDeclaringClass());
                if (klass == null) {
                } else {
                    // side effect of getstatic: may call class initializer
                    if (DEBUG) {
                        System.err.println("getstatic call class init " + klass);
                    }
                    processClassInitializer(klass);
                }
            }

            // skip getfields of primitive type (optimisation)
            if (field.getFieldType().isPrimitiveType()) {
                /// Lee: begin
                if(field.getFieldType().equals(TypeReference.Int)) {

                    if(isStatic){
                        //TODO: implement this!
                    }else{
                        PointerKey refKey = getPointerKeyForLocal(ref);
                        if(!system.isImplicit(refKey)) {
                            system.newSideEffect(getBuilder().new GetFieldOperator(f, system.findOrCreatePointsToSet(def)), refKey);
                        }
                    }
                }
                /// Lee: end
                return;
            }

            if (hasNoInterestingUses(lval)) {
                system.recordImplicitPointsToSet(def);
            } else {
                if (isStatic) {
                    PointerKey fKey = getPointerKeyForStaticField(f);
//          system.newConstraint(def, assignOperator, fKey);

                    /// Lee: begin
                    Context ctxt = node.getContext();
                    if(ctxt.get(FirstMethodContextSelector.FIRST_METHOD) instanceof FirstMethod) {
                        final IMethod path = ((FirstMethod)ctxt.get(FirstMethodContextSelector.FIRST_METHOD)).getMethod();

                        if(path != null) {
                            PropagationCallGraphBuilder.FilterOperator pathFilter = new PathSeparationFilter(path);

//            try {
                            system.newStatement(system.findOrCreatePointsToSet(def), pathFilter, system.findOrCreatePointsToSet(fKey), true, true);
//              system.newConstraint(def, pathFilter, fKey);
//            }catch(OutOfMemoryError e){
//              System.out.println(system.findOrCreatePointsToSet(fKey) + " \n " + fKey);
//              e.printStackTrace();
//            }
                        }
                    }
                    /// Lee: end

                } else {
                    PointerKey refKey = getPointerKeyForLocal(ref);
                    // if (!supportFullPointerFlowGraph &&
                    // contentsAreInvariant(ref)) {
                    if (contentsAreInvariant(symbolTable, du, ref)) {
                        system.recordImplicitPointsToSet(refKey);
                        InstanceKey[] ik = getInvariantContents(ref);
                        for (int i = 0; i < ik.length; i++) {
                            if (!representsNullType(ik[i])) {
                                system.findOrCreateIndexForInstanceKey(ik[i]);
                                PointerKey p = getPointerKeyForInstanceField(ik[i], f);
                                system.newConstraint(def, assignOperator, p);
                            }
                        }
                    } else {
                        system.newSideEffect(getBuilder().new GetFieldOperator(f, system.findOrCreatePointsToSet(def)), refKey);
                    }
                }
            }
        }

        public void visitPutInternal(int rval, int ref, boolean isStatic, FieldReference field) {

            if (DEBUG) {
                System.err.println("visitPut " + field);
            }

            // skip putfields of primitive type
            if (field.getFieldType().isPrimitiveType()) {
                /// Lee: begin
                if(field.getFieldType().equals(TypeReference.Int) && ref != -1){
                    SymbolTable symTab = node.getIR().getSymbolTable();
                    IField f = getClassHierarchy().resolveField(field);

                    if(symTab.isIntegerConstant(rval)) {
                        if(node.getMethod().getDeclaringClass().getClassLoader().equals(ClassLoaderReference.Application)) {
                            if (isStatic) {
                                //TODO: implement this!
                            } else {
                                PointerKey refKey = getPointerKeyForLocal(ref);
                                InstanceKey ik = getBuilder().getInstanceKeyForConstant(TypeReference.JavaLangInteger, symTab.getConstantValue(rval));

                                if (!system.isImplicit(refKey)) {
                                    system.newSideEffect(getBuilder().new InstancePutFieldOperator(f, ik), refKey);
                                }
                            }
                        }
                    } else{
                        PointerKey actual = getPointerKeyForLocal(rval);
                        PointerKey refKey = getPointerKeyForLocal(ref);

                        if (!system.isImplicit(refKey) && !system.isImplicit(actual)) {
                            system.newSideEffect(getBuilder().new PutFieldOperator(f, system.findOrCreatePointsToSet(actual)), refKey);
                        }
                    }
                }
                /// Lee: end
                return;
            }
            ClassHierarchy c;
            IField f = getClassHierarchy().resolveField(field);
            if (f == null) {
                if (DEBUG) {
                    System.err.println("Could not resolve field " + field);
                }
                Warnings.add(FieldResolutionFailure.create(field));
                return;
            }
            assert f.getFieldTypeReference().getName().equals(field.getFieldType().getName()) :
                    "name clash of two fields with the same name but different type: " + f.getReference() + " <=> " + field;
            assert isStatic || !symbolTable.isStringConstant(ref) : "put to string constant shouldn't be allowed?";
            if (isStatic) {
                processPutStatic(rval, field, f);
            } else {
                processPutField(rval, ref, f);
            }
        }
    }

    protected void processCallingConstraints(CGNode caller, SSAAbstractInvokeInstruction instruction, CGNode target,
                                             InstanceKey[][] constParams, PointerKey uniqueCatchKey) {
        // TODO: i'd like to enable this optimization, but it's a little tricky
        // to recover the implicit points-to sets with recursion. TODO: don't
        // be lazy and code the recursive logic to enable this.
        // if (hasNoInstructions(target)) {
        // // record points-to sets for formals implicitly .. computed on
        // // demand.
        // // TODO: generalize this by using hasNoInterestingUses on parameters.
        // // however .. have to be careful to cache results in that case ... don't
        // // want
        // // to recompute du each time we process a call to Object.<init> !
        // for (int i = 0; i < instruction.getNumberOfUses(); i++) {
        // // we rely on the invariant that the value number for the ith parameter
        // // is i+1
        // final int vn = i + 1;
        // PointerKey formal = getPointerKeyForLocal(target, vn);
        // if (target.getMethod().getParameterType(i).isReferenceType()) {
        // system.recordImplicitPointsToSet(formal);
        // }
        // }
        // } else {
        // generate contraints from parameter passing
        int nUses = instruction.getNumberOfParameters();
        int nExpected = target.getMethod().getNumberOfParameters();

    /*
     * int nExpected = target.getMethod().getReference().getNumberOfParameters(); if (!target.getMethod().isStatic() &&
     * !target.getMethod().isClinit()) { nExpected++; }
     */

        if (nUses != nExpected) {
            // some sort of unverifiable code mismatch. give up.
            return;
        }

        // we're a little sloppy for now ... we don't filter calls to
        // java.lang.Object.
        // TODO: we need much more precise filters than cones in order to handle
        // the various types of dispatch logic. We need a filter that expresses
        // "the set of types s.t. x.foo resolves to y.foo."
        for (int i = 0; i < instruction.getNumberOfParameters(); i++) {
            if (target.getMethod().getParameterType(i).isReferenceType()) {
                PointerKey formal = getTargetPointerKey(target, i);
                if (constParams != null && constParams[i] != null) {
                    InstanceKey[] ik = constParams[i];
                    for (int j = 0; j < ik.length; j++) {
                        system.newConstraint(formal, ik[j]);
                    }
                } else {
                    if (instruction.getUse(i) < 0) {
                        Assertions.UNREACHABLE("unexpected " + instruction + " in " + caller);
                    }
                    PointerKey actual = getPointerKeyForLocal(caller, instruction.getUse(i));
                    if (formal instanceof FilteredPointerKey) {
                        system.newConstraint(formal, filterOperator, actual);
                    } else {
                        system.newConstraint(formal, assignOperator, actual);
                    }
                }
            }
            // Lee : begin
            else if(target.getMethod().getParameterType(i).equals(TypeReference.JavaLangInteger) || target.getMethod().getParameterType(i).equals(JavaPrimitiveType.INT.getTypeReference())){
                PointerKey formal = getTargetPointerKey(target, i);
                int useVar = instruction.getUse(i);
                if(caller.getIR().getSymbolTable().isIntegerConstant(useVar) && caller.getMethod().getDeclaringClass().getClassLoader().getReference().equals(ClassLoaderReference.Application)){
                    int intValue = caller.getIR().getSymbolTable().getIntValue(useVar);
                    system.newConstraint(formal, new ConstantKey<Integer>(intValue, cha.lookupClass(TypeReference.JavaLangInteger)));
                }else{
                    PointerKey actual = getPointerKeyForLocal(caller, useVar);
                    if (formal instanceof FilteredPointerKey) {
                        system.newConstraint(formal, filterOperator, actual);
                    } else {
                        system.newConstraint(formal, assignOperator, actual);
                    }
                }
            }
            // Lee : end
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
        // }
    }

    private static class FieldResolutionFailure extends Warning {

        final FieldReference field;

        FieldResolutionFailure(FieldReference field) {
            super(Warning.SEVERE);
            this.field = field;
        }

        @Override
        public String getMsg() {
            return getClass().toString() + " : " + field;
        }

        public static FieldResolutionFailure create(FieldReference field) {
            return new FieldResolutionFailure(field);
        }
    }

    public class PathSeparationFilter extends FilterOperator {
        private IMethod path;

        public PathSeparationFilter(IMethod path){
            this.path = path;
        }

        @Override
        public byte evaluate(PointsToSetVariable lhs, PointsToSetVariable rhs) {
            MutableIntSet is = (new MutableSparseIntSetFactory()).make();

            if (rhs.getValue() != null) {
                for (InstanceKey ik : getInstances(rhs.getValue())) {
                    Iterator<Pair<CGNode, NewSiteReference>> iP = ik.getCreationSites(callGraph);

                    while (iP.hasNext()) {
                        Pair<CGNode, NewSiteReference> p = iP.next();
                        CGNode n = p.fst;

                        if (callGraph.getFakeRootNode().equals(n)) {
                            is.add(system.getInstanceIndex(ik));
                            continue;
                        }
                        //Lee: begin
                        IMethod creationPath = ((FirstMethod) n.getContext().get(FirstMethodContextSelector.FIRST_METHOD)).getMethod();

                        if (creationPath == null || path.equals(creationPath)) {
                            is.add(system.getInstanceIndex(ik));
                        }
                        //Lee: end
                    }
                }
            }
            boolean changed = lhs.addAll(is);
            return changed ? CHANGED : NOT_CHANGED;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof PathSeparationFilter){
                PathSeparationFilter psf = (PathSeparationFilter) obj;
                if(psf.path.equals(path))
                    return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + this.path.hashCode();
        }
    }
}
