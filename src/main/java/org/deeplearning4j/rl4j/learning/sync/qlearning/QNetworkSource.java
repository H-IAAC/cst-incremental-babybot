package org.deeplearning4j.rl4j.learning.sync.qlearning;

import org.deeplearning4j.rl4j.network.dqn.IDQN;

/**
 * An interface for all implementations capable of supplying a Q-Network
 *
 * @author leolellisr
 */
public interface QNetworkSource {
    IDQN getQNetwork();
}
