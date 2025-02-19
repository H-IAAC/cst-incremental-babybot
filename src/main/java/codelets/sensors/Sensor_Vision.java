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

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class Sensor_Vision extends Codelet {
    private MemoryObject vision_read;
    private SensorI vision;
    private int stage;
    
    public Sensor_Vision(SensorI vision){
        this.vision = vision;
        this.stage = this.vision.getStage();
    }

    public int getStage() {
        return this.stage;    
    }
    
    public void setStage(int newstage) {
        this.stage = newstage;    
    }
    
    
    @Override
    public void accessMemoryObjects() {
        vision_read = (MemoryObject) this.getOutput("VISION");
    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {

        vision_read.setI(vision.getData());
        this.stage = vision.getStage();
        
    }
    
}
