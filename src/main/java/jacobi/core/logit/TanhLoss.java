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
package jacobi.core.logit;

import java.util.Arrays;

import jacobi.api.Matrix;
import jacobi.core.solver.nonlin.SumLinearArgFunc;

/**
 * Loss function for Logistic Regression using Tanh as the Sigmoid function.
 * 
 * <p>The tanh(x) = (e<sup>x<sup> - e<sup>-x</sup>)/(e<sup>x<sup> + e<sup>-x</sup>) is
 * just a shifted and scaled logistic function, which has range (-1, 1). However given
 * this range, the loss function can be directly fitted to the outcome y<sub>k</sub> &isin; {-1, 1},
 * and the computation thus takes a slightly different form from the log-likelihood approach.</p>
 * 
 * <p>
 * Given observations y<sub>k</sub> &isin; {-1, 1}, consider the loss function<br>
 * L(a) = -&sum;y<sub>k</sub>t<br>, where t = tanh(ax<sub>k</sub>)
 * L'(a) = [-x<sub>i</sub>&sum;y<sub>k</sub>(1 - t<sup>2</sup>)]<br>
 * L''(a) = [2x<sub>i</sub>x<sub>j</sub>&sum;y<sub>k</sub>t(1 - t<sup>2</sup>)]<br>
 * </p>
 *  
 * @author Y.K. Chan
 *
 */
public class TanhLoss extends SumLinearArgFunc<double[]> {

	/**
     * Constructor.
     * @param consts  Constant coefficients for arguments
     * @param weights  Signed weights with signs representing outcomes
     */
	public TanhLoss(Matrix consts, double[] weights) {
		super(consts);
		this.weights = weights;
	}

	@Override
	protected double valueAt(double[] inter, int index, double x) {
		return -this.weights[index] * inter[index];
	}

	@Override
	protected double slopeAt(double[] inter, int index, double x) {
		return -this.weights[index] * (1 - inter[index] * inter[index]);
	}

	@Override
	protected double convexityAt(double[] inter, int index, double x) {
		double t = inter[index];
		return 2 * this.weights[index] * t * (1 - t * t);
	}

	@Override
	protected double[] prepare(double[] pos, double[] args) {
		return Arrays.stream(args).map(Math::tanh).toArray();
	}

	private double[] weights;
}
