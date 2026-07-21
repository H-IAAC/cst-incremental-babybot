package state;

public class Salience16x16ContinuousEncoder
        extends AbstractSalienceEncoder {

    private final boolean normalize;

    public Salience16x16ContinuousEncoder() {
        this(true);
    }

    public Salience16x16ContinuousEncoder(
            boolean normalize
    ) {
        super(16, 16);
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
        return "SALIENCE_16X16_CONTINUOUS";
    }
}