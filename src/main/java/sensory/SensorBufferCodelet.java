/*******************************************************************************
 * Copyright (c) 2012  DCA-FEEC-UNICAMP
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 * 
 * Contributors:
 *     K. Raizer, A. L. O. Paraense, R. R. Gudwin - initial API and implementation
 ******************************************************************************/

package sensory;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author leandro
 */
public class SensorBufferCodelet extends Codelet {
    private MemoryObject sensor_input;
    private MemoryObject buffer_output;
    private String sensorName;
    private String bufferName;
    private int maxcapacity;
    private int ignore;
    
    public SensorBufferCodelet(String sensorName, String bufferName, int maxcpcty) {
        super();
        this.bufferName = bufferName;
        this.sensorName = sensorName;
        maxcapacity = maxcpcty;
        ignore = 1;
    }

    @Override
    public void accessMemoryObjects() {
        sensor_input = (MemoryObject) this.getInput(sensorName);
        buffer_output = (MemoryObject) this.getOutput(bufferName);
    }

    @Override
    public void calculateActivation() {
        
    }

    @Override
    public void proc() {
        if(ignore == 1){
            ignore = 0;
            return;
        }
        
        /*try {
            Thread.sleep(5);
//            System.out.println("\u001B[31m"+"TRY CATCH");
        } catch (Exception e) {
            Thread.currentThread().interrupt();
        }*/
                
        List sonar_buffer_list = (List) buffer_output.getI();
        
        if(sonar_buffer_list.size() == maxcapacity){
            sonar_buffer_list.remove(0);
        }
        
        Object sensor_input_data = (Object) sensor_input.getI();
        MemoryObject cloned_data = null;
        
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try
        {
           ByteArrayOutputStream bos = 
                 new ByteArrayOutputStream(); // A
           oos = new ObjectOutputStream(bos); // B
           // serialize and pass the object
           oos.writeObject(sensor_input);   // C
           oos.flush();               // D
           ByteArrayInputStream bin = 
              new ByteArrayInputStream(bos.toByteArray()); // E
           ois = new ObjectInputStream(bin);                  // F
           // return the new object
           cloned_data = (MemoryObject) ois.readObject(); // G
           
           oos.close();
           ois.close();
        }
        catch(IOException | ClassNotFoundException e)
        {
           System.out.println("Exception in ObjectCloner = " + e);
        }
        
        sonar_buffer_list.add(cloned_data);

                
//        System.out.println("BUFFER SIZE "+sonar_buffer_list.size());
    }
        
}
