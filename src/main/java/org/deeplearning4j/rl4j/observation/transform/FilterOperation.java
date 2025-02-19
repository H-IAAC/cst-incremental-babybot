package org.deeplearning4j.rl4j.observation.transform;

import java.util.Map;

/**
 * Used with {@link TransformProcess TransformProcess} to filter-out an observation.
 *
 * @author leolellisr
 */
public interface FilterOperation {
    /**
     * The logic that determines if the observation should be skipped.
     *
     * @param channelsData the name of the channel
     * @param currentObservationStep The step number if the observation in the current episode.
     * @param isFinalObservation true if this is the last observation of the episode
     * @return true if the observation should be skipped
     */
    boolean isSkipped(Map<String, Object> channelsData, int currentObservationStep, boolean isFinalObservation);
}
