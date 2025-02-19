

package org.deeplearning4j.rl4j.learning.sync.qlearning.discrete;

import org.deeplearning4j.rl4j.learning.configuration.QLearningConfiguration;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.mdp.MDP;
import org.deeplearning4j.rl4j.network.configuration.DQNDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.network.dqn.DQNFactory;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDenseRBF;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.space.Encodable;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.util.DataManagerTrainingListener;
import org.deeplearning4j.rl4j.util.IDataManager;
import java.util.logging.Logger;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.rl4j.mdp.EnvConstructive;
//import org.deeplearning4j.rl4j.mdp.gym.GymEnv;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.observation.Observation;
import org.deeplearning4j.rl4j.policy.EpsGreedy;
import org.deeplearning4j.rl4j.policy.IPolicy;
import org.deeplearning4j.rl4j.space.Box;
import org.deeplearning4j.rl4j.util.DataManager;

/**
 * @author leolellisr
 */
public class QLearningDiscreteDenseRBF<OBSERVATION extends Encodable> extends QLearningDiscreteConstructive<OBSERVATION> {


    @Deprecated
    public QLearningDiscreteDenseRBF(EnvConstructive<OBSERVATION, Integer, DiscreteSpace> mdp, IDQN dqn, QLearning.QLConfiguration conf,
                                  IDataManager dataManager) {
        this(mdp, dqn, conf);
        addListener(new DataManagerTrainingListener(dataManager));
        Logger.getAnonymousLogger().info("add Listener QLearningDiscreteDenseRBF 1");
    }

    @Deprecated
    public QLearningDiscreteDenseRBF(EnvConstructive<OBSERVATION, Integer, DiscreteSpace> mdp, IDQN dqn, QLearning.QLConfiguration conf) {
        super(mdp, dqn, conf.toLearningConfiguration(), conf.getEpsilonNbStep());
        Logger.getAnonymousLogger().info("build QLearningDiscreteDenseRBF 2");
    }

    public QLearningDiscreteDenseRBF(EnvConstructive<OBSERVATION, Integer, DiscreteSpace> mdp, IDQN dqn, QLearningConfiguration conf) {
        super(mdp, dqn, conf, conf.getEpsilonNbStep());
        Logger.getAnonymousLogger().info("QLearningDiscreteDenseRBF 3");
    }

    @Deprecated
    public QLearningDiscreteDenseRBF(EnvConstructive<OBSERVATION, Integer, DiscreteSpace> mdp, DQNFactory factory,
                                  QLearning.QLConfiguration conf, IDataManager dataManager) {
        this(mdp, factory.buildDQN(mdp.getObservationSpace().getShape(), mdp.getActionSpace().getSize()), conf,
                        dataManager);
        Logger.getAnonymousLogger().info("build DQN QLearningDiscreteDenseRBF 4");
    }

    @Deprecated
    public QLearningDiscreteDenseRBF(EnvConstructive<OBSERVATION, Integer, DiscreteSpace> mdp, DQNFactory factory,
                                  QLearning.QLConfiguration conf) {
        this(mdp, factory.buildDQN(mdp.getObservationSpace().getShape(), mdp.getActionSpace().getSize()), conf);
        Logger.getAnonymousLogger().info("QLearningDiscreteDenseRBF 5");
    }

    public QLearningDiscreteDenseRBF(EnvConstructive<OBSERVATION, Integer, DiscreteSpace> mdp, DQNFactory factory,
                                  QLearningConfiguration conf) {
        this(mdp, factory.buildDQN(mdp.getObservationSpace().getShape(), mdp.getActionSpace().getSize()), conf);
        Logger.getAnonymousLogger().info("QLearningDiscreteDenseRBF 6");
    }

    @Deprecated
    public QLearningDiscreteDenseRBF(EnvConstructive<OBSERVATION, Integer, DiscreteSpace> mdp, DQNFactoryStdDenseRBF.Configuration netConf,
                                  QLearning.QLConfiguration conf, IDataManager dataManager) {

        this(mdp, new DQNFactoryStdDenseRBF(netConf.toNetworkConfiguration()), conf, dataManager);
        Logger.getAnonymousLogger().info("new DQNFactoryStdDenseRBF QLearningDiscreteDenseRBF 7");
    }

    @Deprecated
    public QLearningDiscreteDenseRBF(EnvConstructive<OBSERVATION, Integer, DiscreteSpace> mdp, DQNFactoryStdDenseRBF.Configuration netConf,
                                  QLearning.QLConfiguration conf) {
        this(mdp, new DQNFactoryStdDenseRBF(netConf.toNetworkConfiguration()), conf);
        Logger.getAnonymousLogger().info("QLearningDiscreteDenseRBF 8");
    }

    public QLearningDiscreteDenseRBF(EnvConstructive<OBSERVATION, Integer, DiscreteSpace> mdp, DQNDenseNetworkConfiguration netConf,
                                  QLearningConfiguration conf) {
        this(mdp, new DQNFactoryStdDenseRBF(netConf), conf);
        Logger.getAnonymousLogger().info("QLearningDiscreteDenseRBF 9");
    }


}
