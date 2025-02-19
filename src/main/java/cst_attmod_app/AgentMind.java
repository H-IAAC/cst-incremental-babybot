/*
 * /*******************************************************************************
 *  * Copyright (c) 2012  DCA-FEEC-UNICAMP
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3
 *  * which accompanies this distribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  * 
 *  * Contributors:
 *  *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *  ******************************************************************************/
 
package cst_attmod_app;


import attention.WinnerPicker;
import attention.SalMap;
import attention.Winner;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.Memory;
import br.unicamp.cst.core.entities.MemoryContainer;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;
import sensory.SensorBufferCodelet;
import codelets.learner.AcommodationCodelet;
import codelets.learner.ActionExecCodelet;
import codelets.learner.AssimilationCodelet;
import codelets.learner.DecisionCodelet;
import codelets.motor.MotorCodelet;
import codelets.sensors.Sensor_Vision;
import codelets.sensors.Sensor_Depth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import outsideCommunication.OutsideCommunication;
import codelets.learner.RewardComputerCodelet;
import codelets.learner.LearnerCodelet;
import codelets.learner.QLearningL;
import codelets.sensors.CFM;
import codelets.sensors.BU_FM_Color;
import codelets.sensors.BU_FM_Depth;
import codelets.sensors.TD_FM_Color;
import codelets.sensors.TD_FM_Depth;
import codelets.motivation.CuriosityDrive_MotivationCodelet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import codelets.support.*;

/**
 *
 * @author L. L. Rossi (leolellisr)
 */
public class AgentMind extends Mind {
    public static final int Resolution = 256;
    public static final int Buffersize = 100;
    public static final int Visiondimension = Resolution*Resolution*3;
    public static final int Vision_image_dimension = Resolution*Resolution;
    public static final int Sensor_dimension = 256;
    public static final boolean debug = true;
    public static final boolean saverCodelet = false;
    private int index_hunger, index_curiosity, print_step;
        private String stringOutputac = "", stringOutputreS = "", stringOutputreC = "";
private long seed;
    public AgentMind(OutsideCommunication oc, String mode, String motivation, 
            int num_tables, int print_step,long seed) throws IOException{
        super();
        oc.vision.setIValues(0, num_tables);
        this.print_step = print_step;
        this.seed = seed;
        
        //System.out.println("AgentMind");
        //////////////////////////////////////////////
        //Declare Memory Objects
        //////////////////////////////////////////////
        
        // Motivation
        // MemoryObject curiosity_motivationMO = null, curiosity_activationMO  = null, hunger_motivationMO  = null;
        MemoryContainer motivationMC = new MemoryContainer("MOTIVATION");

        //Motor - Neck
        MemoryObject motorActionMO;
        MemoryObject Neck_Yaw, Head_Pitch;
        motorActionMO = createMemoryObject("MOTOR", "");
        Neck_Yaw = createMemoryObject("NECK_YAW", new Float(0));
        Head_Pitch = createMemoryObject("HEAD_PITCH", new Float(0));
        
        ArrayList<Memory> motorMOs = new ArrayList<>();
        motorMOs.add(motorActionMO);
        motorMOs.add(Neck_Yaw);
        motorMOs.add(Head_Pitch);
        
        //Sensors
        
        
        //Vision Sensor
        List vision_data = Collections.synchronizedList(new ArrayList<Float>(Visiondimension));
        MemoryObject vision_read = createMemoryObject("VISION", vision_data);
        
        //Vision Sensor
        List depth_data = Collections.synchronizedList(new ArrayList<Float>(Vision_image_dimension));
        MemoryObject depth_read = createMemoryObject("DEPTH", depth_data);
        
        

        //Vision buffer
        List vision_buffer_list = Collections.synchronizedList(new ArrayList<Memory>(Buffersize));
        MemoryObject vision_bufferMO = createMemoryObject("VISION_BUFFER",vision_buffer_list);
        
               
        //Sensor Buffers
        //Vision buffer
        List depth_buffer_list = Collections.synchronizedList(new ArrayList<Memory>(Buffersize));
        MemoryObject depth_bufferMO = createMemoryObject("DEPTH_BUFFER", depth_buffer_list);
        
        
                //Depth FM
        List depthFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject depth_fmMO = createMemoryObject("DEPTH_FM", depthFM);
        
        
        
      // Color data 

          
      
        //Color bottom-up Feature Maps

        //Vision color FM
        List visionColorFM = Collections.synchronizedList(new ArrayList<ArrayList<ArrayList<Float>>>());
        ArrayList vision_redFM = new ArrayList<>(); //= (List) vision_FM.get(0); // Get red data
        ArrayList vision_greenFM= new ArrayList<>(); // = (List) vision_FM.get(1); // Get green data
        ArrayList vision_blueFM = new ArrayList<>(); // = (List) vision_FM.get(2); // Get blue data
        visionColorFM.add(vision_redFM);
        visionColorFM.add(vision_greenFM);
        visionColorFM.add(vision_blueFM);
        MemoryObject vision_color_fmMO = createMemoryObject("VISION_COLOR_FM", visionColorFM);
        

        //Top-down FMs
        //Depth FM
        List depthtopFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject depth_top_fmMO = createMemoryObject("DEPTH_TOP_FM", depthtopFM);
        
        //Color top-down Feature Maps

        //Vision Color
        List visionColortopFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject vision_color_top_fmMO = createMemoryObject("VISION_COLOR_TOP_FM", visionColortopFM);
        
        //Vision region
        List visionRegiontopFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject vision_region_top_fmMO = createMemoryObject("REGION_TOP_FM", visionRegiontopFM);
        
        //Variable:  type_winner       
        List typeWinner= Collections.synchronizedList(new ArrayList<Float>());
        MemoryObject type_fmMO = createMemoryObject("TYPE", typeWinner);
        

        //
        // Weight list
        
        List weights = Collections.synchronizedList(new ArrayList<Float>(7));
        weights.add(1.0f); // Red
        weights.add(1.0f); // Green
        weights.add(1.0f); // Blue
        weights.add(1.0f); // Depth
        weights.add(1.0f); // Color Top
        weights.add(1.0f); // Depth Top
        weights.add(1.0f); // Region Top
    
        MemoryObject weightsMO = createMemoryObject("FM_WEIGHTS",weights);
        
        //Combined Feat data
        List CombFM = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());        
        MemoryObject combFMMO = createMemoryObject("COMB_FM",CombFM);
        
        
        //ATTENTIONAL MAP
        List att_mapList = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());        
        MemoryObject attMapMO = createMemoryObject("ATTENTIONAL_MAP",att_mapList);
        
        //WINNERS
        List winnersList = Collections.synchronizedList(new ArrayList<Winner>());
        MemoryObject winnersMO = createMemoryObject("WINNERS",winnersList);
        
        //SALIENCY MAP
        List saliencyMap = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject salMapMO = createMemoryObject("SALIENCY_MAP", saliencyMap);
                
        //ACTIONS 
        List actionsList = Collections.synchronizedList(new ArrayList<String>());
        MemoryObject actionsMO = createMemoryObject("ACTIONS", actionsList);
        
        //STATES 
        List statesList = Collections.synchronizedList(new ArrayList<String>());
        MemoryObject statesMO = createMemoryObject("STATES", statesList);
        
        
        //QTables
        List qtableList = Collections.synchronizedList(new ArrayList<QLearningL>());
        MemoryObject qtableMO = createMemoryObject("QTABLE", qtableList);
        
        
        //REWARDS
        
        List rewardsList = Collections.synchronizedList(new ArrayList<Integer>());
        MemoryObject rewardsMO = createMemoryObject("REWARDS", rewardsList);

        // PROCEDURAL MEMORY
        //List proceduralMemory = Collections.synchronizedList(new ArrayList<HashMap<Observation, ArrayList<Integer>>>());
        MemoryContainer proceduralMO = createMemoryContainer("PROCEDURAL");
        
        
        
        // Desired features
        
        List desFeatC = Collections.synchronizedList(new ArrayList<ArrayList<Float>>());
        MemoryObject desFeatCMO = createMemoryObject("DESFEAT_C", desFeatC);
        
        List desFeatD = Collections.synchronizedList(new ArrayList<Float>());
        MemoryObject desFeatDMO = createMemoryObject("DESFEAT_D", desFeatD);
        
        List desFeatR = Collections.synchronizedList(new ArrayList<ArrayList<Integer>>());
        MemoryObject desFeatRMO = createMemoryObject("DESFEAT_R", desFeatR);
        
//        
//        
//        ////////////////////////////////////////////
//        //Codelets
//        ////////////////////////////////////////////
////        
        //Motor - Neck
        Codelet motors = new MotorCodelet(oc.HeadPitch_m, oc.NeckYaw_m);
        motors.addInputs(motorMOs);
        insertCodelet(motors);
        
        //Vision Sensor
        Codelet visions = new Sensor_Vision(oc.vision);
        visions.addOutput(vision_read);
        insertCodelet(visions);

        
        //Depth Sensor
        Codelet depths = new Sensor_Depth(oc.depth, oc.vision);
        //visions.addInput(stage_fmMO);
        depths.addOutput(depth_read);
        insertCodelet(depths);
        
        //Sensor Buffers
        //Vision data
        Codelet vision_buffer = new SensorBufferCodelet("VISION", "VISION_BUFFER", Buffersize);
        vision_buffer.addInput(vision_read);
        vision_buffer.addOutput(vision_bufferMO);
        insertCodelet(vision_buffer);

        
        //Depth data
        Codelet depth_buffer = new SensorBufferCodelet("DEPTH", "DEPTH_BUFFER", Buffersize);
        depth_buffer.addInput(depth_read);
        depth_buffer.addOutput(depth_bufferMO);
        insertCodelet(depth_buffer);
        
        //Buffers list
        ArrayList<String> sensbuff_names_vision = new ArrayList<>();
        sensbuff_names_vision.add("VISION_BUFFER");
        sensbuff_names_vision.add("DEPTH_BUFFER");
        
        

        //Feature Maps bottom-up
        //Red FM
        Codelet vision_color_fm_c = new BU_FM_Color(oc.vision, sensbuff_names_vision.size(),
                sensbuff_names_vision,"VISION_COLOR_FM",Buffersize,Sensor_dimension,print_step);
        vision_color_fm_c.addInput(vision_bufferMO);
        vision_color_fm_c.addOutput(vision_color_fmMO);
        insertCodelet(vision_color_fm_c);


                
        //Depth FM
        Codelet depth_fm_c = new BU_FM_Depth(oc.vision, sensbuff_names_vision.size(),
                sensbuff_names_vision,"DEPTH_FM",Buffersize,Sensor_dimension, print_step);
        depth_fm_c.addInput(depth_bufferMO);
        depth_fm_c.addOutput(depth_fmMO);
        insertCodelet(depth_fm_c);
        
     // TOP DOWN
        Codelet vision_color_top_fm_c = new TD_FM_Color(oc.vision, sensbuff_names_vision.size(), 
                sensbuff_names_vision, "VISION_COLOR_TOP_FM",Buffersize,Sensor_dimension, print_step);
        vision_color_top_fm_c.addInput(vision_bufferMO);
        vision_color_top_fm_c.addInput(winnersMO);
        vision_color_top_fm_c.addInput(desFeatCMO);
        vision_color_top_fm_c.addOutput(vision_color_top_fmMO);
        insertCodelet(vision_color_top_fm_c);
        
         
      
        //Depth FM
        Codelet depth_top_fm_c = new TD_FM_Depth(oc.vision, sensbuff_names_vision.size(),
                sensbuff_names_vision,"DEPTH_TOP_FM",Buffersize,Sensor_dimension, print_step);
        depth_top_fm_c.addInput(winnersMO);
        depth_top_fm_c.addInput(depth_bufferMO);
        depth_top_fm_c.addInput(desFeatDMO);
        depth_top_fm_c.addInput(desFeatRMO);
        depth_top_fm_c.addOutput(depth_top_fmMO);
        depth_top_fm_c.addOutput(vision_region_top_fmMO);
        insertCodelet(depth_top_fm_c);
     
        
        ArrayList<String> FMnames = new ArrayList<>();
        FMnames.add("VISION_COLOR_FM");
        FMnames.add("DEPTH_FM");
        FMnames.add("VISION_COLOR_TOP_FM");
        FMnames.add("DEPTH_TOP_FM");
        FMnames.add("REGION_TOP_FM");
        
        //CFM
        Codelet comb_fm_c = new CFM(oc.vision, FMnames.size(), FMnames,Buffersize,Sensor_dimension, 
                print_step, oc);
        comb_fm_c.addInput(vision_color_fmMO);
        comb_fm_c.addInput(depth_fmMO);
        comb_fm_c.addInput(vision_color_top_fmMO);
        comb_fm_c.addInput(depth_top_fmMO);
        comb_fm_c.addInput(vision_region_top_fmMO);
        comb_fm_c.addInput(weightsMO);
        comb_fm_c.addOutput(combFMMO);
        comb_fm_c.addOutput(type_fmMO);
        insertCodelet(comb_fm_c);
        
        //SALIENCY MAP CODELET
        Codelet sal_map_cod = new SalMap(oc.vision, "SALIENCY_MAP", "COMB_FM", "ATTENTIONAL_MAP", Buffersize, 
                Sensor_dimension, print_step);
        sal_map_cod.addInput(combFMMO);
        sal_map_cod.addInput(attMapMO);
        sal_map_cod.addOutput(salMapMO);
        insertCodelet(sal_map_cod);
        
        //DECISION MAKING CODELET
        Codelet dec_mak_cod = new WinnerPicker(oc.vision, "WINNERS", "ATTENTIONAL_MAP", "SALIENCY_MAP", 
                Buffersize, Sensor_dimension, print_step);
        dec_mak_cod.addInput(salMapMO);
        dec_mak_cod.addInput(type_fmMO);
        dec_mak_cod.addOutput(winnersMO);
        dec_mak_cod.addOutput(attMapMO);
        insertCodelet(dec_mak_cod);
        
            //CURIOSITY REWARD CODELET
            Codelet reward_cod = new RewardComputerCodelet(oc, Buffersize, Sensor_dimension, mode, motivation, "", "REWARDS_STRING_OUTPUT", num_tables);
            reward_cod.addInput(salMapMO);
            reward_cod.addInput(winnersMO);        
            if(motivation.equals("drives")){
                reward_cod.addInput(motivationMC);
              }

            reward_cod.addInput(actionsMO);
            reward_cod.addOutput(rewardsMO);      
            insertCodelet(reward_cod);
            
            
            Codelet learner_cod = new LearnerCodelet(oc.vrep, oc.clientID, oc, Buffersize, mode, motivation,
                    "", "QTABLE", num_tables,this.seed );
            learner_cod.addInput(salMapMO);
            learner_cod.addInput(rewardsMO);
            learner_cod.addInput(actionsMO);
            learner_cod.addInput(statesMO);
            if(motivation.equals("drives")){
                learner_cod.addInput(motivationMC);
              }
            learner_cod.addOutput(qtableMO);
            insertCodelet(learner_cod);
            
            
        
        
        Codelet decision_cod = new DecisionCodelet(oc, Buffersize, Sensor_dimension, mode, motivation, num_tables);
         decision_cod.addInput(salMapMO);
        if(motivation.equals("drives")) decision_cod.addInput(motivationMC);
        decision_cod.addInput(qtableMO);
        decision_cod.addInput(rewardsMO);
        decision_cod.addOutput(actionsMO);
        decision_cod.addOutput(statesMO);
        insertCodelet(decision_cod);
        
        Codelet action_exec_cod = new ActionExecCodelet(oc,  mode, Buffersize, Sensor_dimension, num_tables);
         action_exec_cod.addInput(salMapMO);
         action_exec_cod.addInput(winnersMO);
         action_exec_cod.addInput(vision_color_fmMO);
         action_exec_cod.addInput(depth_fmMO);
         action_exec_cod.addInput(actionsMO);
        if(motivation.equals("drives")) action_exec_cod.addInput(motivationMC);         
         action_exec_cod.addOutputs(motorMOs);
         action_exec_cod.addOutput(desFeatCMO);
         action_exec_cod.addOutput(desFeatDMO);
         action_exec_cod.addOutput(desFeatRMO);
         insertCodelet(action_exec_cod);
         
        // Assimilation
        Codelet assimilation_cod = new AssimilationCodelet(oc, motivation, num_tables);
        assimilation_cod.addInput(actionsMO);
        assimilation_cod.addInput(statesMO);
        assimilation_cod.addInput(rewardsMO);
        if(motivation.equals("drives")){
            assimilation_cod.addInput(motivationMC);
        }
        assimilation_cod.addOutput(proceduralMO);
        insertCodelet(assimilation_cod);
        
        // Acommodation
        Codelet acommodation_cod = new AcommodationCodelet(oc, motivation, num_tables);
        acommodation_cod.addInput(actionsMO);
        acommodation_cod.addInput(statesMO);
        acommodation_cod.addInput(rewardsMO);
        
        if(motivation.equals("drives")){
            acommodation_cod.addInput(motivationMC);
        }
        acommodation_cod.addOutput(proceduralMO);
        insertCodelet(acommodation_cod);
        
        if(motivation.equals("drives")){
            // Motivation
            Codelet curiosity_motivation_cod = new CuriosityDrive_MotivationCodelet("Curiosity_Motivation", 0.0, 1.0, 0.0, oc,num_tables);
            curiosity_motivation_cod.addInput(actionsMO);
            curiosity_motivation_cod.addInput(statesMO);
            curiosity_motivation_cod.addInput(proceduralMO);
            curiosity_motivation_cod.addOutput(motivationMC);
            insertCodelet(curiosity_motivation_cod);


        }
        

        ///////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////
        
        // NOTE Sets the time interval between the readings
        // sets a time step for running the codelets to avoid heating too much your machine
        for (Codelet c : this.getCodeRack().getAllCodelets())
            c.setTimeStep(200);
	
        try {
            Thread.sleep(200);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        
     
	// Start Cognitive Cycle
	start(); 
	
    }
}
