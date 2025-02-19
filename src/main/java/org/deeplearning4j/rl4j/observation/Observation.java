

package org.deeplearning4j.rl4j.observation;

import lombok.Getter;
import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Represent an observation from the environment
 *
 * @author leolellisr
 */
public class Observation implements Encodable {

    /**
     * A singleton representing a skipped observation
     */
    public static Observation SkippedObservation = new Observation(null);

    /**
     * @return A INDArray containing the data of the observation
     */
    @Getter
    private final INDArray data;

    @Override
    public double[] toArray() {
        return data.data().asDouble();
    }

    public boolean isSkipped() {
        return data == null;
    }

    public Observation(INDArray data) {
        this.data = data;
    }

    /**
     * Creates a duplicate instance of the current observation
     * @return
     */
    public Observation dup() {
        if(data == null) {
            return SkippedObservation;
        }

        return new Observation(data.dup());
    }
    
   /* @Override
    public INDArray getData(){
        return data;
    }*/
}
