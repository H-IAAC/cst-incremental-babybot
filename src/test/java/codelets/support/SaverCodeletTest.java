/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package codelets.support;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import br.unicamp.cst.core.entities.Mind;

import br.unicamp.cst.core.exceptions.CodeletActivationBoundsException;
import br.unicamp.cst.core.exceptions.CodeletThresholdBoundsException;
import br.unicamp.cst.support.TimeStamp;
import java.util.Arrays;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ic-unicamp
 */
public class SaverCodeletTest {
    
    public MemoryObject source;
    public MemoryObject destination;
    public saverCodelet testsaverCodelet;
    public boolean debug = false;
    /**
     * Test class initialization for the Combined Feature Map. Creates a test 
     * mind, with 4 inputs (3 sources and 1 weight) and 2 outputs. The codelet 
     * to be tested is initialized as a CFM and inserted into the created mind. 
     * Inputs and outputs are added to the codelet and it is set to 
     * publish-subscribe. The mind is then initiated.
     * 
     */
    public SaverCodeletTest() {
        Mind testMind = new Mind();
        source = testMind.createMemoryObject( "SOURCE");      

        CopyOnWriteArrayList<String> FMnames = new CopyOnWriteArrayList<>();
        FMnames.add("SOURCE");
        testsaverCodelet = new saverCodelet(4, "test.txt");
        testMind.insertCodelet(testsaverCodelet);
        testsaverCodelet.addInput(source);
        testsaverCodelet.setIsMemoryObserver(true);
	source.addMemoryObserver(testsaverCodelet);
              
        testMind.start();
        
        
        //List fulllist = (List)destination.getI();
        
        
    }
    
    /**
    * Test 1. Inputs have sequential elements from 1 to 4. The weight vector is 
    * initialized with 1s only. Thus, the output element will be the sum of the 
    * elements of the same position [3, 6, 9, 12 ...].
    * 
    * Test 2. Inputs have elements equal to 1. The weight vector is initialized 
    * with 1s only. Thus, the output element will be the sum of elements of the 
    * same position [3, 3, 3, 3 ...].
    * 
    * Test 3. Inputs have elements equal to 1. The weight vector is initialized 
    * with sequential elements from 1 to 3. Thus, the output element will be the 
    * sum of elements in the same position [1*1+1*2+1* 3=6, 6, 6, 6...]
    * 
    */
    @Test
    public void testsaverCodelet() {
            SaverCodeletTest test = new SaverCodeletTest();

            if(debug) System.out.println("Testing ... ");
            
            long oldtimestamp = test.source.getTimestamp();
            if(debug) System.out.println(" Timestamp before: "+TimeStamp.getStringTimeStamp(oldtimestamp, "dd/MM/yyyy HH:mm:ss.SSS"));
            
            CopyOnWriteArrayList<CopyOnWriteArrayList<Float>> arrList_test = new CopyOnWriteArrayList<CopyOnWriteArrayList<Float>>();
            
            // Test 1
            long newtimestamp = test.source.getTimestamp();
             for (int i = 0; i < 4; i++) {
                 if(debug) System.out.println("source: Timestamp after: "+TimeStamp.getStringTimeStamp(test.source.getTimestamp(),"dd/MM/yyyy HH:mm:ss.SSS"));
                 newtimestamp = test.source.getTimestamp();
                test.source.setI("test1 "+newtimestamp);
                sleep(10);

             }
            newtimestamp = test.source.getTimestamp();
            if(debug) System.out.println(" new Timestamp afterr: "+TimeStamp.getStringTimeStamp(newtimestamp,"dd/MM/yyyy HH:mm:ss.SSS"));
            sleep(20);
           // Test 2            
            newtimestamp = test.source.getTimestamp();
             for (int i = 0; i < 4; i++) {
                 System.out.println("source: Timestamp after: "+TimeStamp.getStringTimeStamp(test.source.getTimestamp(),"dd/MM/yyyy HH:mm:ss.SSS"));
                 newtimestamp = test.source.getTimestamp();
                test.source.setI("test2 "+newtimestamp);
                sleep(10);

             }
            newtimestamp = test.source.getTimestamp();
            if(debug) System.out.println(" new Timestamp afterr: "+TimeStamp.getStringTimeStamp(newtimestamp,"dd/MM/yyyy HH:mm:ss.SSS"));

    }
            
    private void sleep(int time) {
        try{ Thread.sleep(time); } catch(Exception e){e.printStackTrace();}
    }
}