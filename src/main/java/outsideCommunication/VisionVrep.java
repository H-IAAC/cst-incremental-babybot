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
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.representation.idea.Idea;
import codelets.support.MLflowLogger;
import coppelia.CharWA;
import coppelia.FloatWA;
import coppelia.IntWA;

import coppelia.IntW;
import coppelia.remoteApi;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
 import java.util.List;   
import java.util.Collections; 

import java.io.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;   

import java.time.format.DateTimeFormatter;  
import java.time.LocalDateTime;    

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;


/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class VisionVrep implements SensorI{
    private final IntW vision_handles;
    private final remoteApi vrep;
    private final int clientID; 
    private  int time_graph;
    private List<Float> vision_data;   
    private int stage, num_epoch, nact;    
    private int res = 256;
    private int max_time_graph=100;
    private int MAX_ACTION_NUMBER = 500;
	private boolean mlf = false, debug = true, aux_a=false, next_act = true, next_actR = true;
    private int max_epochs;
    private ArrayList<Float> lastLinef;
    private ArrayList<Integer> lastLinei;
    private ArrayList<String> executedActions;
    private String mtype, lastAction;
    private String runId="";
    public VisionVrep(remoteApi vrep, int clientid, IntW vision_handles, int max_epochs, int num_tables, 
            int stage, int exp, String runId, int res, int max_time_graph, int MAX_ACTION_NUMBER) {
        this.time_graph = 0;
        
        vision_data = Collections.synchronizedList(new ArrayList<>(res*res*3));
        this.vrep = vrep;
        this.stage =stage;
       this.num_epoch = exp;
       if(mlf) this.runId = runId;
        this.nact = 0;
        this.vision_handles = vision_handles;
        clientID = clientid;
        this.max_epochs = max_epochs;
        for (int i = 0; i < res*res*3; i++) {
            vision_data.add(0f);
        }    
        lastLinef = new ArrayList();
        lastLinei = new ArrayList();
        executedActions = new ArrayList();
        this.res = res;
        this.max_time_graph = max_time_graph;
        // Float Global_Reward, _, _, CurV, CurD, Instant_Reward
        // Int n_tables, exp, _, _, act_n, _
        
        for(int i=0;i<8;i++){
            lastLinef.add(0f);
            lastLinei.add(0);
        }

        lastLinei.set(1, num_epoch);
        next_act = true;
        next_actR = true;
        
// Step 1: Start a new experiment run

    if(this.num_epoch==1){
        if(mlf){
            runId = MLflowLogger.startRun(num_tables+"QTable"+num_epoch);
        
        if (runId == null) {
            System.out.println("Failed to start an MLflow run. Exiting...");
            return;
        }
        }
    }}
    
    @Override
    public boolean getNextActR(){
        return next_actR;
    }
     @Override
    public void setNextActR(boolean next_ac){
        this.next_actR=next_ac;
    }
    
    @Override
    public boolean getNextAct(){
        return next_act;
    }
     @Override
    public void setNextAct(boolean next_ac){
        this.next_act=next_ac;
    }
    
    @Override
    public String gettype(){
        return this.mtype;
    }
    @Override
    public String getLastAction() {
            
        return  this.lastAction;
    }
    
    @Override
    public void setLastAction(String a) {
        this.lastAction=a;
    }
    
    @Override
    public void addAction(String a) {
       executedActions.add(a);
    }

    @Override
    public ArrayList<String> getExecutedAct() {
        return executedActions;
    }
    
    @Override
    public float getFValues(int i) {
        return lastLinef.get(i);
    }
    
    @Override
    public void setFValues(int i, float f) {
       lastLinef.set(i, f);
    }
    
    @Override
    public float getIValues(int i) {
        return lastLinei.get(i);
    }
    
    @Override
    public void setIValues(int i, int f) {
        if(i==4 && f>this.lastLinei.get(i)+1){
            f-=1;
        }
       lastLinei.set(i, f);
    }
    
    private void setResizedColorData(char[] pixels_red, char[] pixels_green, char[] pixels_blue, int f){
	float MeanValue_r = 0;
        float MeanValue_g = 0;
        float MeanValue_b = 0;
        for(int n = 0;n<res/f;n++){
            int ni = (int) (n*f);
            int no = (int) (f+n*f);
            for(int m = 0;m<res/f;m++){    
                int mi = (int) (m*f);
                int mo = (int) (f+m*f);
                for (int y = ni; y < no; y++) {
                    for (int x = mi; x < mo; x++) {
                        float Fvalue_r = (float) pixels_red[y*res+x];     
                        MeanValue_r += Fvalue_r;
                        float Fvalue_g = (float) pixels_green[y*res+x];     
                        MeanValue_g += Fvalue_g;
                        float Fvalue_b = (float) pixels_blue[y*res+x];     
                        MeanValue_b += Fvalue_b;
                    }
                }
                float correct_mean_r = MeanValue_r/(f*f);
                float correct_mean_g = MeanValue_g/(f*f);
                float correct_mean_b = MeanValue_b/(f*f);
                for (int y = ni; y < no; y++) {
                    for (int x = mi; x < mo; x++) {
                        vision_data.set(3*(y*res+x), correct_mean_r);
                        vision_data.set(3*(y*res+x)+1, correct_mean_g);
                        vision_data.set(3*(y*res+x)+2, correct_mean_b);
                    }
                }
                        //System.out.println("Mean_r: "+ correct_mean_r + " Mean_g: "+ correct_mean_g +" Mean_b: "+ correct_mean_b +" ni: "+ni+" no: "+no+" mi: "+mi+" mo: "+mo);
                MeanValue_r = 0;
                MeanValue_g = 0;
                MeanValue_b = 0;
            }
        }         
    }
    @Override
    public boolean endEpoch(){
        /*try {
			Thread.sleep(20);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}*/
        
        FloatWA position = new FloatWA(3);
	vrep.simxGetObjectPosition(clientID, vision_handles.getValue(), -1, position,
        vrep.simx_opmode_streaming);
	boolean m_act;
        m_act = lastLinei.get(4)>this.getMaxActions();
        
//	printToFile(position.getArray()[2], "positions.txt");
        //if(debug) System.out.println("Marta on exp "+this.getEpoch()+" with z = "+position.getArray()[2]);        
        if (position.getArray()[2] < 0.35 || position.getArray()[0] > 0.2  || m_act || lastLinei.get(2)==1) {
            
            if(mlf){
             MLflowLogger.logMetric(runId, "Total_Actions", lastLinei.get(4), lastLinei.get(1));
            MLflowLogger.logMetric(runId, "GlobalReward", lastLinef.get(0), lastLinei.get(1));
            MLflowLogger.logMetric(runId, "InstantReward", lastLinef.get(5), lastLinei.get(1));
            MLflowLogger.logMetric(runId, "Cur_Drive", lastLinef.get(3), lastLinei.get(1));
            
            OperatingSystemMXBean osBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

            double systemCpuLoad = osBean.getSystemCpuLoad() * 100; // CPU load in percentage
        double processCpuLoad = osBean.getProcessCpuLoad() * 100;
        long freeMemory = osBean.getFreePhysicalMemorySize(); // Free memory in bytes
        long totalMemory = osBean.getTotalPhysicalMemorySize();

           

 // Log metrics to MLflow
        MLflowLogger.logMetric(runId, "system_cpu_load", systemCpuLoad, lastLinei.get(1));
        MLflowLogger.logMetric(runId, "process_cpu_load", processCpuLoad, lastLinei.get(1));
        MLflowLogger.logMetric(runId, "free_memory", freeMemory / (1024 * 1024), lastLinei.get(1)); // Convert to MB
        MLflowLogger.logMetric(runId, "total_memory", totalMemory / (1024 * 1024), lastLinei.get(1)); // Convert to MB

            }
            System.out.println("Marta crashed on exp "+this.getEpoch()+" with z = "+position.getArray()[2]+
                    " Act:"+lastLinei.get(4));
                            
            printToFile("rewards.txt",true);
            printToFile("nrewards.txt",false);
            
            vrep.simxPauseCommunication(clientID, true);
            vrep.simxStopSimulation(clientID, vrep.simx_opmode_oneshot_wait);
            
            vrep.simxPauseCommunication(clientID, false);
            vrep.simxStartSimulation(clientID, remoteApi.simx_opmode_oneshot_wait);
            /*try {
			Thread.sleep(20);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}  */
            
            lastLinei.set(1, lastLinei.get(1)+1);
            
            lastLinef.set(0,(float) 0);
            lastLinef.set(1,(float) 0);
            lastLinef.set(2,(float) 0);
            lastLinef.set(3,(float) 1);
            lastLinef.set(4,(float) 0);
            lastLinef.set(5,(float) 0);
            lastLinef.set(6,(float) 0);
            lastLinef.set(7,(float) 0);
            
            lastLinei.set(4,0);
            //lastLinei.set(5,100);
            lastLinei.set(6,0);
            lastLinei.set(7,0);
            lastLinei.set(2,0);
            executedActions.clear();
            this.setNextAct(true);
            this.setNextActR(true);
            aux_a = false;
            if (lastLinei.get(0) == 1 && lastLinei.get(1)  > this.getMaxEpochs()) {
                   if(mlf) MLflowLogger.endRun(runId);
                System.exit(0);
            } 
           

            
            return true;
        }
           
             printToFile("nrewards.txt",false);
             this.setNextAct(true);
            this.setNextActR(true);
           
            return false;
    }
    
    @Override
    public boolean endEpochR(){
      /*  try {
			Thread.sleep(20);
		} catch (Exception e) {
			Thread.currentThread().interrupt();
		}*/
      //System.out.println("End epoch R");
        FloatWA position = new FloatWA(3);
	vrep.simxGetObjectPosition(clientID, vision_handles.getValue(), -1, position,
        vrep.simx_opmode_streaming);
	boolean m_act;
        m_act = lastLinei.get(4)>this.getMaxActions();
        
        return this.getEpoch() > 1 && (position.getArray()[2] < 0.35 || position.getArray()[0] > 0.2  || m_act);
    }
    
    @Override
    public int getMaxActions() {
            
        return  MAX_ACTION_NUMBER;
    }
    
    @Override
    public int getMaxEpochs() {
            
        return  this.max_epochs;
    }
    
    @Override
    public int getEpoch() {
        return lastLinei.get(1);
    }
    
   
    
    @Override
    public void setEpoch(int newEpoch) {
       this.lastLinei.set(1, newEpoch);    
    }
    
  
    
    @Override
    public int getnAct(){
        return this.lastLinei.get(4);
    }
    
    @Override
    public void setnAct(int a){
        if(a>this.lastLinei.get(4)+1){
            a-=1;
        }
        this.lastLinei.set(4, a);
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
       /*try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
*/
        if ( vision_handles.getValue() == 0) {
                        System.err.println("Vision Invalid clientID or vision handle. Exiting...");
                        return vision_data; // Exit if critical values are uninitialized
                    }
        
        char temp_RGB[];                            //char Array to get RGB data of Vision Sensor
        
        CharWA image_RGB = new CharWA(res*res*3);           //CharWA that returns RGB data of Vision Sensor
        IntWA resolution = new IntWA(2);            //Array to get resolution of Vision Sensor
        int ret_RGB;
        long startTime = System.currentTimeMillis();
        
        int retries = 3;
        while (retries > 0) {
            try {
                        ret_RGB = vrep.simxGetVisionSensorImage(clientID, vision_handles.getValue(), resolution, 
                        image_RGB, 0, vrep.simx_opmode_streaming); 

                if (ret_RGB == remoteApi.simx_return_ok) {
                    break;  // Exit loop if call is successful
                }
            } catch (Exception e) {
                //System.out.println("Error retrieving vision buffer, retrying...");
                retries--;
                if (retries == 0) {
                    System.out.println("Failed to retrieve vision buffer after retries. Exiting gracefully.");
                    break;
                }
            }
        }


        try {
         }
        catch(Exception e){
        System.out.println("error vision ");
    }
        while (System.currentTimeMillis()-startTime < 2000)
        {
            ret_RGB = vrep.simxGetVisionSensorImage(clientID, vision_handles.getValue(), resolution, image_RGB, 0, 
                    remoteApi.simx_opmode_buffer);
            if (ret_RGB == remoteApi.simx_return_ok  || ret_RGB == remoteApi.simx_return_novalue_flag){
                
                int count_aux = 0; 
                temp_RGB = image_RGB.getArray();
                char[] pixels_red = new char[res*res];
                char[] pixels_green = new char[res*res];
                char[] pixels_blue = new char[res*res];
                
                
                for(int y =0; y < res; y++){  
                    for(int x =0; x < res; x++){  
                        char pixel_red = temp_RGB[3*(y*res+x)];
                        char pixel_green = temp_RGB[3*(y*res+x)+1];
                        char pixel_blue = temp_RGB[3*(y*res+x)+2];
                        pixels_red[count_aux]=pixel_red;
                        pixels_green[count_aux]=pixel_green;
                        pixels_blue[count_aux]=pixel_blue;
                        count_aux += 1;
                    } 
                }
                if(stage==3){
                    int pixel_len = 3;
                    int cont_pix = 0;
                    for(int i =0; i < res*res; i++){
                        vision_data.set(cont_pix, (float)pixels_red[i]);
                        vision_data.set(cont_pix+1, (float)pixels_green[i]);
                        vision_data.set(cont_pix+2, (float)pixels_blue[i]);
                        cont_pix += pixel_len;
                         
                    }
                }
                
                if(stage==2) setResizedColorData(pixels_red, pixels_green, pixels_blue, 2);
                if(stage==1) setResizedColorData(pixels_red, pixels_green, pixels_blue, 4);
                
            } else{
                int count_aux = 0; 
                for(int y =0; y < res; y++){  
                    for(int x =0; x < res; x++){  
                        vision_data.set(count_aux, new Float(0));
                        vision_data.set(count_aux+1, new Float(0));
                        vision_data.set(count_aux+2, new Float(0));
                        count_aux += 3;
                    }
                }
            }

        
        
        }
        
        // SYNC

       // printToFile(vision_data);        
        return  vision_data;
    }
    

	@Override
	public void resetData() {
		// TODO Auto-generated method stub
		
	}

    @Override
    public int getAux() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
    private void printToFile(String filename, boolean debugp){
    
        
        try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                String s = " QTables:"+lastLinei.get(0)+
                        " Exp:"+lastLinei.get(1)+
                        " Nact:"+lastLinei.get(4)+ 
                        " Ri:"+lastLinef.get(5)+
                        " CurV:"+lastLinef.get(3)+" dCurV:"+lastLinef.get(4)+
                        " G_Reward:"+lastLinef.get(0)+" Ri:"+lastLinef.get(5)+
                        " LastAct: "+lastAction;
                out.println(s);
                
                s = " QTables:"+lastLinei.get(0)+
                        " Exp:"+lastLinei.get(1)+
                        " Nact:"+lastLinei.get(4)+ 
                        " Ri:"+lastLinef.get(5)+
                        " CurV:"+lastLinef.get(3)+" dCurV:"+lastLinef.get(4)+
                        " G_Reward:"+lastLinef.get(0)+" Ri:"+lastLinef.get(5)+
                        " LastAct: "+lastAction;
                
                if(debugp) System.out.println(s);
                
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        
      
    }
    
}
