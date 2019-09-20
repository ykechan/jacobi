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
package jacobi.core.solver.nonlin;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Weighted;

/**
 * Implementation of iterative optimizer that tries different optimizers in sequence until
 * a solution is found or exhausted the computation limit.
 * 
 * <p>For some problem it is normally convex and worth to try an aggressive approach,
 * but may fails to converge in some cases. Thus it is beneficial to fallback to more
 * conservative approach upon encountering such situation.</p>
 * 
 * <p>The computation resources are allocated in a geometric progression, i.e. the next
 * optimizer is given double the computational resources of the last one. The total limit
 * would not exceed the hard limit given to ensure turnaround in a reasonably expected time.</p>
 * 
 * <p>The first solution with error under given tolerance is returned.
 * If none of them have tolerable error value, the one with the least error is chosen. 
 * If none of the optimizers find any solution, a guess for the initial position is used.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class QueuedOptimizer implements IterativeOptimizer {
	
	/**
	 * Constructor.
	 * @param optimizers  List of optimizers
	 */
	public QueuedOptimizer(List<IterativeOptimizer> optimizers) {
		this.optimizers = optimizers;
	}

	@Override
	public Weighted<ColumnVector> optimize(VectorFunction func, 
			Supplier<double[]> init, 
			long limit, double epsilon) {
		long subLim = this.subLimit(limit);
		long used = 0L;
		
		Weighted<ColumnVector> ans = null;
		for(IterativeOptimizer opt : this.optimizers) {
			long budget = Math.min(subLim, limit - used);
			if(budget <= 0 || (ans != null && ans.weight < epsilon)) {
				break;
			}
			
			Weighted<ColumnVector> optima = opt.optimize(func, init, budget, epsilon);						
			
			if(optima != null && (ans == null || ans.weight > optima.weight)) {
				ans = optima;
			}
			
			subLim *= 2;
			used += budget;
		}
		return ans == null ? this.defaultSolution(func, init) : ans;
	}		
	
	/**
	 * Find the computation resources threshold for the 1st optimizer
	 * @param limit  Limit for all the optimizers
	 * @return  Computation resources threshold for the 1st optimizer
	 */
	protected long subLimit(long limit) {
		// Sum{1 + 2^n} = 1 - 2^(n+1) / (1 - 2) = 2^(n+1) - 1
		long pow = 1 << (this.optimizers.size() + 1) - 1;
		return Math.max(1L, limit / pow);
	}	
	
	/**
	 * Construct a default solution
	 * @param func  Vector function
	 * @param init  Factory method for starting position
	 * @return  Default solution that is the starting position, with L-1 norm of the gradient as error.
	 */
	protected Weighted<ColumnVector> defaultSolution(VectorFunction func, Supplier<double[]> init) {
		double[] ans = init.get();
		double[] df = func.grad(ans).getVector();
		return new Weighted<>(
			new ColumnVector(ans), 
			Arrays.stream(df).map(Math::abs).max().orElse(0.0)
		);
	}

	private List<IterativeOptimizer> optimizers;
}
