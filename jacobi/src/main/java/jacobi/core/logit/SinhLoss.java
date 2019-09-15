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
 * Sinh as Sigmoid function in Logistic Regression.
 * 
 * <p>Consider sinh(x) = (e<sup>x</sup> + e<sup>-x</sup>) / 2</p>
 * 
 * <p>sinh'(x) = (e<sup>x</sup> - e<sup>-x</sup>) / 2</p>
 * 
 * <p>sinh''(x) = sinh(x)</p>
 *  
 * @author Y.K. Chan
 *
 */
public class SinhLoss extends SumLinearArgFunc<double[]> {

	public SinhLoss(Matrix consts, double[] weights) {
		super(consts);
		this.weights = weights;
	}

	@Override
	protected double valueAt(double[] inter, int index, double x) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected double slopeAt(double[] inter, int index, double x) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected double convexityAt(double[] inter, int index, double x) {
		return 0;
	}

	@Override
	protected double[] prepare(double[] pos, double[] args) {
		return Arrays.stream(args)
			.map(Math::exp)
			.toArray();
	}

	private double[] weights;
}
