

package org.deeplearning4j.rl4j.learning;

import org.deeplearning4j.rl4j.mdp.MDP;

/**
 * The common API between Learning and AsyncThread.
 *
 * Express the ability to count the number of step of the current training.
 * Factorization of a feature between threads in async and learning process
 * for the web monitoring
 *
 * @author leolellisr
 */
public interface IEpochTrainer {
    int getStepCount();
    int getEpochCount();
    int getEpisodeCount();
    int getCurrentEpisodeStepCount();
    IHistoryProcessor getHistoryProcessor();
    MDP getMdp();
}
