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

public class ActionExecCodelet extends Codelet 

{

    private int time_graph;
    private static final float CRASH_TRESHOLD = 0.28f;

    private static int MAX_ACTION_NUMBER;

    private static int MAX_EXPERIMENTS_NUMBER;

    private QLearningL ql;
    


    private Idea motivationMO;
    private MemoryObject motorActionMO;
    private MemoryObject neckMotorMO;
    private MemoryObject headMotorMO;
    private MemoryObject desFC, desFD, desFR;
    private List<String> actionsList;
  
    private OutsideCommunication oc;
    private final int timeWindow;
    private final int sensorDimension;
    
    private float vel = 2f,angle_step=0.1f;
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
   
    private int action_index;
    private int experiment_number, exp_s, exp_c;
    private int stage, fovea;
    
    private String mode;

    
    private float yawPos = 0f, headPos = 0f;   
    private boolean crashed = false;
    private boolean debug = false, sdebug = false;
    private int aux_crash = 0;
    private ArrayList<String> executedActions  = new ArrayList<>();
    private ArrayList<String> allActionsList;
    private ArrayList<Float> lastLine, lastRed, lastGreen, lastBlue, lastDist;
    private List winnersList, colorReadings, redReadings, greenReadings, blueReadings, distReadings;
    private List saliencyMap;
    private int aux_resetr=-1,aux_reset=-1, curiosity_lv, red_c, green_c, blue_c, cur_a=0, sur_a=0,num_tables;
    private  String nameMotivation;
    public ActionExecCodelet (OutsideCommunication outc, String mode, int tWindow, int sensDimn, int num_tables) {

        super();
        time_graph = 0;
        sensorDimension = sensDimn;
        this.num_tables=num_tables;

        // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
        // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
        // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
        // aa0: focus td color; aa1: focus td depth; aa2: focus td region.
        allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11",
                "am12", "am13", "aa0", "am14", "am15", "am16")); //"aa1", "aa2", 
        // States are 0 1 2 ... 5^256-1

        oc = outc;
        yawPos = oc.NeckYaw_m.getSpeed();
        headPos = oc.HeadPitch_m.getSpeed();                
        this.stage = this.oc.vision.getStage();

        this.mode = mode;

        timeWindow = tWindow;
        curiosity_lv = 0;
        red_c = 0;
        green_c = 0;
        blue_c =0;
        MAX_ACTION_NUMBER = oc.vision.getMaxActions();
        MAX_EXPERIMENTS_NUMBER = oc.vision.getMaxEpochs();
        experiment_number = oc.vision.getEpoch();

        
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
        MemoryContainer MC = (MemoryContainer) this.getInput("MOTIVATION");
            motivationMO = (Idea) MC.getI();
       
        MO = (MemoryObject) this.getInput("VISION_COLOR_FM");
        colorReadings = (List) MO.getI();
/*                MO = (MemoryObject) this.getInput("VISION_GREEN_FM");
        greenReadings = (List) MO.getI();
        MO = (MemoryObject) this.getInput("VISION_BLUE_FM");
        blueReadings = (List) MO.getI();*/
        //MO = (MemoryObject) this.getInput("DEPTH_FM");
        distReadings = (List) MO.getI();

        MO = (MemoryObject) this.getInput("ACTIONS");
        actionsList = (List) MO.getI();

        motorActionMO = (MemoryObject) this.getOutput("MOTOR");
        neckMotorMO = (MemoryObject) this.getOutput("NECK_YAW");
        headMotorMO = (MemoryObject) this.getOutput("HEAD_PITCH");

        desFC = (MemoryObject) this.getOutput("DESFEAT_C");
        desFD = (MemoryObject) this.getOutput("DESFEAT_D");
        desFR = (MemoryObject) this.getOutput("DESFEAT_R");
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
        crashed = false;
        yawPos = oc.NeckYaw_m.getSpeed();
        headPos = oc.HeadPitch_m.getSpeed(); 
        //System.out.println("yawPos: "+yawPos+" headPos: "+headPos);
        /*try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }    */   
        
        if(actionsList.size()<1 || winnersList.size()<1 ){
            if(debug){
            System.out.println("ACT_EXEC----- actionsList.size():"+actionsList.size());

            System.out.println("ACT_EXEC----- winnersList.size():"+winnersList.size());
            }
            return;
        }
        String actionToTake = actionsList.get(actionsList.size() - 1);
         if(sdebug) System.out.println("ACT_EXEC -----  Exp: "+ experiment_number 
                +" ----- Act: "+ actionToTake+" ----- N_act: "+oc.vision.getIValues(4)+" Curiosity_lv: "
                +curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c);
        
        Winner lastWinner = (Winner) winnersList.get(winnersList.size() - 1);
        winnerIndex = lastWinner.featureJ;
       
        
        //oc.vision.setIValues(4, (int) (oc.vision.getIValues(4)+1));
        
        
        if(!executedActions.contains(actionToTake)) executedActions.add(actionToTake);
            
            // AM1 - Move neck to left
            if (actionToTake.equals("am1")) {
                yawPos = yawPos-angle_step;
                neckMotorMO.setI(yawPos);

            }
            
            // AM2 - Move neck to right
            else if (actionToTake.equals("am2")) {
                yawPos = yawPos+angle_step;
                neckMotorMO.setI(yawPos);

            }
            
            // AM3 - Move head up
            else if (actionToTake.equals("am3")) {
                headPos = headPos-angle_step;
                headMotorMO.setI(headPos);

            }
            
            // AM4 - Move head down
            else if (actionToTake.equals("am4")) {
                headPos = headPos+angle_step;
                headMotorMO.setI(headPos);

            }
            
            // AM5 - Fovea 0
            else if (actionToTake.equals("am5")) {
                fovea = 0;

            }
            
            // AM6 - Fovea 1
            else if (actionToTake.equals("am6")) {
                fovea = 1;

            }
            
            // AM7 - Fovea 2
            else if (actionToTake.equals("am7")) {
                fovea = 2;

            }
            
            // AM8 - Fovea 3
            else if (actionToTake.equals("am8")) {
                fovea = 3;

            }
            
            // AM9 - Fovea 4
            else if (actionToTake.equals("am9")) {
                fovea = 4;

            }
            
            // AM10 - Neck to focus
            // just Stage 3
             else if (actionToTake.equals("am10") && this.stage == 3) {
                if(fovea == 0 || fovea == 2){
                    yawPos = yawPos-angle_step;
                    neckMotorMO.setI(yawPos);
                }
                else if(fovea == 1 || fovea == 3){
                    yawPos = yawPos+angle_step;
                    neckMotorMO.setI(yawPos);
                }
             }
             
             // AM11 - Head to focus
             else if (actionToTake.equals("am11") && this.stage == 3) {
                if(fovea == 0 || fovea == 2){
                    yawPos = yawPos+angle_step;
                    neckMotorMO.setI(yawPos);
                }
                else if(fovea == 1 || fovea == 3){
                    yawPos = yawPos-angle_step;
                    neckMotorMO.setI(yawPos);
                }
             }
             
             // AM12 - Neck away focus
             else if (actionToTake.equals("am12") && this.stage == 3) {
                if(fovea == 3 || fovea == 2){
                    headPos = headPos-angle_step;
                    headMotorMO.setI(headPos);
                }
                else if(fovea == 1 || fovea == 0){
                    headPos = headPos+angle_step;
                    headMotorMO.setI(headPos);
                }
             }
             
             // AM13 - Head away focus
             else if (actionToTake.equals("am13") && this.stage == 3) {
                if(fovea == 3 || fovea == 2){
                    headPos = headPos+angle_step;
                    headMotorMO.setI(headPos);
                }
                else if(fovea == 1 || fovea == 0){
                    headPos = headPos-angle_step;
                    headMotorMO.setI(headPos);
                }
             }
             


             // attentional actions

            // AA0 - Define desired color
            else if (actionToTake.equals("aa0") && this.stage == 3) {
                List desired_feat_color = (List) desFC.getI();        
                if(desired_feat_color.size() == timeWindow){
                    desired_feat_color.remove(0);
                }
                desired_feat_color.add(new ArrayList<>());
                ArrayList<Float> desired_feat_color_t = (ArrayList<Float>) desired_feat_color.get(desired_feat_color.size()-1);
                if(calculateMean(lastRed)>calculateMean(lastBlue) && calculateMean(lastRed)>calculateMean(lastGreen)){
                    desired_feat_color_t.add((float) 255.0);
                    desired_feat_color_t.add((float) 0.0);
                    desired_feat_color_t.add((float) 0.0);
                }
                if(calculateMean(lastGreen)>calculateMean(lastBlue) && calculateMean(lastGreen)>calculateMean(lastRed)){
                    desired_feat_color_t.add((float) 0.0);
                    desired_feat_color_t.add((float) 255.0);
                    desired_feat_color_t.add((float) 0.0);
                }    
                if(calculateMean(lastBlue)>calculateMean(lastGreen) && calculateMean(lastBlue)>calculateMean(lastRed)){
                    desired_feat_color_t.add((float) 0.0);
                    desired_feat_color_t.add((float) 0.0);
                    desired_feat_color_t.add((float) 255.0);
                }
            } 

            // AA1 - Define desired distance
            else if (actionToTake.equals("aa1") && this.stage == 3) {
                   List desired_feat_dist = (List) desFD.getI();        
                if(desired_feat_dist.size() == timeWindow){
                    desired_feat_dist.remove(0);
                }
                desired_feat_dist.add((float) 0.0);
              }
            
            // AA2 - Define desired region
            else if (actionToTake.equals("aa2") && this.stage == 3) {
                  List desired_feat_reg = (List) desFR.getI();        
                if(desired_feat_reg.size() == timeWindow){
                    desired_feat_reg.remove(0);
                }
                desired_feat_reg.add(new ArrayList<>());
                ArrayList<Integer> desired_feat_reg_t = (ArrayList<Integer>) desired_feat_reg.get(desired_feat_reg.size()-1);
                desired_feat_reg_t.add(8);
                desired_feat_reg_t.add(8);
                
            }
            
            if(aux_reset!=-1){
                if(aux_reset==0){
                    oc.vision.setNextAct(false);
                    oc.reset_positions();
                    aux_reset=-1;
                }else{
                    aux_reset-=1;
                }
            }
            if(aux_resetr!=-1){
                if(aux_resetr==0){
                    oc.reset_positions();
                    aux_resetr=-1;
                }else{
                    aux_resetr-=1;
                }
            }
            check_stop_experiment();
            //printToFile("object_count.txt");
    } 

    

	
	
	
    public void check_stop_experiment() {

        /*if(yawPos>1.4f || yawPos<-1.4f || headPos>0.6f || headPos<-0.4f ){
            crashed = true;
        }*/
        
        if(this.oc.vision.endEpoch() ){
             crashed = true;
             this.oc.vision.setIValues(4, (int) 0);
             
        } else{
            this.oc.vision.setIValues(4, (int) (this.oc.vision.getIValues(4)+1));
        }
        
/*        try {
			Thread.sleep(20);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}
        */
        if (mode.equals("learning") &&  crashed  ){
            oc.shuffle_positions();
            oc.reset_positions();

            
            curiosity_lv = 0;
            red_c = 0;
            green_c = 0;
            blue_c = 0;

            aux_crash = 0;
            headMotorMO.setI(0f);
            neckMotorMO.setI(0f);
            yawPos = 0f;
            headPos = 0f;
            
            executedActions.clear();
        } else if (mode.equals("exploring") &&  crashed ) {
            /*System.out.println("Max number of actions or crashed. Exp: "+ experiment_number +
                    " exp_c:"+exp_c+" exp_s:"+exp_s+
                    " ----- N_act: "+oc.vision.getnAct()+"\n Curiosity_lv: "+curiosity_lv+
                    " Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c);/*/
            //aux_crash = 0;
            oc.shuffle_positions();
            oc.reset_positions();

            headMotorMO.setI(0f);
            neckMotorMO.setI(0f);
            yawPos = 0f;
            headPos = 0f;
            //action_number = 0;
            executedActions.clear();
        }
    }

public static float calculateMean(ArrayList<Float> list) {
            if (list == null) {
                return 0; // Return 0 if the list is empty or handle it as required
            }
            
            if (list.isEmpty()) {
                return 0; // Return 0 if the list is empty or handle it as required
            }

            float sum = 0;
            for (float value : list) {
                sum += value;
            }

            return sum / list.size();
        } 
   /* private void printToFile(String filename){
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
    LocalDateTime now = LocalDateTime.now();
    boolean bool_d = false;
    if(mode=="learning"){
        if (num_tables == 1) bool_d = experiment_number > MAX_EXPERIMENTS_NUMBER;
        else bool_d = exp_c > MAX_EXPERIMENTS_NUMBER && exp_s > MAX_EXPERIMENTS_NUMBER;        
    } else  bool_d = experiment_number > MAX_EXPERIMENTS_NUMBER;
        
    if ( !bool_d ) {
        MemoryObject battery_lv = (MemoryObject) battReadings.get(battReadings.size()-1);
        int battery_lvint = (int)battery_lv.getI();
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+" num_tables:"+num_tables+" Exp:"+experiment_number+
                        " exp_c:"+exp_c+" exp_s:"+exp_s+" Nact:"+action_number+
                        " cur_a: "+cur_a+" sur_a: "+sur_a+" Battery:"+battery_lvint+" Curiosity_lv:"
                        +curiosity_lv+" Red:"+red_c+" Green:"+green_c+" Blue:"+blue_c);

                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }


    }
		
*/
}
