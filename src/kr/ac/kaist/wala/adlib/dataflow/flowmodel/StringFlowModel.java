package kr.ac.kaist.wala.adlib.dataflow.flowmodel;

import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

/**
 * Created by leesh on 11/10/2017.
 */
public class StringFlowModel extends ClassFlowModel{

    // for single-ton object
    private static StringFlowModel instance;

    public static StringFlowModel getInstance(){
        if(instance == null)
            instance = new StringFlowModel();
        return instance;
    }

    private StringFlowModel(){}

    @Override
    protected void init() {
        ref = TypeReference.JavaLangString;

        methods = new MethodFlowModel[]{
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>()V")), new int[]{}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([B)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([BLjava/nio/charset/Charset;)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([BI)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([BII)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([BIILjava/nio/charset/Charset;)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([BIII)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([BIILjava/lang/String;)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([BLjava/lang/String;)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([C)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([CII)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>([III)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>(Ljava/lang/String;)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>(Ljava/lang/StringBuffer;)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("<init>(Ljava/lang/StringBuilder;)V")), new int[]{1}, new int[]{MethodFlowModel.RECEIVERV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("chatAt(I)C")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("chatPointAt(I)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("chatPointBefore(I)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("chatPointCount(II)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("compareTo(Ljava/lang/String;)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("compareToIgnoreCase(Ljava/lang/String;)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("concat(Ljava/lang/String;)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV,1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("contains(Ljava/lang/CharSequence;)B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("contentEquals(Ljava/lang/CharSequence;)B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("contentEquals(Ljava/lang/StringBuffer;)B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("copyValueOf([C)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("copyValueOf([CII)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("endsWith(Ljava/lang/String;)B")), new int[]{1}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("equals(Ljava/lang/Object;)B")), new int[]{1}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("equalsIgnoreCase(Ljava/lang/String;)B")), new int[]{1}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("format(Ljava/util/Locale;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;")), new int[]{2,3}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;")), new int[]{1,2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getBytes()[B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getBytes(Ljava/nio/charset/Charset;)[B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getBytes(II[BI)V")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{3}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getBytes(Ljava/lang/String;)[B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getChars(II[CI)V")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{3}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("hashCode()I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("indexOf(I)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("indexOf(II)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("indexOf(Ljava/lang/String;)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("indexOf(Ljava/lang/String;I)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("intern()Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("isEmpty()B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("lastIndexOf(I)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("lastIndexOf(II)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("lastIndexOf(Ljava/lang/String;)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("lastIndexOf(Ljava/lang/String;I)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("length()I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("matches(Ljava/lang/String;)B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("offsetByCodePoints(II)I")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("regionMatches(BIL/java/lang/String;II)B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("regionMatches(IL/java/lang/String;II)B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("replace(CC)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV,2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV,2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV,2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("replaceFirst(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV,2}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("split(Ljava/lang/String;)[Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("split(Ljava/lang/String;I)[Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("startsWith(Ljava/lang/String;)B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("startsWith(Ljava/lang/String;I)B")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("subSequence(II)Ljava/lang/CharSequence;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("substring(I)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("substring(II)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("toCharArray()[C")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("toLowerCase()Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("toLowerCase(Ljava/util/Locale;)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("toString()Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("toUpperCase()Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("toUpperCase(Ljava/util/Locale;)Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("trim()Ljava/lang/String;")), new int[]{MethodFlowModel.RECEIVERV}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("valueOf(B)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("valueOf(C)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("valueOf([C)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("valueOf([CII)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("valueOf(D)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("valueOf(F)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("valueOf(I)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("valueOf(J)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("valueOf(Ljava/lang/Object;)Ljava/lang/String;")), new int[]{1}, new int[]{MethodFlowModel.RETV}),
        };
    }
}
