
// Unused file. Intend to remove set motors from learner

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.behavior;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.io.IOException;
import java.util.List;
import java.util.stream.IntStream;
import outsideCommunication.OutsideCommunication;

/**
 *
 * @author leolellisr
 */
public class BehavioralCodelet extends Codelet {
    
    private List actions;
    
    private MemoryObject motorActionMO;
    private MemoryObject neckMotorMO;
    private MemoryObject headMotorMO;
    private MemoryObject foveaMO;
    
    private OutsideCommunication oc;
    
    private int stage;
    
    private float vel = 2f,angle_step;
    
    private int[] posLeft = {0, 4, 8, 12};
    private int[] posRight = {3, 7, 11, 15};
    private int[] posUp = {12, 13, 14, 15};
    private int[] posDown = {0, 1, 2, 3};
    private int[] posCenter = {5, 6, 9, 10};
    
    private int[] fovea0 = {0, 1, 4, 5};
    private int[] fovea1 = {2, 3, 6, 7};
    private int[] fovea2 = {8, 9, 12, 13};
    private int[] fovea3 = {10, 11, 14, 15};
    public BehavioralCodelet (OutsideCommunication outc, int tWindow, int sensDim, String mode) throws IOException {
        oc = outc;
        this.stage = this.oc.vision.getStage();
        if(this.stage == 1) angle_step = 0.1f;
	if(this.stage == 2) angle_step = 0.05f;
	if(this.stage == 3) angle_step = 0.01f;
		
    }
    
    // This method is used in every Codelet to capture input, broadcast 
	// and output MemoryObjects which shall be used in the proc() method. 
	// This abstract method must be implemented by the user. 
	// Here, the user must get the inputs and outputs it needs to perform proc.
	@Override
	public void accessMemoryObjects() {
		
		MemoryObject MO;
                MO = (MemoryObject) this.getInput("ACTIONS");
                actions = (List) MO.getI();
                
                
                motorActionMO = (MemoryObject) this.getOutput("MOTOR");
                neckMotorMO = (MemoryObject) this.getOutput("NECK_YAW");
                headMotorMO = (MemoryObject) this.getOutput("HEAD_PITCH");
                foveaMO = (MemoryObject) this.getOutput("FOVEA");
                        
                

	}
        
        @Override
	public void calculateActivation() {
		// TODO Auto-generated method stub	
	}
        
    // Main Codelet function, to be implemented in each subclass.
	@Override
	public void proc() {
                long start_proc = System.currentTimeMillis();
                float yawPos = oc.NeckYaw_m.getSpeed();
                float headPos = oc.HeadPitch_m.getSpeed(); 
                System.out.println("yawPos: "+yawPos+" headPos: "+headPos);
		try {
                    Thread.sleep(50);
                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }
                int actionToTake = (int) actions.get(actions.size() -1);
                    if (actionToTake == 1) {
                                yawPos = yawPos-angle_step;
                                neckMotorMO.setI(yawPos);
                        }

                        else if (actionToTake == 2) {
                                yawPos = yawPos+angle_step;
                                neckMotorMO.setI(yawPos);
                                
                        }
                        else if (actionToTake == 3) {
                                headPos = headPos-angle_step;
                                headMotorMO.setI(headPos);
                                 
                        }
                        else if (actionToTake == 4) {
                                headPos = headPos+angle_step;
                                headMotorMO.setI(headPos);
                                 
                        }
                        else if (actionToTake == 5) {
                            foveaMO.setI(0);
                             
                        }
                        else if (actionToTake == 6) {
                            foveaMO.setI(1);
                             
                        }
                        else if (actionToTake == 7) {
                            foveaMO.setI(2);
                             
                        }
                        else if (actionToTake == 8) {
                            foveaMO.setI(3);
                             
                        }
                        else if (actionToTake == 9) {
                            foveaMO.setI(4);
                             
                        }
               
        }        
}


