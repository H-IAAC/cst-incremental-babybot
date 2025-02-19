/**
 * *****************************************************************************
 * Copyright (c) 2012  DCA-FEEC-UNICAMP
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * Contributors:
 *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 *****************************************************************************
 */
package attention;

import CommunicationInterface.SensorI;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class disSalMap extends Codelet {
    
    private  int time_graph, print_step;
    private final int max_time_graph=100;

    private List saliencyMap;
    private List combFeatMap;
    private List attMap;
    private String saliencyMapName;
    private String combFeatMapName;
    private String attMapName;
    private int timeWindow;
    private int sensordimension;

    private SensorI vision;
    
    public disSalMap(SensorI vision, String salMapName, String combFMName, 
            String AttMName, int timeWin, int sensorDim, int print_step){
        this.time_graph = 0;
        saliencyMapName = salMapName;
        combFeatMapName = combFMName;
        attMapName = AttMName; 
        timeWindow = timeWin;
        sensordimension = sensorDim;
        this.vision = vision;
        this.print_step = print_step;
    }
    
    @Override
    public void accessMemoryObjects() {
        MemoryObject MO;
        MO = (MemoryObject) this.getOutput(saliencyMapName);
        saliencyMap = (List) MO.getI();
        MO = (MemoryObject) this.getInput(combFeatMapName);
        combFeatMap = (List) MO.getI();
        MO = (MemoryObject) this.getInput(attMapName);
        attMap =  (List) MO.getI();
        

    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {
    	try {
            Thread.sleep(50);
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        ArrayList<Float> salMap_sizeMinus1 = null;
        
        if(saliencyMap.size() == timeWindow){
            saliencyMap.remove(0);
        }
        
        saliencyMap.add(new ArrayList<Float>());
        salMap_sizeMinus1 = (ArrayList < Float >) saliencyMap.get(saliencyMap.size()-1);
        for (int j = 0; j < sensordimension; j++) {
            salMap_sizeMinus1.add(new Float(0));
        }
        
        ArrayList<Float> mostRecentCFMarray = null;
        ArrayList<Float> mostRecentAttMarray = null;
        
        if(!attMap.isEmpty() && !combFeatMap.isEmpty()){
            mostRecentAttMarray = (ArrayList<Float>) attMap.get(attMap.size()-1);
            mostRecentCFMarray = (ArrayList<Float>) combFeatMap.get(combFeatMap.size()-1);

            for (int j = 0; j < sensordimension; j++) {
                salMap_sizeMinus1.set(j, mostRecentAttMarray.get(j)*mostRecentCFMarray.get(j));
            }
            
        }
        
        printToFile(salMap_sizeMinus1, "salMap.txt");
    }
    
    private void printToFile(Object object,String filename){
        if(this.vision.getEpoch() == 1 || this.vision.getEpoch()%print_step == 0){
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
            LocalDateTime now = LocalDateTime.now();
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+"_"+vision.getEpoch()+"_"+time_graph+" "+ object);
                time_graph++;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
