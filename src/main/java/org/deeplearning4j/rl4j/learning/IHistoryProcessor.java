
package org.deeplearning4j.rl4j.learning;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Value;
import org.nd4j.linalg.api.ndarray.INDArray;
import lombok.Builder.Default;
/**
 * @author leolellisr 
 *
 * An IHistoryProcessor come directly from the atari DQN paper.
 * It applies pre-processing the pixels of one state (gray-scaling + resizing)
 * then stacks it in different channels to be fed to a conv net
 */
public interface IHistoryProcessor {
    int historyLength = 4;
    int rescaledHeight = 84;
    int rescaledWidth = 84;
    
    public static int getHistoryLength(){
        return historyLength;
    }
    
    public static int getRescaledHeight(){
        return rescaledHeight;
    }
    
    public static int getRescaledWidth(){
        return rescaledWidth;
    }
    
   
    
    Configuration getConf();

    /** Returns compressed arrays, which must be rescaled based
     *  on the value returned by {@link #getScale()}. */
    INDArray[] getHistory();

    void record(INDArray image);

    void add(INDArray image);

    void startMonitor(String filename, int[] shape);

    void stopMonitor();

    boolean isMonitoring();

    /** Returns the scale of the arrays returned by {@link #getHistory()}, typically 255. */
    double getScale();

    @AllArgsConstructor
    @Builder
    @Data
    
    public static class Configuration {
        @Builder.Default int historyLength = 4;
        @Builder.Default int rescaledWidth = 84;
        @Builder.Default int rescaledHeight = 84;
        @Builder.Default int croppingWidth = 84;
        @Builder.Default int croppingHeight = 84;
        @Builder.Default int offsetX = 0;
        @Builder.Default int offsetY = 0;
        @Builder.Default int skipFrame = 4;

        public int[] getShape() {
            return new int[] {getHistoryLength(), getRescaledHeight(), getRescaledWidth()};
        }
        
        public int getHistoryLength(){
            return historyLength;
        }
        
        public int getSkipFrame(){
            return skipFrame;
        }
        
        public int getCroppingHeight(){
            return croppingHeight;
        }
        
        public int getCroppingWidth(){
            return croppingWidth;
        }
        public int getOffsetY(){
            return offsetY;
        }
        public int getOffsetX(){
            return offsetX;
        }
        public int getRescaledHeight(){
            return rescaledHeight;
        }
        public int getRescaledWidth(){
            return rescaledWidth;
        }
    }
}
