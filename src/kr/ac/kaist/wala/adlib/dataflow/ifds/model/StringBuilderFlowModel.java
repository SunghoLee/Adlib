package kr.ac.kaist.wala.adlib.dataflow.ifds.model;

import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

/**
 * Created by leesh on 11/10/2017.
 */
public class StringBuilderFlowModel extends ClassFlowModel{

    // for single-ton object
    private static StringBuilderFlowModel instance;

    public static StringBuilderFlowModel getInstance(){
        if(instance == null)
            instance = new StringBuilderFlowModel();
        return instance;
    }

    private StringBuilderFlowModel(){}

    @Override
    protected void init() {
        ref = TypeReference.JavaLangStringBuilder;

        methods = new MethodFlowModel[]{
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>()V")), new int[]{}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>(Ljava/lang/CharSequence;)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>(I)V")), new int[]{}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>(Ljava/lang/String;)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(Z)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(C)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append([C)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append([CII)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(Ljava/lang/CharSequence;)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(Ljava/lang/CharSequence;II)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(D)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(F)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(I)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(J)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(Ljava/lang/Object;)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(Ljava/lang/String;)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("append(Ljava/lang/StringBuffer;)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("appendCodePoint(I)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("capacity()I")), new int[]{}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("charAt(I)C")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("codePointAt(I)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("codePointBefore(I)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("codePointCount(II)I")), new int[]{}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("delete(II)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("deleteCharAt(I)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("ensureCapacity(I)V")), new int[]{}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getChars(II[CI)V")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{3}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("indexOf(Ljava/lang/String;)I")), new int[]{1}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("indexOf(Ljava/lang/String;I)I")), new int[]{1}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(IZ)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(IC)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(I[C)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(I[CII)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(ILjava/lang/CharSequence;)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(ILjava/lang/CharSequence;II)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(ID)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(IF)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(II)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(IJ)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(ILjava/lang/Object;)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("insert(ILjava/lang/String;)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("lastIndexOf(Ljava/lang/String;)I")), new int[]{1}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("lastIndexOf(Ljava/lang/String;I)I")), new int[]{1}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("length()I")), new int[]{}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("offsetByCodePoints(II)I")), new int[]{}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("replace(IILjava/lang/String;)Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV, 3}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("reverse()Ljava/lang/StringBuilder;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("setCharAt(IC)V")), new int[]{MethodFlowModel.RECEIVERV, 2}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("setLength(I)V")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("subSequence(II)Ljava/lang/CharSequence;")), new int[]{MethodFlowModel.RECEIVERV,2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("subString(I)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("subString(II)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("toString()Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("trimToSize()V")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
        };
    }
}
