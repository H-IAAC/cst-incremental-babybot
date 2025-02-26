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

/**
 *
 * @author L. M. Berto
 * @author L. L. Rossi (leolellisr)
 */
public class Winner {
    public int featureJ;
    public int origin;
    public long fireTime;
    
    public Winner(int featJ, int orig, long fireT){
        featureJ = featJ;
        origin = orig;
        fireTime = fireT;
    }
    
    @Override
    public String toString(){
        return "featJ "+featureJ+" origin "+origin+" fireTime "+fireTime;
    }
}
