/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/

package org.deeplearning4j.rl4j.network.dqn;

import org.deeplearning4j.examples.advanced.features.customizingdl4j.activationfunctions.RBFActivation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.rl4j.network.configuration.DQNDenseNetworkConfiguration;
import org.deeplearning4j.rl4j.util.Constants;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.IUpdater;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import java.util.Arrays;
import org.deeplearning4j.rl4j.network.configuration.DQNDenseNetworkConfiguration.DQNDenseNetworkConfigurationBuilder;

/**
 * @author rubenfiszel (ruben.fiszel@epfl.ch) 7/13/16.
 */

@Value
public class DQNFactoryStdDenseRBF_1 implements DQNFactory {

    DQNDenseNetworkConfiguration conf;

    public DQNFactoryStdDenseRBF_1(DQNDenseNetworkConfiguration netConf) {
        conf = netConf;
    }

    @Override
    public DQN_RBF buildDQN(int[] numInputs, int numOutputs) {
        int nIn = 1;

        for (int i : numInputs) {
            nIn *= i;
        }

        NeuralNetConfiguration.ListBuilder confB = new NeuralNetConfiguration.Builder().seed(Constants.NEURAL_NET_SEED)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                //.updater(conf.getUpdater() != null ? conf.getUpdater() : new Adam())
                .weightInit(WeightInit.XAVIER)
                //.l2(conf.getL2())
                .list()
                .layer(0,
                        new DenseLayer.Builder()
                                .nIn(nIn)
                                .nOut(conf.getNumHiddenNodes())
                                .activation(new RBFActivation ()).build()
                               //.activation(Activation.RELU).build()
                               //.activation(new CustomActivationDefinition()).build() 
                );


        for (int i = 1; i < conf.getNumLayers(); i++) {
            confB.layer(i, new DenseLayer.Builder().nIn(conf.getNumHiddenNodes()).nOut(conf.getNumHiddenNodes())
                    .activation(new RBFActivation ()).build());
                    //.activation(Activation.RELU).build());
            
        }
        //confB.layer(conf.getNumLayers()-1, new RBFLayer.Builder().nIn(conf.getNumHiddenNodes()).nOut(conf.getNumHiddenNodes())
        //            .activation(Activation.RELU).build());
        confB.layer(conf.getNumLayers(),
                new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(new RBFActivation ())
                        //.activation(Activation.IDENTITY)
                        .nIn(conf.getNumHiddenNodes())
                        .nOut(numOutputs)
                        .build()
        );


        MultiLayerConfiguration mlnconf = confB.build();
        MultiLayerNetwork model = new MultiLayerNetwork(mlnconf);
        model.init();
        //if (conf.getListeners() != null) {
        //    model.setListeners(conf.getListeners());
        //} else {
            model.setListeners(new ScoreIterationListener(Constants.NEURAL_NET_ITERATION_LISTENER));
        //}
        return new DQN_RBF(model);
    }

    @AllArgsConstructor
    @Value
    @Builder
    @Deprecated
    public static class Configuration {

        int numLayer;
        int numHiddenNodes;
        double l2;
        IUpdater updater;
        TrainingListener[] listeners;

        

        /**
         * Converts the deprecated Configuration to the new NetworkConfiguration format
         */
        public DQNDenseNetworkConfiguration toNetworkConfiguration() {
            DQNDenseNetworkConfigurationBuilder builder = DQNDenseNetworkConfiguration.builder()
                    .numHiddenNodes(numHiddenNodes)
                    .numLayers(numLayer);
                    //.l2(l2)
                    //.updater(updater);

            //if (listeners != null) {
            //    builder.listeners(Arrays.asList(listeners));
            //}

            return builder.build();
        }
        
    /*    public Configuration(int i, int i0, double d, IUpdater object, TrainingListener[] object0) {
              this.numLayer = i;
              this.numHiddenNodes = i0;
              this.l2 = d;
              this.updater = object;
              this.listeners = object0;
        }*/
    }

}
