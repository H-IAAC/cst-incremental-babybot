/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/

package org.deeplearning4j.rl4j.network.dqn;

/**
 * @author leolellisr 
 */
public interface DQNFactory_1 {

    IDQN buildDQN(int shapeInputs[], int numOutputs);

}
