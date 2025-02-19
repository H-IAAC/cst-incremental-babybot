

package org.deeplearning4j.rl4j.learning.sync.qlearning.discrete;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.deeplearning4j.gym.StepReply;
import org.deeplearning4j.rl4j.experience.ExperienceHandler;
import org.deeplearning4j.rl4j.experience.ReplayMemoryExperienceHandler;
import org.deeplearning4j.rl4j.learning.IHistoryProcessor.Configuration;
import org.deeplearning4j.rl4j.learning.IHistoryProcessor;
import org.deeplearning4j.rl4j.learning.Learning;
import org.deeplearning4j.rl4j.learning.configuration.QLearningConfiguration;
import org.deeplearning4j.rl4j.learning.sync.Transition;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.TDTargetAlgorithm.DoubleDQN;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.TDTargetAlgorithm.ITDTargetAlgorithm;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.TDTargetAlgorithm.StandardDQN;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.Encodable;
import org.deeplearning4j.rl4j.observation.Observation;
import org.deeplearning4j.rl4j.learning.sync.qlearning.TargetQNetworkSource;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.policy.EpsGreedy;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.util.LegacyMDPWrapper;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.rng.Random;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deeplearning4j.rl4j.mdp.EnvConstructive;
import org.deeplearning4j.rl4j.space.Box;



/**
 * @author leolellisr
 * <p>
 * DQN or Deep Q-Learning in the Discrete domain
 * <p>
 * http://arxiv.org/abs/1312.5602
 */
public abstract class QLearningDiscreteConstructive<O extends Encodable> extends QLearning<O, Integer, DiscreteSpace> {

    @Getter
    final private QLearningConfiguration configuration;
    private final LegacyMDPWrapper<O, Integer, DiscreteSpace> mdp;
    @Getter
    private DQNPolicy<O> policy;
    @Getter
    private EpsGreedy<O, Integer, DiscreteSpace> egPolicy;

    @Getter
    private IDQN qNetwork;
    @Getter
    @Setter(AccessLevel.PROTECTED)
    IDQN targetQNetwork;
    private int epochs = 0;
    @Getter
    private int lastAction;
    private double accuReward = 0;

    ITDTargetAlgorithm tdTargetAlgorithm;

    // TODO: User a builder and remove the setter
    @Getter(AccessLevel.PROTECTED) @Setter
    private ExperienceHandler<Integer, Transition<Integer>> experienceHandler;

    protected LegacyMDPWrapper<O, Integer, DiscreteSpace> getLegacyMDPWrapper() {
        return mdp;
    }

    public QLearningDiscreteConstructive(EnvConstructive<O, Integer, DiscreteSpace> mdp, IDQN dqn, QLearningConfiguration conf, int epsilonNbStep) {
        this(mdp, dqn, conf, epsilonNbStep, Nd4j.getRandomFactory().getNewRandomInstance(conf.getSeed()));
    }

    public QLearningDiscreteConstructive(EnvConstructive<O, Integer, DiscreteSpace> mdp, IDQN dqn, QLearningConfiguration conf, int epsilonNbStep, Random random) {
        this.configuration = conf;
        this.mdp = new LegacyMDPWrapper<>(mdp, null);
        qNetwork = dqn;
        targetQNetwork = dqn.clone();
        policy = new DQNPolicy(getQNetwork());
        egPolicy = new EpsGreedy(policy, mdp, conf.getUpdateStart(), epsilonNbStep, random, (float) conf.getMinEpsilon(),
                this);

        tdTargetAlgorithm = conf.isDoubleDQN()
                ? new DoubleDQN((TargetQNetworkSource) this, conf.getGamma(), conf.getErrorClamp())
                : new StandardDQN((TargetQNetworkSource) this, conf.getGamma(), conf.getErrorClamp());

        experienceHandler = new ReplayMemoryExperienceHandler(conf.getExpRepMaxSize(), conf.getBatchSize(), random);
    }

    public EnvConstructive<O, Integer, DiscreteSpace> getMdp() {
        return mdp.getWrappedMDP();
    }

    public void postEpoch() {
        epochs += 1;
        boolean new_action = true;
        if (getHistoryProcessor() != null)
            getHistoryProcessor().stopMonitor();
        if(epochs % 100==0){
            Logger.getAnonymousLogger().log(Level.INFO, "epochs: {0} \n "
                    + "getNN get nns 0 params shape: {1} \n"
                    + "getNN get nns length: {2} \n"
                    + "getNN get nns 0 params: {3} \n "
                    + "getMDP size Action {4} \n"
                    + "getMDP observation {5}",  
                    
                    new Object[] {  epochs, 
                                    qNetwork.getNeuralNetworks()[0].params().shapeInfoToString(),
                                    qNetwork.getNeuralNetworks().length,
                                    qNetwork.getNeuralNetworks()[0].params().toString(),
                                    getMdp().getActionSpace().getSize(), 
                                    getMdp().getObservationSpace()    
                    });
            
            
        }
    }

    public void preEpoch() {
        lastAction = mdp.getActionSpace().noOp();
        accuReward = 0;
        experienceHandler.reset();
        if(epochs % 100==0){
            Logger.getAnonymousLogger().log(Level.INFO, "epochs: {0} \n "
                    + "getNN get nns 0 params shape: {1} \n"
                    + "getNN get nns length: {2} \n"
                    + "getNN get nns 0 params: {3} \n "
                    + "getMDP size Action {4} \n"
                    + "getMDP observation {5}",  
                    
                    new Object[] {  epochs, 
                                    qNetwork.getNeuralNetworks()[0].params().shapeInfoToString(),
                                    qNetwork.getNeuralNetworks().length,
                                    qNetwork.getNeuralNetworks()[0].params().toString(),
                                    getMdp().getActionSpace().getSize(), 
                                    getMdp().getObservationSpace()    
                    });
            
            
        }
    }
    
    public void newAction(){
                INDArray newNN = qNetwork.getNeuralNetworks()[0].params();
                INDArray zeros = Nd4j.zeros(1, 2);
                INDArray combined = Nd4j.concat(0,newNN,zeros);
                //qNetwork = qNetwork.setNeuralNetworks(combined);
                
                // mdp.getActionSpace().setSize(mdp.getActionSpace().getSize()+1);
}

    public void setPolicy(DQNPolicy<O> new_policy){
        policy = new_policy;
} 
//    @Override
    public void setHistoryProcessor(IHistoryProcessor historyProcessor) {
        super.setHistoryProcessor(historyProcessor.getConf());
        mdp.setHistoryProcessor(historyProcessor);
    }

    /**
     * Single step of training
     *
     * @param obs last obs
     * @return relevant info for next step
     */
    public QLearning.QLStepReturn<Observation> trainStep(Observation obs) {
        mdp.setState(obs);
        boolean isHistoryProcessor = getHistoryProcessor() != null;
        int skipFrame = isHistoryProcessor ? getHistoryProcessor().getConf().getSkipFrame() : 1;
        int historyLength = isHistoryProcessor ? getHistoryProcessor().getConf().getHistoryLength() : 1;
        int updateStart = this.getConfiguration().getUpdateStart()
                + ((this.getConfiguration().getBatchSize() + historyLength) * skipFrame);

        Double maxQ = Double.NaN; //ignore if Nan for stats

        //if step of training, just repeat lastAction
        if (!obs.isSkipped()) {
            INDArray qs = getQNetwork().output(obs);
            int maxAction = Learning.getMaxAction(qs);
            maxQ = qs.getDouble(maxAction);

            lastAction = getEgPolicy().nextAction(Nd4j.create(obs.toArray()));
        }

        StepReply<Observation> stepReply = mdp.step(lastAction);
        accuReward += stepReply.getReward() * configuration.getRewardFactor();

        //if it's not a skipped frame, you can do a step of training
        if (!obs.isSkipped()) {

            // Add experience
            experienceHandler.addExperience(obs, lastAction, accuReward, stepReply.isDone());
            accuReward = 0;

            // Update NN
            // FIXME: maybe start updating when experience replay has reached a certain size instead of using "updateStart"?
            if (this.getStepCount() > updateStart) {
                DataSet targets = setTarget(experienceHandler.generateTrainingBatch());
                getQNetwork().fit(targets.getFeatures(), targets.getLabels());
            }
        }

        return new QLearning.QLStepReturn<>(maxQ, getQNetwork().getLatestScore(), stepReply);
    }

    protected DataSet setTarget(List<Transition<Integer>> transitions) {
        if (transitions.size() == 0)
            throw new IllegalArgumentException("too few transitions");

        return tdTargetAlgorithm.computeTDTargets(transitions);
    }

 //   @Override
    protected void finishEpoch(Observation observation) {
        experienceHandler.setFinalObservation(observation);
    }
    
    public void setReward(double reward){
        mdp.setReward(reward);
    }
    
    //@Override
    //public QLearningConfiguration getConfiguration(){
    //    return configuration;
    //}
    
    //@Override
    //public void setTargetQNetwork(IDQN new_net){
    //    targetQNetwork = new_net;
    //}
    
    //@Override
    //public IDQN getTargetQNetwork(){
    //    return targetQNetwork;
    //}
    
    //@Override
    //public IDQN getQNetwork(){
    //    return qNetwork;
    //}
}
