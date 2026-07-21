package metrics;

public class StepMetrics {

    public String runId;
    public long seed;
    public int stage;
    public int experiment;
    public int episode;
    public int step;

    public String algorithm;
    public String representation;
    public String action;

    public double rawReward;
    public double scaledReward;
    public double externalReward;
    public double intrinsicReward;

    public boolean targetVisible;
    public boolean targetInFov;

    public double yawErrorDeg;
    public double pitchErrorDeg;
    public double absoluteAngularErrorDeg;

    public long decisionTimeNs;
    public long learningTimeNs;
}