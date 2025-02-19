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

public class RewardComputerCodelet extends Codelet 

{

    private int time_graph;

    private static int MAX_ACTION_NUMBER;

    private static int MAX_EXPERIMENTS_NUMBER;

    private QLearningL ql;
    

    private List winnersList;
    private List saliencyMap, curiosityMot, curiosityAct;
    private Idea motivationMO;
    private MemoryObject rewardMO;//, reward_stringMO, action_stringMO;
    private List<String> actionsList;
    
    private OutsideCommunication oc;
    private final int timeWindow;
    private final int sensorDimension;
    
    private float vel = 2f,angle_step;
    
    private double global_reward;
    private int action_number, action_index;
    private int experiment_number;
    private int stage;
    int fovea; 
    private String mode;
    private Integer winnerIndex;
    private Integer winnerFovea = -1, winnerGreen = -1, winnerBlue = -1, winnerRed = -1, winnerDist = -1;
    private int[] posLeft = {0, 4, 8, 12};
    private int[] posRight = {3, 7, 11, 15};
    private int[] posUp = {12, 13, 14, 15};
    private int[] posDown = {0, 1, 2, 3};
    private int[] posCenter = {5, 6, 9, 10};
    
    private int[] fovea0 = {0, 1, 4, 5};
    private int[] fovea1 = {2, 3, 6, 7};
    private int[] fovea2 = {8, 9, 12, 13};
    private int[] fovea3 = {10, 11, 14, 15};
    private float yawPos = 0f, headPos = 0f;   
    private boolean crashed = false, nrewards = true;
    private boolean debug = false, sdebug = false, m_i = true;
    private int num_tables, aux_crash = 0;
    private ArrayList<String> allActionsList;
    private ArrayList<Float> lastLine, lastRed, lastGreen, lastBlue, lastDist;
    private String motivation, stringOutput = "", nameOutput;
    private float  reward_i = 0, lcur_drive=1, cur_drive=1, r_imp=0, g_imp=0, b_imp=0, cur_delta;
    //private Idea ideaMotivation;
    public RewardComputerCodelet (OutsideCommunication outc, int tWindow, int sensDim, String mode, String motivation, 
            String motivationType,String nameOutput, int num_tables) {

    super();
    time_graph = 0;

    global_reward = 0;
    reward_i = 0;
    action_number = 0;

    this.num_tables = num_tables;

    this.motivation = motivation;
    // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
    // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
    // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
    // aa0: focus td color; aa1: focus td depth; aa2: focus td region.
    allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", 
            "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0",  "aa1", "aa2", "am14", "am15", 
            "am16")); //"aa1", "aa2",
    this.oc = outc;   
    MAX_ACTION_NUMBER = oc.vision.getMaxActions();
    MAX_EXPERIMENTS_NUMBER = oc.vision.getMaxEpochs();

    timeWindow = tWindow;
    sensorDimension = sensDim;
    this.mode = mode;
    experiment_number = oc.vision.getEpoch();

        
                /* try {
                Thread.sleep(200);
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }*/

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
        MO = (MemoryObject) this.getInput("WINNERS");
        winnersList = (List) MO.getI();



     

        if(this.motivation.equals("drives")){
            MemoryContainer MC = (MemoryContainer) this.getInput("MOTIVATION");
            motivationMO = (Idea) MC.getI();
            
        }
        rewardMO = (MemoryObject) this.getOutput("REWARDS");
        //reward_stringMO = (MemoryObject) this.getOutput(this.nameOutput);
        //action_stringMO = (MemoryObject) this.getOutput("ACTION_STRING_OUTPUT");


        MO = (MemoryObject) this.getInput("ACTIONS");
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
    
      public static double calculateMean(ArrayList<Double> list) {
            if (list == null) {
                return 0; // Return 0 if the list is empty or handle it as required
            }
            
            if (list.isEmpty()) {
                return 0; // Return 0 if the list is empty or handle it as required
            }

            double sum = 0;
            for (double value : list) {
                sum += value;
            }

            return sum / list.size();
        } 

    // Main Codelet function, to be implemented in each subclass.
    @Override
    public void proc() {
        
        crashed = false;
        reward_i=0;
        

        
        try {
            yawPos = oc.NeckYaw_m.getSpeed();
            headPos = oc.HeadPitch_m.getSpeed(); 
                //System.out.println("yawPos: "+yawPos+" headPos: "+headPos);
        } catch (Exception e) {
             if(debug) System.out.println("getSpeed null ");
            return;
        }
      /*  try {
        Thread.sleep(50);
        } catch (Exception e) {
        Thread.currentThread().interrupt();
        }       */

        if(debug) System.out.println(
                " motivationValues - C: "+motivationMO.getValue());

        if(motivationMO == null){
              if(debug) System.out.println("Rewardcomputer motivationMO is null");
            return;
        }
        if(actionsList.isEmpty()){
            System.out.println("actionsList.isEmpty()");
            return;
        } 
        
        
        if (!saliencyMap.isEmpty() && !winnersList.isEmpty()) {

            Winner lastWinner = (Winner) winnersList.get(winnersList.size() - 1);
            winnerIndex = lastWinner.featureJ;

                        // Find reward of the current state, given previous  winner 

            

                        if(oc.vision.getNextActR()){
           if(nrewards){ Double reward = 1d;
            reward_i += reward;
           }
                        // Gets last action taken
            String lastAction = actionsList.get(actionsList.size() - 1);
            action_number += 1;            

                    // Motivation

            int i = 0;
            
            if(this.motivation.equals("drives")){                        
                //nameMotivation = motivationName;
               
                    cur_drive = oc.vision.getFValues(3);
                    cur_delta = lcur_drive-cur_drive;
                    cur_delta = Math.round(cur_delta * 10) / 10.0f;
                    oc.vision.setFValues(4,cur_delta);
                       
                    if(cur_drive<0) cur_drive = (float) 0.0;
                    else if(cur_drive>1) cur_drive = (float) 1.0;
                    
                    
                    float cur_f = 10;

                    
                    if(cur_delta!=0){
                        if(cur_drive==0.0)  reward_i += 1;
                        if(cur_drive>0.0 &&  cur_drive<=0.2)  reward_i += 1*0.5;
                        if(cur_drive==1.0)  reward_i -= 1;
                        if(cur_drive>=0.8 &&  cur_drive<1.0)  reward_i -= 1*0.5;
                    }
                    // cur_f = cur_delta*cur_delta;
                   // if(cur_delta!=0)  reward_i += 1*cur_delta;
                    //else if(cur_drive>lcur_drive) 
                    reward_i += 1*cur_delta;
                    
                    
                    
                
                lcur_drive=cur_drive;
                
 if(sdebug) System.out.println("~~ REWARD - QTables:"+num_tables+
                        
                        " CurV:"+cur_drive+" LCurV:"+lcur_drive+" dCurV:"+cur_delta
                        +" Ri:"+reward_i);
                
                    
                    

                    oc.vision.setNextActR(false);
            }
            

             int winner =   getStateFromSalMap();

//    
      if(sdebug)    System.out.println("~End~ REWARD -  QTables:"+num_tables+" Exp: "+ experiment_number +
                    " - Act: "+lastAction + " - N_act: "+action_number+" - Winner: "+winnerIndex+
                    " - W_Fovea: "+winnerFovea+"\n Type:"+
                        " CurV:"+cur_drive+" dCurV:"+cur_delta+" Ri:"+reward_i);
            if (lastAction.equals("am1")) {
                yawPos = yawPos-angle_step;
                         //neckMotorMO.setI(yawPos);
                if(winnerFovea !=-1 && IntStream.of(posLeft).anyMatch(x -> x == winnerFovea) && stage > 1){
                    if(nrewards) reward_i += 1;
                }
            }

            else if (lastAction.equals("am2")) {
                yawPos = yawPos+angle_step;
                         //neckMotorMO.setI(yawPos);
                if(winnerFovea !=-1 && IntStream.of(posRight).anyMatch(x -> x == winnerFovea)){
                    if(nrewards) reward_i += 1;
                    }
            }
            else if (lastAction.equals("am3")) {
                    headPos = headPos-angle_step;
                     // headMotorMO.setI(headPos);
                    if(winnerFovea !=-1 && IntStream.of(posUp).anyMatch(x -> x == winnerFovea)) {
                        if(nrewards) reward_i += 1;
                    } 
            }
            else if (lastAction.equals("am4")) {
                    headPos = headPos+angle_step;
                    //headMotorMO.setI(headPos);
                    if(winnerFovea !=-1 && IntStream.of(posDown).anyMatch(x -> x == winnerFovea)){
                        if(nrewards) reward_i += 1;
                    } 
            }
            else if (lastAction.equals("am5")) {
                fovea = 0;
                if(winnerFovea !=-1 && IntStream.of(fovea0).anyMatch(x -> x == winnerFovea)){
                        if(nrewards) reward_i += 1;
                    } 
            }
            else if (lastAction.equals("am6")) {
                fovea = 1;
                if(winnerFovea !=-1 && IntStream.of(fovea1).anyMatch(x -> x == winnerFovea)){
                        if(nrewards) reward_i += 1;
                    } 
            }
            else if (lastAction.equals("am7")) {
                fovea = 2;
                if(winnerFovea !=-1 && IntStream.of(fovea2).anyMatch(x -> x == winnerFovea)){
                        if(nrewards) reward_i += 1;
                    } 
            }
            else if (lastAction.equals("am8")) {
                fovea = 3;
                if(winnerFovea !=-1 && IntStream.of(fovea3).anyMatch(x -> x == winnerFovea)){
                        if(nrewards) reward_i += 1;
                    } 
            }
            else if (lastAction.equals("am9")) {
                fovea = 4;
                if(winnerFovea !=-1 && IntStream.of(posCenter).anyMatch(x -> x == winnerFovea)){
                        if(nrewards) reward_i += 1;
                    } 
            }

            // just Stage 3
             else if (lastAction.equals("am10") && this.stage == 3) {
                if(fovea == 0 || fovea == 2){
                    yawPos = yawPos-angle_step;
                   //  neckMotorMO.setI(yawPos);
                }
                else if(fovea == 1 || fovea == 3){
                    yawPos = yawPos+angle_step;
                   //  neckMotorMO.setI(yawPos);
                }
                if(nrewards) reward_i += 1;
             }
             else if (lastAction.equals("am11") && this.stage == 3) {
                if(fovea == 0 || fovea == 2){
                    yawPos = yawPos+angle_step;
                   //  neckMotorMO.setI(yawPos);
                }
                else if(fovea == 1 || fovea == 3){
                    yawPos = yawPos-angle_step;
                   //  neckMotorMO.setI(yawPos);
                }
                if(nrewards) reward_i += 1;
             }
             else if (lastAction.equals("am12") && this.stage == 3) {
                if(fovea == 3 || fovea == 2){
                    headPos = headPos-angle_step;
                   //  headMotorMO.setI(headPos);
                }
                else if(fovea == 1 || fovea == 0){
                    headPos = headPos+angle_step;
                   //  headMotorMO.setI(headPos);
                }
                if(nrewards) reward_i += 1;
             }
             else if (lastAction.equals("am13") && this.stage == 3) {
                if(fovea == 3 || fovea == 2){
                    headPos = headPos+angle_step;
                   //  headMotorMO.setI(headPos);
                }
                else if(fovea == 1 || fovea == 0){
                    headPos = headPos-angle_step;
                   // headMotorMO.setI(headPos);
                }
                if(nrewards) reward_i += 1;
             }


        List rewardsList = (List) rewardMO.getI();        

        if(rewardsList.size() == timeWindow){
            rewardsList.remove(0);
        } 
        
                if(this.oc.vision.endEpochR()){
            // System.out.println("MORREU");
                    reward_i -= 100;
                    lcur_drive=0;
                    oc.vision.setFValues(4, cur_delta);
            }
                //Math.pow(Math.E,*0.05/350)
                reward_i = Math.round(reward_i * 10) / 10.0f;

        //reward_i += 0.00006*oc.vision.getnAct()*oc.vision.getEpoch();
        
        global_reward =  oc.vision.getFValues(0) +reward_i;
        if(global_reward < -120) global_reward = -120;
        if(reward_i < -120) reward_i = -120;
        rewardsList.add(global_reward);
        
            oc.vision.setFValues(0, (float) global_reward);
            oc.vision.setFValues(5, reward_i);

        
       
        
        if(sdebug) System.out.println("~End~ REWARD -  QTables:"+num_tables+" Exp: "+ experiment_number +
                    " - N_act: "+action_number+ " - Winner: "+winnerIndex+
                    " - W_Fovea: "+winnerFovea+
                        " CurV:"+cur_drive+" dCurV:"+cur_delta+" Ri:"+reward_i);
        
                        }
                       
        }

    }


    // Discretization
    // Normalize and transform a salience map into one state
    // Normalized values between 0 and 1 can be mapped into 0, 1, 2, 3 or 4
    // Them this values are computed into one respective state
    public Integer getStateFromSalMap() {


        // Getting just the last entry (current sal map)
        lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);

        /*if (Collections.max(lastLine) == 0) aux_crash += 1;
        else aux_crash = 0; 

        if(action_number > 10 && aux_crash> 5 ){
            crashed = true;
        }
        */

        if (Collections.max(lastLine) > 0){
          for(int n = 0;n<4;n++){
            int ni = (int) (n*4);
            int no = (int) (4+n*4);
            for(int m = 0;m<4;m++){    
                int mi = (int) (m*4);
                int mo = (int) (4+m*4);
                for (int y = ni; y < no; y++) {

                    for (int x = mi; x < mo; x++) {
                        int i = (y*16+x);
                        if(i == winnerIndex) winnerFovea = n*4+m;
                    }
                }
            }
          }
        }
        return winnerFovea;
    }
		
    /**
     *
     * @param list
     * @return
     */
    public static float calculateMeanf(ArrayList<Float> list) {
        if (list.isEmpty()) {
            return 0; // Return 0 if the list is empty or handle it as required
        }

        float sum = 0;
        for (float value : list) {
            sum += value;
        }

        return sum / list.size();
    }

    private void printToFile(Object object,String filename, int action_num){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        boolean exp_b = false;
    
         exp_b = this.experiment_number < MAX_EXPERIMENTS_NUMBER;
        
        if ( exp_b) {
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+" "+ object+" QTables:"+num_tables+
                        " Exp:"+experiment_number+
                        " Nact:"+action_num+
                        " CurV:"+cur_drive+" dCurV:"+cur_delta+" Ri:"+reward_i);
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
      
    }
		

}
