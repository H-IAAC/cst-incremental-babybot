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
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 * @author L. L. Rossi (leolellisr)
 * Obs: This class represents the implementations present in the proposed scheme for: 
 * DiscretizationCodelet; adaptation; accommodation and assimilation. 
 * Procedural Memory is represented by QTable.
 */

public class OldLearnerCodelet extends Codelet 

{

	private int time_graph;
	private static final float CRASH_TRESHOLD = 0.28f;
	
	private static final int MAX_ACTION_NUMBER = 500;
	
	private static final int MAX_EXPERIMENTS_NUMBER = 100;
	
	private QLearning ql;
    

    private List winnersList, colorReadings, redReadings, greenReadings, blueReadings, distReadings, battReadings;
    private List saliencyMap, curiosityMot, curiosityAct;
    private Idea motivationMO;
    private MemoryObject motorActionMO, reward_stringMO, action_stringMO;
    private MemoryObject neckMotorMO;
    private MemoryObject headMotorMO;
    private List<String> actionsList;
    private List<String> statesList;
    
    private OutsideCommunication oc;
    private final int timeWindow;
    private final int sensorDimension;
    
    private float vel = 2f,angle_step;
    
    private int global_reward;
    private int curiosity_lv, red_c, green_c, blue_c;
    private int action_number, action_index;
    private int experiment_number;
    private int stage;
    int fovea; 
    private String mode;
    private Random gerador = new Random();
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
    private boolean crashed = false;
    private boolean debug = false;
    private int aux_crash = 0;
    private ArrayList<String> executedActions  = new ArrayList<>();
    private ArrayList<String> allActionsList;
    private ArrayList<Integer> curiosity_motivationIntensity;
    private Map<String, ArrayList<Integer>> proceduralMemory = new HashMap<String, ArrayList<Integer>>();
    private ArrayList<Float> lastLine, lastRed, lastGreen, lastBlue, lastDist;
    private String motivation, nameMotivation, stringOutput = "";
    private float  mot_value=0, hug_drive=0, cur_drive=0, r_imp=0, g_imp=0, b_imp=0;
    //private Idea ideaMotivation;
	public OldLearnerCodelet (OutsideCommunication outc, int tWindow, int sensDim, String mode, String motivation) {
		
		super();
		time_graph = 0;
		
		global_reward = 0;
		
		action_number = 0;
		
		experiment_number = 1;
                
                curiosity_lv = 0;
                red_c = 0;
                green_c = 0;
                blue_c =0;
                
                this.motivation = motivation;
                // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
                // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
                // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
                // aa0: focus td color; aa1: focus td depth; aa2: focus td region.
		allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0", "aa1", "aa2", "am14", "am15", "am16"));
                if(this.motivation.equals("drives")) curiosity_motivationIntensity  = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0));
		// States are 0 1 2 ... 5^256-1
		ArrayList<String> allStatesList = new ArrayList<>(Arrays.asList(IntStream.rangeClosed(0, (int)Math.pow(2, 16)-1).mapToObj(String::valueOf).toArray(String[]::new)));
		
                // QLearning initialization
		ql = new QLearning();
                ql.setAlpha((double) 0.9);
		ql.setActionsList(allActionsList);
                oc = outc;
                yawPos = oc.NeckYaw_m.getSpeed();
                headPos = oc.HeadPitch_m.getSpeed();                
                this.stage = this.oc.vision.getStage();
                
		// learning mode ---> build Qtable from scratch
		if (mode.equals("learning") && this.stage == 1) {
		// Initialize QTable to 0
			for (int i=0; i < allStatesList.size(); i ++) {
                            for (int j=0; j < allActionsList.size(); j++) {
                                    ql.setQ(0, allStatesList.get(i), allActionsList.get(j));
                            }
			}
		} else if (mode.equals("learning") && this.stage > 1){
                    try {
                            ql.recoverQ();
			}
                    catch (Exception e) {
                            System.out.println("ERROR LOADING QTABLE");
                            System.exit(1);
			}
                }
                
		// exploring mode ---> reloads Qtable 
		else {
                    try {
			ql.recoverQ();
                    }
                    catch (Exception e) {
                        System.out.println("ERROR LOADING QTABLE");
			System.exit(1);
                    }
		}
		
		angle_step = 0.1f;
		
		timeWindow = tWindow;
        sensorDimension = sensDim;
        this.mode = mode;
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
                
                MO = (MemoryObject) this.getInput("VISION_COLOR_FM");
                colorReadings = (List) MO.getI();
/*                MO = (MemoryObject) this.getInput("VISION_GREEN_FM");
                greenReadings = (List) MO.getI();
                MO = (MemoryObject) this.getInput("VISION_BLUE_FM");
                blueReadings = (List) MO.getI();*/
                MO = (MemoryObject) this.getInput("DEPTH_FM");
                distReadings = (List) MO.getI();
                
                MO = (MemoryObject) this.getInput("BATTERY_BUFFER");
                battReadings = (List) MO.getI();
                
                if(this.motivation.equals("drives")){
                    MemoryContainer MC = (MemoryContainer) this.getInput("MOTIVATION");
                    motivationMO = (Idea) MC.getI();
                }
                motorActionMO = (MemoryObject) this.getOutput("MOTOR");
                neckMotorMO = (MemoryObject) this.getOutput("NECK_YAW");
                headMotorMO = (MemoryObject) this.getOutput("HEAD_PITCH");
                reward_stringMO = (MemoryObject) this.getOutput("REWARDS_STRING_OUTPUT");
                action_stringMO = (MemoryObject) this.getOutput("ACTION_STRING_OUTPUT");
                
                        
                MO = (MemoryObject) this.getOutput("ACTIONS");
                actionsList = (List) MO.getI();

                MO = (MemoryObject) this.getOutput("STATES");
                statesList = (List) MO.getI();

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
		try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }       

                // Use the Random class to generate a random index
                Random random = new Random();
                        
                MemoryObject battery_lv = (MemoryObject) battReadings.get(battReadings.size()-1);
                if(debug) System.out.println("battery_lv: "+battery_lv);
                int battery_lvint = (int)battery_lv.getI();
		

                
                System.out.println("Exp: "+ experiment_number + " num action: "+action_number+ " Reward: "+global_reward+" Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c);
		String state = "-1";
		
		if (!saliencyMap.isEmpty() && !winnersList.isEmpty()) {
			
			Winner lastWinner = (Winner) winnersList.get(winnersList.size() - 1);
			winnerIndex = lastWinner.featureJ;
			state = getStateFromSalMap();
                        
			if (!actionsList.isEmpty() && mode.equals("learning")) {
                            
				// Find reward of the current state, given previous  winner 
				check_stop_experiment(mode);
				Double reward = 1d;
				global_reward += reward;
                                
				// Gets last action taken
				String lastAction = actionsList.get(actionsList.size() - 1);
                                
				// Gets last state that was in
				String lastState = statesList.get(statesList.size() - 1);
                                
				// Updates QLearning table // Adaptation
				ql.update(lastState, lastAction, reward);
			}
			
			statesList.add(state);
			// Select best action to take
                        String actionToTake = ql.getAction(state);

                            // Motivation

                        //ideaMotivation = (Idea) this.motivationMO.get(this.motivationMO.size()-1);
                        
                        
                        int i = 0;
                        double max_action = 0.0;
                        ArrayList<Integer> max_list = new ArrayList<Integer>();
                        if(this.motivation.equals("drives")){                        
                            nameMotivation = motivationMO.getName();
                            if(nameMotivation.equals("CURIOSITY")){
                                ArrayList<Double> valueMotivation = (ArrayList<Double>) motivationMO.getValue();

                                for(double action : valueMotivation){
                                    if(action > max_action){
                                        max_action = action;
                                    } 
                                }
                                
                                for(double action : valueMotivation){
                                    if(action == max_action){
                                        max_list.add(i);
                                    }
                                    i += 1;
                                }
                                // Retrieve the random element from the ArrayList
                                action_index = max_list.get(random.nextInt(max_list.size()));
                                actionToTake = allActionsList.get(action_index);
                                cur_drive = (float) max_action;
                                
                            } else {
                                double valueMotivation = (double) motivationMO.getValue();
                                hug_drive = (float) valueMotivation;
                                if(valueMotivation > 0.7) {
                                    if(calculateMean(lastGreen)>0.01 || calculateMean(lastBlue)>0.01){
                                        if (calculateMean(lastGreen)>calculateMean(lastBlue)) action_index = 18;
                                        else if(calculateMean(lastBlue)>calculateMean(lastGreen)) action_index = 19;
                                    }
                                    actionToTake = allActionsList.get(action_index);
                                    System.out.println("Hunger drive learner, action: "+allActionsList.get(action_index));
                                }                                
                            }
                            
                            mot_value = cur_drive + hug_drive;
                        

                        
                        } else if(this.motivation.equals("impulses")){

                // allActions: am0: focus; am1: neck left; am2: neck right; am3: head up; am4: head down; 
                // am5: fovea 0; am6: fovea 1; am7: fovea 2; am8: fovea 3; am9: fovea 4; 
                // am10: neck tofocus; am11: head tofocus; am12: neck awayfocus; am13: head awayfocus
                // aa0: focus td color; aa1: focus td depth; aa2: focus td region.

                            ArrayList<String> affordancesList  = new ArrayList<>(Arrays.asList("am1", "am2", "am3", "am4", "am5", "aa0", "aa1", "aa2"));

                            if(calculateMean(lastRed)>0.01 || calculateMean(lastGreen)>0.01 || calculateMean(lastBlue)>0.01){
                                if(calculateMean(lastRed)>calculateMean(lastGreen) && calculateMean(lastRed)>calculateMean(lastBlue)) affordancesList.add("am14");
                                else if(calculateMean(lastGreen)>calculateMean(lastRed) && calculateMean(lastGreen)>calculateMean(lastBlue)) affordancesList.add("am15");
                                else if(calculateMean(lastBlue)>calculateMean(lastGreen) && calculateMean(lastBlue)>calculateMean(lastRed)) affordancesList.add("am16");
                            // Retrieve the random element from the ArrayList
                                actionToTake = affordancesList.get(random.nextInt(affordancesList.size()));
                                r_imp= calculateMean(lastRed);
                                g_imp= calculateMean(lastGreen);
                                b_imp= calculateMean(lastBlue);
                                mot_value = r_imp+g_imp+b_imp;

                            } 
                        }
                        
                        //System.out.println("Exp: "+ experiment_number + " Original Action "+actionToTake + " num action: "+action_number);
                        if(this.stage < 3) {
                            if(actionToTake.equals("aa0")||actionToTake.equals("aa1")||actionToTake.equals("aa2")||actionToTake.equals("am10")||actionToTake.equals("am11")||actionToTake.equals("am12")||actionToTake.equals("am13")){
                                actionToTake = allActionsList.get(gerador.nextInt(7));
                            }
                        }
			actionsList.add(actionToTake);
			
			action_number++;
			
			motorActionMO.setI(actionToTake);
			
			System.out.println("Exp: "+ experiment_number + " Action: "+actionToTake + " num action: "+action_number+ " Winner: "+winnerIndex+ " WinnerFovea: "+winnerFovea);
                        if(!executedActions.contains(actionToTake)) executedActions.add(actionToTake);
                        if (actionToTake.equals("am1")) {
                                yawPos = yawPos-angle_step;
                                neckMotorMO.setI(yawPos);
                                if(winnerFovea !=-1 && IntStream.of(posLeft).anyMatch(x -> x == winnerFovea) && stage > 1){
                                    global_reward += 1;
                                }
                        }

                        else if (actionToTake.equals("am2")) {
                                yawPos = yawPos+angle_step;
                                neckMotorMO.setI(yawPos);
                                if(winnerFovea !=-1 && IntStream.of(posRight).anyMatch(x -> x == winnerFovea)){
                                    global_reward += 1;
                                }
                        }
                        else if (actionToTake.equals("am3")) {
                                headPos = headPos-angle_step;
                                headMotorMO.setI(headPos);
                                if(winnerFovea !=-1 && IntStream.of(posUp).anyMatch(x -> x == winnerFovea)) {
                                    global_reward += 1;
                                } 
                        }
                        else if (actionToTake.equals("am4")) {
                                headPos = headPos+angle_step;
                                headMotorMO.setI(headPos);
                                if(winnerFovea !=-1 && IntStream.of(posDown).anyMatch(x -> x == winnerFovea)){
                                    global_reward += 1;
                                } 
                        }
                        else if (actionToTake.equals("am5")) {
                            fovea = 0;
                            if(winnerFovea !=-1 && IntStream.of(fovea0).anyMatch(x -> x == winnerFovea)){
                                    global_reward += 1;
                                } 
                        }
                        else if (actionToTake.equals("am6")) {
                            fovea = 1;
                            if(winnerFovea !=-1 && IntStream.of(fovea1).anyMatch(x -> x == winnerFovea)){
                                    global_reward += 1;
                                } 
                        }
                        else if (actionToTake.equals("am7")) {
                            fovea = 2;
                            if(winnerFovea !=-1 && IntStream.of(fovea2).anyMatch(x -> x == winnerFovea)){
                                    global_reward += 1;
                                } 
                        }
                        else if (actionToTake.equals("am8")) {
                            fovea = 3;
                            if(winnerFovea !=-1 && IntStream.of(fovea3).anyMatch(x -> x == winnerFovea)){
                                    global_reward += 1;
                                } 
                        }
                        else if (actionToTake.equals("am9")) {
                            fovea = 4;
                            if(winnerFovea !=-1 && IntStream.of(posCenter).anyMatch(x -> x == winnerFovea)){
                                    global_reward += 1;
                                } 
                        }
                        
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

                         else if (actionToTake.equals("am14") && this.stage == 3) {
                            if(calculateMean(lastRed)>0.01 && calculateMean(lastGreen)<0.015 && calculateMean(lastBlue)<0.015){
                                try {
                                    oc.set_object_back(0);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(OldLearnerCodelet.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                if(debug) System.out.println("GOT RED");
                                global_reward += 1;
                                red_c += 1;
                                curiosity_lv += 2;
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    Thread.currentThread().interrupt();
                                }
                                oc.reset_positions();
                            }
                            else {
                                global_reward -= 1;
                            }
                         }
                         
                         else if (actionToTake.equals("am15") && this.stage == 3) {
                            if(calculateMean(lastGreen)>0.01 && calculateMean(lastRed)<0.015 && calculateMean(lastBlue)<0.015){
                                try {
                                    oc.set_object_back(1);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(OldLearnerCodelet.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                curiosity_lv += 1;
                                green_c += 1;
                                if(debug) System.out.println("GOT GREEN");
                                global_reward += 1;
                                try {
                                    Thread.sleep(500);
                                } catch (Exception e) {
                                    Thread.currentThread().interrupt();
                                }
                                oc.reset_positions();
                            }
                            else {
                                global_reward -= 1;
                            }
                         }
                         
                         else if (actionToTake.equals("am16") && this.stage == 3) {
                            if(calculateMean(lastBlue)>0.01 && calculateMean(lastGreen)<0.015 && calculateMean(lastRed)<0.015){
                                try {
                                    oc.set_object_back(2);
                                } catch (InterruptedException ex) {
                                    Logger.getLogger(OldLearnerCodelet.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                if(debug) System.out.println("GOT BLUE");
                                global_reward += 1;
                                blue_c += 1;
                                try {
                                    Thread.sleep(1000);
                                } catch (Exception e) {
                                    Thread.currentThread().interrupt();
                                }
                                oc.reset_positions();
                            }
                            else {
                                global_reward -= 1;
                            }
                         }
                         
                         // attentional actions
                         
                         // aa0 move to color tod down
                        else if (actionToTake.equals("aa0") && this.stage == 3) {
				// Moving neck to left yawPos > -1.4 && 
                                if(winnerBlue !=-1 && IntStream.of(fovea0).anyMatch(x -> x == winnerBlue)) {
                                    fovea = 0; 
                               }
                                else if(winnerBlue !=-1 && IntStream.of(fovea1).anyMatch(x -> x == winnerBlue)) {
                                    fovea = 1; 
                               }
                                else if(winnerBlue !=-1 && IntStream.of(fovea2).anyMatch(x -> x == winnerBlue)) {
                                    fovea = 2; 
                               }
                                else if(winnerBlue !=-1 && IntStream.of(fovea3).anyMatch(x -> x == winnerBlue)) {
                                    fovea = 3; 
                               } else fovea = 4;
			} 
                        
                        else if (actionToTake.equals("aa1") && this.stage == 3) {
				// Moving neck to left
                                if(winnerDist !=-1 && IntStream.of(fovea0).anyMatch(x -> x == winnerDist)) {
                                    fovea = 0; 
                               }
                                else if(winnerDist !=-1 && IntStream.of(fovea1).anyMatch(x -> x == winnerDist)) {
                                    fovea = 1; 
                               }
                                else if(winnerDist !=-1 && IntStream.of(fovea2).anyMatch(x -> x == winnerDist)) {
                                    fovea = 2; 
                               }
                                else if(winnerDist !=-1 && IntStream.of(fovea3).anyMatch(x -> x == winnerDist)) {
                                    fovea = 3; 
                               } else fovea = 4;
                          }

			else if (actionToTake.equals("aa2") && this.stage == 3) {
                                fovea = 4;
			}
			// Do nothing
			else {
                                if(winnerFovea !=-1 && IntStream.of(posCenter).anyMatch(x -> x == winnerFovea)) global_reward += 1; 
                                
			}
		} 
                
//		time_graph = printToFile(state, "states.txt", time_graph, true, action_number);
//                printToFile(actionsList.get(actionsList.size() - 1), "actions.txt", time_graph, true, action_number);
                //stringOutput.clear();
                //stringOutput.add("actions.txt");
                if(mot_value==0) mot_value = 1;
                 if(this.motivation.equals("drives")){  
                    stringOutput = time_graph+" Exp number:"+experiment_number+" Action num: "+action_number+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c+"action: "+actionsList.get(actionsList.size() - 1)+" mot_value: "+mot_value+" hug_drive: "+(float) (hug_drive/mot_value*100)+" cur_drive: "+(float) (cur_drive/mot_value*100);
                } else if(this.motivation.equals("impulses")){
                    stringOutput = time_graph+" Exp number:"+experiment_number+" Action num: "+action_number+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c+"action: "+actionsList.get(actionsList.size() - 1)+" mot_value: "+mot_value+" r_imp: "+(float) (r_imp/mot_value*100)+" g_imp: "+(float) (g_imp/mot_value*100)+" b_imp: "+(float) (b_imp/mot_value*100);
                }
                 
                 if(stringOutput==null) System.out.println("stringOutput==null");
                 if(action_stringMO==null) System.out.println("action_stringMO==null");
                 
if(stringOutput!=null && action_stringMO != null)                action_stringMO.setI(stringOutput);
		if (mode.equals("exploring")) {
			//oc.marta_position.getData();
			check_stop_experiment(mode);

		}
		
	}
	
	
	
	public void check_stop_experiment(String mode) {

                if(yawPos>1.4f || yawPos<-1.4f || headPos>0.6f || headPos<-0.4f ){
                    crashed = true;
                }
                if(crashed){
                    global_reward -= 10;
                            
                }
                MemoryObject battery_lv = (MemoryObject) battReadings.get(battReadings.size()-1);
                int battery_lvint = (int)battery_lv.getI();
                
		if (mode.equals("learning") && ((action_number >= MAX_ACTION_NUMBER ) || crashed || battery_lvint==0) ){
			System.out.println("Max number of actions or crashed");
                        oc.shuffle_positions();
                        oc.reset_positions();
                        
                        
                        aux_crash = 0;
			headMotorMO.setI(0f);
                        neckMotorMO.setI(0f);
                        yawPos = 0f;
                        headPos = 0f;
			experiment_number++;
                        //experiment_number = printToFile(global_reward, "rewards.txt", experiment_number, false, action_number);
//                        stringOutput.clear();
 //                       stringOutput.add("rewards.txt");
                        if(mot_value==0) mot_value = 1;
                        if(this.motivation.equals("drives")){  
                            stringOutput = time_graph+" Exp number:"+experiment_number+" Action num: "+action_number+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c+" reward: "+global_reward+" mot_value: "+mot_value+" hug_drive: "+(float) (hug_drive/mot_value*100)+" cur_drive: "+(float) (cur_drive/mot_value*100);
                        } else if(this.motivation.equals("impulses")){
                            stringOutput = time_graph+" Exp number:"+experiment_number+" Action num: "+action_number+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c+" reward: "+global_reward+" mot_value: "+mot_value+" r_imp: "+(float) (r_imp/mot_value*100)+" g_imp: "+(float) (g_imp/mot_value*100)+" b_imp: "+(float) (b_imp/mot_value*100);
                        }
                        if(stringOutput!=null && reward_stringMO != null)   reward_stringMO.setI(stringOutput);
                        oc.vision.setEpoch(experiment_number);
                        action_number = 0;
                        global_reward = 0;
                        curiosity_lv = 0;
                        executedActions.clear();
                        red_c = 0;
                        green_c = 0;
                        blue_c = 0;
			if (experiment_number%50 ==0 ) { 
				ql.storeQ();
				
			}
                        if (experiment_number > MAX_EXPERIMENTS_NUMBER) {
                            ql.storeQ();
                            System.exit(0);
                        }
                        ql.setB(0.95-(0.95*experiment_number/MAX_EXPERIMENTS_NUMBER));
			//oc.marta_position.resetData();
			try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            Thread.currentThread().interrupt();
                        }
		} else if (mode.equals("exploring") && (action_number >= MAX_ACTION_NUMBER ) || crashed || battery_lvint==0) {
                    System.out.println("Max number of actions or crashed");
                        aux_crash = 0;
                        oc.shuffle_positions();
                        oc.reset_positions();
                        
                        headMotorMO.setI(0f);
                        neckMotorMO.setI(0f);
                        yawPos = 0f;
                        headPos = 0f;
			//experiment_number = printToFile(global_reward, "rewards.txt", experiment_number, false, action_number);
                        experiment_number++;
                        //stringOutput.clear();
                        //stringOutput.add("rewards.txt");
                        if(mot_value==0) mot_value = 1;
                        if(this.motivation.equals("drives")){  
                            stringOutput = time_graph+" Exp number:"+experiment_number+" Action num: "+action_number+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c+" reward: "+global_reward+" mot_value: "+mot_value+" hug_drive: "+(float) (hug_drive/mot_value*100)+" cur_drive: "+(float) (cur_drive/mot_value*100);
                        } else if(this.motivation.equals("impulses")){
                            stringOutput = time_graph+" Exp number:"+experiment_number+" Action num: "+action_number+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c+" reward: "+global_reward+" mot_value: "+mot_value+" r_imp: "+(float) (r_imp/mot_value*100)+" g_imp: "+(float) (g_imp/mot_value*100)+" b_imp: "+(float) (b_imp/mot_value*100);
                        }
                      if(stringOutput!=null && reward_stringMO != null)    reward_stringMO.setI(stringOutput);
                        oc.vision.setEpoch(experiment_number);
			curiosity_lv = 0;
                        action_number = 0;
                        global_reward = 0;
                        executedActions.clear();
                        red_c = 0;
                        green_c = 0;
                        blue_c = 0;
                        if (experiment_number > MAX_EXPERIMENTS_NUMBER) {
                            
                            System.exit(0);
                        }
                }
	}
	
        // Discretization
	// Normalize and transform a salience map into one state
		// Normalized values between 0 and 1 can be mapped into 0, 1, 2, 3 or 4
		// Them this values are computed into one respective state
		public String getStateFromSalMap() {
			
			
                        ArrayList<Float> mean_lastLine = new ArrayList<>();
                        for(int i=0; i<16;i++){
                            mean_lastLine.add(0f);
                        }
                        redReadings = (List) colorReadings.get(0);
                        greenReadings = (List) colorReadings.get(1);
                        blueReadings = (List) colorReadings.get(2);
                        
			// Getting just the last entry (current sal map)
			lastLine = (ArrayList<Float>) saliencyMap.get(saliencyMap.size() -1);
                        lastRed = (ArrayList<Float>) redReadings.get(redReadings.size() -1);
                        lastGreen = (ArrayList<Float>) greenReadings.get(greenReadings.size() -1);
                        lastBlue = (ArrayList<Float>) blueReadings.get(blueReadings.size() -1);

                        lastDist = (ArrayList<Float>) distReadings.get(distReadings.size() -1);

                        //if (calculateMean(lastRed)<0.01 && calculateMean(lastGreen)<0.01 && calculateMean(lastBlue)<0.01) aux_crash += 1;
                        if (Collections.max(lastLine) == 0) aux_crash += 1;
                        else aux_crash = 0; 
                        
                        if(action_number > 5 && aux_crash> 5 ){
                            crashed = true;
                        }
                        
                        int indexRed = -1;
                        int indexGreen = -1;
                        int indexBlue = -1;                        
                        int indexDist = -1;

                        if (this.stage == 3) {
                            
                            if (lastRed.indexOf(Collections.max(lastRed))>-1) indexRed = lastRed.indexOf(Collections.max(lastRed));
                            if (lastGreen.indexOf(Collections.max(lastGreen))>-1) indexGreen = lastGreen.indexOf(Collections.max(lastGreen));
                            if (lastBlue.indexOf(Collections.max(lastBlue))>-1) indexBlue = lastBlue.indexOf(Collections.max(lastBlue));
                                                        
                            if (lastDist.indexOf(Collections.max(lastDist))>-1) indexDist = lastDist.indexOf(Collections.max(lastDist));   
                        }
                        
                        if(debug){
                            System.out.println("lastRed: "+calculateMean(lastRed));
                            System.out.println("indexRed: "+indexRed);
                            System.out.println("lastGreen: "+calculateMean(lastGreen));
                            System.out.println("indexGreen: "+indexGreen);

                            System.out.println("lastBlue: "+calculateMean(lastBlue));
                            System.out.println("indexBlue: "+indexBlue);
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
                                    if(i == indexRed && indexRed != -1) winnerRed = n*4+m;
                                    if(i == indexGreen && indexGreen != -1) winnerGreen = n*4+m;
                                    if(i == indexBlue && indexBlue != -1) winnerBlue = n*4+m;
                                    if(i == indexDist && indexDist != -1) winnerDist = n*4+m;
                                    if(i == winnerIndex) winnerFovea = n*4+m;

                                    float Fvalue_r = (float) lastLine.get(i);                         
                                    MeanValue.add(Fvalue_r);

                                }
                            }
                            float correct_mean_r = Collections.max(MeanValue);
                            
                            mean_lastLine.set(n*4+m, correct_mean_r);
                            MeanValue.clear();
                            
                            }
                        }
                        if(debug){
                            System.out.println("winnerRed: "+winnerRed);
                            System.out.println("winnerGreen: "+winnerGreen);
                            System.out.println("winnerBlue: "+winnerBlue);
                        }
                    }
			// For normalizing readings between 0 and 1 before transforming to state 
			Float max = Collections.max(mean_lastLine);
			Float min = Collections.min(mean_lastLine);		
			// System.out.println("mean_lastLine len: "+mean_lastLine.size()+" max: "+max+ " min: "+min);
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
			return stateVal.toString();
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
            
	private int printToFile(Object object,String filename, int counter, boolean check, int action_num){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        
        if (!check || experiment_number < MAX_EXPERIMENTS_NUMBER) {
            MemoryObject battery_lv = (MemoryObject) battReadings.get(battReadings.size()-1);
            int battery_lvint = (int)battery_lv.getI();
	        try(FileWriter fw = new FileWriter("profile/"+filename,true);
	            BufferedWriter bw = new BufferedWriter(fw);
	            PrintWriter out = new PrintWriter(bw))
	        {
	            out.println(dtf.format(now)+"_"+counter+" "+ object+" Exp number:"+experiment_number+" Action num: "+action_num+ " Battery: "+battery_lvint+" Curiosity_lv: "+curiosity_lv+" Red: "+red_c+" Green: "+green_c+" Blue: "+blue_c);
	            
	            out.close();
	            return ++counter;
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        }
      
		return counter;

	}
		

}
