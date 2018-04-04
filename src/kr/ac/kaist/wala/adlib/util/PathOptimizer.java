package kr.ac.kaist.wala.adlib.util;

import com.ibm.wala.ipa.cfg.BasicBlockInContext;
import com.ibm.wala.ssa.SSAInstruction;
import kr.ac.kaist.wala.adlib.dataflow.ifds.DataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.LocalDataFact;
import kr.ac.kaist.wala.adlib.dataflow.ifds.PropagationPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leesh on 04/04/2018.
 */
public class PathOptimizer {
    public static List<PropagationPoint> optimize(List<PropagationPoint> path){
        List<PropagationPoint> nPath = new ArrayList<>();

        for(int i=0; i<path.size(); i++){
            if(i == 0){
                nPath.add(path.get(i));
            }else{
                PropagationPoint pp =path.get(i);

                BasicBlockInContext bb = pp.getBlock();

                if(bb.isEntryBlock() || bb.isExitBlock()){
                    nPath.add(pp);
                    continue;
                }

                DataFact df = pp.getFact();

                if(df instanceof LocalDataFact){
                    LocalDataFact ldf = (LocalDataFact) df;
                    SSAInstruction inst = bb.getLastInstruction();
                    if(inst != null){
                        for(int j=0; j<inst.getNumberOfUses(); j++){
                            if(inst.getUse(j) == ldf.getVar()) {
                                nPath.add(pp);
                                break;
                            }
                        }
                    }
                }else{
                    nPath.add(pp);
                }
            }
        }

        return nPath;
    }
}
