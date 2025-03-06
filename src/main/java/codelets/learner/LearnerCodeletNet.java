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
    private boolean debug = false;
    private ArrayList<String> allActionsList;
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
    private String path_model = "/models/pol";
    
    public static QLearning.QLConfiguration MARTA_QL =
                        new QLearning.QLConfiguration(
                                123,    //Random seed
                                500,    //Max step By epoch
                                200, //Max step
                                10000, //Max size of experience replay
                                32,     //size of batches
                                500,    //target update (hard)
                                10,     //num step noop warmup
                                0.01,   //reward scaling
                                0.99,   //gamma
                                1.0,    //td-error clipping
                                0.1f,   //min epsilon
                                1000,   //num step for eps greedy anneal
                                false    //double DQN
                        );

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
            long seed) throws IOException {
        super();
        this.vrep=vrep;

        time_graph = 0;

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
        allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", 
                "aa0", "aa1", "aa2")); //
        // States are 0 1 2 ... 5^256-1
     //   ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(2, 16)-1).mapToObj(String::valueOf).toArray(String[]::new)));
        int salMax = (int)Math.pow(2, 16); // Sal has 65536 values (0 to 65535)
        
       int numStates; 
        experiment_number = oc.vision.getEpoch();
        this.stage = this.oc.vision.getStage();
      

        int maxStep = 500;
                
                
                EnvConstructive<Box, Integer, DiscreteSpace> mdp = new EnvConstructive(maxStep, allActionsList.size());
                DataManager manager = new DataManager(true);
                
                
                
		// learning mode ---> build DQN from scratch
		if (mode.equals("learning") && this.stage == 1 && experiment_number == 1) {
			dql = new QLearningDiscreteDenseRBF(mdp, MARTA_NET, MARTA_QL, manager);
        
		} else if (mode.equals("learning") && (this.stage > 1  || experiment_number > 1)){
                    try {
                        dql = new QLearningDiscreteDenseRBF(mdp, DQNPolicy.load(currentDir+path_model).getNeuralNet(), MARTA_QL,
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
			dql = new QLearningDiscreteDenseRBF(mdp, DQNPolicy.load(currentDir+path_model).getNeuralNet(), MARTA_QL,
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

    public static Object getLast(List list) {
        if (list.isEmpty()) {
                return list.get(list.size()-1);
        }
        return null;
    }

    
    @Override
    public void proc() {
        if(debug) System.out.println("Learner proc");
        
        if(oc.vision.getIValues(5)==0){
        QLStepReturn<Observation> obsStep = null;
        
        Observation lastState; 
        if(!statesList.isEmpty()) {
            lastState = (Observation) statesList.get(statesList.size() - 1);
            if(debug) System.out.println("state list is NOT empty");
        }
        else{
            float[] initialStateArray = new float[272];
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
               obsStep = dql.trainSp(lastState);
                 
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
                            pol.save(currentDir+path_model);
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
    }




		
}
