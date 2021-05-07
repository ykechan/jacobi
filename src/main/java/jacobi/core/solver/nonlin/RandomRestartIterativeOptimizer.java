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

import java.util.function.Supplier;

import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Weighted;

/**
 * Implementation of random restart iterative optimizer.
 * 
 * @author Y.K. Chan
 *
 */
public class RandomRestartIterativeOptimizer implements IterativeOptimizer {
	
	/**
	 * Constructor.
	 * @param optimizer  Base optimizer
	 * @param epochs  Number of epochs
	 */
	public RandomRestartIterativeOptimizer(IterativeOptimizer optimizer, int epochs) {
		this.optimizer = optimizer;
		this.epochs = epochs;
	}
	
	@Override
	public Weighted<ColumnVector> optimize(VectorFunction func, Supplier<double[]> init, long limit, double epsilon) {
		double[] ans = null;
		double minFx = 0.0;
		
		long stop = limit / this.epochs;
		for(int i = 0; i < this.epochs; i++){
			Weighted<ColumnVector> fx = this.optimizer.optimize(func, init, stop, epsilon);
			double y = func.at(fx.item.getVector());
			
			if(ans == null || y < minFx){
				ans = fx.item.getVector();
				minFx = y;
			}
		}
		
		if(ans == null){
			// no answer found
			ans = init.get();
			minFx = func.at(ans);
		}
		return new Weighted<>(new ColumnVector(ans), minFx);
	}
	
	private IterativeOptimizer optimizer;
	private int epochs;
}
