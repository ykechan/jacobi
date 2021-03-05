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
package jacobi.core.solver.nonlin;

/**
 * Implementation of a step of gradient descent.
 * 
 * <p>
 * Gradient descent is an iterative optimization algorithm of the following sequence:
 * 
 * x<sub>n + 1</sub> = x<sub>n</sub> - &lambda;&nabla;f, where &lambda; is a small learning rate.
 * 
 * It is Newton-Raphson method but instead of computing the inverse Hessian matrix, it uses a small
 * learning rate to rein in the step.
 * </p>
 * 
 * @author Y.K. Chan
 */
public class GradientDescentStep implements IterativeOptimizerStep {
	
	/**
	 * Constructor.
	 * @param learningRate  Learning rate
	 */
	public GradientDescentStep(double learningRate) {
		this.learningRate = learningRate;
	}

	@Override
	public double[] delta(VectorFunction func, double[] curr) {
		double[] df = func.grad(curr).getVector();
		for(int i = 0; i < df.length; i++) {
			df[i] *= -this.learningRate;
		}
		return df;
	}

	private double learningRate;
}
