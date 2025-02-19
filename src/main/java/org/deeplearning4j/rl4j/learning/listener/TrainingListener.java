package org.deeplearning4j.rl4j.learning.listener;

import org.deeplearning4j.rl4j.learning.IEpochTrainer;
import org.deeplearning4j.rl4j.learning.ILearning;
import org.deeplearning4j.rl4j.util.IDataManager;

/**
 * The base definition of all training event listeners
 *
 * @author leolellisr
 */
public interface TrainingListener {
    enum ListenerResponse {
        /**
         * Tell the learning process to continue calling the listeners and the training.
         */
        CONTINUE,

        /**
         * Tell the learning process to stop calling the listeners and terminate the training.
         */
        STOP,
    }

    /**
     * Called once when the training starts.
     * @return A ListenerResponse telling the source of the event if it should go on or cancel the training.
     */
    ListenerResponse onTrainingStart();

    /**
     * Called once when the training has finished. This method is called even when the training has been aborted.
     */
    void onTrainingEnd();

    /**
     * Called before the start of every epoch.
     * @param trainer A {@link IEpochTrainer}
     * @return A ListenerResponse telling the source of the event if it should continue or stop the training.
     */
    ListenerResponse onNewEpoch(IEpochTrainer trainer);

    /**
     * Called when an epoch has been completed
     * @param trainer A {@link IEpochTrainer}
     * @param statEntry A {@link org.deeplearning4j.rl4j.util.IDataManager.StatEntry}
     * @return A ListenerResponse telling the source of the event if it should continue or stop the training.
     */
    ListenerResponse onEpochTrainingResult(IEpochTrainer trainer, IDataManager.StatEntry statEntry);

    /**
     * Called regularly to monitor the training progress.
     * @param learning A {@link ILearning}
     * @return A ListenerResponse telling the source of the event if it should continue or stop the training.
     */
    ListenerResponse onTrainingProgress(ILearning learning);
}
