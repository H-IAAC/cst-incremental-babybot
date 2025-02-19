/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/

package org.deeplearning4j.rl4j.observation.transform;

import org.datavec.api.transform.Operation;
import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public class EncodableToINDArrayTransform implements Operation<Encodable, INDArray> {
    @Override
    public INDArray transform(Encodable encodable) {
        return Nd4j.create(encodable.toArray());
    }
}
