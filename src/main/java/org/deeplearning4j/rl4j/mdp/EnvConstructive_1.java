
package org.deeplearning4j.rl4j.mdp;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.learning.NeuralNetFetchable;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.ArrayObservationSpace;
import org.deeplearning4j.rl4j.space.ActionSpace;
import org.deeplearning4j.rl4j.space.Box;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.space.HighLowDiscrete;
import org.deeplearning4j.rl4j.space.Encodable;
import org.deeplearning4j.rl4j.space.ObservationSpace;
import org.nd4j.linalg.api.rng.Random;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;

import lombok.extern.slf4j.Slf4j;
import lombok.Getter;
import lombok.Setter;
import org.deeplearning4j.rl4j.observation.Observation;
/**
 * @author leolellisr
 *
 * A toy MDP where reward are given in every case.
 * Useful to debug.
 */
@Slf4j
public class EnvConstructive <OBSERVATION extends Encodable, A, AS extends ActionSpace<A>> implements MDP<OBSERVATION, A, AS> {
// implements MDP<EnvState, Integer, DiscreteSpace> 
    final private int maxStep;
    //@Getter
    private DiscreteSpace actionSpace;
    @Getter
    private ObservationSpace<OBSERVATION> observationSpace = (ObservationSpace<OBSERVATION>) new ArrayObservationSpace<Box>(new int[] {1,256});
    private EnvState envState;
    private double reward;
    @Setter
    private NeuralNetFetchable<IDQN> fetchable;

    public EnvConstructive(int maxStep, int numActions) {
        this.maxStep = maxStep;
        this.actionSpace = new DiscreteSpace(numActions);
        this.envState = new EnvState(new Observation(Nd4j.zeros(1, 1)), 0);
        this.reward = 1;
    }

    public void printTest(int maxStep) {
        int nRows = 1;
        int nColumns = 1;
        INDArray input = Nd4j.create(maxStep, 1);
        for (int i = 0; i < maxStep; i++) {
            input.putRow(i, Nd4j.create(new EnvState((Observation) Nd4j.zeros(1, 1), i).toArray()));
        }
        INDArray output = fetchable.getNeuralNet().output(input);
                Logger.getAnonymousLogger().log(Level.INFO, "EnvConstructive print test output: {0}", output.toString());

    }

    public void close() {}

//    @Override
    public boolean isDone() {
        return envState.getStep() == maxStep;    }


    public OBSERVATION reset() {
        int nRows = 1;
        int nColumns = 1;
        if (fetchable != null)
            printTest(maxStep);
        envState = new EnvState((Observation) Nd4j.zeros(nRows, nColumns), 0);
        return (OBSERVATION) envState;
    }

    @Override
    public StepReply<OBSERVATION> step(A a) {
        envState = new EnvState(new Observation(envState.getData()), envState.getStep()+1);
        return new StepReply<>( (OBSERVATION) envState, reward, isDone(), null);
    }

    public EnvConstructive newInstance() {
        EnvConstructive envConstructive = new EnvConstructive(maxStep, actionSpace.getSize());
        envConstructive.setFetchable(fetchable);
        return envConstructive;
    }

       @Override
        public AS getActionSpace() {
            return (AS) actionSpace; 
        }
    
        public void setState(Observation obs){
            envState = new EnvState(obs, envState.getStep()+1);;
        }
        
        public void setReward(double new_reward){
            reward = new_reward;
        }
//    public void setFetchable(NeuralNetFetchable<IDQN>  fetchable_i){
//        fetchable = fetchable_i;
//    }

//    public ObservationSpace<OBSERVATION> getObservationSpace(){
//        return observationSpace;
//    }

}
