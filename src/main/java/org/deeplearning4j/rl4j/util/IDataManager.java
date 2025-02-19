/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/

package org.deeplearning4j.rl4j.util;

import org.deeplearning4j.rl4j.learning.ILearning;
import org.deeplearning4j.rl4j.learning.Learning;

import java.io.IOException;

public interface IDataManager {

    boolean isSaveData();
    String getVideoDir();
    void appendStat(StatEntry statEntry) throws IOException;
    void writeInfo(ILearning iLearning) throws IOException;
    void save(ILearning learning) throws IOException;

    //In order for jackson to serialize StatEntry
    //please use Lombok @Value (see QLStatEntry)
    interface StatEntry {
        int getEpochCounter();

        int getStepCounter();

        double getReward();
    }
}
