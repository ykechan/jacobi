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

import java.util.Arrays;
import java.util.function.Supplier;

import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Weighted;

/**
 * Iterative optimizer that stops when reaching a stationary point.
 * 
 * <p>In the context of gradient descent, momentum is the exponential moving average
 * of the steps in each iteration. A stationary point is that the momentum reaches
 * zero within an error tolerance. This class stops the iteration whenever a stationary
 * point is found.</p>
 * 
 * <p>This class is for optimizing function that is not smooth and the gradient may
 * change abruptly. </p>
 * 
 * @author Y.K. Chan
 *
 */
public class StationaryIterativeOptimizer implements IterativeOptimizer {
	
	/**
	 * Constructor
	 * @param step  Iterative optimizer step
	 * @param decayRate  Decay rate in momentum
	 */
	public StationaryIterativeOptimizer(IterativeOptimizerStep step, double decayRate) {
		this.step = step;
		this.decayRate = decayRate;
	}

	@Override
	public Weighted<ColumnVector> optimize(VectorFunction func, 
			Supplier<double[]> init, long limit, double epsilon) {
		double[] start = init.get();
		
		double[] x = Arrays.copyOf(start, start.length);
		double[] mv = new double[x.length];
		
		double minFx = func.at(x);
		double[] min = Arrays.copyOf(x, x.length);
		
		int span = this.decayRate < epsilon ? 1 : (int) Math.floor(1 / this.decayRate);
		
		for(long t = 0; t < limit; t++){
			double[] dx = this.step.delta(func, x);
			double len = this.move(x, dx, mv);
			
			double fx = func.at(x);
			if(fx < minFx){
				minFx = fx;
				min = Arrays.copyOf(x, x.length);
			}
			
			if(t > span && len < epsilon){
				break;
			}
		}
		
		return new Weighted<>(new ColumnVector(min), minFx);
	}
	
	/**
	 * Move the current position along the delta vector, and update the momentum
	 * @param x  Current position
	 * @param dx  Delta vector
	 * @param mv  Momentum
	 * @return  Norm of the momentum
	 */
	protected double move(double[] x, double[] dx, double[] mv) {
		double beta = this.decayRate;
		double dv = 0;
		
		for(int i = 0; i < x.length; i++){
			x[i] += dx[i];
			
			mv[i] *= beta;
			mv[i] += (1 - beta) * dx[i];
			
			double w = Math.abs(mv[i]);
			if(w > dv){
				dv = w;
			}
		}
		return dv;
	}

	private IterativeOptimizerStep step;
	private double decayRate;
}
