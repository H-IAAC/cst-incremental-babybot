package state;

import config.StateRepresentation;

public final class StateEncoderFactory {

    private StateEncoderFactory() {
    }

    public static StateEncoder create(
            StateRepresentation representation
    ) {
        if (representation == null) {
            throw new IllegalArgumentException(
                    "StateRepresentation cannot be null"
            );
        }

        switch (representation) {

            case SALIENCE_4X4_BINARY:
                return new Salience4x4BinaryEncoder();

            case SALIENCE_4X4_CONTINUOUS:
                return new Salience4x4ContinuousEncoder();

            case SALIENCE_8X8_CONTINUOUS:
                return new Salience8x8ContinuousEncoder();

            case SALIENCE_16X16_CONTINUOUS:
                return new Salience16x16ContinuousEncoder();

            case SALIENCE_16X16_WITH_INTERNAL_STATE:
                throw new UnsupportedOperationException(
                        "SALIENCE_16X16_WITH_INTERNAL_STATE "
                        + "still not implemented"
                );

            default:
                throw new IllegalArgumentException(
                        "Representation not suported: "
                        + representation
                );
        }
    }
}