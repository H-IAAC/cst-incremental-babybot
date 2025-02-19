

package org.deeplearning4j.rl4j.learning;

import org.deeplearning4j.rl4j.learning.configuration.ILearningConfiguration;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.policy.IPolicy;
import org.deeplearning4j.rl4j.space.ActionSpace;
import org.deeplearning4j.rl4j.space.Encodable;

/**
 * @author leolellisr
 *
 * A common interface that any training method should implement
 */
public interface ILearning<OBSERVATION extends Encodable, A, AS extends ActionSpace<A>> {

    IPolicy<A> getPolicy();

    void train();

    int getStepCount();

    ILearningConfiguration getConfiguration();

    MDP<OBSERVATION, A, AS> getMdp();

    IHistoryProcessor getHistoryProcessor();



}
