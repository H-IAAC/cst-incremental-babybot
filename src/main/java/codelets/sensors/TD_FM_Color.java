/*
 * /*******************************************************************************
 *  * Copyright (c) 2012  DCA-FEEC-UNICAMP
 *  * All rights reserved. This program and the accompanying materials
 *  * are made available under the terms of the GNU Lesser Public License v3
 *  * which accompanies this vision_redribution, and is available at
 *  * http://www.gnu.org/licenses/lgpl.html
 *  * 
 *  * Contributors:
 *  *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *  ******************************************************************************/
 
package codelets.sensors;

import CommunicationInterface.SensorI;
import attention.Winner;
import br.unicamp.cst.core.entities.MemoryObject;
import sensory.FeatMapCodelet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class TD_FM_Color extends FeatMapCodelet {
    private List winnersList;
    private float mr = 255, print_step;                     //Max Value for VisionSensor
    private final int max_time_graph=100;
    private final int res = 256;                     //Resolution of VisionSensor
    private  int time_graph;
    private final int slices = 16;                    //Slices in each coordinate (x & y) 
    private SensorI vision;
    private int stage;
    private MemoryObject desired_feature;
    private Float red_goal, green_goal, blue_goal;  
    private Winner lastWinner; 
    public TD_FM_Color(SensorI vision, int nsensors, ArrayList<String> sens_names, 
            String featmapname,int timeWin, int mapDim, int print_step) {
        super(nsensors, sens_names, featmapname,timeWin,mapDim);
        this.time_graph = 0;
        this.vision = vision;
        this.stage = this.vision.getStage();
        this.red_goal = 0f;
        this.green_goal = 0f;
        this.blue_goal = 255f;  
        this.print_step=print_step;
    }

    public ArrayList<Float> getColorGoal(){
        ArrayList<Float> color_goal  = new ArrayList<>();
        color_goal.add(this.red_goal);
        color_goal.add(this.green_goal);
        color_goal.add(this.blue_goal);
        return color_goal;
    }
    
    public void setColorGoal(float new_red, float new_green, float new_blue){
        this.red_goal = new_red;
        this.green_goal = new_green;
        this.blue_goal = new_blue;
    }
    
    @Override
    public void calculateActivation() {
        
    }
   
    @Override
    public void accessMemoryObjects() {
        for (int i = 0; i < num_sensors; i++) {
            sensor_buffers.add((MemoryObject)this.getInput(sensorbuff_names.get(i)));
        }
        featureMap = (MemoryObject) this.getOutput(feat_map_name);
        winners = (MemoryObject) this.getInput("WINNERS");
        desired_feature = (MemoryObject) this.getInput("DESFEAT_C");
        regionMO = (MemoryObject) this.getOutput("REGION_TOP_FM");
    }
    
    @Override
    public void proc() {
        this.stage = vision.getStage();
       /* try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }*/
        
        MemoryObject vision_bufferMO = (MemoryObject) sensor_buffers.get(0);        //Gets Vision Data
        
        List visionData_buffer;
        visionData_buffer = (List) vision_bufferMO.getI();
        List winnerList = (List) winners.getI();
        
        if(!winnerList.isEmpty())  lastWinner = (Winner) winnerList.get(winnerList.size()-1);
        
        List vision_color_FM = (List) featureMap.getI();        
        if(vision_color_FM.size() == timeWindow){
            vision_color_FM.remove(0);
        }
        vision_color_FM.add(new ArrayList<>());
        int t = vision_color_FM.size()-1;
        ArrayList<Float> vision_color_FM_t = (ArrayList<Float>) vision_color_FM.get(t);
        for (int j = 0; j < mapDimension; j++) {
            vision_color_FM_t.add(new Float(0));
        }
        
        if(this.stage>2){
            MemoryObject visionDataMO;

            if(visionData_buffer.size() < 1){
                return;
            }

            
            List desFeatList = (List) desired_feature.getI();
            if( !desFeatList.isEmpty()){
                ArrayList<Float> desFeat = (ArrayList<Float>) desFeatList.get(desFeatList.size()-1);
                if(!desFeat.isEmpty()){
                    setColorGoal(desFeat.get(0),desFeat.get(1),desFeat.get(2));
                }
            }
        
            visionDataMO = (MemoryObject)visionData_buffer.get(visionData_buffer.size()-1);

            List visionData;

            visionData = (List) visionDataMO.getI();
        
            Float Fvalue_r, Fvalue_g, Fvalue_b;
            float MeanValue_r = 0, MeanValue_g = 0, MeanValue_b = 0;
            ArrayList<Float> vision_mean_color = new ArrayList<>();
            ArrayList<Float> visionData_Array_r = new ArrayList<>();
            ArrayList<Float> visionData_Array_g = new ArrayList<>();
            ArrayList<Float> visionData_Array_b = new ArrayList<>();
            for (int j = 0; j < res*res; j++) {
                visionData_Array_r.add(new Float(0));
                visionData_Array_g.add(new Float(0));
                visionData_Array_b.add(new Float(0));
            }

            int pixel_len = 3;
            int count_3 = 0;
            //System.out.println("Vision data r size:"+visionData.size());
            for (int j = 0; j+pixel_len < visionData.size(); j+= pixel_len) {

                Fvalue_r = (Float) visionData.get(j);               //Gets JUST red values for VisionData
                Fvalue_g = (Float) visionData.get(j+1);
                Fvalue_b = (Float) visionData.get(j+2);
                visionData_Array_r.set(count_3, Fvalue_r);        //red data
                visionData_Array_g.set(count_3, Fvalue_g);        //green data
                visionData_Array_b.set(count_3, Fvalue_b);        //blue data
                count_3 += 1;
            }
            //System.out.println("Vision r size:"+visionData_Array.size());
             //Converts res*res image to res/slices*res/slices sensors
            float new_res = (res/slices)*(res/slices);
            float new_res_1_2 = (res/slices);

            for(int n = 0;n<slices;n++){
                int ni = (int) (n*new_res_1_2);
                int no = (int) (new_res_1_2+n*new_res_1_2);
                for(int m = 0;m<slices;m++){    
                    int mi = (int) (m*new_res_1_2);
                    int mo = (int) (new_res_1_2+m*new_res_1_2);
                    for (int y = ni; y < no; y++) {
                        for (int x = mi; x < mo; x++) {
                            Fvalue_r = visionData_Array_r.get(y*res+x); 
                            Fvalue_g = visionData_Array_g.get(y*res+x);
                            Fvalue_b = visionData_Array_b.get(y*res+x);  
                            MeanValue_r += Fvalue_r;
                            MeanValue_g += Fvalue_g;
                            MeanValue_b += Fvalue_b;


                        }
                    }
                    float correct_mean_r = MeanValue_r/new_res;
                    float correct_mean_g = MeanValue_g/new_res;
                    float correct_mean_b = MeanValue_b/new_res;

                    //System.out.println("Mean r: "+ correct_mean_r +" Mean g: "+correct_mean_g+" Mean b: "+correct_mean_b+" ni: "+ni+" no: "+no+" mi: "+mi+" mo: "+mo);
                    //if(correct_mean>this.max_value) this.max_value = correct_mean;
                    //System.out.println("max_value r: "+ max_value);
                    if(Math.abs(correct_mean_r-red_goal)/mr<0.2 && Math.abs(correct_mean_g-green_goal)/mr<0.2 && Math.abs(correct_mean_b-blue_goal)/mr<0.2) vision_mean_color.add(new Float(1));
                    else if(Math.abs(correct_mean_r-red_goal)/mr<0.4 && Math.abs(correct_mean_g-green_goal)/mr<0.4 && Math.abs(correct_mean_b-blue_goal)/mr<0.4) vision_mean_color.add(new Float(0.75));
                    else if(Math.abs(correct_mean_r-red_goal)/mr<0.6 && Math.abs(correct_mean_g-green_goal)/mr<0.6 && Math.abs(correct_mean_b-blue_goal)/mr<0.6) vision_mean_color.add(new Float(0.5));
                    else if(Math.abs(correct_mean_r-red_goal)/mr<0.8 && Math.abs(correct_mean_g-green_goal)/mr<0.8 && Math.abs(correct_mean_b-blue_goal)/mr<0.8) vision_mean_color.add(new Float(0.25));
                    else vision_mean_color.add(new Float(0));     

                    //vision_mean_red.add(correct_mean/mr);
                    MeanValue_r = 0;
                    MeanValue_g = 0;
                    MeanValue_b = 0;
                }
            }


            for (int j = 0; j < vision_mean_color.size(); j++) {

                vision_color_FM_t.set(j, vision_mean_color.get(j));
            }   
        }
        printToFile(vision_color_FM_t, "top_down_color.txt");
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
    

