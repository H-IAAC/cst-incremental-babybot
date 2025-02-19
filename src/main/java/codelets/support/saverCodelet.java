/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.support;

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
 * @author ic-unicamp
 */
public class saverCodelet extends Codelet 
{
    private String stringtoMap;
    private ArrayList MapString;
    private int buffer_size, activation;
    private long last_timestep = 0;
    private MemoryObject MO;
    private String filename;
    private boolean debug = false;
    public saverCodelet(int buffer_size, String filename){
        MapString = new ArrayList<String>();
        this.buffer_size = buffer_size;
        this.filename = filename;
        this.activation = 0;
    }
    @Override
    public void accessMemoryObjects() {
                if(!this.inputs.isEmpty()){
                    MO = (MemoryObject) this.inputs.get(0);
                    stringtoMap = (String) MO.getI();
                }
        }
    
    	// Main Codelet function, to be implemented in each subclass.
	@Override
	public void proc() {
            //System.out.println(" proc saver codelet. stringtoMap:"+stringtoMap+"MO.getTimestamp:"+MO.getTimestamp()+"last_timestep"+this.last_timestep);
            System.out.println("save"+filename+" : "+MapString.size()+"/"+this.buffer_size+" last_timestep: "+last_timestep+"MO.getTimestamp()"+MO.getTimestamp());
            
            if(stringtoMap != null && MO.getTimestamp() != this.last_timestep){
                System.out.println("added string to map");
                MapString.add(stringtoMap);
                this.last_timestep = MO.getTimestamp();
            }
            
            if(MapString.size() == this.buffer_size){
                System.out.println("print to file");
                printToFile(MapString, this.filename);
                MapString.clear();
            }
        }
        
        private void printToFile(Object object,String filename){
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm_ss");  
        LocalDateTime now = LocalDateTime.now();
        
        try(FileWriter fw = new FileWriter("profile/"+filename,true);
	            BufferedWriter bw = new BufferedWriter(fw);
	            PrintWriter out = new PrintWriter(bw))
	        {
	            out.println(dtf.format(now)+" "+ object);	            
	            out.close();
	            
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
        
	}

    @Override
    public void calculateActivation() {
        this.activation = 0;
    }

}