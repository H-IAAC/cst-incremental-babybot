/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/

package org.deeplearning4j.rl4j.network.configuration;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.deeplearning4j.optimize.api.TrainingListener;
import org.nd4j.linalg.learning.config.IUpdater;

import java.util.List;


@Data
@SuperBuilder
@NoArgsConstructor
public class NetworkConfiguration {

    /**
     * The learning rate of the network
     */
    @Builder.Default
    private double learningRate = 0.01;

    /**
     * L2 regularization on the network
     */
    @Builder.Default
    private double l2 = 0.0;

    /**
     * The network's gradient update algorithm
     */
    private IUpdater updater;

    /**
     * Training listeners attached to the network
     */
    @Singular
    private List<TrainingListener> listeners;

}
