
package org.deeplearning4j.rl4j.observation.transform.operation.historymerge;

import org.deeplearning4j.rl4j.observation.transform.operation.HistoryMergeTransform;
import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * HistoryMergeElementStore is used with the {@link HistoryMergeTransform HistoryMergeTransform}. Used to supervise how data from the
 * HistoryMergeTransform is stored.
 *
 * @author leolellisr
 */
public interface HistoryMergeElementStore {
    /**
     * Add an element into the store
     * @param observation
     */
    void add(INDArray observation);

    /**
     * Get the content of the store
     * @return the content of the store
     */
    INDArray[] get();

    /**
     * Used to tell the HistoryMergeTransform that the store is ready. The HistoryMergeTransform will tell the {@link org.deeplearning4j.rl4j.observation.transform.TransformProcess TransformProcess}
     * to skip the observation is the store is not ready.
     * @return true if the store is ready
     */
    boolean isReady();

    /**
     * Resets the store to an initial state.
     */
    void reset();
}
