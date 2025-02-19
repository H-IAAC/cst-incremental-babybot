/*******************************************************************************
 * @leolellisr
 ******************************************************************************/
package org.deeplearning4j.rl4j.observation.transform.legacy;

import org.datavec.api.transform.Operation;
import org.datavec.image.data.ImageWritable;
import org.datavec.image.loader.NativeImageLoader;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;

public class ImageWritableToINDArrayTransform implements Operation<ImageWritable, INDArray> {

    private final NativeImageLoader loader = new NativeImageLoader();

    @Override
    public INDArray transform(ImageWritable imageWritable) {

        int height = imageWritable.getHeight();
        int width = imageWritable.getWidth();
        int channels = imageWritable.getFrame().imageChannels;

        INDArray out = null;
        try {
            out = loader.asMatrix(imageWritable);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Convert back to uint8 and reshape to the number of channels in the image
        out = out.reshape(channels, height, width);
        INDArray compressed = out.castTo(DataType.INT);
        return compressed;
    }
}
