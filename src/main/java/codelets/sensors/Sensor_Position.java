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
import outsideCommunication.OutsideCommunication;

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class Sensor_Position extends Codelet {
    private MemoryObject position_read;
    private SensorI position, vision;
    private int stage;
    private OutsideCommunication oc;
    private float yawPos = 0f, headPos = 0f; 
    public Sensor_Position(OutsideCommunication oc){
        this.oc = oc;
    }

   
    
    @Override
    public void accessMemoryObjects() {
        position_read = (MemoryObject) this.getOutput("POSITION");
    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {
        try {
            yawPos = oc.NeckYaw_m.getSpeed();
            headPos = oc.HeadPitch_m.getSpeed(); 
                System.out.println("Rewards - yawPos: "+yawPos+" headPos: "+headPos);
        } catch (Exception e) {
             System.out.println("getSpeed null ");
            return;
        }
        double MartaX = -0.0609;
        double MartaY = -2.0745;
        double MartaZ = 0.58;
        float[] posPioneer = oc.vision.getPosition("Pioneer1");
        double dx = posPioneer[0] - MartaX;
        double dy = posPioneer[1] - MartaY;
        double dz = posPioneer[2] - MartaZ;
        // ângulo do Pioneer nos planos XY e YZ
        double targetYaw = Math.atan2(dy, dx); // radianos
        double targetYawDeg = Math.toDegrees(targetYaw);
        
        double distXY = Math.sqrt(dx*dx + dy*dy);

        double targetPitch = Math.atan2(dz, distXY);
        double targetPitchDeg = Math.toDegrees(targetPitch);
        // converte neckYaw (yawPos) e neckPitch (pitchPos) para graus

        //System.out.println("Target angle (rad): " + targetYaw);
        //System.out.println("Target angle (deg): " + targetYawDeg);

        // converte neckYaw (yawPos) para graus
        double neckYawDeg = Math.toDegrees(yawPos);
        double headPitchDeg = Math.toDegrees(headPos);
        // calcula diferença corrigindo offset de 90° do eixo do sensor
        double yawDiff = targetYawDeg - (neckYawDeg + 90);
        yawDiff = ((yawDiff + 180) % 360) - 180;

        double pitchDiff = targetPitchDeg - headPitchDeg;
        pitchDiff = ((pitchDiff + 180) % 360) - 180;
        System.out.println("Yaw diff (deg): " + Math.abs(yawDiff));
System.out.println("pitch Diff (deg): " + Math.abs(pitchDiff));

        // verifica se está dentro do FOV 2D (horizontal e vertical)
        if (Math.abs(yawDiff) < 30 && Math.abs(pitchDiff) < 30) {
            oc.vision.setIValues(5, 1);
        } else {
            oc.vision.setIValues(5, 0);
        }

        oc.vision.setFValues(7, (float) yawDiff);
        oc.vision.setFValues(8, (float) pitchDiff);   
    }
    
}
