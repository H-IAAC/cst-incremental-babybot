/*
 * /*******************************************************************************
 *  * Copyright (c) 2012  DCA-FEEC-UNICAMP
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3
 *  * which accompanies this depthribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  * 
 *  * Contributors:
 *  *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *  ******************************************************************************/
 
package codelets.sensors;

import CommunicationInterface.SensorI;
import br.unicamp.cst.core.entities.MemoryObject;
import sensory.FeatMapCodelet;
import codelets.motor.Lock;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import static java.lang.Math.abs;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class BU_FM_Depth extends FeatMapCodelet {
private final float mr = 10;                     //Max Value for VisionSensor
private final int max_time_graph=100;
private final int res = 256;                     //Resolution of VisionSensor
private  int time_graph,print_step;
private final int slices = 16;                    //Slices in each coordinate (x & y) 
private SensorI vision;
private boolean debug = false;
    public BU_FM_Depth(SensorI vision, int nsensors, ArrayList<String> sens_names, 
            String featmapname,int timeWin, int mapDim, int print_step) {
        super(nsensors, sens_names, featmapname,timeWin,mapDim);
        this.time_graph = 0;
        this.vision = vision;
        this.print_step=print_step;
    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {
        /*try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }*/
        MemoryObject depth_bufferMO = (MemoryObject) sensor_buffers.get(1);        //Gets Data
        
        List depthData_buffer;
        depthData_buffer = (List) depth_bufferMO.getI();

        List depthFM = (List) featureMap.getI();        
        
        if(depthFM.size() == timeWindow){
            depthFM.remove(0);
        }
        
        depthFM.add(new ArrayList<>());
                
        int t = depthFM.size()-1;
        
        
        ArrayList<Float> depthFM_t = (ArrayList<Float>) depthFM.get(t);
        
        for (int j = 0; j < mapDimension; j++) {
            depthFM_t.add(new Float(0));
        }
                
        MemoryObject depthDataMO;
        if(depthData_buffer.size() < 1){
            return;
        }
        depthDataMO = (MemoryObject)depthData_buffer.get(depthData_buffer.size()-1);

        List depthData;

        depthData = (List) depthDataMO.getI();
        
        if(depthData.size() < 1){
            return;
        }
        if(debug)  System.out.println("FM_BU depthData len: "+depthData.size());         
        
        Float Fvalue;
        float MeanValue = 0;
        ArrayList<Float> depth_mean_red = new ArrayList<>();
        ArrayList<Float> n_depth = new ArrayList<>();
        
        // get mean all elements
        float sum = 0;
        for (var value : depthData) {
            sum += (float) value;
        }
    
        float mean_all = mr-sum / depthData.size();
        
         //Converts res*res image to res/slices*res/slices sensors
        float new_res = (res/slices)*(res/slices);
        float new_res_1_2 = (res/slices);
        int count_mean = 0;
        for(int n = 0;n<slices;n++){
            int ni = (int) (n*new_res_1_2);
            int no = (int) (new_res_1_2+n*new_res_1_2);
            for(int m = 0;m<slices;m++){    
                int mi = (int) (m*new_res_1_2);
                int mo = (int) (new_res_1_2+m*new_res_1_2);
                for (int y = ni; y < no; y++) {
                    for (int x = mi; x < mo; x++) {
                        Fvalue = (Float) depthData.get(y*res+x);
                        if(Fvalue != 0) Fvalue = mr - Fvalue;
                        MeanValue += Fvalue;
                        count_mean++;
                    }
                }
                float correct_mean = MeanValue/new_res - mean_all;
                //System.out.println("Mean: "+ correct_mean +" Count_mean: "+count_mean+" ni: "+ni+" no: "+no+" mi: "+mi+" mo: "+mo);
                depth_mean_red.add(correct_mean/mr);
                MeanValue = 0;
                count_mean=0;
            }
        }
        
        for (int j = 0; j < depth_mean_red.size(); j++) {
           
            depthFM_t.set(j, depth_mean_red.get(j));
        }   
        printToFile(depthFM_t,"depthFM.txt");
    }
   private void printToFile(Object object,String filename    ){
        if(this.vision.getEpoch() %print_step == 0){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
      
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+"_"+(int) this.vision.getIValues(1)+"_"+(int)this.vision.getIValues(4) +"_"+time_graph+" "+ object);
                //if(time_graph == max_time_graph-1) System.out.println(filename+": "+time_graph);          
                time_graph++;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
}
    

