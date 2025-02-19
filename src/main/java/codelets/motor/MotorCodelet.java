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
    
package codelets.motor;

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import CommunicationInterface.MotorI;

public class MotorCodelet extends Codelet {
    
    private MemoryObject motorActionMO;
    private MemoryObject head_pitch_pos_MO, neck_yaw_pos_MO; 
    private final MotorI head_pitch_m, neck_yaw_m;
    
    private final int MOVEMENT_TIME = 2000; // 2 seconds
    
    public MotorCodelet(MotorI head_pitch, MotorI neck_yaw){
    	super();
        head_pitch_m = head_pitch;
        neck_yaw_m = neck_yaw;
    }

    @Override
    public void accessMemoryObjects() {
        motorActionMO = (MemoryObject) this.getInput("MOTOR");
        neck_yaw_pos_MO = (MemoryObject) this.getInput("NECK_YAW");
        head_pitch_pos_MO = (MemoryObject) this.getInput("HEAD_PITCH");
    }

    @Override
    public void calculateActivation() {

    }

    @Override
    public void proc() {
    	try {
            Thread.sleep(2000);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
    	
//    	String action = (String) motorActionMO.getI();
        neck_yaw_m.setSpeed((float) neck_yaw_pos_MO.getI());
        head_pitch_m.setSpeed((float) head_pitch_pos_MO.getI());
    }
    
}
