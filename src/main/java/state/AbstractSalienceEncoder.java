package state;

import org.deeplearning4j.rl4j.observation.Observation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

public abstract class AbstractSalienceEncoder
        implements StateEncoder {

    private final int outputRows;
    private final int outputColumns;

    protected AbstractSalienceEncoder(
            int outputRows,
            int outputColumns
    ) {
        if (outputRows <= 0
                || outputColumns <= 0) {
            throw new IllegalArgumentException(
                    "Dimensões de saída inválidas"
            );
        }

        this.outputRows = outputRows;
        this.outputColumns = outputColumns;
    }

    @Override
    public Observation encode(
            StateSnapshot snapshot
    ) {
        float[] encoded =
                encodeToArray(snapshot);

        INDArray observationData =
                Nd4j.create(
                        new float[][] {
                                encoded
                        }
                );

        return new Observation(observationData);
    }

    protected float[] maxPool(
            StateSnapshot snapshot
    ) {
        int inputRows =
                snapshot.getSalienceRows();

        int inputColumns =
                snapshot.getSalienceColumns();

        if (inputRows % outputRows != 0
                || inputColumns % outputColumns != 0) {

            throw new IllegalArgumentException(
                    "O mapa "
                    + inputRows
                    + "x"
                    + inputColumns
                    + " não pode ser dividido em "
                    + outputRows
                    + "x"
                    + outputColumns
            );
        }

        int rowKernel =
                inputRows / outputRows;

        int columnKernel =
                inputColumns / outputColumns;

        float[] pooled =
                new float[
                        outputRows
                        * outputColumns
                ];

        int outputIndex = 0;

        for (int outputRow = 0;
                outputRow < outputRows;
                outputRow++) {

            for (int outputColumn = 0;
                    outputColumn < outputColumns;
                    outputColumn++) {

                float maximum =
                        -Float.MAX_VALUE;

                int startRow =
                        outputRow * rowKernel;

                int startColumn =
                        outputColumn * columnKernel;

                for (int rowOffset = 0;
                        rowOffset < rowKernel;
                        rowOffset++) {

                    for (int columnOffset = 0;
                            columnOffset < columnKernel;
                            columnOffset++) {

                        float value =
                                snapshot.getSalience(
                                        startRow
                                                + rowOffset,
                                        startColumn
                                                + columnOffset
                                );

                        if (value > maximum) {
                            maximum = value;
                        }
                    }
                }

                pooled[outputIndex] =
                        maximum == -Float.MAX_VALUE
                                ? 0.0f
                                : maximum;

                outputIndex++;
            }
        }

        return pooled;
    }

    protected float[] normalizeMinMax(
            float[] values
    ) {
        if (values == null
                || values.length == 0) {
            return new float[0];
        }

        float minimum = Float.MAX_VALUE;
        float maximum = -Float.MAX_VALUE;

        for (float value : values) {

            if (!Float.isFinite(value)) {
                continue;
            }

            if (value < minimum) {
                minimum = value;
            }

            if (value > maximum) {
                maximum = value;
            }
        }

        float[] normalized =
                new float[values.length];

        if (minimum == Float.MAX_VALUE
                || maximum == -Float.MAX_VALUE) {
            return normalized;
        }

        float range = maximum - minimum;

        if (Math.abs(range) < 1.0e-8f) {

            for (int index = 0;
                    index < normalized.length;
                    index++) {

                normalized[index] =
                        sanitize(values[index]);
            }

            return normalized;
        }

        for (int index = 0;
                index < values.length;
                index++) {

            float value = sanitize(values[index]);

            normalized[index] =
                    (value - minimum) / range;
        }

        return normalized;
    }

    protected float sanitize(float value) {

        if (Float.isNaN(value)
                || Float.isInfinite(value)) {
            return 0.0f;
        }

        return value;
    }

    protected int getOutputRows() {
        return outputRows;
    }

    protected int getOutputColumns() {
        return outputColumns;
    }

    @Override
    public int inputSize() {
        return outputRows * outputColumns;
    }
}