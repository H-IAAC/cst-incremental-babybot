package state;

public class Salience4x4BinaryEncoder
        extends AbstractSalienceEncoder {

    private final float threshold;
    private final boolean useMeanThreshold;

    public Salience4x4BinaryEncoder() {
        /*
         * O limiar médio é mais robusto que simplesmente
         * testar valor > 0.
         */
        this(0.0f, true);
    }

    public Salience4x4BinaryEncoder(
            float threshold
    ) {
        this(threshold, false);
    }

    public Salience4x4BinaryEncoder(
            float threshold,
            boolean useMeanThreshold
    ) {
        super(4, 4);

        this.threshold = threshold;
        this.useMeanThreshold =
                useMeanThreshold;
    }

    @Override
    public float[] encodeToArray(
            StateSnapshot snapshot
    ) {
        float[] pooled =
                maxPool(snapshot);

        float effectiveThreshold =
                useMeanThreshold
                        ? calculateMean(pooled)
                        : threshold;

        float[] result =
                new float[pooled.length];

        for (int index = 0;
                index < pooled.length;
                index++) {

            result[index] =
                    pooled[index]
                            > effectiveThreshold
                            ? 1.0f
                            : 0.0f;
        }

        return result;
    }

    public int encodeAsInteger(
            StateSnapshot snapshot
    ) {
        float[] binary =
                encodeToArray(snapshot);

        int state = 0;

        for (int index = 0;
                index < binary.length;
                index++) {

            if (binary[index] >= 0.5f) {
                state |= (1 << index);
            }
        }

        return state;
    }

    private float calculateMean(
            float[] values
    ) {
        if (values.length == 0) {
            return 0.0f;
        }

        float sum = 0.0f;

        for (float value : values) {
            sum += value;
        }

        return sum / values.length;
    }

    @Override
    public String name() {
        return "SALIENCE_4X4_BINARY";
    }
}