package codelets.learner;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;
import java.util.Dictionary;

import attention.Winner;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.learning.QLearning;
import br.unicamp.cst.representation.idea.Idea;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import outsideCommunication.OutsideCommunication;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.deeplearning4j.rl4j.learning.sync.qlearning.discrete.QLearningDiscreteConstructive.QLStepReturn;
import org.deeplearning4j.rl4j.observation.Observation;
/**
 * @author L. L. Rossi (leolellisr)
 * Obs: This class represents the implementations present in the proposed scheme for: 
 * DiscretizationCodelet; adaptation; accommodation and assimilation. 
 * Procedural Memory is represented by QTable.
 */

public class DecisionCodelet extends Codelet {
    
private int time_graph;
private static final float CRASH_TRESHOLD = 0.28f;
private static int MAX_ACTION_NUMBER;

private static int MAX_EXPERIMENTS_NUMBER;
private QLearningSQL ql;
private Idea motivationMO;
private MemoryObject motorActionMO, reward_stringMO, action_stringMO;
private MemoryObject neckMotorMO;
private MemoryObject headMotorMO;
private List<String> actionsList;
private List<Integer> allStatesList;
private List<QLStepReturn> qList;
private List<Double>  rewardList;
private OutsideCommunication oc;
private final int timeWindow;
private final int sensorDimension;
private List saliencyMap;
private float vel = 2f,angle_step;

private int curiosity_lv, red_c, green_c, blue_c;
private int action_index;
private int experiment_number;
private int stage, action_number=0;
int fovea; 
private String mode;


private float yawPos = 0f, headPos = 0f;   
private boolean crashed = false;
private boolean debug = true, sdebug = false;
private int num_tables, aux_crash = 0;
private ArrayList<String> executedActions  = new ArrayList<>();
private ArrayList<String> allActionsList;
private Map<String, ArrayList<Integer>> proceduralMemory = new HashMap<String, ArrayList<Integer>>();
private String output, motivation, stringOutput = "";
private ArrayList<Float> lastLine;
private String motivationName;
public DecisionCodelet (OutsideCommunication outc, int tWindow, int sensDim, String mode, String motivation, int num_tables) {

    super();
    time_graph = 0;

    this.num_tables = num_tables;
    this.motivation = motivation;
    // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
    // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
    // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
    // aa0: focus td color; aa1: focus td depth; aa2: focus td region.
    allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12",
            "am13", "aa0", "aa1", "aa2", "am14", "am15", "am16")); //"aa1", "aa2", 
    // States are 0 1 2 ... 5^256-1
    //ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(2, 16)-1).mapToObj(String::valueOf).toArray(String[]::new)));

    oc = outc;

    this.stage = this.oc.vision.getStage();


    angle_step = 0.1f;
    experiment_number = oc.vision.getEpoch();

    timeWindow = tWindow;
    sensorDimension = sensDim;
    this.mode = mode;
    MAX_ACTION_NUMBER = oc.vision.getMaxActions();
    MAX_EXPERIMENTS_NUMBER = oc.vision.getMaxEpochs();
    
              /*try {
                Thread.sleep(200);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
   */
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

    MO = (MemoryObject) this.getInput("REWARDS");
            rewardList = (List) MO.getI();
            MO = (MemoryObject) this.getInput("DQN");
            qList = (List) MO.getI();
    MO = (MemoryObject) this.getOutput("STATES");
        allStatesList = (List) MO.getI();

        MO = (MemoryObject) this.getOutput("ACTIONS");
        actionsList = (List) MO.getI();

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

    // Main Codelet function, to be implemented in each subclass.
    @Override
    public void proc() {
                //System.out.println("yawPos: "+yawPos+" headPos: "+headPos);
	/*try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }   */  
        QLStepReturn<Observation> ql = null;
        
        if(motivationMO == null){
            if(sdebug) System.out.println("DECISION -----  motivationMO is null");
                return;
            }
        
        
       if(debug) System.out.println("  Decision proc"); 
       if(qList.isEmpty()){
                if(debug) System.out.println("  qtable empty"); 
                return;
       }
        ql = qList.get(qList.size()-1);
        
        
       
        if(ql==null){
            if(debug) System.out.println("  ql==null"); 
            return;
        }
        
        if(debug) System.out.println("  post first DQN"); 
        
        int state = -1;
        if(!saliencyMap.isEmpty() ) state = getStateFromSalMap();
        if(debug) System.out.println("  Decision state:"+state); 
        int actionToTake = ql.getLastAction();
                // Select best action to take

        
        if(actionsList.size() == timeWindow){
                    actionsList.remove(0);
        } 
                
        actionsList.add(String.valueOf(actionToTake));
        
        if(allStatesList.size() == timeWindow){
                    allStatesList.remove(0);
        } 
        if(debug)  System.out.println("  Decision actionToTake:"+actionToTake);      
        allStatesList.add(state);
        action_number += 1;
        oc.vision.addAction(String.valueOf(actionToTake));
        oc.vision.setLastAction(String.valueOf(actionToTake));
    }
	
	

	

    public Observation getStateFromSalMap() {
        ArrayList<Float> mean_lastLine = new ArrayList<>();
        for(int i=0; i<16;i++) mean_lastLine.add(0f);
        

			// Getting just the last entry (current sal map)
			lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);

        
        return stateVal;
    }
		
	
    public static float calculateMean(ArrayList<Float> list) {
        if (list.isEmpty()) {
            return 0; // Return 0 if the list is empty or handle it as required
        }

        float sum = 0;
        for (float value : list) {
            sum += value;
        }

        return sum / list.size();
    }

}
