/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/
package org.deeplearning4j.rl4j.observation.transform.operation;

import org.datavec.api.transform.Operation;
import org.nd4j.common.base.Preconditions;
import org.nd4j.linalg.api.ndarray.INDArray;

public class SimpleNormalizationTransform implements Operation<INDArray, INDArray> {

    private final double offset;
    private final double divisor;

    public SimpleNormalizationTransform(double min, double max) {
        Preconditions.checkArgument(min < max, "Min must be smaller than max.");

        this.offset = min;
        this.divisor = (max - min);
    }

    @Override
    public INDArray transform(INDArray input) {
        if(offset != 0.0) {
            input.subi(offset);
        }

        input.divi(divisor);

        return input;
    }
}
