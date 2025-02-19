

package org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.TDTargetAlgorithm;

import org.deeplearning4j.rl4j.learning.sync.Transition;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QNetworkSource;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.DataSet;

import java.util.List;

/**
 * The base of all TD target calculation algorithms that use deep learning.
 *
 * @author leolellisr
 */
public abstract class BaseTDTargetAlgorithm implements ITDTargetAlgorithm<Integer> {

    protected final QNetworkSource qNetworkSource;
    protected final double gamma;

    private final double errorClamp;
    private final boolean isClamped;

    /**
     *
     * @param qNetworkSource The source of the Q-Network
     * @param gamma The discount factor
     * @param errorClamp Will prevent the new Q-Value from being farther than <i>errorClamp</i> away from the previous value. Double.NaN will disable the clamping.
     */
    protected BaseTDTargetAlgorithm(QNetworkSource qNetworkSource, double gamma, double errorClamp) {
        this.qNetworkSource = qNetworkSource;
        this.gamma = gamma;

        this.errorClamp = errorClamp;
        isClamped = !Double.isNaN(errorClamp);
    }

    /**
     *
     * @param qNetworkSource The source of the Q-Network
     * @param gamma The discount factor
     * Note: Error clamping is disabled with this ctor
     */
    protected BaseTDTargetAlgorithm(QNetworkSource qNetworkSource, double gamma) {
        this(qNetworkSource, gamma, Double.NaN);
    }

    /**
     * Called just before the calculation starts
     * @param observations A INDArray of all observations stacked on dimension 0
     * @param nextObservations A INDArray of all next observations stacked on dimension 0
     */
    protected void initComputation(INDArray observations, INDArray nextObservations) {
        // Do nothing
    }

    /**
     * Compute the new estimated Q-Value for every transition in the batch
     *
     * @param batchIdx The index in the batch of the current transition
     * @param reward The reward of the current transition
     * @param isTerminal True if it's the last transition of the "game"
     * @return The estimated Q-Value
     */
    protected abstract double computeTarget(int batchIdx, double reward, boolean isTerminal);

    @Override
    public DataSet computeTDTargets(List<Transition<Integer>> transitions) {

        int size = transitions.size();

        INDArray observations = Transition.buildStackedObservations(transitions);
        INDArray nextObservations = Transition.buildStackedNextObservations(transitions);

        initComputation(observations, nextObservations);

        INDArray updatedQValues = qNetworkSource.getQNetwork().output(observations);

        for (int i = 0; i < size; ++i) {
            Transition<Integer> transition = transitions.get(i);
            double yTarget = computeTarget(i, transition.getReward(), transition.isTerminal());

            if(isClamped) {
                double previousQValue = updatedQValues.getDouble(i, transition.getAction());
                double lowBound = previousQValue - errorClamp;
                double highBound = previousQValue + errorClamp;
                yTarget = Math.min(highBound, Math.max(yTarget, lowBound));
            }
            updatedQValues.putScalar(i, transition.getAction(), yTarget);
        }

        return new org.nd4j.linalg.dataset.DataSet(observations, updatedQValues);
    }
}
