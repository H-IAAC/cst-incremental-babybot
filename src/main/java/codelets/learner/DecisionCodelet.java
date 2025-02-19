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
private List<QLearningSQL> qTableList;
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
            MO = (MemoryObject) this.getInput("QTABLE");
            qTableList = (List) MO.getI();
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
        QLearningSQL ql = null;
        
        if(motivationMO == null){
            if(sdebug) System.out.println("DECISION -----  motivationMO is null");
                return;
            }
        
        
       if(debug) System.out.println("  Decision proc"); 
       if(qTableList.isEmpty()){
                if(debug) System.out.println("  qtable empty"); 
                return;
       }
        ql = qTableList.get(qTableList.size()-1);
        
        
       
        if(ql==null){
            return;
        }
        
        if(debug) System.out.println("  post first qtable"); 
        
        int state = -1;
        if(!saliencyMap.isEmpty() ) state = getStateFromSalMap();
        if(debug) System.out.println("  Decision state:"+state); 
        String actionToTake = ql.getAction(state);
        
                // Select best action to take

        
        if(actionsList.size() == timeWindow){
                    actionsList.remove(0);
        } 
                
        actionsList.add(actionToTake);
        
        if(allStatesList.size() == timeWindow){
                    allStatesList.remove(0);
        } 
        if(debug)  System.out.println("  Decision actionToTake:"+actionToTake);      
        allStatesList.add(state);
        action_number += 1;
        oc.vision.addAction(actionToTake);
        oc.vision.setLastAction(actionToTake);
    }
	
	

	
        // Discretization
	// Normalize and transform a salience map into one state
		// Normalized values between 0 and 1 can be mapped into 0, 1, 2, 3 or 4
		// Them this values are computed into one respective state
    public int getStateFromSalMap() {
        ArrayList<Float> mean_lastLine = new ArrayList<>();
        for(int i=0; i<16;i++) mean_lastLine.add(0f);
        

			// Getting just the last entry (current sal map)
			lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);

       /* try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        } */
                        
        if (Collections.max(lastLine) == 0) aux_crash += 1;
        else aux_crash = 0; 

        if(action_number > 5 && aux_crash> 5 ){
            crashed = true;
            oc.vision.setIValues(2, 1);
        }

        if (Collections.max(lastLine) > 0){
            ArrayList<Float> MeanValue = new ArrayList<>();
            for(int n = 0;n<4;n++){
            int ni = (int) (n*4);
            int no = (int) (4+n*4);
            for(int m = 0;m<4;m++){    
                int mi = (int) (m*4);
                int mo = (int) (4+m*4);
                for (int y = ni; y < no; y++) {

                    for (int x = mi; x < mo; x++) {
                        int i = (y*16+x);

                        float Fvalue_r = (float) lastLine.get(i);                         
                        MeanValue.add(Fvalue_r);

                    }
                }
                float correct_mean_r = Collections.max(MeanValue);

                mean_lastLine.set(n*4+m, correct_mean_r);
                MeanValue.clear();

                }
            }

        }
        // For normalizing readings between 0 and 1 before transforming to state 
        Float max = Collections.max(mean_lastLine);
        Float min = Collections.min(mean_lastLine);		
        Integer discreteVal = 0;
        Integer stateVal = 0;
        for (int i=0; i < 16; i++) {
            // Normalizing value
            Float normVal; 
            if(max>0) normVal = (mean_lastLine.get(i)-min)/(max-min);
            else normVal = 0f;
            // Getting discrete value
            if (normVal <= 0.5) {
                    discreteVal = 0;
            }
            else if (normVal > 0.5) {
                    discreteVal = 1;
            }

            // Getting state from discrete value
            stateVal += (int) Math.pow(2, i)*discreteVal;
        }
        
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
/*
    private void printToFile(Object object,String filename, int action_num){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        boolean exp_b = false;
        Idea curI = (Idea) motivationMO.get(0);
        Idea surI = (Idea) motivationMO.get(1);
        boolean surB;
        try{
        surB = ((double) surI.getValue() > (double) Collections.max((List) curI.getValue())  && exp_s<MAX_ACTION_NUMBER) || exp_c>MAX_ACTION_NUMBER;
        }
        catch(Exception e){
        surB = true;
        }
        if(num_tables == 1) exp_b = this.experiment_number < MAX_EXPERIMENTS_NUMBER;
        else if(!surB) exp_b = this.exp_c < MAX_EXPERIMENTS_NUMBER;
        else exp_b = this.exp_s < MAX_EXPERIMENTS_NUMBER;
        
        if ( exp_b) {
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+" "+ object+" Exp:"+experiment_number+" ExpC:"+this.exp_c +" ExpS:"+this.exp_s +
                        " Nact:"+action_num+" Type:"+motivationName);

                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

*/
}
