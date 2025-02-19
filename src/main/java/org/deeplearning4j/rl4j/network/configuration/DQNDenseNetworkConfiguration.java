/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/

package org.deeplearning4j.rl4j.network.configuration;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
//@Builder
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class DQNDenseNetworkConfiguration extends NetworkConfiguration {

        /**
     * The number of layers in the dense network
     */
    @Builder.Default
    private int numLayers = 3;

    /**
     * The number of hidden neurons in each layer
     */
    @Builder.Default
    private int numHiddenNodes = 100;
    
    //public int getNumHiddenNodes(){
    //return numHiddenNodes;
    //}
    //public int getNumLayers(){
    //return numLayers;
    //}
}
