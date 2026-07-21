package state;

public class Salience8x8ContinuousEncoder
        extends AbstractSalienceEncoder {

    private final boolean normalize;

    public Salience8x8ContinuousEncoder() {
        this(true);
    }

    public Salience8x8ContinuousEncoder(
            boolean normalize
    ) {
        super(8, 8);
        this.normalize = normalize;
    }

    @Override
    public float[] encodeToArray(
            StateSnapshot snapshot
    ) {
        float[] pooled =
                maxPool(snapshot);

        if (normalize) {
            return normalizeMinMax(pooled);
        }

        return pooled;
    }

    @Override
    public String name() {
        return "SALIENCE_8X8_CONTINUOUS";
    }
}