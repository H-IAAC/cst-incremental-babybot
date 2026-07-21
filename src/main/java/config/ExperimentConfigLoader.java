package config;

public final class ExperimentConfigLoader {

    private ExperimentConfigLoader() {
    }

    public static ExperimentConfig fromArgs(String[] args) {
        ExperimentConfig config = new ExperimentConfig();

        for (String arg : args) {
            String[] parts = arg.split("=", 2);

            if (parts.length != 2) {
                continue;
            }

            switch (parts[0]) {
                case "--stage":
                    config.stage = Integer.parseInt(parts[1]);
                    break;
                case "--experiment":
                    config.experiment = Integer.parseInt(parts[1]);
                    break;
                case "--seed":
                    config.seed = Long.parseLong(parts[1]);
                    break;
                case "--resolution":
                    config.inputResolution = Integer.parseInt(parts[1]);
                    break;
                case "--algorithm":
                    config.algorithm =
                            LearningAlgorithm.valueOf(parts[1]);
                    break;
                case "--representation":
                    config.stateRepresentation =
                            StateRepresentation.valueOf(parts[1]);
                    break;
                case "--motivation":
                    config.motivationEnabled =
                            Boolean.parseBoolean(parts[1]);
                    break;
                case "--intrinsic-reward":
                    config.intrinsicRewardEnabled =
                            Boolean.parseBoolean(parts[1]);
                    break;
                case "--top-down":
                    config.topDownEnabled =
                            Boolean.parseBoolean(parts[1]);
                    break;
                case "--freeze":
                    config.freezePolicy =
                            Boolean.parseBoolean(parts[1]);
                    break;
                case "--run-id":
                    config.runId = parts[1];
                    break;
                default:
                    break;
            }
        }

        return config;
    }
}