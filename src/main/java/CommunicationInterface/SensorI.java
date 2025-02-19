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
 
package CommunicationInterface;

import java.util.ArrayList;

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public interface SensorI {
    public boolean getNextActR();
    public void setNextActR(boolean next_ac);
    public void setNextAct(boolean next_ac);
     public boolean getNextAct();
     public String gettype();
    public String getLastAction();
    public void setLastAction(String a);
    public boolean endEpochR();
    public ArrayList<String> getExecutedAct();
     public void addAction(String a);
    public float getFValues(int i);
    public void setFValues(int i, float f);
    public float getIValues(int i);
    public void setIValues(int i, int f);     
    public void setEpoch(int exp);
    public int getEpoch();
    public Object getData();
    public void resetData();
    public int getMaxActions();
    public int getMaxEpochs();
    public boolean endEpoch();
    public int getStage();
public int getAux();
public int getnAct();
public void setnAct(int a);
    public void setStage(int stage);
}
