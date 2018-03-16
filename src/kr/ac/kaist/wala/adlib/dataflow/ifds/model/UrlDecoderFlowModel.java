package kr.ac.kaist.wala.adlib.dataflow.ifds.model;

import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.types.Selector;
import com.ibm.wala.types.TypeReference;

/**
 * Created by leesh on 11/10/2017.
 */
public class UrlDecoderFlowModel extends ClassFlowModel{

    // for single-ton object
    private static UrlDecoderFlowModel instance;

    public static UrlDecoderFlowModel getInstance(){
        if(instance == null)
            instance = new UrlDecoderFlowModel();
        return instance;
    }

    private UrlDecoderFlowModel(){}

    @Override
    protected void init() {
        ref = TypeReference.find(ClassLoaderReference.Primordial, "Ljava/net/URLDecoder");

        methods = new MethodFlowModel[]{
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("decode(Ljava/lang/String;)Ljava/lang/String;")), new int[]{0}, new int[]{MethodFlowModel.RETV}),
                MethodFlowModel.make(MethodReference.findOrCreate(ref, Selector.make("decode(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")), new int[]{0,1}, new int[]{MethodFlowModel.RETV}),
        };
    }
}
