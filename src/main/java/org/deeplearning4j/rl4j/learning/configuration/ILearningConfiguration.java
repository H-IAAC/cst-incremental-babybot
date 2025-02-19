/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/

package org.deeplearning4j.rl4j.learning.configuration;

public interface ILearningConfiguration {
    Long getSeed();

    int getMaxEpochStep();

    int getMaxStep();

    double getGamma();

    double getRewardFactor();
}
