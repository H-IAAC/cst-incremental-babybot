package metrics;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class TimingRegistry {

    private final Map<String, TimingData> timings =
            new ConcurrentHashMap<String, TimingData>();

    public TimerContext start(String componentName) {
        return new TimerContext(
                this,
                componentName,
                System.nanoTime()
        );
    }

    public void record(
            String componentName,
            long durationNs
    ) {
        if (componentName == null
                || componentName.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "componentName não pode ser vazio"
            );
        }

        if (durationNs < 0L) {
            return;
        }

        TimingData data =
                timings.computeIfAbsent(
                        componentName,
                        key -> new TimingData()
                );

        data.record(durationNs);
    }

    public TimingSummary getSummary(
            String componentName
    ) {
        TimingData data =
                timings.get(componentName);

        if (data == null) {
            return new TimingSummary(
                    componentName,
                    0L,
                    0L,
                    0.0,
                    0L,
                    0L,
                    0L
            );
        }

        return data.createSummary(componentName);
    }

    public List<TimingSummary> getAllSummaries() {

        List<TimingSummary> result =
                new ArrayList<TimingSummary>();

        for (Map.Entry<String, TimingData> entry
                : timings.entrySet()) {

            result.add(
                    entry.getValue()
                            .createSummary(entry.getKey())
            );
        }

        Collections.sort(
                result,
                new Comparator<TimingSummary>() {
                    @Override
                    public int compare(
                            TimingSummary first,
                            TimingSummary second
                    ) {
                        return first.componentName.compareTo(
                                second.componentName
                        );
                    }
                }
        );

        return result;
    }

    public void reset() {
        timings.clear();
    }

    public void writeCsv(
            Path outputFile,
            String runId,
            long seed,
            int stage,
            String scenario,
            int episode,
            long environmentSteps
    ) throws IOException {

        Path parent = outputFile.getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        boolean writeHeader =
                !Files.exists(outputFile)
                || Files.size(outputFile) == 0L;

        if (writeHeader) {
            String header =
                    "run_id,"
                    + "seed,"
                    + "stage,"
                    + "scenario,"
                    + "episode,"
                    + "environment_steps,"
                    + "component,"
                    + "calls,"
                    + "total_ns,"
                    + "mean_ns,"
                    + "min_ns,"
                    + "max_ns,"
                    + "p95_ns,"
                    + "calls_per_environment_step,"
                    + "time_per_environment_step_ns"
                    + System.lineSeparator();

            Files.write(
                    outputFile,
                    header.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8
                    ),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        }

        for (TimingSummary summary : getAllSummaries()) {

            double callsPerStep =
                    environmentSteps > 0L
                            ? (double) summary.calls
                                    / environmentSteps
                            : Double.NaN;

            double timePerStep =
                    environmentSteps > 0L
                            ? (double) summary.totalNs
                                    / environmentSteps
                            : Double.NaN;

            String line =
                    csv(runId) + ","
                    + seed + ","
                    + stage + ","
                    + csv(scenario) + ","
                    + episode + ","
                    + environmentSteps + ","
                    + csv(summary.componentName) + ","
                    + summary.calls + ","
                    + summary.totalNs + ","
                    + decimal(summary.meanNs) + ","
                    + summary.minNs + ","
                    + summary.maxNs + ","
                    + summary.p95Ns + ","
                    + decimal(callsPerStep) + ","
                    + decimal(timePerStep)
                    + System.lineSeparator();

            Files.write(
                    outputFile,
                    line.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8
                    ),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        }
    }

    private static String decimal(double value) {
        if (Double.isNaN(value)) {
            return "";
        }

        return String.format(
                Locale.US,
                "%.8f",
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

    private static class TimingData {

        private final LongAdder calls =
                new LongAdder();

        private final LongAdder totalNs =
                new LongAdder();

        private final List<Long> samples =
                Collections.synchronizedList(
                        new ArrayList<Long>()
                );

        private volatile long minNs = Long.MAX_VALUE;
        private volatile long maxNs = Long.MIN_VALUE;

        public synchronized void record(long durationNs) {

            calls.increment();
            totalNs.add(durationNs);

            if (durationNs < minNs) {
                minNs = durationNs;
            }

            if (durationNs > maxNs) {
                maxNs = durationNs;
            }

            samples.add(durationNs);
        }

        public TimingSummary createSummary(
                String componentName
        ) {
            long numberOfCalls = calls.sum();
            long total = totalNs.sum();

            if (numberOfCalls == 0L) {
                return new TimingSummary(
                        componentName,
                        0L,
                        0L,
                        0.0,
                        0L,
                        0L,
                        0L
                );
            }

            List<Long> copy;

            synchronized (samples) {
                copy = new ArrayList<Long>(samples);
            }

            Collections.sort(copy);

            int p95Index =
                    (int) Math.ceil(copy.size() * 0.95) - 1;

            if (p95Index < 0) {
                p95Index = 0;
            }

            if (p95Index >= copy.size()) {
                p95Index = copy.size() - 1;
            }

            long p95 = copy.get(p95Index);

            return new TimingSummary(
                    componentName,
                    numberOfCalls,
                    total,
                    (double) total / numberOfCalls,
                    minNs == Long.MAX_VALUE ? 0L : minNs,
                    maxNs == Long.MIN_VALUE ? 0L : maxNs,
                    p95
            );
        }
    }

    public static class TimerContext
            implements AutoCloseable {

        private final TimingRegistry registry;
        private final String componentName;
        private final long startNs;

        private boolean closed;

        private TimerContext(
                TimingRegistry registry,
                String componentName,
                long startNs
        ) {
            this.registry = registry;
            this.componentName = componentName;
            this.startNs = startNs;
        }

        @Override
        public void close() {
            if (closed) {
                return;
            }

            long duration =
                    System.nanoTime() - startNs;

            registry.record(
                    componentName,
                    duration
            );

            closed = true;
        }
    }

    public static class TimingSummary {

        public final String componentName;
        public final long calls;
        public final long totalNs;
        public final double meanNs;
        public final long minNs;
        public final long maxNs;
        public final long p95Ns;

        public TimingSummary(
                String componentName,
                long calls,
                long totalNs,
                double meanNs,
                long minNs,
                long maxNs,
                long p95Ns
        ) {
            this.componentName = componentName;
            this.calls = calls;
            this.totalNs = totalNs;
            this.meanNs = meanNs;
            this.minNs = minNs;
            this.maxNs = maxNs;
            this.p95Ns = p95Ns;
        }
    }
}