/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jacobi.core.classifier.nn;

import java.util.List;
import java.util.function.ToIntFunction;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.ColumnVector;
import jacobi.core.op.MulT;

/**
 * This class represents the data model for a feed-forward neural network.
 * 
 * <p>
 * A feed-forward neural network is constructed by a sequence of layers. A layer consists 
 * a set of neurons, and neurons between consecutive layers are fully connected. The first layer
 * is input layer, which accepts data input, and the final layer is the output layer,
 * which is interpreted as the probability of this instance falling into different categories.
 * </p>
 * 
 * <p>A neuron accepts an scalar input and output a scalar value transformed by an activation
 * function. The scalar input of a neuron is a linear combination of the output of the previous
 * layer.</p>
 * 
 * <p>To describe a feed-forward neural network mathematically, some notations are introduced here.<br>
 * Let F<sub>k</sub> be the output vector function of the k-th layer, and <br>
 *     F<sub>a..k</sub> = F<sub>k</sub>(F<sub>k-1</sub>( ...F<sub>a</sub>(x) ))<br>
 * 
 * F<sub>0</sub>(x) = x is the input layer.
 * F<sub>k</sub>(x) = [g(A<sub>k, i</sub> * x)], 
 * where g is the activation function 
 *   and A<sub>k, i</sub> is the i-th row of weight matrix A<sub>k</sub>. 
 * 
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class FFNeuralNetwork implements ToIntFunction<double[]> {
    
    /**
     * List of layer of neurons
     */
    public final List<Layer> layers;
    
    /**
     * Constructor.
     * @param layers
     */
    public FFNeuralNetwork(List<Layer> layers) {
        this(layers, new MulT());
    }

    /**
     * Constructor
     * @param layers  List of layer of neurons
     * @param mulT    Implementation of matrix multiplication with transpose
     */
    protected FFNeuralNetwork(List<Layer> layers, MulT mulT) {
        this.layers = layers;
        this.mulT = mulT;
    }
    
    @Override
    public int applyAsInt(double[] inst) {
        double[] vector = inst;
        for(Layer layer : this.layers) {
            vector = this.flow(layer, vector);
        }
        return this.argmax(vector);
    }
    
    protected double[] flow(Layer layer, double[] input) {
        Matrix args = this.mulT.compute(layer.weights, Matrices.wrap(new double[][] {input}));
        if(args instanceof ColumnVector) {
            double[] out = ((ColumnVector) args).getVector();
            for(int i = 0; i < out.length; i++){
                out[i] = layer.activator.valueAt(out[i]);
            }
            return out;
        }
        throw new UnsupportedOperationException("Un-expected return class " + args);
    }
    
    protected int argmax(double[] elem) {
        int arg = 0;
        for(int i = 1; i < elem.length; i++){
            if(elem[i] > elem[arg]){
                arg = i;
            }
        }
        return arg;
    }
    
    private MulT mulT;
    
    /**
     * Data object for a layer of neurons.
     * 
     * @author Y.K. Chan
     *
     */
    public static class Layer {
        
        /**
         * Weights of this layer
         */
        public final Matrix weights;
        
        /**
         * Activation function of this layer
         */
        public final Activator activator;

        /**
         * Constructor.
         * @param weights  Weights of this layer
         * @param activator  Activation function of this layer
         */
        public Layer(Matrix weights, Activator activator) {
            this.weights = weights;
            this.activator = activator;
        }
        
    }

}
