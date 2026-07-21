package state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StateSnapshot {

    private final List<Float> salienceValues;

    private final int salienceRows;
    private final int salienceColumns;

    private float curiosity;
    private float neckYaw;
    private float headPitch;
    private float neckYawSpeed;
    private float headPitchSpeed;
    private int foveaPosition;

    public StateSnapshot(
            List<Float> salienceValues,
            int salienceRows,
            int salienceColumns
    ) {
        if (salienceValues == null) {
            throw new IllegalArgumentException(
                    "salienceValues não pode ser null"
            );
        }

        if (salienceRows <= 0
                || salienceColumns <= 0) {
            throw new IllegalArgumentException(
                    "Dimensões do mapa devem ser positivas"
            );
        }

        int expectedSize =
                salienceRows * salienceColumns;

        if (salienceValues.size() != expectedSize) {
            throw new IllegalArgumentException(
                    "Mapa possui "
                    + salienceValues.size()
                    + " valores, mas eram esperados "
                    + expectedSize
            );
        }

        this.salienceValues =
                Collections.unmodifiableList(
                        new ArrayList<Float>(
                                salienceValues
                        )
                );

        this.salienceRows = salienceRows;
        this.salienceColumns = salienceColumns;
    }

    public static StateSnapshot fromSalienceHistory(
            List<?> salienceHistory,
            int rows,
            int columns
    ) {
        if (salienceHistory == null
                || salienceHistory.isEmpty()) {
            throw new IllegalArgumentException(
                    "Histórico de saliência está vazio"
            );
        }

        Object last =
                salienceHistory.get(
                        salienceHistory.size() - 1
                );

        if (!(last instanceof List<?>)) {
            throw new IllegalArgumentException(
                    "O último item do histórico "
                    + "não é uma lista"
            );
        }

        List<?> rawValues = (List<?>) last;

        List<Float> values =
                new ArrayList<Float>(
                        rawValues.size()
                );

        for (Object value : rawValues) {

            if (!(value instanceof Number)) {
                throw new IllegalArgumentException(
                        "Valor de saliência não numérico: "
                        + value
                );
            }

            values.add(
                    ((Number) value).floatValue()
            );
        }

        return new StateSnapshot(
                values,
                rows,
                columns
        );
    }

    public List<Float> getSalienceValues() {
        return salienceValues;
    }

    public float getSalience(
            int row,
            int column
    ) {
        if (row < 0
                || row >= salienceRows
                || column < 0
                || column >= salienceColumns) {

            throw new IndexOutOfBoundsException(
                    "Posição inválida: "
                    + row
                    + ", "
                    + column
            );
        }

        return salienceValues.get(
                row * salienceColumns + column
        );
    }

    public int getSalienceRows() {
        return salienceRows;
    }

    public int getSalienceColumns() {
        return salienceColumns;
    }

    public float getCuriosity() {
        return curiosity;
    }

    public void setCuriosity(float curiosity) {
        this.curiosity = curiosity;
    }

    public float getNeckYaw() {
        return neckYaw;
    }

    public void setNeckYaw(float neckYaw) {
        this.neckYaw = neckYaw;
    }

    public float getHeadPitch() {
        return headPitch;
    }

    public void setHeadPitch(float headPitch) {
        this.headPitch = headPitch;
    }

    public float getNeckYawSpeed() {
        return neckYawSpeed;
    }

    public void setNeckYawSpeed(
            float neckYawSpeed
    ) {
        this.neckYawSpeed = neckYawSpeed;
    }

    public float getHeadPitchSpeed() {
        return headPitchSpeed;
    }

    public void setHeadPitchSpeed(
            float headPitchSpeed
    ) {
        this.headPitchSpeed = headPitchSpeed;
    }

    public int getFoveaPosition() {
        return foveaPosition;
    }

    public void setFoveaPosition(
            int foveaPosition
    ) {
        this.foveaPosition = foveaPosition;
    }
}