/*
 * The MIT License
 *
 * Copyright 2021 Y.K. Chan
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
package jacobi.api.classifier.svm;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

import jacobi.api.classifier.Classifier;
import jacobi.core.op.Dot;

/**
 * Classifier model class using a support vector machine.
 * 
 * <p>
 * A support vector machine classify an instance feature vector x to true when
 * 
 * &lt;w, x&gt; > b, where w is the coefficient value and b is some bias value.
 * 
 * &lt;., .&gt; can be generalized to some other functions instead of the dot
 * product
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class SupportVectorMachine implements Classifier<Boolean>, ToDoubleFunction<double[]> {

	/**
	 * Constructor.
	 * @param coeff  Coefficient value
	 * @param bias  Bias value
	 */
	public SupportVectorMachine(double[] coeff, double bias) {
		this.coeff = coeff;
		this.bias = bias;
	}
	
	/**
	 * Get number of dimensions of the hyperplane
	 * @return  Number of dimensions
	 */
	public int dim() {
		return this.coeff.length;
	}
	
	/**
	 * Get the coefficient of a certain term
	 * @param index  Index of the dimension of the term
	 * @return  Value of the coefficient
	 */
	public double getCoeff(int index){
		return this.coeff[index];
	}
	
	/**
	 * Get the bias value
	 * @return  Value of the bias term
	 */
	public double getBias() {
		return this.bias;
	}

	@Override
	public Boolean apply(double[] t) {
		return this.applyAsDouble(t) > 0;
	}

	@Override
	public double applyAsDouble(double[] value) {
		return Dot.prod(this.coeff, value) - this.bias;
	}
	
	@Override
	public String toString() {
		return "<" + Arrays.toString(this.coeff) + ", .> - " + this.bias;
	}

	private double[] coeff;
	private double bias;
}
