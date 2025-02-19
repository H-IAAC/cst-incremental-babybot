/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.learner;

import br.unicamp.cst.learning.QLearning;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author ic-unicamp
 */
public class QLearningL extends QLearning{
    
    private boolean showDebugMessages=false;
    private ArrayList<Integer> statesList;
    private ArrayList<String> actionsList;
    private String fileName="QTable.txt";
    private HashMap<Integer, HashMap<String,Double>> Q;


    private double e=0.1; //Probability of choosing the best action instead of a random one
    private double alpha=0.5; //Here, alpha is the learning rate parameter
    private double gamma=0.9; //discount factor
    private double b=0.95; // probability of random action choice deciding for the previous action instead of randomly choosing one from the action list
    //	private int statesCount,actionsCount;
    private String a="",al="";
    private int s=-1,sl=-1;
    private double reward=0;
    private Random r=new Random();

    
    public QLearningL(){
		statesList=new ArrayList<Integer>();
		actionsList=new ArrayList<String>();
		Q = new HashMap<Integer, HashMap<String,Double>>(); // Q learning
	}
    
    public void setFilename(String file){
        this.fileName = file;
        
    }
    
    /**
         * Default Constructor.
         */
	
        
        
       
        /**
         * This method set Q value with parameters Qval, state and action.
         * @param Qval
         * @param state
         * @param action 
         */
	public void setQ(double Qval, int state, String action){
		HashMap<String,Double> tempS=this.Q.get(state);
		if(tempS!=null){
			//This state already exists, So I have to check if it already contains this action
			if(tempS.get(action)!=null){
				//the action already exists, So I just update it to the new one
				tempS.put(action, Qval);
			}
			else{
				if(!actionsList.contains(action)){//TODO something wicked here. I shouldn't need to perform this test...
					actionsList.add(action);
				}
				tempS.put(action, Qval);				
			}
		}else{
			//this state doesn't exist yet, so I must create it and populate it with nActions-1 valued 0 and one action valued Qval
			HashMap<String,Double> tempNew= new  HashMap<String,Double>();
			tempNew.put(action, Qval);
			statesList.add(state);
			this.Q.put(state, tempNew);
		}
	}
        
        /**
        * Returns the utility value Q related to the given state/action pair
        * @param state
        * @param action
        * @return
        */
	public double getQ(int state,String action){
		double dQ=0;
		if(!(Q.get(state)==null || Q.get(state).get(action)==null)){
			dQ=Q.get(state).get(action);
		}
		return  dQ;
	}

        /**
        * Returns the maximum Q value for sl. 
        * @param sl
        * @return Q Value
        */
	public double maxQsl(int sl){
		double maxQinSl=0;
		String maxAl="";
		double val=0;
		if(this.Q.get(sl)!=null){
			HashMap<String,Double> tempSl=this.Q.get(sl);
			ArrayList<String> tempA=new ArrayList<String>();
			tempA.addAll(this.actionsList);

			// Finds out the action with maximum value for sl
			Iterator<Map.Entry<String, Double>> it = tempSl.entrySet().iterator(); 

			while (it.hasNext()) { 
				Map.Entry<String, Double> pairs = it.next(); 
				val= pairs.getValue(); 
				tempA.remove(pairs.getKey());
				if(val>maxQinSl){
					maxAl=pairs.getKey();
					maxQinSl=val;
				} 
			}
			if(!tempA.isEmpty() && maxQinSl<0){maxQinSl=0;} //Assigning 0 to unknown state/action pair
		}
		return maxQinSl;
	}

        /**
        * This methods is responsible for update the state.
        * @param stateIWas state I was previously
        * @param actionIDid action I did while at the previous state
        * @param rewardIGot reward I got after moving from previous state to the present one
        */
	public void update(int stateIWas,String actionIDid, double rewardIGot) {
		//which is calculated whenever action a is executed in state s leading to state s'
		this.sl=stateIWas;
		this.al=actionIDid;

		if(!a.equals("")&& s !=-1){
			//			if(!s.equals(sl)){//Updates only if state changes, is this correct?
			double Qas=this.getQ(s, a);
			double MaxQ=this.maxQsl(this.sl);
			double newQ= Qas  + alpha * (rewardIGot + gamma * MaxQ - Qas); //TODO  not sure if its reward or rewardIGot
			this.setQ(newQ, s, a);
			//				System.out.println("== Update ============");
			//				System.out.println("a: "+a+"  s: "+s+"  al: "+al+"  sl: "+sl+"  Qas: "+Qas+"  MaxQ: "+MaxQ+"  newQ: "+newQ);
			//				System.out.println("======================");
			//				this.printQ();
			//			}
		}

		a=this.al;
		s=this.sl;
		reward=rewardIGot;
	}


        /**
        * This print Q values.
        */
	public void printQ() {
		System.out.println("------ Printed Q -------");
		Iterator<Map.Entry<Integer, HashMap<String, Double>>> itS = this.Q.entrySet().iterator(); 
		while (itS.hasNext()) { 
			Map.Entry<Integer, HashMap<String, Double>> pairs = itS.next(); 			
			HashMap<String,Double> tempA = pairs.getValue(); 
			Iterator<Map.Entry<String, Double>> itA = tempA.entrySet().iterator();
			double val=0;
			System.out.print("State("+pairs.getKey()+") actions: ");
			while(itA.hasNext()){
				Map.Entry<String, Double> pairsA = itA.next();
				val=pairsA.getValue();
				System.out.print("["+pairsA.getKey()+": "+val+"] ");
			}			
			System.out.println("");
		} 

		System.out.println("----------------------------");
	}
        
    @Override
    public void storeQ(){
            try (FileWriter writer = new FileWriter(this.fileName)) {
            writer.write("{"); // Start of JSON object
            Iterator<Map.Entry<Integer, HashMap<String, Double>>> itS = Q.entrySet().iterator();

            while (itS.hasNext()) {
                Map.Entry<Integer, HashMap<String, Double>> pairs = itS.next();
                int state = pairs.getKey();
                HashMap<String, Double> actions = pairs.getValue();

                writer.write("\"" + state + "\": {"); // Write state as JSON object

                Iterator<Map.Entry<String, Double>> itA = actions.entrySet().iterator();
                while (itA.hasNext()) {
                    Map.Entry<String, Double> actionPair = itA.next();
                    String action = actionPair.getKey();
                    double value = actionPair.getValue();

                    // Write action-value pair as JSON key-value pair
                    writer.write("\"" + action + "\":" + value);

                    // Add a comma if this is not the last action-value pair
                    if (itA.hasNext()) {
                        writer.write(",");
                    }
                }

                writer.write("}"); // End of state JSON object

                // Add a comma if this is not the last state
                if (itS.hasNext()) {
                    writer.write(",");
                }
            }

            writer.write("}"); // End of JSON object
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    
    }

    /**
     *  Recover Q values from file in JSON structure.
     */
    @Override
    public void recoverQ(){
            //...checks on aFile are elided
            StringBuilder contents = new StringBuilder();

            try {
                    //use buffering, reading one line at a time
                    //FileReader always assumes default encoding is OK!
                    BufferedReader input  =  new BufferedReader(new FileReader(fileName));
                    try {
                            String line = null; //not declared within while loop
                            /*
                             * readLine is a bit quirky :
                             * it returns the content of a line MINUS the newline.
                             * it returns null only for the END of the stream.
                             * it returns an empty String if two newlines appear in a row.
                             */
                            while (( line = input.readLine()) != null){
                                    contents.append(line);
                                    //contents.append(System.getProperty("line.separator"));
                            }
                    }
                    finally {
                            input.close();
                    }
            }
            catch (IOException ex){
                    ex.printStackTrace();
            }


            //		actionValuePair.put(pairsA.getKey(), val);
            //	}			

            //		System.out.println("contents: "+contents.toString());
            JSONObject actionsStatePairs;
            try {
                    actionsStatePairs = new JSONObject(contents.toString());
                    //			System.out.println("actionsStatePairs.toString(): "+actionsStatePairs.toString());


                    Iterator itS = actionsStatePairs.keys(); 
                    while (itS.hasNext()) { 
                            int state=(int) itS.next();
                            //				System.out.println("itS.next(): "+state);
                            JSONObject pairAS =  (JSONObject) actionsStatePairs.get(String.valueOf(state)); 	

                            Iterator itA = pairAS.keys();
                            while(itA.hasNext()){
                                    String action=itA.next().toString();

                                    double value = pairAS.getDouble(action);

                                    this.setQ(value, state, action);

                            }
                    }

            } catch (JSONException e1) {

                    e1.printStackTrace();
            } 

    }
}
