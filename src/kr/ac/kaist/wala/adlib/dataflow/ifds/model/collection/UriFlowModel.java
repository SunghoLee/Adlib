package kr.ac.kaist.wala.adlib.dataflow.ifds.model.collection;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.ClassFlowModel;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.MethodFlowModel;

/**
 * Created by leesh on 11/10/2017.
 */
public class UriFlowModel extends ClassFlowModel{

    // for single-ton object
    private static UriFlowModel instance;

    public static UriFlowModel getInstance(){
        if(instance == null)
            instance = new UriFlowModel();
        return instance;
    }

    private UriFlowModel(){}

    /**

     URL	toURL()
     Constructs a URL from this URI.
     */
    @Override
    protected void init() {
        ref = TypeReference.find(ClassLoaderReference.Primordial, "Landroid/net/Uri");

        methods = new MethodFlowModel[]{
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("create(Ljava/lang/String;)Landroid/net/Uri;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("parse(Ljava/lang/String;)Landroid/net/Uri;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getHost()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getLastPathSegment()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getAuthority()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getPath()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getPort()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getQuery()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getRawAuthority()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getRawFragment()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getRawPath()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getRawQuery()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getRawSchemeSpecificPart()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getRawUserInfo()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getScheme()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getSchemeSpecificPart()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("getUserInfo()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("normalize()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("parseServerAuthority()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("relativize(Landroid/net/Uri;)Ljava/lang/String;")), new int[]{0,1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("resolve(Ljava/lang/String;)Landroid/net/Uri;")), new int[]{0,1}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("toASCIIString()Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("toURL()Ljava/net/URL;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),

        };
    }
}
