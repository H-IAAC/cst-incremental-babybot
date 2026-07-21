package experiments;

import config.ExperimentConfig;
import config.LearningAlgorithm;
import config.StateRepresentation;
import cst_attmod_app.CST_incremental_babybot;

public class BatchExperimentRunner {

    public static void main(String[] args)
            throws Exception {

        long[] seeds = {
                1234L,
                5678L,
                91011L
        };

        for (long seed : seeds) {

            run(
                    seed,
                    LearningAlgorithm.TABULAR_Q,
                    StateRepresentation
                            .SALIENCE_4X4_BINARY
            );

            run(
                    seed,
                    LearningAlgorithm.DQN,
                    StateRepresentation
                            .SALIENCE_4X4_BINARY
            );

            run(
                    seed,
                    LearningAlgorithm.DQN,
                    StateRepresentation
                            .SALIENCE_16X16_CONTINUOUS
            );

            run(
                    seed,
                    LearningAlgorithm
                            .ONLINE_Q_NETWORK,
                    StateRepresentation
                            .SALIENCE_16X16_CONTINUOUS
            );
        }
    }

    private static void run(
            long seed,
            LearningAlgorithm algorithm,
            StateRepresentation representation
    ) throws Exception {

        String runId =
                algorithm
                + "_"
                + representation
                + "_seed_"
                + seed;

        String[] args = {
                "--seed=" + seed,
                "--algorithm=" + algorithm,
                "--representation=" + representation,
                "--run-id=" + runId
        };

        CST_incremental_babybot.main(args);
    }
}