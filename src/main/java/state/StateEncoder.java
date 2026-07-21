package state;

import org.deeplearning4j.rl4j.observation.Observation;

public interface StateEncoder {

    Observation encode(StateSnapshot snapshot);

    float[] encodeToArray(StateSnapshot snapshot);

    int inputSize();

    String name();
}