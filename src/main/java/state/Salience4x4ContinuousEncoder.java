package state;

public class Salience4x4ContinuousEncoder
        extends AbstractSalienceEncoder {

    private final boolean normalize;

    public Salience4x4ContinuousEncoder() {
        this(true);
    }

    public Salience4x4ContinuousEncoder(
            boolean normalize
    ) {
        super(4, 4);
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
        return "SALIENCE_4X4_CONTINUOUS";
    }
}