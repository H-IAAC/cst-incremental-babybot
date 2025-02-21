

package org.deeplearning4j.rl4j.mdp;

import lombok.Value;
import org.deeplearning4j.rl4j.observation.Observation;
import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * @author leolellisr 17/12/22
 */
@Value
public class EnvState_1 implements Encodable {

    private Observation data;
    private int step;
    
    public EnvState_1(Observation i, int step_i){
        data = i;
        step = step_i;
    }
    public double[] toArray() {
        double[] ar = data.toArray();
        
        return ar;
    }

    public int getStep(){
        return step;
    }
    
    //@Override
    public boolean isSkipped() {
        return false;
    }

    @Override
    public INDArray getData() {
        return data.getData();
    }

    public Observation getObservation() {
        return data;
    }
    
    //@Override
    public Encodable dup() {
        return null;
    }
}
