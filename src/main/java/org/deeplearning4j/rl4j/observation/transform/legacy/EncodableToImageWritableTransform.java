/*******************************************************************************
 * @author leolellisr
 ******************************************************************************/
package org.deeplearning4j.rl4j.observation.transform.legacy;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.opencv.opencv_core.Mat;
import org.datavec.api.transform.Operation;
import org.datavec.image.data.ImageWritable;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.rl4j.space.Encodable;
import org.nd4j.linalg.api.buffer.DataType;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import static org.bytedeco.opencv.global.opencv_core.CV_32FC;
import static org.bytedeco.opencv.global.opencv_core.CV_32FC3;
import static org.bytedeco.opencv.global.opencv_core.CV_32S;
import static org.bytedeco.opencv.global.opencv_core.CV_32SC;
import static org.bytedeco.opencv.global.opencv_core.CV_32SC3;
import static org.bytedeco.opencv.global.opencv_core.CV_64FC;
import static org.bytedeco.opencv.global.opencv_core.CV_8UC3;

public class EncodableToImageWritableTransform implements Operation<Encodable, ImageWritable> {

    final static NativeImageLoader nativeImageLoader = new NativeImageLoader();

    @Override
    public ImageWritable transform(Encodable encodable) {
        return new ImageWritable(nativeImageLoader.asFrame(Nd4j.create(encodable.toArray()), Frame.DEPTH_UBYTE));
    }

}
