package metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Locale;

public class MetricsRecorder implements AutoCloseable {

    private final Path stepsFile;
    private final Path episodesFile;
    private final Path proceduresFile;

    public MetricsRecorder(Path runDirectory)
            throws IOException {

        Files.createDirectories(runDirectory);

        this.stepsFile =
                runDirectory.resolve("steps.csv");

        this.episodesFile =
                runDirectory.resolve("episodes.csv");

        this.proceduresFile =
                runDirectory.resolve("procedures.csv");

        writeHeaders();
    }

    private void writeHeaders() throws IOException {

        writeHeaderIfNecessary(
                stepsFile,
                "timestamp,"
                + "run_id,"
                + "seed,"
                + "stage,"
                + "scenario,"
                + "episode,"
                + "step,"
                + "training,"
                + "algorithm,"
                + "representation,"
                + "action,"
                + "action_source,"
                + "raw_reward,"
                + "scaled_reward,"
                + "external_reward,"
                + "intrinsic_reward,"
                + "procedure_reward,"
                + "top_down_reward,"
                + "failure_penalty,"
                + "target_visible,"
                + "target_in_fov,"
                + "yaw_error_deg,"
                + "pitch_error_deg,"
                + "angular_error_deg,"
                + "absolute_angular_error_deg,"
                + "target_salience,"
                + "max_distractor_salience,"
                + "head_yaw,"
                + "head_pitch,"
                + "fovea_position,"
                + "terminal,"
                + "terminal_reason,"
                + "decision_time_ns,"
                + "learning_time_ns"
        );

        writeHeaderIfNecessary(
                episodesFile,
                "run_id,"
                + "seed,"
                + "stage,"
                + "scenario,"
                + "episode,"
                + "training,"
                + "algorithm,"
                + "representation,"
                + "episode_length,"
                + "raw_return,"
                + "scaled_return,"
                + "external_return,"
                + "intrinsic_return,"
                + "reward_per_step,"
                + "success,"
                + "fov_fraction,"
                + "mean_absolute_angular_error,"
                + "rmse_angular_error,"
                + "integrated_absolute_error,"
                + "target_loss_count,"
                + "action_count,"
                + "unique_action_count,"
                + "reorientation_count,"
                + "head_path_length_deg,"
                + "time_to_first_fixation_steps,"
                + "time_to_reacquire_steps,"
                + "terminal_failure,"
                + "terminal_reason,"
                + "wall_clock_time_ms"
        );

        writeHeaderIfNecessary(
                proceduresFile,
                "run_id,"
                + "seed,"
                + "stage,"
                + "scenario,"
                + "episode,"
                + "step,"
                + "procedure_id,"
                + "event,"
                + "creation_stage,"
                + "creation_episode,"
                + "usage_count,"
                + "success_count,"
                + "action,"
                + "details"
        );
    }

    private static void writeHeaderIfNecessary(
            Path file,
            String header
    ) throws IOException {

        if (Files.exists(file)
                && Files.size(file) > 0L) {
            return;
        }

        Files.write(
                file,
                (header + System.lineSeparator())
                        .getBytes(
                                java.nio.charset.StandardCharsets.UTF_8
                        ),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    public synchronized void recordStep(
            StepMetrics metrics
    ) throws IOException {

        if (metrics == null) {
            throw new IllegalArgumentException(
                    "StepMetrics não pode ser null"
            );
        }

        append(
                stepsFile,
                metrics.toCsv()
        );
    }

    public synchronized void recordEpisode(
            EpisodeMetrics metrics
    ) throws IOException {

        if (metrics == null) {
            throw new IllegalArgumentException(
                    "EpisodeMetrics não pode ser null"
            );
        }

        append(
                episodesFile,
                metrics.toCsv()
        );
    }

    public synchronized void recordProcedure(
            ProcedureMetrics metrics
    ) throws IOException {

        if (metrics == null) {
            throw new IllegalArgumentException(
                    "ProcedureMetrics não pode ser null"
            );
        }

        append(
                proceduresFile,
                metrics.toCsv()
        );
    }

    private static void append(
            Path file,
            String line
    ) throws IOException {

        Files.write(
                file,
                (line + System.lineSeparator())
                        .getBytes(
                                java.nio.charset.StandardCharsets.UTF_8
                        ),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );
    }

    public Path getStepsFile() {
        return stepsFile;
    }

    public Path getEpisodesFile() {
        return episodesFile;
    }

    public Path getProceduresFile() {
        return proceduresFile;
    }

    @Override
    public void close() {
        // Não há writer permanente.
    }

    public static class StepMetrics {

        public String timestamp = "";
        public String runId = "";
        public long seed;

        public int stage;
        public String scenario = "";
        public int episode;
        public int step;

        public boolean training;

        public String algorithm = "";
        public String representation = "";

        public String action = "";
        public String actionSource = "";

        public double rawReward;
        public double scaledReward;
        public double externalReward;
        public double intrinsicReward;
        public double procedureReward;
        public double topDownReward;
        public double failurePenalty;

        public boolean targetVisible;
        public boolean targetInFov;

        public double yawErrorDeg = Double.NaN;
        public double pitchErrorDeg = Double.NaN;
        public double angularErrorDeg = Double.NaN;
        public double absoluteAngularErrorDeg =
                Double.NaN;

        public double targetSalience = Double.NaN;
        public double maxDistractorSalience =
                Double.NaN;

        public double headYaw = Double.NaN;
        public double headPitch = Double.NaN;
        public int foveaPosition = -1;

        public boolean terminal;
        public String terminalReason = "";

        public long decisionTimeNs;
        public long learningTimeNs;

        public String toCsv() {

            return String.join(
                    ",",
                    csv(timestamp),
                    csv(runId),
                    Long.toString(seed),
                    Integer.toString(stage),
                    csv(scenario),
                    Integer.toString(episode),
                    Integer.toString(step),
                    Boolean.toString(training),
                    csv(algorithm),
                    csv(representation),
                    csv(action),
                    csv(actionSource),
                    decimal(rawReward),
                    decimal(scaledReward),
                    decimal(externalReward),
                    decimal(intrinsicReward),
                    decimal(procedureReward),
                    decimal(topDownReward),
                    decimal(failurePenalty),
                    Boolean.toString(targetVisible),
                    Boolean.toString(targetInFov),
                    decimal(yawErrorDeg),
                    decimal(pitchErrorDeg),
                    decimal(angularErrorDeg),
                    decimal(absoluteAngularErrorDeg),
                    decimal(targetSalience),
                    decimal(maxDistractorSalience),
                    decimal(headYaw),
                    decimal(headPitch),
                    Integer.toString(foveaPosition),
                    Boolean.toString(terminal),
                    csv(terminalReason),
                    Long.toString(decisionTimeNs),
                    Long.toString(learningTimeNs)
            );
        }
    }

    public static class EpisodeMetrics {

        public String runId = "";
        public long seed;

        public int stage;
        public String scenario = "";
        public int episode;

        public boolean training;

        public String algorithm = "";
        public String representation = "";

        public int episodeLength;

        public double rawReturn;
        public double scaledReturn;
        public double externalReturn;
        public double intrinsicReturn;
        public double rewardPerStep;

        public boolean success;
        public double fovFraction;

        public double meanAbsoluteAngularError;
        public double rmseAngularError;
        public double integratedAbsoluteError;

        public int targetLossCount;
        public int actionCount;
        public int uniqueActionCount;
        public int reorientationCount;

        public double headPathLengthDeg;

        public int timeToFirstFixationSteps = -1;
        public int timeToReacquireSteps = -1;

        public boolean terminalFailure;
        public String terminalReason = "";

        public long wallClockTimeMs;

        public String toCsv() {

            return String.join(
                    ",",
                    csv(runId),
                    Long.toString(seed),
                    Integer.toString(stage),
                    csv(scenario),
                    Integer.toString(episode),
                    Boolean.toString(training),
                    csv(algorithm),
                    csv(representation),
                    Integer.toString(episodeLength),
                    decimal(rawReturn),
                    decimal(scaledReturn),
                    decimal(externalReturn),
                    decimal(intrinsicReturn),
                    decimal(rewardPerStep),
                    Boolean.toString(success),
                    decimal(fovFraction),
                    decimal(meanAbsoluteAngularError),
                    decimal(rmseAngularError),
                    decimal(integratedAbsoluteError),
                    Integer.toString(targetLossCount),
                    Integer.toString(actionCount),
                    Integer.toString(uniqueActionCount),
                    Integer.toString(reorientationCount),
                    decimal(headPathLengthDeg),
                    Integer.toString(
                            timeToFirstFixationSteps
                    ),
                    Integer.toString(
                            timeToReacquireSteps
                    ),
                    Boolean.toString(terminalFailure),
                    csv(terminalReason),
                    Long.toString(wallClockTimeMs)
            );
        }
    }

    public static class ProcedureMetrics {

        public String runId = "";
        public long seed;

        public int stage;
        public String scenario = "";
        public int episode;
        public int step;

        public String procedureId = "";
        public String event = "";

        public int creationStage;
        public int creationEpisode;

        public long usageCount;
        public long successCount;

        public String action = "";
        public String details = "";

        public String toCsv() {

            return String.join(
                    ",",
                    csv(runId),
                    Long.toString(seed),
                    Integer.toString(stage),
                    csv(scenario),
                    Integer.toString(episode),
                    Integer.toString(step),
                    csv(procedureId),
                    csv(event),
                    Integer.toString(creationStage),
                    Integer.toString(creationEpisode),
                    Long.toString(usageCount),
                    Long.toString(successCount),
                    csv(action),
                    csv(details)
            );
        }
    }

    private static String decimal(double value) {

        if (Double.isNaN(value)
                || Double.isInfinite(value)) {
            return "";
        }

        return String.format(
                Locale.US,
                "%.10f",
                value
        );
    }

    private static String csv(String value) {

        if (value == null) {
            return "";
        }

        return "\""
                + value.replace("\"", "\"\"")
                + "\"";
    }
}