package kr.ac.kaist.wala.adlib.analysis.malicious;

import kr.ac.kaist.wala.adlib.dataflow.ifds.model.ClassFlowModel;
import kr.ac.kaist.wala.adlib.dataflow.ifds.model.FlowModelHandler;

import java.util.Arrays;
import java.util.List;

/**
 * Created by leesh on 13/03/2018.
 */
public class MaliciousFlowModelHandler extends FlowModelHandler {

    public MaliciousFlowModelHandler(List<ClassFlowModel> mModels){
        List<ClassFlowModel> nModels =  Arrays.asList(super.models);
        nModels.addAll(mModels);
        super.models = nModels.toArray(new ClassFlowModel[0]);
    }
}
