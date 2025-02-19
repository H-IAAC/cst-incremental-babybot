/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.learner;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;

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
import java.util.HashMap;
import java.util.Map;

import outsideCommunication.OutsideCommunication;

/**
 *
 * @author leolellisr
 */
public class AcommodationCodelet extends Codelet 
{
    private List states,  actions, proceduralList;
    private MemoryContainer proceduralMemoryMO;
    private OutsideCommunication oc;
    private int stage, nActions, num_tables;
    private List<String> allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13", "aa0", "am14", "am15", "am16")); //"aa1", "aa2", 
    private Idea motivationMO;
    private String motivation;
    private boolean debug = false;
    public AcommodationCodelet(OutsideCommunication outc, String motivation, int num_tables){
        super();
        this.oc = outc;
        this.stage = this.oc.vision.getStage();
        this.motivation = motivation;
    }
    
    @Override
	public void accessMemoryObjects() {
		
		MemoryObject MO;
                MO = (MemoryObject) this.getInput("STATES");
                states = (List) MO.getI();
                MO = (MemoryObject) this.getInput("ACTIONS");
                actions = (List) MO.getI();
                if(this.motivation.equals("drives")){
                    MemoryContainer MC = (MemoryContainer) this.getInput("MOTIVATION");
                    motivationMO = (Idea) MC.getI();
                }
                proceduralMemoryMO = (MemoryContainer) this.getOutput("PROCEDURAL");
                //proceduralList = (List) proceduralMemoryMO.getI();
        }
        
        @Override
	public void calculateActivation() {
		// TODO Auto-generated method stub
		
	}
        
       // Main Codelet function, to be implemented in each subclass.
	@Override
	public void proc() {
	
            if(stage == 1 || stage == 2){
                nActions = 10;
            }else if(stage == 3){
                nActions = 17;
            }
            
		/*try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }*/
                if(motivationMO == null){
                  if(debug) System.out.println("Rewardcomputer motivationMO is null");
                return;
            }
               
                if (!states.isEmpty() && !actions.isEmpty()  ){
                Object state = (Object) states.get(states.size() - 1);
                String action = (String) actions.get(actions.size() - 1);
                int action_n = allActionsList.indexOf(action);
                if(action_n>-1){
                double reward = 0;
                
                
                double activation  = (double) motivationMO.getValue();
                boolean verify_memory = verify_if_memory_exists(state.toString());
                    if(verify_memory){
                        ArrayList<Integer> info = null;
                        for(Memory memory : proceduralMemoryMO.getAllMemories()) {
                            if(memory.getName().equalsIgnoreCase(state.toString())){
                                info = (ArrayList<Integer>) memory.getI();
                                if(info.size() != nActions){
                                    for(int i=0; i<nActions-info.size();i++){
                                        info.add(0);
                                    }
                                }
                                if(info.size()>action_n){
                                    info.set(action_n, (int) reward);
                                    break;
                                }   
                            }
                        }
                        proceduralMemoryMO.setI(info, activation , state.toString());
                    }
                }
                }
        }
        
         public boolean verify_if_memory_exists(String name_m){
            boolean exists = false;
            //if(!proceduralMemoryMO.getAllMemories().isEmpty()){
                for(Memory memory : proceduralMemoryMO.getAllMemories()) {
                    if(memory.getName().equalsIgnoreCase(name_m)){
                        exists = true;
                        break;
                    }
                }
            //}
            return exists;
        }
         
    public static double calculateMean(ArrayList<Double> list) {
        if (list.isEmpty()) {
            return 0; // Return 0 if the list is empty or handle it as required
        }

        double sum = 0;
        for (double value : list) {
            sum += value;
        }

        return sum / list.size();
    }

}
