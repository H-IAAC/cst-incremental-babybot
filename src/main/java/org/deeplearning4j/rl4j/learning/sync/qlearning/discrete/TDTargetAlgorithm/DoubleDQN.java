

package org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.TDTargetAlgorithm;

import org.deeplearning4j.rl4j.learning.sync.qlearning.TargetQNetworkSource;
import java.util.List;
import org.deeplearning4j.rl4j.learning.sync.Transition;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.factory.Nd4j;

/**
 * The Double-DQN algorithm based on "Deep Reinforcement Learning with Double Q-learning" (https://arxiv.org/abs/1509.06461)
 *
 * @author leolellisr
 */
public class DoubleDQN extends BaseDQNAlgorithm {

    private static final int ACTION_DIMENSION_IDX = 1;

    // In litterature, this corresponds to: max_{a}Q(s_{t+1}, a)
    private INDArray maxActionsFromQNetworkNextObservation;

    public DoubleDQN(TargetQNetworkSource qTargetNetworkSource, double gamma) {
        super(qTargetNetworkSource, gamma);
    }

    public DoubleDQN(TargetQNetworkSource qTargetNetworkSource, double gamma, double errorClamp) {
        super(qTargetNetworkSource, gamma, errorClamp);
    }

    @Override
    protected void initComputation(INDArray observations, INDArray nextObservations) {
        super.initComputation(observations, nextObservations);

        maxActionsFromQNetworkNextObservation = Nd4j.argMax(qNetworkNextObservation, ACTION_DIMENSION_IDX);
    }

    /**
     * In litterature, this corresponds to:<br />
     *      Q(s_t, a_t) = R_{t+1} + \gamma * Q_{tar}(s_{t+1}, max_{a}Q(s_{t+1}, a))
     * @param batchIdx The index in the batch of the current transition
     * @param reward The reward of the current transition
     * @param isTerminal True if it's the last transition of the "game"
     * @return The estimated Q-Value
     */
    @Override
    protected double computeTarget(int batchIdx, double reward, boolean isTerminal) {
        double yTarget = reward;
        if (!isTerminal) {
            yTarget += gamma * targetQNetworkNextObservation.getDouble(batchIdx, maxActionsFromQNetworkNextObservation.getInt(batchIdx));
        }

        return yTarget;
    }

   
}
