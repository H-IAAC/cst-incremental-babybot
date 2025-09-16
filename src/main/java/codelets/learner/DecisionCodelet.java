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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
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
private List<Observation> allStatesList;
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
private boolean debug = false, sdebug = false;
private int num_tables, aux_crash = 0,  aux_mt = 0, num_pioneer;
private ArrayList<String> executedActions  = new ArrayList<>();
private ArrayList<String> allActionsList;
private Map<String, ArrayList<Integer>> proceduralMemory = new HashMap<String, ArrayList<Integer>>();
private String output, motivation, stringOutput = "";
private ArrayList<Float> lastLine;
private String motivationName;
public DecisionCodelet (OutsideCommunication outc, int tWindow, int sensDim, String mode, String motivation, int num_tables, int num_pioneer) {

    super();
    time_graph = 0;

    this.num_tables = num_tables;
    this.num_pioneer= num_pioneer;
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
        if(debug) System.out.println("  Decision proc"); 
                System.out.println(" Decision proc yawPos: "+yawPos+" headPos: "+headPos);
	/*try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }   */  
        
        if(experiment_number!=oc.vision.getIValues(1)){
            aux_crash = 0;
            aux_mt = 0;
        }
        QLStepReturn<Observation> ql = null;
        
        if(motivationMO == null){
            if(debug) System.out.println("DECISION -----  motivationMO is null");
                return;
            }
        
        
       
       if(qList.isEmpty()){
                if(debug) System.out.println(" Decision qtable empty"); 
                return;
       }
        ql = qList.get(qList.size()-1);
        
        
       
        if(ql==null){
            if(debug) System.out.println(" Decision ql==null"); 
            return;
        }
        
        if(debug) System.out.println("Decision ql not null"); 
        
        Observation state = null;
        if(!saliencyMap.isEmpty() ) state = getStateFromSalMap();
        if(debug) System.out.println("  Decision state:"+state.getData()); 
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
        System.out.println("  \n end decision");
    }
	
	

	

    public Observation getStateFromSalMap() {
        lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);
       
        // Drive Curiosidade
        float driveValueFloat = (float) oc.vision.getFValues(3);
        
        if(debug) System.out.println("  \nDecision driveValueFloat:"+driveValueFloat);      
        // Fovea  pos
        float foveaPositionFloat = (float) oc.vision.getIValues(2);
        float[] lastLineArray = new float[lastLine.size()];
        
        if(debug) System.out.println("  \nDecision foveaPositionFloat:"+foveaPositionFloat);
        
        if(debug) System.out.println("  \nDecision (\"Pioneer1\"):"+oc.vision.getPosition("Pioneer1").length);
        if(debug && num_pioneer>1) System.out.println("  \nDecision (\"Pioneer2\"):"+oc.vision.getPosition("Pioneer2").length);
        if(debug) System.out.println("  \nDecision (\"HeadPitch\"):"+oc.vision.getPosition("HeadPitch").length);
        if(debug) System.out.println("  \nDecision (\"NeckYaw\"):"+oc.vision.getPosition("NeckYaw").length);
        if(debug) System.out.println("  \nDecision (\"Color 0\"):"+oc.vision.getColor(0).length);
        if(debug&& num_pioneer>1) System.out.println("  \nDecision (\"Color 1\"):"+oc.vision.getColor(0).length);
        if(debug) System.out.println("  \nDecision lastLineArray:"+lastLineArray.length);
        
        // Converter ArrayList<Float> para float[]
        
        for (int i = 0; i < lastLine.size(); i++) {
            lastLineArray[i] = lastLine.get(i);
        }

        
        if(Math.abs(oc.HeadPitch_m.getSpeed()) < 0.001 && Math.abs(oc.NeckYaw_m.getSpeed()) < 0.001){
            System.out.println("  \n Motor stopped");
            aux_mt += 1;
        } else{
             aux_mt = 0;
        }
        oc.vision.setFValues(6, Collections.max(lastLine));
        if(Collections.max(lastLine)<0.00001){
         System.out.println("  \n No salMap");
            aux_crash += 1;
        } else{
             aux_crash = 0;
        }
        
        if(aux_mt>20) {
                System.out.println("  \nSync failed 20");
                oc.vision.setCrash(true);
                aux_mt = 0;
                oc.vision.setIValues(5, 1);
            }else{
             oc.vision.setIValues(5, 0);
        }
        
        if(aux_crash > 10){
            System.out.println("  \n no salicence 10");
            oc.vision.setCrash(true);
            aux_crash = 0;
            
          oc.vision.setIValues(5, 1);
            }else{
             oc.vision.setIValues(5, 0);
        }
        float[] stateArray;
        if(num_pioneer>1){
        // Concatenate all elements in a single array
        stateArray = padOrTrimArray(concatenateArrays(
            new float[]{driveValueFloat}, 
            oc.vision.getPosition("Pioneer1"), 
            oc.vision.getPosition("Pioneer2"), 
            oc.vision.getColor(0), 
            oc.vision.getColor(1),
            new float[]{oc.HeadPitch_m.getSpeed()},
            new float[]{oc.NeckYaw_m.getSpeed()},
            new float[]{foveaPositionFloat}, 
            lastLineArray
        ),272);
        }else{
            stateArray = padOrTrimArray(concatenateArrays(
            new float[]{driveValueFloat}, 
            oc.vision.getPosition("Pioneer1"), 
            new float[]{0, 0, 0},
            oc.vision.getColor(0), 
            new float[]{0, 0, 0},
            new float[]{oc.HeadPitch_m.getSpeed()},
            new float[]{oc.NeckYaw_m.getSpeed()},
            new float[]{foveaPositionFloat}, 
            lastLineArray
        ),272);
        }
        // Criar um INDArray a partir do array de floats
        INDArray observationData = Nd4j.create(new float[][]{stateArray});
        System.out.println("  \n return ObservationData");
        // Criar e retornar a Observation
        return new Observation(observationData);
    }

    private static float[] concatenateArrays(float[]... arrays) {
        int totalLength = 0;
        for (float[] array : arrays) {
            totalLength += array.length;
        }

        float[] result = new float[totalLength];
        int currentIndex = 0;

        for (float[] array : arrays) {
            System.arraycopy(array, 0, result, currentIndex, array.length);
            currentIndex += array.length;
        }

        return result;
    }
    
        private float[] padOrTrimArray(float[] array, int targetSize) {
        float[] newArray = new float[targetSize];
        for (int i = 0; i < targetSize; i++) {
            newArray[i] = (i < array.length) ? array[i] : 0.0f; // Fill with zeros if needed
        }
        return newArray;
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
