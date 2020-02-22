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
package jacobi.core.classifier.svm;

import jacobi.api.Matrix;
import jacobi.core.solver.nonlin.SumLinearArgFunc;

/**
 * Hinge loss function used for maximum-margin classification.
 * 
 * <p>Given &lt;X<sub>k</sub>, y<sub>k</sub>&gt;, y<sub>k</sub> &isin; {-1, 1} </p>
 * 
 * @author Y.K. Chan
 *
 */
public class HingeLoss extends SumLinearArgFunc<Void> {

	public HingeLoss(Matrix consts, double[] weights) {
		super(consts);
		this.weights = weights;
	}

	@Override
	protected double valueAt(Void inter, int index, double x) {
		
		return x > 1.0 ? 0.0 : 1.0 - x;
	}

	@Override
	protected double slopeAt(Void inter, int index, double x) {
		return 0;
	}

	@Override
	protected double convexityAt(Void inter, int index, double x) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Void prepare(double[] pos, double[] args) {
		return null;
	}

	private double[] weights;
}
