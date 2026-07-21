package codelets.learner;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
//import br.unicamp.cst.learning.QLearning;
import br.unicamp.cst.representation.idea.Idea;
import br.unicamp.cst.support.CodeletsProfiler;
import config.ActionSpaceFactory;
import coppelia.remoteApi;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import outsideCommunication.OutsideCommunication;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;
import org.deeplearning4j.rl4j.mdp.EnvConstructive;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDenseRBF;
import org.deeplearning4j.rl4j.space.Box;
import org.deeplearning4j.rl4j.space.DiscreteSpace;
import org.deeplearning4j.rl4j.util.DataManager;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDense;
import org.deeplearning4j.rl4j.learning.sync.qlearning.QLearning;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteConstructive.QLStepReturn;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteDenseRBF;
import org.deeplearning4j.rl4j.network.dqn.DQNFactory;
import org.deeplearning4j.rl4j.network.dqn.DQNFactoryStdDense;
import org.deeplearning4j.rl4j.network.dqn.IDQN;
import org.deeplearning4j.rl4j.observation.Observation;
import org.nd4j.linalg.factory.Nd4j;
import config.ExperimentConfig;
import config.LearningAlgorithm;
import state.StateEncoderFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import metrics.TimingRegistry;
import outsideCommunication.VisionVrep;
import state.StateEncoder;
import state.StateEncoderFactory;

/**
 * @author L. L. Rossi (leolellisr)
 * Obs: This class represents the implementations present in the proposed scheme for: 
 * DiscretizationCodelet; adaptation; accommodation and assimilation. 
 * Procedural Memory is represented by QTable.
 */

public class LearnerCodeletNet extends Codelet 

{

    private int time_graph;


    private static int MAX_ACTION_NUMBER;

    private static int MAX_EXPERIMENTS_NUMBER;
    private QLearningSQL ql;
    

    private List saliencyMap;
    private List statesList;
    private Idea motivationMO;
    private List<String> actionsList;
    private List<QLStepReturn<Observation>> qList;
    private List<Double>  rewardsList;
    private OutsideCommunication oc;
    private final int timeWindow;
    
    
    private double global_reward;
    private int action_number, num_tables;
    private int experiment_number,exp_s, exp_c;;
    private int stage,convergenceCounter=0;
    private String mode;
    private boolean debug = true;
    private List<String> allActionsList;
    private remoteApi vrep;
    private final int clientID;
    private String output, motivation, nameMotivation, motivationType, lastAction = "am0";
    private  boolean end_all;
    
    
    private final int numSalValues = 65536;  // Sal has 2^16 values

    private final double Q_CHANGE_THRESHOLD = 0.001;
    private final int CONVERGENCE_EPOCHS = 100;
    private List<Integer> allStatesList = new ArrayList<>();
    private long seed;
    private QLearningDiscreteDenseRBF<Box> dql;
    public static int policy_step = 1;
    private String currentDir = System.getProperty("user.dir");
    private final ExperimentConfig config;
    private Path path_model;
     private final TimingRegistry timingRegistry;
     private long experimentStartTime;
    

    public static DQNFactoryStdDenseRBF.Configuration MARTA_NET =
                        new DQNFactoryStdDenseRBF.Configuration(
                                2,         //number of layers
                                1,        //number of hidden nodes
                                0.001,     //learning rate
                                null,       //l2 regularization
                                null
                        );
    
    //private int past_exp;
    //private Idea ideaMotivation;
    public LearnerCodeletNet (remoteApi vrep, int clientid, OutsideCommunication outc, int tWindow, 
            String mode, String motivation,  String motivationType,  String output, int num_tables, 
            long seed, ExperimentConfig config, TimingRegistry timingRegistry) throws IOException {
        super();
        
         this.timingRegistry = timingRegistry;
        path_model = Paths.get(config.modelOutputPath);
        Files.createDirectories(path_model.getParent());
    this.experimentStartTime =  System.currentTimeMillis();
        this.vrep=vrep;
this.config = config;
        time_graph = 0;
QLearning.QLConfiguration qlConfiguration =
        createQLConfiguration();
        action_number = 0;
        this.seed = seed;
        this.oc = outc;
        clientID = clientid;
        this.output = output;
        this.motivation = motivation;
        // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
        // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
        // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
        // aa0: focus td color; aa1: focus td depth; aa2: focus td region.
        //allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", 
        //        "aa0", "aa1", "aa2")); //
this.allActionsList =
        ActionSpaceFactory.create(config.actionSet);        
// States are 0 1 2 ... 5^256-1
     //   ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(2, 16)-1).mapToObj(String::valueOf).toArray(String[]::new)));
        int salMax = (int)Math.pow(2, 16); // Sal has 65536 values (0 to 65535)
        
       int numStates; 
        experiment_number = oc.vision.getEpoch();
        this.stage = this.oc.vision.getStage();
      

        int maxStep = 500;
                
                
                EnvConstructive<Box, Integer, DiscreteSpace> mdp = new EnvConstructive(maxStep, allActionsList.size());
                DataManager manager = new DataManager(true);
                
                System.out.println("currentDir: "+currentDir);
                        System.out.println("path_model: "+path_model);
                
		// learning mode ---> build DQN from scratch
		if (mode.equals("learning")  && experiment_number == 1) {
			dql = new QLearningDiscreteDenseRBF(mdp, MARTA_NET, qlConfiguration, manager);
        
		} else if (mode.equals("learning") && (this.stage > 1  || experiment_number > 1)){
                    try {
                        System.out.println("currentDir: "+currentDir);
                        System.out.println("path_model: "+path_model);
                        dql = new QLearningDiscreteDenseRBF(mdp, DQNPolicy.load(currentDir+"/"+path_model).getNeuralNet(), qlConfiguration,
                    manager);
			}
                    catch (Exception e) {
                            System.out.println("ERROR "+e+" LOADING PREVIOUS MODEL");
                            System.exit(1);
			}
                }
                
		// exploring mode ---> reloads Qtable 
		else {
                    try {
			dql = new QLearningDiscreteDenseRBF(mdp, DQNPolicy.load(currentDir+"/"+path_model).getNeuralNet(), qlConfiguration,
                    manager);
                    }
                    catch (Exception e) {
                        System.out.println("ERROR LOADING PREVIOUS MODEL");
			System.exit(1);
                    }
		}
                
if(debug) System.out.println("init learner");
        timeWindow = tWindow;
        this.mode = mode;
        
        MAX_ACTION_NUMBER = oc.vision.getMaxActions();
        MAX_EXPERIMENTS_NUMBER = oc.vision.getMaxEpochs();
    }

    QLearning.QLConfiguration createQLConfiguration() {
        
        int replayCapacity =
                config.algorithm == LearningAlgorithm.ONLINE_Q_NETWORK
                        ? 1
                        : 10000;

        int batchSize =
                config.algorithm == LearningAlgorithm.ONLINE_Q_NETWORK
                        ? 1
                        : 32;

        int targetUpdate =
                config.algorithm == LearningAlgorithm.ONLINE_Q_NETWORK
                        ? 1
                        : 500;

        return new QLearning.QLConfiguration(
                (int) config.seed,
                config.maxSteps,
                200,
                replayCapacity,
                batchSize,
                targetUpdate,
                10,
                0.01,
                0.99,
                1.0,
                (float) config.epsilonEnd,
                config.epsilonDecaySteps,
                false
        );
    }
    
    // This method is used in every Codelet to capture input, broadcast 
    // and output MemoryObjects which shall be used in the proc() method. 
    // This abstract method must be implemented by the user. 
    // Here, the user must get the inputs and outputs it needs to perform proc.
    @Override
    public void accessMemoryObjects() {

        MemoryObject MO;
        MO = (MemoryObject) this.getInput("SALIENCY_MAP");
        saliencyMap = (List) MO.getI();

        if(this.motivation.equals("drives")){
            MemoryContainer MC = (MemoryContainer) this.getInput("MOTIVATION");
            motivationMO = (Idea) MC.getI();
        }               

        if(num_tables==1){
                MO = (MemoryObject) this.getInput("REWARDS");
                rewardsList = (List) MO.getI();
            }
        MO = (MemoryObject) this.getInput("ACTIONS");
        actionsList = (List) MO.getI();

        MO = (MemoryObject) this.getInput("STATES");
        statesList = (List) MO.getI();

        MO = (MemoryObject) this.getOutput(output);
        qList = (List) MO.getI();


    }

    // This abstract method must be implemented by the user. 
    // Here, the user must calculate the activation of the codelet
    // before it does what it is supposed to do in proc();

    @Override
    public void calculateActivation() {
            // TODO Auto-generated method stub

    }

    public static Object getLast(List<?> list) {

        if (list == null || list.isEmpty()) {
            return null;
        }

        return list.get(list.size() - 1);
    }

    
    @Override
    public void proc() {
        
        try(
                
                TimingRegistry.TimerContext ignored =
                timingRegistry.start(
                        getClass().getSimpleName()
                );
                
                ){
long startNs = System.nanoTime();
        if(debug) System.out.println("Learner proc");
        
        if(oc.vision.getIValues(5)==0){
        QLStepReturn<Observation> obsStep = null;
        
        Observation lastState; 
        if(!statesList.isEmpty()) {
            lastState = (Observation) statesList.get(statesList.size() - 1);
            if(debug) System.out.println("state list is NOT empty");
        }
        else{
            int inputSize =
                    StateEncoderFactory
                            .create(config.stateRepresentation)
                            .inputSize();

            StateEncoder encoder =
                    StateEncoderFactory.create(
                            config.stateRepresentation
                    );

            float[] initialStateArray =
                    new float[encoder.inputSize()];
            lastState = new Observation(Nd4j.create(new float[][]{initialStateArray}));

            if(debug) System.out.println("state list is empty");
        }
        if (!oc.vision.endEpochR()) {
            

            try {
               if(debug) System.out.println("Learner try");
                
                if(mode.equals("learning")){
                float reward = oc.vision.getFValues(0) ;
            
                 dql.setReward(reward);
                }
                if (!config.freezePolicy) {
                    obsStep = dql.trainSp(lastState);
                }
                // Update Q-values and track Q-value changes
                
                

            } catch (Exception e) {
                System.out.println("No state to update: " + e.getMessage());
            }

        }

           if(debug) System.out.println("trainSp");

            if (mode.equals("learning") && oc.vision.endEpochR()) {
                dql.postEpoch();
                        dql.incrementEpoch();
                        System.out.println("end epoch before save model");
                        DQNPolicy<Box> pol = dql.getPolicy();
                        try {
                            pol.save(path_model.toString());
                            if (experiment_number > MAX_EXPERIMENTS_NUMBER) {

                                System.exit(0);
                            }
                        }
                        catch (Exception e) {
                            System.out.println("ERROR "+e+" SAVING MODEL");
                            System.exit(1);
			}
                        System.out.println("end epoch after save model");
            }
            
            if(debug) System.out.println("post step");
        
        if(debug) System.out.println("obsStep:"+obsStep);
        if(qList.size() == timeWindow){
                qList.remove(0);
            }
        qList.add(obsStep);
        }
        timingRegistry.record(
        getClass().getSimpleName(),
        System.nanoTime() - startNs
);
        
    }}




		
}
