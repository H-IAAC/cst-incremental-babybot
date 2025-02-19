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
public abstract class CombFeatMapCodelet extends Codelet {
    protected MemoryObject comb_feature_mapMO;        
    protected List feature_maps;
    protected ArrayList<String> feat_maps_names;
    protected int num_feat_maps;
    protected MemoryObject weights;
    protected int timeWindow;
    protected int CFMdimension;
    protected MemoryObject winnersType;
    public CombFeatMapCodelet(int numfeatmaps, ArrayList<String> featmapsnames,int timeWin, int CFMdim){
        feature_maps = new ArrayList<MemoryObject>();
        num_feat_maps = numfeatmaps;
        feat_maps_names = featmapsnames;
        timeWindow = timeWin;
        CFMdimension = CFMdim;
    }

    @Override
    public void accessMemoryObjects() {
        for (int i = 0; i < num_feat_maps; i++) {
            feature_maps.add((MemoryObject)this.getInput(feat_maps_names.get(i)));
        }
        weights = (MemoryObject) this.getInput("FM_WEIGHTS");
        comb_feature_mapMO = (MemoryObject) this.getOutput("COMB_FM");
        winnersType = (MemoryObject) this.getOutput("TYPE");
        //winners = (MemoryObject) this.getOutput("WINNERS");
    }

    @Override
    public void calculateActivation() {
        
    }
    
    public abstract void calculateCombFeatMap();

    @Override
    public void proc(){
        calculateCombFeatMap();
    }
    
}
