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
import br.unicamp.cst.core.entities.MemoryObject;
import sensory.CombFeatMapCodelet;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import outsideCommunication.OutsideCommunication;


/**
 *
 * @author leolellisr
 */
public class CFM extends CombFeatMapCodelet {
private static final int BOTTOM_UP = 0;
private static final int TOP_DOWN = 1;
private  int time_graph;

//private  int max_time_graph = 100;
private SensorI sensor;
private int stage, print_step;
private boolean debug = false;
OutsideCommunication oc;
    public CFM(SensorI sensor, int numfeatmaps, ArrayList<String> featmapsnames, 
            int timeWin, int CFMdim, int print_step, OutsideCommunication outc) {
        super(numfeatmaps, featmapsnames,timeWin,CFMdim);
        this.time_graph = 0;
        this.sensor = sensor;
        this.stage = sensor.getStage();
        this.print_step = print_step;
        this.oc = outc;
    }

     
    @Override
    public void calculateCombFeatMap() {
        this.stage = sensor.getStage();
   
       
        /*try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        */
         ArrayList FMk_c = new ArrayList<>();
        for (int k = 0; k < num_feat_maps; k++) {
                MemoryObject FMkMO;
                FMkMO = (MemoryObject) feature_maps.get(k);

                List FMk;
                FMk = (List) FMkMO.getI();
                
                
                if(FMk.size() < 1){
                    return;
                }
                
                if(k==0){
                    for (Object FMk_color : FMk) {
                        FMk_c.add(FMk_color);
                    }
                }else{
                    FMk_c.add(FMk);
                }
                
               
        }
                
        List weight_values = (List) weights.getI();
        
        List combinedFM = (List) comb_feature_mapMO.getI();
        List winnersTypeList = (List) winnersType.getI();
        
        if(combinedFM.size() == timeWindow){
            combinedFM.remove(0);
        }
        if(winnersTypeList.size() == timeWindow){
            winnersTypeList.remove(0);
        }
        
        combinedFM.add(new ArrayList<>());
        winnersTypeList.add(new ArrayList<>());
        
        int t = combinedFM.size()-1;

        List CFMrow, winners_row;
        CFMrow = (List) combinedFM.get(t);
        winners_row = (List) winnersTypeList.get(t);
        
        for(int j = 0; j < CFMdimension; j++){
            CFMrow.add((float) 0);
            winners_row.add(0);
        }
        
       
        
        for (int j = 0; j < CFMrow.size(); j++) {
            float ctj;
            float sum_top=0, sum_bottom=0;
            ctj = 0;
            if(debug) System.out.println("num_feat_maps: "+FMk_c.size());
            for (int k = 0; k < FMk_c.size(); k++) {
                List FMk;
                FMk = (List) FMk_c.get(k);
                
                if(FMk.size() < 1){
                    return;
                }
                
                if(debug) System.out.println("k: "+k+" FMk: "+FMk_c.size());
                List FMk_t = null;
                
                FMk_t = (List) FMk.get(FMk.size()-1);
                if(debug) System.out.println("k: "+k+"FMk_t : "+FMk_t.size());
                
                Float weight_val, fmkt_val;

                if(FMk_t.size()>j) fmkt_val = (Float) FMk_t.get(j); 
                else fmkt_val = (Float) FMk_t.get(j-1);
                weight_val = (Float) weight_values.get(k);
                ctj += weight_val*fmkt_val;
                
                if(stage>2) {
                    if(k>=2) sum_top += weight_val*fmkt_val;
                    else sum_bottom += weight_val*fmkt_val;
                }   
                
            }   
            
            CFMrow.set(j, ctj);
            
            if(sum_top > sum_bottom) winners_row.set(j, TOP_DOWN);
            else winners_row.set(j, BOTTOM_UP);
            
        }
        
        
        printToFile((ArrayList<Float>) CFMrow, "CFM.txt");
        printToFile((ArrayList<Integer>) winners_row, "winnerType.txt");
    }
    
      
  private void printToFile(Object object,String filename    ){
        if(oc.vision.getEpoch() %print_step == 0){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+"_"+(int) oc.vision.getIValues(1)+"_"+(int)oc.vision.getIValues(4) +"_"+time_graph+" "+ object);
                //if(time_graph == max_time_graph-1) System.out.println(filename+": "+time_graph);          
                time_graph++;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
}