

package org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.TDTargetAlgorithm;

import org.deeplearning4j.rl4j.learning.sync.Transition;
import org.nd4j.linalg.dataset.api.DataSet;

import java.util.List;

/**
 * The interface of all TD target calculation algorithms.
 *
 * @param <A> The type of actions
 *
 * @author leolellisr
 */
public interface ITDTargetAlgorithm<A> {
    /**
     * Compute the updated estimated Q-Values for every transition
     * @param transitions The transitions from the experience replay
     * @return A DataSet where every element is the observation and the estimated Q-Values for all actions
     */
    DataSet computeTDTargets(List<Transition<A>> transitions);
}
