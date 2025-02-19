

package org.deeplearning4j.rl4j.learning.sync.qlearning;

import org.deeplearning4j.rl4j.network.dqn.IDQN;

/**
 * An interface that is an extension of {@link QNetworkSource} for all implementations capable of supplying a target Q-Network
 *
 * @author leolellisr
 */
public interface TargetQNetworkSource extends QNetworkSource {
    IDQN getTargetQNetwork();
}
