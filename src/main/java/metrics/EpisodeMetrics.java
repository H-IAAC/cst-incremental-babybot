package metrics;

public class EpisodeMetrics {

    public int episode;
    public int steps;
    public double rawReturn;
    public double rewardPerStep;
    public double meanAbsoluteAngularError;
    public double rmseAngularError;
    public double integratedAbsoluteError;
    public double fovFraction;
    public boolean success;
    public boolean terminalFailure;
    public int actionCount;
    public int targetLossCount;
}