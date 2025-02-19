/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/

package org.deeplearning4j.rl4j.network.dqn;

/**
 * @author leolellisr 
 */
public interface DQNFactory {

    IDQN buildDQN(int shapeInputs[], int numOutputs);

}
