/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/

package org.deeplearning4j.rl4j.learning.configuration;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class LearningConfiguration implements ILearningConfiguration {

    /**
     * Seed value used for training
     */
    @Builder.Default
    private Long seed = System.currentTimeMillis();

    /**
     * The maximum number of steps in each episode
     */
    @Builder.Default
    private int maxEpochStep = 200;

    /**
     * The maximum number of steps to train for
     */
    @Builder.Default
    private int maxStep = 150000;

    /**
     * Gamma parameter used for discounted rewards
     */
    @Builder.Default
    private double gamma = 0.99;

    /**
     * Scaling parameter for rewards
     */
    @Builder.Default
    private double rewardFactor = 1.0;

    @Override
    public Long getSeed(){
        return seed;
    }
    
    @Override
    public double getGamma(){
        return gamma;
    }
    
    @Override
    public double getRewardFactor(){
        return rewardFactor;
    }
    
    @Override
    public int getMaxStep(){
        return maxStep;
    }
    
    @Override
    public int getMaxEpochStep(){
        return maxEpochStep;
    }
}
