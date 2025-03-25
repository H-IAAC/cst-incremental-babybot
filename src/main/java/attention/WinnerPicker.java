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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

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
    private boolean debug = false;
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


        int numWinners = (int) vision.getIValues(7);
        PriorityQueue<Winner> topWinners = new PriorityQueue<>(numWinners, Comparator.comparingInt(w -> w.featureJ));
        Set<Integer> selectedFeatures = new HashSet<>(); // Para evitar duplicatas

        // Lista de candidatos para armazenar j, type e value temporariamente
        List<int[]> candidates = new ArrayList<>();
        List<Long> firetimes = new ArrayList<>();
        for (int z = 0; z < saliencyMap.size(); z++) {
            ArrayList<Float> line = (ArrayList<Float>) saliencyMap.get(z);
            for (int j = 0; j < line.size(); j++) {
                float value = line.get(j);
                if (value > 0 && !winnerType.isEmpty()) {
                    ArrayList<Integer> winnerTypea = (ArrayList<Integer>) winnerType.get(winnerType.size() - 1);
                    if (winnerTypea.isEmpty() || j >= winnerTypea.size()) continue; // Evita erro de índice

                    int type = (winnerTypea.get(j) == TOP_DOWN) ? TOP_DOWN : BOTTOM_UP;

                    if (!selectedFeatures.contains(j)) { // Evita repetição de featureJ
                        candidates.add(new int[]{j, type, (int) (value * 100000)}); // Multiplica para ordenação
                        firetimes.add( System.currentTimeMillis());
                    }
                }
            }
        }

        // Ordenar candidatos pelo maior valor de ativação
        candidates.sort((a, b) -> Integer.compare(b[2], a[2])); // Compara pelo value convertido

        // Adicionar os `numWinners` melhores ao topWinners
        for (int i = 0; i < Math.min(numWinners, candidates.size()); i++) {
            int[] winnerData = candidates.get(i);
            long fire = firetimes.get(i);
            Winner winner = new Winner(winnerData[0], winnerData[1], fire);
            topWinners.offer(winner);
            selectedFeatures.add(winner.featureJ);
        }

        // Converter para lista ordenada
        ArrayList<Winner> winners = new ArrayList<>(topWinners);

        // Calcular os pesos dos winners
        ArrayList<Float> weights = new ArrayList<>();
        for (int i = 0; i < numWinners; i++) {
            weights.add((float) (numWinners - i) / numWinners);
        }

        // Adicionar a lista de winners ao winnersList
        winnersList.add(winners);

        
        int type = BOTTOM_UP;
        /*ArrayList<Integer> linewinner = (ArrayList<Integer>) winnerType.get(winnerType.size()-1);
        if(max != 0 && last_winner_index  != max_index){
            if(linewinner.get(max_index) == TOP_DOWN) type = TOP_DOWN;
            winnersList.add(new Winner(max_index, 
//                    t_max,
                    type, fireTime));
        }*/
        
        printToFile(winners, "winners.txt");
        
        int i,j,w,wl;
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
        if(debug) System.out.println("winners "+winners.size());
                
         for (wl = 0; wl <   winnersList.size(); wl++){
             ArrayList<Winner> winnerl = (ArrayList<Winner>) winnersList.get(wl);
             Long timeCourse = System.currentTimeMillis(); 
        for (w = 0; w < winnerl.size(); w++) {
               
            Winner winner_w = (Winner) winnerl.get(w);
          
               if(debug)  System.out.println("winner_w "+winner_w);
                
            j = winner_w.featureJ;
            if(debug) {System.out.println("\nFiretime:"+(double)(winner_w.fireTime));
            System.out.println("\nFiretime over to this feature :"+(double)(winner_w.fireTime + BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME+BOTTOM_UP_INHIBITORY_TIME));
             
            System.out.println("timeCourse:"+timeCourse);
             
            System.out.println("Firetime<timeCourse");
            
             System.out.println("\nFiretime excitatory 1:"+(double)(winner_w.fireTime + BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME));
            
             System.out.println("timeCourse:"+timeCourse);
            
             System.out.println("Firetime excitatory 2:"+(double)(winner_w.fireTime+BOTTOM_UP_PRE_TIME));
             
              System.out.println("BOTTOM_UP_PRE_TIME:"+BOTTOM_UP_PRE_TIME);
              
              System.out.println("BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME:"+(double)(BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME));
              
             System.out.println("excitatory 1 > timeCourse && excitatory 2 < timeCourse");
            }            
            // The course is over to this feature -> remove it from the list
            if((double)(winner_w.fireTime + BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME+BOTTOM_UP_INHIBITORY_TIME) < timeCourse){
                winners.remove(w);
                if(debug) System.out.println("over to this feature ");
            }
            
            // The course is in excitatory phase
            else if(((double)(winner_w.fireTime+BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME) >= timeCourse) && ((double)(winner_w.fireTime+BOTTOM_UP_PRE_TIME) <= timeCourse)){
                t = (long) (timeCourse - (double)winner_w.fireTime);
                if(debug) System.out.println(" excitatory phas ");
                
                float auxAttWinnerAnt;
                deltai = 0;
                auxAttWinnerAnt = attMap_sizeMinus1.get(j);
                
                // Calculate the activation level for the most central neuron based on the time
                deltaj = exponentialGrowDecayBottomUp(BOTTOM_UP_PRE_TIME, TS, TM, t);
                 if(debug) System.out.println(" attMap_sizeMinus1 size:"+attMap_sizeMinus1.size());
                 if(debug) System.out.println(" weights size:"+weights.size());
                attMap_sizeMinus1.set(j, attMap_sizeMinus1.get(j)+(float)deltaj*(weights.get(w)));
                if(debug) System.out.println(" excitatory BOTTOM_UP_PRE_TIME "+(float)deltaj*(weights.get(w)));
                            
                // Calculate the activation level for the neighbours
                for(i=0; i < j; i++){
                    deltai = gaussian(deltaj, GUASSIAN_WIDTH_EXOGENOUS_SONAR, j, i);
                    attMap_sizeMinus1.set(i, attMap_sizeMinus1.get(i)+(float)deltai*(weights.get(w)));
                }
                if(debug) System.out.println(" excitatory GUASSIAN_WIDTH_EXOGENOUS_SONAR "+(float)deltai*(weights.get(w)));
                
                for(i=j+1; i < sensorDimension; i++){
                    deltai = gaussian(deltaj, GUASSIAN_WIDTH_EXOGENOUS_SONAR, j, i);
                    attMap_sizeMinus1.set(i, attMap_sizeMinus1.get(i)+(float)deltai*(weights.get(w)));
                }
                if(debug) System.out.println(" excitatory GUASSIAN_WIDTH_EXOGENOUS_SONAR "+(float)deltai*(weights.get(w)));
                
                
            }
            
            // The course is in inhibitory phase
            else if(((double)(winner_w.fireTime+BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME+BOTTOM_UP_INHIBITORY_TIME) >= timeCourse) && ((double)(winner_w.fireTime+BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME) <= timeCourse) && winner_w.origin == BOTTOM_UP){
                t = (long) (timeCourse - winner_w.fireTime);
                //System.out.println("inhib "+winner_w);
                if(debug) System.out.println(" inhibitory phas ");
                deltai = 0;
                float auxAttWinnerAnt;
                auxAttWinnerAnt = attMap_sizeMinus1.get(j);
                
                deltaj = exponentialGrowDecayBottomUp(BOTTOM_UP_PRE_TIME+BOTTOM_UP_EXCITATORY_TIME, TS, TM, t);
                //System.out.println("inhib "+deltaj);
                attMap_sizeMinus1.set(j, attMap_sizeMinus1.get(j)-(float)deltaj*(weights.get(w)));
                if(debug) System.out.println(" inhibitory BOTTOM_UP_PRE_TIME "+(float)deltaj*(weights.get(w)));
                
            
                for (i=0; i<j; ++i){
                    deltai = gaussian(deltaj, GUASSIAN_WIDTH_EXOGENOUS_IOR_SONAR, j, i);
                    attMap_sizeMinus1.set(i, attMap_sizeMinus1.get(i)-(float)deltai*(weights.get(w)));
                }
                if(debug) System.out.println(" inhibitory GUASSIAN_WIDTH_EXOGENOUS_IOR_SONAR "+(float)deltai*(weights.get(w)));
                
                for (i=j+1; i<sensorDimension; ++i){
                    deltai = gaussian(deltaj, GUASSIAN_WIDTH_EXOGENOUS_IOR_SONAR, j, i);
                    attMap_sizeMinus1.set(i, attMap_sizeMinus1.get(i)-(float)deltai*(weights.get(w)));
                }
                if(debug) System.out.println(" inhibitory GUASSIAN_WIDTH_EXOGENOUS_IOR_SONAR "+(float)deltai*(weights.get(w)));
                                
            }
           if(debug)  System.out.println("winner_w "+winner_w.featureJ);
        }
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
