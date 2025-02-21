package org.deeplearning4j.examples.advanced.features.customizingdl4j.activationfunctions;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nd4j.common.primitives.Pair;
import org.nd4j.linalg.activations.BaseActivationFunction;
import org.nd4j.linalg.api.ndarray.INDArray;

import org.nd4j.linalg.factory.Nd4j;

import static org.nd4j.linalg.ops.transforms.Transforms.*;

/**
 * This is an example of how to implement a custom activation function that does not take any learnable parameters
 * Custom activation functions of this case should extend from BaseActivationFunction and implement the methods
 * shown here.
 * IMPORTANT: Do not forget gradient checks. Refer to these in the deeplearning4j repo,
 * deeplearning4j-core/src/test/java/org/deeplearning4j/gradientcheck/LossFunctionGradientCheck.java
 *
 * The form of the activation function implemented here is from https://ojs.aaai.org/index.php/AAAI/article/view/16828
 * "Deep Radial-Basis Value Functions for Continuous Control" by Kavosh Asadi et. al.
 *
 *      h(x) = exp(-b*(x-c))
 *
 * @author leolellisr
 */
public class RBFActivation_1 extends BaseActivationFunction{

    /*
        For the forward pass:

        Transform "in" with the activation function. Best practice is to do the transform in place as shown below
        Can support different behaviour during training and test with the boolean argument
     */
    
    private float beta = (float) 0.1;
    private int center = 0;
    
    @Override
    public INDArray getActivation(INDArray in, boolean training) {
        //Modify array "in" inplace to transform it with the activation function
        // h(x) = exp(-b*|x-c|)
        // Logger.getAnonymousLogger().log(Level.INFO, "getActivation in shape {0}:", Arrays.toString(in.shape()));
        
        in.subi(center);
        // Logger.getAnonymousLogger().log(Level.INFO, "getActivation in shape pos sub {0}:", Arrays.toString(in.shape()));

        
        for(int i=0; i < in.length();i++){
            in.putScalar(i, Math.abs( (float) in.getFloat(i) ) );
        }
        // Logger.getAnonymousLogger().log(Level.INFO, "getActivation in shape pos abs {0}:", Arrays.toString(in.shape()));
        
        in.muli(-beta);
        // Logger.getAnonymousLogger().log(Level.INFO, "getActivation in shape pos mul {0}:", Arrays.toString(in.shape()));

        for(int i=0; i < in.length();i++){
            in.putScalar(i, Math.exp( (float) in.getFloat(i) ) );
        }
        // Logger.getAnonymousLogger().log(Level.INFO, "getActivation in shape pos exp {0}:", Arrays.toString(in.shape()));
        
        return in;
    }

    /*
        For the backward pass:
        Given epsilon, the gradient at every activation node calculate the next set of gradients for the backward pass
        Best practice is to modify in place.

        Using the terminology,
            in -> linear input to the activation node
            out    -> the output of the activation node, or in other words h(out) where h is the activation function
            epsilon -> the gradient of the loss function with respect to the output of the activation node, d(Loss)/dout

                h(in) = out;
                d(Loss)/d(in) = d(Loss)/d(out) * d(out)/d(in)
                              = epsilon * h'(in)
     */
    @Override
    public Pair<INDArray,INDArray> backprop(INDArray in, INDArray epsilon) {
        //dldZ here is h'(in) in the description above
        //
        //      h(x) = exp(-b*(|x-c|)) 
        //      h'(x) = -b * exp(-b*(|x-c|))
        // x: in
        // c: center
        // b: beta
        
        in.subi(center);
        for(int i=0; i < in.length();i++){
            in.putScalar(i, Math.abs( (float) in.getFloat(i) ) );
        }
        in.muli(-beta);
        for(int i=0; i < in.length();i++){
            in.putScalar(i, Math.exp( (float) in.getFloat(i) ) );
        }
        in.muli(-beta);
        //Multiply with epsilon
        in.muli(epsilon);
        return new Pair<>(in, null);
    }

}
