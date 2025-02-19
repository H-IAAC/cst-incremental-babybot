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
 
package codelets.sensors;

import CommunicationInterface.SensorI;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import codelets.motor.Lock;

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class Sensor_Depth extends Codelet {
    private MemoryObject depth_read;
    private SensorI depth, vision;
    private int stage;
    
    public Sensor_Depth(SensorI depth, SensorI vision){
        this.depth = depth;
        this.vision = vision;
        this.stage = vision.getStage();
    }

   
    
    @Override
    public void accessMemoryObjects() {
        depth_read = (MemoryObject) this.getOutput("DEPTH");
    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {

        depth_read.setI(depth.getData());
        this.stage = vision.getStage();
        depth.setStage(vision.getStage());
    }
    
}
