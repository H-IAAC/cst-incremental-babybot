/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/
package org.deeplearning4j.rl4j.helper;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * INDArray helper methods used by RL4J
 *
 * @author leolellisr
 */
public class INDArrayHelper {

    /**
     * MultiLayerNetwork and ComputationGraph expects input data to be in NCHW in the case of pixels and NS in case of other data types.
     *
     * We must have either shape 2 (NK) or shape 4 (NCHW)
     */
    public static INDArray forceCorrectShape(INDArray source) {

        return source.shape()[0] == 1 && source.shape().length > 1
                ? source
                : Nd4j.expandDims(source, 0);

    }
}
