package metrics;

public class RewardBreakdown {

    public double externalTracking;
    public double intrinsicCuriosity;
    public double procedureInsertion;
    public double topDown;
    public double failurePenalty;

    public double rawTotal() {
        return externalTracking
                + intrinsicCuriosity
                + procedureInsertion
                + topDown
                + failurePenalty;
    }
}