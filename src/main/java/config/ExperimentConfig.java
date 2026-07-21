package config;

public class ExperimentConfig {

    public String runId = "run";
    public long seed = 91011L;

    public int stage = 1;
    public int experiment = 1;
    public int inputResolution = 256;
    public int maxEpisodes = 300;
    public int evaluationEpisodes = 50;
    public int maxSteps = 500;
    public int numberOfPioneers = 1;
public double rewardScale = 0.1;
    public LearningAlgorithm algorithm = LearningAlgorithm.DQN;
    public StateRepresentation stateRepresentation =
            StateRepresentation.SALIENCE_16X16_CONTINUOUS;

    public ActionSet actionSet = ActionSet.FULL;

    public boolean training = true;
    public boolean motivationEnabled = false;
    public boolean intrinsicRewardEnabled = false;
    public boolean topDownEnabled = false;
    public boolean proceduralMemoryEnabled = true;
    public boolean procedureRecallEnabled = true;
    public boolean freezePolicy = false;

    public TransferMode transferMode = TransferMode.FROM_SCRATCH;

    public double epsilonStart = 1.0;
    public double epsilonEnd = 0.1;
    public int epsilonDecaySteps = 1000;

    public String resultDirectory = "profile";
    public String modelInputPath = "";
    public String modelOutputPath = "models/pol";
}