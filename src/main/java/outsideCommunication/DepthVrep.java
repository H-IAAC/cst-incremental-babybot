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

import CommunicationInterface.SensorI;
import coppelia.FloatWA;
import coppelia.IntWA;
import coppelia.IntW;
import coppelia.remoteApi;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;

public class DepthVrep implements SensorI {
    private final IntW vision_handles;
    private final remoteApi vrep;
    private final int clientID;
    private int time_graph;
    private List<Float> depth_data;
    private int stage;
    private final int res = 256, print_step = 1;
    private final int max_time_graph = 100;
    private SensorI vision;
    private boolean debug = false; // Enable debug logging

    public DepthVrep(remoteApi vrep, int clientid, IntW vision_handles, int stageVision, SensorI vision) {
        this.time_graph = 0;
        depth_data = Collections.synchronizedList(new ArrayList<>(res * res));
        this.vrep = vrep;
        this.stage = stageVision;
        this.vision = vision;
        this.vision_handles = vision_handles;
        this.clientID = clientid;

        for (int i = 0; i < res * res; i++) {
            depth_data.add(0f);
        }
    }

    @Override
    public int getStage() {
        return this.stage;
    }

    @Override
    public void setStage(int newstage) {
        this.stage = newstage;
    }

    @Override
    public Object getData() {
/*        try {
            Thread.sleep(200);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }*/
        IntWA resolution = new IntWA(2);
        FloatWA auxValues_WA = new FloatWA(res * res);
        float[] temp_dep;

        int read_depth;
        long startTime = System.currentTimeMillis();
        int retries = 3;

        synchronized (vrep) { // Ensure thread safety with remote API calls
            while (retries > 0) {
                try {
                    if ( vision_handles.getValue() == 0) {
                        System.err.println("Depth Invalid clientID or vision handle. Exiting...");
                        return depth_data; // Exit if critical values are uninitialized
                    }

                    read_depth = vrep.simxGetVisionSensorDepthBuffer(clientID, vision_handles.getValue(), resolution, auxValues_WA, vrep.simx_opmode_streaming);
                    if (read_depth == remoteApi.simx_return_ok) {
                        break; // Exit loop if call is successful
                    } else {
                        if (debug) System.out.println("Depth buffer retrieval failed, retrying...");
                    }
                } catch (Exception e) {
                    //System.err.println("Error retrieving depth buffer: " + e.getMessage());
                    retries--;
                    if (retries == 0) {
                        System.out.println("Failed to retrieve depth buffer after retries. Exiting gracefully.");
                        return depth_data;
                    }
                }
            }

            while (System.currentTimeMillis() - startTime < 2000) {
                try {
                    read_depth = vrep.simxGetVisionSensorDepthBuffer(clientID, vision_handles.getValue(), resolution, auxValues_WA, remoteApi.simx_opmode_buffer);
                    if (read_depth == remoteApi.simx_return_ok || read_depth == remoteApi.simx_return_novalue_flag) {
                        temp_dep = auxValues_WA.getArray();
                        float[] depth_or = new float[res * res];
                        processDepthData(temp_dep, depth_or);
                        return depth_data;
                    } else {
                        resetDepthData();
                    }
                } catch (Exception e) {
                    System.out.println("Error processing depth data: " + e.getMessage());
                }
            }
        }
        return depth_data;
    }

    private void processDepthData(float[] temp_dep, float[] depth_or) {
        int count_aux = 0;
        for (int y = 0; y < res; y++) {
            for (int x = 0; x < res; x++) {
                float depth_c = temp_dep[y * res + x];
                depth_or[count_aux] = Math.min(Math.max(depth_c * 10, 0), 10);
                count_aux++;
            }
        }

        switch (stage) {
            case 3:
                for (int i = 0; i < res * res; i++) {
                    depth_data.set(i, depth_or[i]);
                }
                break;
            case 2:
                downscaleData(depth_or, 2);
                break;
            case 1:
                downscaleData(depth_or, 4);
                break;
        }
    }

    private void downscaleData(float[] depth_or, int factor) {
        float meanValue;
        for (int n = 0; n < res / factor; n++) {
            int ni = n * factor;
            int no = ni + factor;
            for (int m = 0; m < res / factor; m++) {
                int mi = m * factor;
                int mo = mi + factor;
                meanValue = 0;

                for (int y = ni; y < no; y++) {
                    for (int x = mi; x < mo; x++) {
                        meanValue += depth_or[y * res + x];
                    }
                }

                float correct_mean = meanValue / (factor * factor);
                for (int y = ni; y < no; y++) {
                    for (int x = mi; x < mo; x++) {
                        depth_data.set(y * res + x, correct_mean);
                    }
                }
            }
        }
    }

    private void resetDepthData() {
        for (int i = 0; i < res * res; i++) {
            depth_data.set(i, 0f);
        }
    }
	@Override
	public void resetData() {
		// TODO Auto-generated method stub
		
	}

    @Override
    public void setEpoch(int exp) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getEpoch() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getAux() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int getMaxActions() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int getMaxEpochs() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

   


    @Override
    public boolean endEpoch() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public int getnAct() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setnAct(int a) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public float getFValues(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setFValues(int i, float f) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public float getIValues(int i) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setIValues(int i, int f) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ArrayList<String> getExecutedAct() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void addAction(String a) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean endEpochR() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String getLastAction() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setLastAction(String a) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public String gettype() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setNextAct(boolean next_ac) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean getNextAct() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public boolean getNextActR() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void setNextActR(boolean next_ac) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


}
