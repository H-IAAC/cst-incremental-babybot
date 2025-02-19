
package org.deeplearning4j.rl4j.experience;

import lombok.EqualsAndHashCode;
import org.deeplearning4j.rl4j.learning.sync.ExpReplay;
import org.deeplearning4j.rl4j.learning.sync.IExpReplay;
import org.deeplearning4j.rl4j.learning.sync.Transition;
import org.deeplearning4j.rl4j.observation.Observation;
import org.nd4j.linalg.api.rng.Random;
import org.nd4j.linalg.factory.Nd4j;

import java.util.List;

/**
 * @author leolellisr
 * A experience handler that stores the experience in a replay memory. See https://arxiv.org/abs/1312.5602
 * The experience container is a {@link Transition Transition} that stores the tuple observation-action-reward-nextObservation,
 * as well as whether or the not the episode ended after the Transition
 *
 * @param <A> Action type
 */
@EqualsAndHashCode
public class ReplayMemoryExperienceHandler<A> implements ExperienceHandler<A, Transition<A>> {
    private static final int DEFAULT_MAX_REPLAY_MEMORY_SIZE = 150000;
    private static final int DEFAULT_BATCH_SIZE = 32;

    private IExpReplay<A> expReplay;

    private Transition<A> pendingTransition;

    public ReplayMemoryExperienceHandler(IExpReplay<A> expReplay) {
        this.expReplay = expReplay;
    }

    public ReplayMemoryExperienceHandler(int maxReplayMemorySize, int batchSize, Random random) {
        this(new ExpReplay<A>(maxReplayMemorySize, batchSize, random));
    }

    public void addExperience(Observation observation, A action, double reward, boolean isTerminal) {
        setNextObservationOnPending(observation);
        pendingTransition = new Transition<>(observation, action, reward, isTerminal);
    }

    public void setFinalObservation(Observation observation) {
        setNextObservationOnPending(observation);
        pendingTransition = null;
    }

    @Override
    public int getTrainingBatchSize() {
        return expReplay.getBatchSize();
    }

    /**
     * @return A batch of experience selected from the replay memory. The replay memory is unchanged after the call.
     */
    @Override
    public List<Transition<A>> generateTrainingBatch() {
        return expReplay.getBatch();
    }

    @Override
    public void reset() {
        pendingTransition = null;
    }

    private void setNextObservationOnPending(Observation observation) {
        if(pendingTransition != null) {
            pendingTransition.setNextObservation(observation);
            expReplay.store(pendingTransition);
        }
    }

    public class Builder {
        private int maxReplayMemorySize = DEFAULT_MAX_REPLAY_MEMORY_SIZE;
        private int batchSize = DEFAULT_BATCH_SIZE;
        private Random random = Nd4j.getRandom();

        public Builder maxReplayMemorySize(int value) {
            maxReplayMemorySize = value;
            return this;
        }

        public Builder batchSize(int value) {
            batchSize = value;
            return this;
        }

        public Builder random(Random value) {
            random = value;
            return this;
        }

        public ReplayMemoryExperienceHandler<A> build() {
            return new ReplayMemoryExperienceHandler<A>(maxReplayMemorySize, batchSize, random);
        }
    }
}
