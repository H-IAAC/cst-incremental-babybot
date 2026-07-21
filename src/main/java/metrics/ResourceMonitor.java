package metrics;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Locale;

public class ResourceMonitor implements AutoCloseable {

    private final Path outputFile;
    private final MemoryMXBean memoryBean;

    private final java.lang.management.OperatingSystemMXBean
            genericOperatingSystemBean;

    private final com.sun.management.OperatingSystemMXBean
            extendedOperatingSystemBean;

    private boolean headerWritten;

    public ResourceMonitor(Path resultDirectory)
            throws IOException {

        Files.createDirectories(resultDirectory);

        this.outputFile =
                resultDirectory.resolve("resources.csv");

        this.memoryBean =
                ManagementFactory.getMemoryMXBean();

        this.genericOperatingSystemBean =
                ManagementFactory.getOperatingSystemMXBean();

        if (genericOperatingSystemBean
                instanceof com.sun.management.OperatingSystemMXBean) {

            this.extendedOperatingSystemBean =
                    (com.sun.management.OperatingSystemMXBean)
                            genericOperatingSystemBean;

        } else {
            this.extendedOperatingSystemBean = null;
        }

        this.headerWritten =
                Files.exists(outputFile)
                && Files.size(outputFile) > 0;

        writeHeaderIfNecessary();
    }

    private synchronized void writeHeaderIfNecessary()
            throws IOException {

        if (headerWritten) {
            return;
        }

        String header =
                "timestamp,"
                + "run_id,"
                + "seed,"
                + "stage,"
                + "scenario,"
                + "episode,"
                + "step,"
                + "elapsed_time_ms,"
                + "available_processors,"
                + "system_load_average,"
                + "process_cpu_load,"
                + "system_cpu_load,"
                + "process_cpu_time_ns,"
                + "heap_used_bytes,"
                + "heap_committed_bytes,"
                + "heap_max_bytes,"
                + "non_heap_used_bytes,"
                + "non_heap_committed_bytes,"
                + "physical_memory_total_bytes,"
                + "physical_memory_free_bytes,"
                + "swap_total_bytes,"
                + "swap_free_bytes"
                + System.lineSeparator();

        Files.write(
                outputFile,
                header.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
        );

        headerWritten = true;
    }

    public synchronized ResourceSnapshot sample(
            String runId,
            long seed,
            int stage,
            String scenario,
            int episode,
            int step,
            long elapsedTimeMs
    ) throws IOException {

        ResourceSnapshot snapshot =
                createSnapshot(
                        runId,
                        seed,
                        stage,
                        scenario,
                        episode,
                        step,
                        elapsedTimeMs
                );

        append(snapshot);

        return snapshot;
    }

    public ResourceSnapshot createSnapshot(
            String runId,
            long seed,
            int stage,
            String scenario,
            int episode,
            int step,
            long elapsedTimeMs
    ) {

        MemoryUsage heap =
                memoryBean.getHeapMemoryUsage();

        MemoryUsage nonHeap =
                memoryBean.getNonHeapMemoryUsage();

        int availableProcessors =
                genericOperatingSystemBean
                        .getAvailableProcessors();

        double systemLoadAverage =
                genericOperatingSystemBean
                        .getSystemLoadAverage();

        double processCpuLoad = Double.NaN;
        double systemCpuLoad = Double.NaN;
        long processCpuTimeNs = -1L;

        long physicalMemoryTotal = -1L;
        long physicalMemoryFree = -1L;
        long swapTotal = -1L;
        long swapFree = -1L;

        if (extendedOperatingSystemBean != null) {

            processCpuLoad =
                    extendedOperatingSystemBean
                            .getProcessCpuLoad();

            systemCpuLoad =
                    extendedOperatingSystemBean.getSystemCpuLoad();

            processCpuTimeNs =
                    extendedOperatingSystemBean
                            .getProcessCpuTime();

            physicalMemoryTotal =
                    extendedOperatingSystemBean.getTotalPhysicalMemorySize();

            physicalMemoryFree =
                    extendedOperatingSystemBean.getFreePhysicalMemorySize();

            swapTotal =
                    extendedOperatingSystemBean
                            .getTotalSwapSpaceSize();

            swapFree =
                    extendedOperatingSystemBean
                            .getFreeSwapSpaceSize();
        }

        return new ResourceSnapshot(
                Instant.now().toString(),
                runId,
                seed,
                stage,
                scenario,
                episode,
                step,
                elapsedTimeMs,
                availableProcessors,
                systemLoadAverage,
                processCpuLoad,
                systemCpuLoad,
                processCpuTimeNs,
                heap.getUsed(),
                heap.getCommitted(),
                heap.getMax(),
                nonHeap.getUsed(),
                nonHeap.getCommitted(),
                physicalMemoryTotal,
                physicalMemoryFree,
                swapTotal,
                swapFree
        );
    }

    private void append(ResourceSnapshot snapshot)
            throws IOException {

        String line =
                snapshot.toCsv()
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

    public Path getOutputFile() {
        return outputFile;
    }

    @Override
    public void close() {
        // Não há stream mantido aberto.
    }

    public static class ResourceSnapshot {

        public final String timestamp;
        public final String runId;
        public final long seed;
        public final int stage;
        public final String scenario;
        public final int episode;
        public final int step;
        public final long elapsedTimeMs;

        public final int availableProcessors;
        public final double systemLoadAverage;
        public final double processCpuLoad;
        public final double systemCpuLoad;
        public final long processCpuTimeNs;

        public final long heapUsedBytes;
        public final long heapCommittedBytes;
        public final long heapMaxBytes;

        public final long nonHeapUsedBytes;
        public final long nonHeapCommittedBytes;

        public final long physicalMemoryTotalBytes;
        public final long physicalMemoryFreeBytes;
        public final long swapTotalBytes;
        public final long swapFreeBytes;

        public ResourceSnapshot(
                String timestamp,
                String runId,
                long seed,
                int stage,
                String scenario,
                int episode,
                int step,
                long elapsedTimeMs,
                int availableProcessors,
                double systemLoadAverage,
                double processCpuLoad,
                double systemCpuLoad,
                long processCpuTimeNs,
                long heapUsedBytes,
                long heapCommittedBytes,
                long heapMaxBytes,
                long nonHeapUsedBytes,
                long nonHeapCommittedBytes,
                long physicalMemoryTotalBytes,
                long physicalMemoryFreeBytes,
                long swapTotalBytes,
                long swapFreeBytes
        ) {
            this.timestamp = timestamp;
            this.runId = runId;
            this.seed = seed;
            this.stage = stage;
            this.scenario = scenario;
            this.episode = episode;
            this.step = step;
            this.elapsedTimeMs = elapsedTimeMs;
            this.availableProcessors = availableProcessors;
            this.systemLoadAverage = systemLoadAverage;
            this.processCpuLoad = processCpuLoad;
            this.systemCpuLoad = systemCpuLoad;
            this.processCpuTimeNs = processCpuTimeNs;
            this.heapUsedBytes = heapUsedBytes;
            this.heapCommittedBytes = heapCommittedBytes;
            this.heapMaxBytes = heapMaxBytes;
            this.nonHeapUsedBytes = nonHeapUsedBytes;
            this.nonHeapCommittedBytes =
                    nonHeapCommittedBytes;
            this.physicalMemoryTotalBytes =
                    physicalMemoryTotalBytes;
            this.physicalMemoryFreeBytes =
                    physicalMemoryFreeBytes;
            this.swapTotalBytes = swapTotalBytes;
            this.swapFreeBytes = swapFreeBytes;
        }

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
                    Long.toString(elapsedTimeMs),
                    Integer.toString(availableProcessors),
                    decimal(systemLoadAverage),
                    decimal(processCpuLoad),
                    decimal(systemCpuLoad),
                    Long.toString(processCpuTimeNs),
                    Long.toString(heapUsedBytes),
                    Long.toString(heapCommittedBytes),
                    Long.toString(heapMaxBytes),
                    Long.toString(nonHeapUsedBytes),
                    Long.toString(nonHeapCommittedBytes),
                    Long.toString(physicalMemoryTotalBytes),
                    Long.toString(physicalMemoryFreeBytes),
                    Long.toString(swapTotalBytes),
                    Long.toString(swapFreeBytes)
            );
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

            String escaped =
                    value.replace("\"", "\"\"");

            return "\"" + escaped + "\"";
        }
    }
}