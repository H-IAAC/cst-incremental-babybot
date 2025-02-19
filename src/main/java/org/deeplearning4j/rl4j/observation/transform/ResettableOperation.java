/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/
package org.deeplearning4j.rl4j.observation.transform;

/**
 * The {@link TransformProcess TransformProcess} will call reset() (at the start of an episode) of any step that implement this interface.
 */
public interface ResettableOperation {
    /**
     * Called by TransformProcess when an episode starts. See {@link TransformProcess#reset() TransformProcess.reset()}
     */
    void reset();
}
