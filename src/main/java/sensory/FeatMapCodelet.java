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
package sensory;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author leandro
 */
public abstract class FeatMapCodelet extends Codelet{
    
    protected List sensor_buffers;
    protected ArrayList<String> sensorbuff_names;
    protected MemoryObject featureMap;
    protected String feat_map_name;
    protected double dt = 0.05;
    protected int num_sensors;
    protected int timeWindow;
    protected int mapDimension;
    protected MemoryObject winners, regionMO;
    public FeatMapCodelet(int nsensors, ArrayList<String> sensbuff_names, String featmapname,int timeWin, int mapDim){
        sensor_buffers = new ArrayList<MemoryObject>();
        num_sensors = nsensors;
        sensorbuff_names = sensbuff_names;
        feat_map_name = featmapname;
        timeWindow = timeWin;
        mapDimension = mapDim;
    }
    

    @Override
    public void accessMemoryObjects() {
        for (int i = 0; i < num_sensors; i++) {
            sensor_buffers.add((MemoryObject)this.getInput(sensorbuff_names.get(i)));
        }
        featureMap = (MemoryObject) this.getOutput(feat_map_name);
        //winnersType = (MemoryObject) this.getOutput("TYPE");
        winners = (MemoryObject) this.getInput("WINNERS");
        
        regionMO = (MemoryObject) this.getOutput("REGION_TOP_FM");
    }    
}
