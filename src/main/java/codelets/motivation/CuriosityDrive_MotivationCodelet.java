/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.motivation;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.motivational.Drive;
import br.unicamp.cst.motivational.MotivationalCodelet;
import br.unicamp.cst.representation.idea.Idea;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
public class CuriosityDrive_MotivationCodelet extends MotivationalCodelet 
{   private Idea curiosity_motivation_id;
    private List actions, rewards;
    private Idea motivationMO;
    private MemoryObject activationMO;
    private OutsideCommunication oc;
    private int stage, nActions;
    private ArrayList<String> allActionsList;
    private double activation;
    private boolean debug = false;
    private int index = -1;
    private MemoryContainer motivationMC;
    private ArrayList<Double> curiosity_motivation_list;
    private int experiment_number, action_number, exp_c, num_tables;
    private static int MAX_ACTION_NUMBER;
private List<String> allStatesList;	
    private static int MAX_EXPERIMENTS_NUMBER;   
    private MemoryContainer proceduralMemoryMO;
    private float exp_fact = (float) 0.15;
    public CuriosityDrive_MotivationCodelet(String id, double level, double priority, double urgencyThreshold, 
            OutsideCommunication outc, int num_tables){
        super(id, level, priority, urgencyThreshold);
        this.oc = outc;
        this.stage = this.oc.vision.getStage();
        this.activation = 0.0;
        MAX_ACTION_NUMBER = oc.vision.getMaxActions();
        MAX_EXPERIMENTS_NUMBER = oc.vision.getMaxEpochs();
        this.num_tables = num_tables;
        exp_c = oc.vision.getEpoch();
         if(stage == 1 || stage == 2){
            nActions = 14;
            allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13")); //"aa1", "aa2", 
        }else if(stage > 2){
            nActions = 17;
            allActionsList  = new ArrayList<>(Arrays.asList("am0", "am1", "am2", "am3", "am4", "am5", "am6", "am7", "am8", "am9", "am10", "am11", "am12", "am13",
                    "aa0", "aa1", "aa2")); //, 
        }
        //curiosity_motivation_list = new ArrayList<>(Collections.nCopies(nActions, 0.0));


    }
    
    @Override
    public void accessMemoryObjects() {
		
        MemoryObject MO;
        MO = (MemoryObject) this.getInput("ACTIONS");
        actions = (List) MO.getI();
        
        MO = (MemoryObject) this.getInput("STATES");
        allStatesList = (List) MO.getI();


        motivationMC = (MemoryContainer) this.getOutput("MOTIVATION");
 proceduralMemoryMO = (MemoryContainer) this.getInput("PROCEDURAL");
        
        if(debug) System.out.println("Curiosity MC name: "+this.motivationMC.getName());
        ArrayList<Memory> allMemories = this.motivationMC.getAllMemories();
        if(debug) System.out.println("Curiosity MC size: "+allMemories.size());
        if(debug) System.out.println("Curiosity MC: "+allMemories);

    }
        
    @Override
    public void calculateActivation() {
        if(actions.isEmpty()) this.activation = (double) 1.0-Math.pow(exp_fact, oc.vision.getnAct());

    }

    public double getActivation() {
        this.calculateActivation();
        return this.activation;

    }

    public boolean verify_if_memory_exists(String name){
            boolean exists = false;
            if(!proceduralMemoryMO.getAllMemories().isEmpty()){
                for(Memory memory : proceduralMemoryMO.getAllMemories()) {
                    if(memory.getName().equalsIgnoreCase(name)){
                        exists = true;
                        break;
                    }
                }
            }
            return exists;
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
        
        getActivation();
       
        
      /*  try {
        Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }*/

        if(actions.isEmpty()){
             this.activation = 0.9;
            }
       
        /*} else if (stage > 1 && !allStatesList.isEmpty()){
            String lastState = allStatesList.get(allStatesList.size()-1);
            boolean verify_memory = verify_if_memory_exists(lastState);
            if(curiosity_motivation_id.getValue()!=null) curiosity_motivation_list = 
                    (ArrayList<Double>) curiosity_motivation_id.getValue();
            for(String action : allActionsList){
                if(!verify_memory){
                    if(!actions.contains(action) ) {
                        //System.out.println("curiosity actions doesnt conatins action");
                        curiosity_motivation_list.set(allActionsList.indexOf(action), 1.0-
                                        0.1*oc.vision.getnAct());
                    }
                    if(actions.contains(action) && curiosity_motivation_id.getValue()!=null){
                       if(debug) System.out.println("actions conatins action "+
                               Collections.frequency(actions, action) );
                        curiosity_motivation_list.set(allActionsList.indexOf(action),  
                                curiosity_motivation_list.get(allActionsList.indexOf(action))-
                                        0.1*(Collections.frequency(actions, action)));
                    }else if(actions.contains(action) && curiosity_motivation_id.getValue()==null){
                       if(debug) System.out.println("actions conatins action "+
                               Collections.frequency(actions, action) );
                        curiosity_motivation_list.set(allActionsList.indexOf(action), 
                                1-0.1*(Collections.frequency(actions, action)));
                    }
                    
                }else if(curiosity_motivation_list.get(allActionsList.indexOf(action)) <1 &&
                        curiosity_motivation_list.get(allActionsList.indexOf(action)) >=0.2) {
                    curiosity_motivation_list.set(allActionsList.indexOf(action),
                            curiosity_motivation_list.get(allActionsList.indexOf(action))-0.1);
                }else if(curiosity_motivation_list.get(allActionsList.indexOf(action)) < 0.1 || 
                        curiosity_motivation_list.get(allActionsList.indexOf(action)) == 0.1) {
                    curiosity_motivation_list.set(allActionsList.indexOf(action), 0.0);
                }
            }
        }



        curiosity_motivation_id.setValue(curiosity_motivation_list);*/
        else{
            int count_cur = 0;
            ArrayList<String> getExecutedAct = oc.vision.getExecutedAct();
            for(int action=0; action < allActionsList.size(); action++){
                if(!getExecutedAct.contains(String.valueOf(action))) count_cur+=1;
            }
        activation = (double) count_cur/allActionsList.size();
        activation = (double) Math.ceil(activation / 0.2) * 0.2;
        if(debug) System.out.println("curiosity a: "+activation+"count_cur: "+count_cur);
        }
        oc.vision.setFValues(3, (float) this.activation);
        curiosity_motivation_id = new Idea("CURIOSITY", this.activation);
        if(debug) System.out.println("curiosity_motivation_list: "+curiosity_motivation_list);
        if(index == -1) index = motivationMC.setI(curiosity_motivation_id, activation);
        else motivationMC.setI(curiosity_motivation_id, activation, index);
       // printToFile(activation,"curiosity_drive.txt", action_number);
        //action_number+=1;    
       /* boolean exp_b = false;
        if(num_tables == 1) exp_b = this.experiment_number != this.oc.vision.getEpoch();
        else exp_b = this.exp_c != this.oc.vision.getEpoch("C");
        */
        
        /*if(exp_b){
           this.experiment_number = this.oc.vision.getEpoch();
           this.exp_c = this.oc.vision.getEpoch("C");
           
            action_number=0;
        }*/
        //}
    }

    @Override
    public double calculateSimpleActivation(List<Memory> list) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public double calculateSecundaryDriveActivation(List<Memory> list, List<Drive> list1) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    /*private void printToFile(Object object,String filename, int action_num){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        boolean exp_b = false;
        if(num_tables == 1) exp_b = this.experiment_number < MAX_EXPERIMENTS_NUMBER;
        else exp_b = this.exp_c < MAX_EXPERIMENTS_NUMBER;
        
        if ( exp_b) {
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+" "+ object+" Exp:"+experiment_number+" ExpC: "+this.exp_c+
                        " Nact:"+action_num+" Type:CURIOSITY");

                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }*/
}
