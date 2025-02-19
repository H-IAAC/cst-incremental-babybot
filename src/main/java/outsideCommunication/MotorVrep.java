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
 
package outsideCommunication;

import coppelia.remoteApi;
import CommunicationInterface.MotorI;
import coppelia.FloatW;

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class MotorVrep implements MotorI {
    
    private float speed;        //Position of joint
    private final remoteApi vrep;
    private final int clientID;
    private final int motor_handle;
    private final boolean debug = false;
    public MotorVrep(remoteApi rApi_, int clientid, int mot_han){
        vrep = rApi_;
        clientID = clientid;
        motor_handle = mot_han;   
        speed=0;
    }
    
    @Override
    public float getSpeed() {  //Actually gets position of joint
        return speed;
    }

    @Override
    public int setSpeed(float speed) {  //Actually sets new position on joint
        this.speed = speed;
        //vrep.simxPauseCommunication(clientID,true);
        //vrep.simxPauseCommunication(clientID, true);
        int ret = vrep.simxSetJointTargetPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_oneshot);
        
        if( ret == remoteApi.simx_error_noerror) {
    	   
            if(debug) System.out.println(motor_handle+" new_pos 0:"+speed);
            //vrep.simxPauseCommunication(clientID,false);
            //vrep.simxSynchronousTrigger(clientID);
           return 0;
       } 
       
        /*int ret1 = vrep.simxSetJointTargetPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_buffer);
        if( ret1 == remoteApi.simx_error_noerror) {
    	   
            System.out.println(motor_handle+" new_pos 1:"+speed);
            vrep.simxPauseCommunication(clientID,false);
           return 0;
       } else System.out.println(motor_handle+" ret 1:"+ret1);
       
        int ret2 = vrep.simxSetJointTargetPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_streaming);
        if(ret2 == remoteApi.simx_error_noerror){
    	   
            System.out.println(motor_handle+" new_pos 2:"+speed);
            vrep.simxPauseCommunication(clientID,false);
           return 0;
       } else System.out.println(motor_handle+" ret 2:"+ret2);
       
        int ret3 = vrep.simxSetJointPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_oneshot);
        if(ret3 == remoteApi.simx_error_noerror){
    	   
            System.out.println(motor_handle+" new_pos 3:"+speed);
            vrep.simxPauseCommunication(clientID,false);
           return 0;
       } else System.out.println(motor_handle+" ret 3:"+ret3);
        
        int ret4 = vrep.simxSetJointPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_buffer);
        if(ret4 == remoteApi.simx_error_noerror){
    	   
            System.out.println(motor_handle+" new_pos 4:"+speed);
            vrep.simxPauseCommunication(clientID,false);
           return 0;
       } else System.out.println(motor_handle+" ret 4:"+ret4);
        
       int ret5 = vrep.simxSetJointPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_streaming);
       if(ret5 == remoteApi.simx_error_noerror) {
    	   
            System.out.println(motor_handle+" new_pos 5:"+speed);
            vrep.simxPauseCommunication(clientID,false);
           return 0;
       }*/ else {
           //System.out.println(motor_handle+" ret 5:"+ret5);
           if(debug) System.out.println(motor_handle+" ret 0:"+ret);
           FloatW pos = new FloatW(-1);
           int err = vrep.simxGetJointPosition(clientID, motor_handle, pos, remoteApi.simx_opmode_buffer);
           if(debug) System.out.println(motor_handle+"Error setting new_pos :"+speed+". Actual pos: "+pos.getValue());
           //vrep.simxPauseCommunication(clientID,false);
           //vrep.simxSynchronousTrigger(clientID);
           return 1;
       }
       
    }
    
    public int setPos(float speed) {  //Actually sets new position on joint
        this.speed = speed;
        //vrep.simxPauseCommunication(clientID,true);
        //vrep.simxPauseCommunication(clientID, true);
        int ret = vrep.simxSetJointTargetPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_oneshot);
        
        if( ret == remoteApi.simx_error_noerror) {
    	   
            if(debug) System.out.println(motor_handle+" new_pos 0:"+speed);
            //vrep.simxPauseCommunication(clientID,false);
            //vrep.simxSynchronousTrigger(clientID);
           return 0;
       } 
       
        /*int ret1 = vrep.simxSetJointTargetPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_buffer);
        if( ret1 == remoteApi.simx_error_noerror) {
    	   
            System.out.println(motor_handle+" new_pos 1:"+speed);
            vrep.simxPauseCommunication(clientID,false);
           return 0;
       } else System.out.println(motor_handle+" ret 1:"+ret1);
       
        int ret2 = vrep.simxSetJointTargetPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_streaming);
        if(ret2 == remoteApi.simx_error_noerror){
    	   
            System.out.println(motor_handle+" new_pos 2:"+speed);
            vrep.simxPauseCommunication(clientID,false);
           return 0;
       } else System.out.println(motor_handle+" ret 2:"+ret2);
       
        int ret3 = vrep.simxSetJointPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_oneshot);
        if(ret3 == remoteApi.simx_error_noerror){
    	   
            System.out.println(motor_handle+" new_pos 3:"+speed);
            vrep.simxPauseCommunication(clientID,false);
           return 0;
       } else System.out.println(motor_handle+" ret 3:"+ret3);
        
        int ret4 = vrep.simxSetJointPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_buffer);
        if(ret4 == remoteApi.simx_error_noerror){
    	   
            System.out.println(motor_handle+" new_pos 4:"+speed);
            vrep.simxPauseCommunication(clientID,false);
           return 0;
       } else System.out.println(motor_handle+" ret 4:"+ret4);
        
       int ret5 = vrep.simxSetJointPosition(clientID, motor_handle, speed, remoteApi.simx_opmode_streaming);
       if(ret5 == remoteApi.simx_error_noerror) {
    	   
            System.out.println(motor_handle+" new_pos 5:"+speed);
            vrep.simxPauseCommunication(clientID,false);
           return 0;
       }*/ else {
           //System.out.println(motor_handle+" ret 5:"+ret5);
           if(debug) System.out.println(motor_handle+" ret 0:"+ret);
           FloatW pos = new FloatW(-1);
           int err = vrep.simxGetJointPosition(clientID, motor_handle, pos, remoteApi.simx_opmode_buffer);
           if(debug) System.out.println(motor_handle+"Error setting new_pos :"+speed+". Actual pos: "+pos.getValue());
           //vrep.simxPauseCommunication(clientID,false);
           //vrep.simxSynchronousTrigger(clientID);
           return 1;
       }
       
    }
}
