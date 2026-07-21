package codelets.learner;

import java.util.HashMap;
import java.util.Map;

public class SparseQTable {

    private final Map<String, double[]> table = new HashMap<>();
    private final int numberOfActions;

    public SparseQTable(int numberOfActions) {
        this.numberOfActions = numberOfActions;
    }

    public double[] getOrCreate(String stateKey) {
        return table.computeIfAbsent(
                stateKey,
                key -> new double[numberOfActions]
        );
    }

    public int size() {
        return table.size();
    }
}