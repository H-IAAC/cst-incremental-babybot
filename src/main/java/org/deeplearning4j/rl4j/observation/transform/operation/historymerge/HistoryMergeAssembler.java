package org.deeplearning4j.rl4j.observation.transform.operation.historymerge;

import org.deeplearning4j.rl4j.observation.transform.operation.HistoryMergeTransform;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * A HistoryMergeAssembler is used with the {@link HistoryMergeTransform HistoryMergeTransform}. This interface defines how the array of INDArray
 * given by the {@link HistoryMergeElementStore HistoryMergeElementStore} is packaged into the single INDArray that will be
 * returned by the HistoryMergeTransform
 *
 * @author leolellisr
 */
public interface HistoryMergeAssembler {
    /**
     * Assemble an array of INDArray into a single INArray
     * @param elements The input INDArray[]
     * @return the assembled INDArray
     */
    INDArray assemble(INDArray[] elements);
}
