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
public class WinnerPicker extends Codelet{
    
    private  int time_graph;
    
    private boolean first = true;
    
    private List winnersList;
    private List attentionalMap;
    private List saliencyMap;
    private List winnerType;
    private String salMapName;
    private String winnersListName;
    private String attentionalMapName;
    private int timeWindow, print_step;
    private int sensorDimension;
    private boolean debug;
    private final int max_time_graph=100;
    
    private static final double GUASSIAN_WIDTH_EXOGENOUS_SONAR = 0.5;
    private static final double GUASSIAN_WIDTH_EXOGENOUS_IOR_SONAR = 0.5;

    private static final double BOTTOM_UP_PRE_TIME = 2000;
    private static final double BOTTOM_UP_EXCITATORY_TIME = 2000;
    private static final double BOTTOM_UP_INHIBITORY_TIME = 4000;

    private static final int BOTTOM_UP = 0;
    private static final int TOP_DOWN = 1;
    
    private static final double TOP_DOWN_PRE_TIME = 3;
    private static final double TOP_DOWN_EXCITATORY_TIME = 80;
    private static final double TOP_DOWN_INHIBITORY_TIME = 6;

    private static final double SIGMA_IOR_SONAR = 0.02;
    private static final double T1_IOR_SONAR =  1;
    private static final double TMAX = 200;

    private static final double TS = 100;
    private static final double TM = 1000;
    private SensorI vision;

    public WinnerPicker(SensorI vision, String winListName, String attMapName,
            String salMName, int tWindow, int sensDim, int print_step){
        super();
        this.time_graph = 0;
        winnersListName = winListName;
        attentionalMapName = attMapName;
        timeWindow = tWindow;
        sensorDimension = sensDim;
        salMapName = salMName;
        this.vision = vision;
        this.print_step = print_step;
        
    }

    @Override
    public void accessMemoryObjects() {
        MemoryObject MO;
        MO = (MemoryObject) this.getInput(salMapName);
        saliencyMap = (List) MO.getI();
        MO = (MemoryObject) this.getInput("TYPE");
        winnerType = (List) MO.getI();
        MO = (MemoryObject) this.getOutput(winnersListName);
        winnersList = (List) MO.getI();
        MO = (MemoryObject) this.getOutput(attentionalMapName);
        attentionalMap = (List) MO.getI();

    }

    @Override
    public void calculateActivation() {


    }

    @Override
    public void proc() {
    	try {
            Thread.sleep(80);//Estava 80
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }
        //winner computation
        float max = 0;
        int max_index = -1;
        long fireTime = 0;



        for(int t = 0; t < saliencyMap.size();t++){
            ArrayList<Float> line;
            line = (ArrayList<Float>) saliencyMap.get(t);
            for (int j = 0; j < line.size(); j++) {
            	
                if(line.get(j) > max){
                    max = line.get(j);
                    max_index = j;
                    fireTime = System.currentTimeMillis();
//                    t_max = t;
                }
            }
        }
        int last_winner_index = -1;
        if (!winnersList.isEmpty()) {
        	Winner last_winner = (Winner) winnersList.get(winnersList.size()-1);
        	last_winner_index = last_winner.featureJ;
        } 
        
        int type = BOTTOM_UP;
        ArrayList<Integer> linewinner = (ArrayList<Integer>) winnerType.get(winnerType.size()-1);
        if(max != 0 && last_winner_index  != max_index){
            if(linewinner.get(max_index) == TOP_DOWN) type = TOP_DOWN;
            winnersList.add(new Winner(max_index, 
//                    t_max,
                    type, fireTime));
        }
        printToFile(max_index, "winners.txt");
        
        int i,j,w;
        double deltaj, deltai;
        long t;

        if(attentionalMap.size() == timeWindow){
            attentionalMap.remove(0);
        }

        ArrayList<Float> attMap_sizeMinus1 = null;

        attentionalMap.add(new ArrayList<>());
        for(j = 0; j < sensorDimension; j++){
            attMap_sizeMinus1 = (ArrayList < Float >)attentionalMap.get(attentionalMap.size()-1);
            attMap_sizeMinus1.add(1F);
        }
        if(debug) System.out.println("winnersList "+winnersList.size());
                
                
        for (w = 0; w < winnersList.size(); w++) {
            Long timeCourse = System.currentTimeMillis();
            Winner winner_w = (Winner) winnersList.get(w);
          
                if(debug) System.out.println("winner_w "+winner_w);
                
            j = winner_w.featureJ;

            // The course is over to this feature -> remove it from the list
            if((winner_w.fireTime + BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME+BOTTOM_UP_INHIBITORY_TIME) < timeCourse){
                winnersList.remove(w);
            }

            // The course is in excitatory phase
            else if((winner_w.fireTime+BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME >= timeCourse) && (winner_w.fireTime+BOTTOM_UP_PRE_TIME <= timeCourse)){
                t = timeCourse - winner_w.fireTime;
                
                
                float auxAttWinnerAnt;
                auxAttWinnerAnt = attMap_sizeMinus1.get(j);
                
                // Calculate the activation level for the most central neuron based on the time
                deltaj = exponentialGrowDecayBottomUp(BOTTOM_UP_PRE_TIME, TS, TM, t);
                attMap_sizeMinus1.set(j, attMap_sizeMinus1.get(j)+(float)deltaj);
                            
                // Calculate the activation level for the neighbours
                for(i=0; i < j; i++){
                    deltai = gaussian(deltaj, GUASSIAN_WIDTH_EXOGENOUS_SONAR, j, i);
                    attMap_sizeMinus1.set(i, attMap_sizeMinus1.get(i)+(float)deltai);
                }

                for(i=j+1; i < sensorDimension; i++){
                    deltai = gaussian(deltaj, GUASSIAN_WIDTH_EXOGENOUS_SONAR, j, i);
                    attMap_sizeMinus1.set(i, attMap_sizeMinus1.get(i)+(float)deltai);
                }
                
                
            }
            
            // The course is in inhibitory phase
            else if(((winner_w.fireTime+BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME+BOTTOM_UP_INHIBITORY_TIME) >= timeCourse) && ((winner_w.fireTime+BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME) <= timeCourse) && winner_w.origin == BOTTOM_UP){
                t = timeCourse - winner_w.fireTime;
                //System.out.println("inhib "+winner_w);
                
                float auxAttWinnerAnt;
                auxAttWinnerAnt = attMap_sizeMinus1.get(j);
                
                deltaj = exponentialGrowDecayBottomUp(BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME, TS, TM, t);
                //System.out.println("inhib "+deltaj);
                attMap_sizeMinus1.set(j, attMap_sizeMinus1.get(j)-(float)deltaj);

            
                for (i=0; i<j; ++i){
                    deltai = gaussian(deltaj, GUASSIAN_WIDTH_EXOGENOUS_IOR_SONAR, j, i);
                    attMap_sizeMinus1.set(i, attMap_sizeMinus1.get(i)-(float)deltai);
                }
                
                for (i=j+1; i<sensorDimension; ++i){
                    deltai = gaussian(deltaj, GUASSIAN_WIDTH_EXOGENOUS_IOR_SONAR, j, i);
                    attMap_sizeMinus1.set(i, attMap_sizeMinus1.get(i)-(float)deltai);
                }
                                
            }
           if(debug)  System.out.println("winner_w "+winner_w.featureJ);
        }
        
               
       printToFile(attMap_sizeMinus1, "attMap.txt");
    }

    private double exponentialGrowDecayBottomUp(double pre, double ts, double tm, float t) {
        double h;

		if ((t-pre) > 0) h=1;
		else if ((t-pre) == 0)  h=0.5;
		else h=0;
	
	
		return ((Math.exp(-1*(t-pre)/ tm) - Math.exp(-1*(t-pre)/ ts)) * h);
    }
    
///gaussian(delta_j, WIDTH, j, i)
    private double gaussian(double height, double width, int posCenter, int position) {        
        return (height*Math.exp(-1*((Math.pow((float)position-posCenter,2))/(2*Math.pow(width,2)))));
    }
   
     private void printToFile(Object object,String filename    ){
        if(this.vision.getEpoch() %print_step == 0){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                out.println(dtf.format(now)+"_"+(int) this.vision.getIValues(1)+"_"+(int) this.vision.getIValues(4) +"_"+time_graph+" "+ object);
                //if(time_graph == max_time_graph-1) System.out.println(filename+": "+time_graph);          
                time_graph++;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
    }
/*    
    private void printToFileComplet(long t, Object winner, long fireTime, long timeCourse, float attAntWinner, float attAftWinner, double delta, int winnersListSize, String fase, String filename){
        
        if(time_graph < max_time_graph*2){
            try(FileWriter fw = new FileWriter("profile/"+filename,true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter out = new PrintWriter(bw))
            {
                if(first){
                    out.println("TIME - PHASE - WINNER - FIRETIME - TIMECOURSE - DIFFTIMES - VALUEJANT - DELTA - VALUEJAFT = WINSIZE");
                    first = false;
                }
                out.println(time_graph+" "+ fase + " " + winner +  " " + fireTime + " " + timeCourse + " " + t + " " + attAntWinner + " " + delta + " "+ attAftWinner + " " + winnersListSize);
                //time_graph++;
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
*/

}
