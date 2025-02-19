

package org.deeplearning4j.rl4j.observation.transform.operation.historymerge;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * HistoryStackAssembler is used with the HistoryMergeTransform. This assembler will
 * stack along the dimension 0. For example if the store elements are of shape [ Height, Width ]
 * the output will be of shape [ Stacked, Height, Width ]
 *
 * @author leolellisr
 */
public class HistoryStackAssembler implements HistoryMergeAssembler {

    /**
     * Will return a new INDArray with one more dimension and with elements stacked along dimension 0.
     *
     * @param elements Array of INDArray
     * @return A new INDArray with 1 more dimension than the input elements
     */
    @Override
    public INDArray assemble(INDArray[] elements) {
        // build the new shape
        long[] elementShape = elements[0].shape();
        long[] newShape = new long[elementShape.length + 1];
        newShape[0] = elements.length;
        System.arraycopy(elementShape, 0, newShape, 1, elementShape.length);

        // stack the elements in result on the dimension 0
        INDArray result = Nd4j.create(newShape);
        for(int i = 0; i < elements.length; ++i) {
            result.putRow(i, elements[i]);
        }
        return result;
    }
}
