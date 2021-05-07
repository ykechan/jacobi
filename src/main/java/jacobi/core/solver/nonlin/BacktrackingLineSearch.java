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

import jacobi.core.op.Dot;
import jacobi.core.util.Real;

/**
 * Implementation of the backtracking line search.
 * 
 * <p>Gradient descent based algorithm simply goes in the opposite direction of the gradient vector, which
 * indicates a lower value. For smooth function this would converge to a local minima but the convergence 
 * speed depends on the learning rate, which tends to be small. This is also not ideal when using sub-gradients
 * which the derivative is not continuous, causing the algorithm to overshot and difficult to find the location
 * which the gradient is zero.</p>
 * 
 * <p>
 * Consider the taylor expansion of f(x + a) = f(x) + af'(x) + higher order terms.
 * 
 * f(x) - f(x + a) ~ -af'(x), thus the decrease should depends on ||a|| and ||f'||.
 * 
 * In multivariate situation, this amounts to the Armijo-Goldstein condition (1966), i.e.
 * f(x + &lambda;dx) &lt;= &lambda;&alpha;&lt;&nabla;f, dx&gt;, where &alpha; &isin; (0, 1] is a control parameter.
 * </p>
 * 
 * <p>
 * In a gradient descent step, if this condition fails to hold indicates the move is too steep. This class
 * would then backtrack in a exponential decay rate until the condition holds. At a certain point if no move
 * satisfy the condition, the move should be small enough to reach the stopping criteria.
 * </p>
 * 
 * <p>With this regulation a larger learning rate can be used in the Gradient descent algorithm.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class BacktrackingLineSearch implements IterativeOptimizerStep {
	
	/**
	 * Default value for the decay rate
	 */
	public static final double DEFAULT_DECAY_RATE = Real.GOLDEN_RATIO;
	
	/**
	 * Default value for the control parameters
	 */
	public static final double DEFAULT_CONTROL_PARAM = 1.0 - Real.GOLDEN_RATIO;
	
	
	/**
	 * Constructor with default control param and decay rate.
	 * @param step  Base optimizer step
	 * @param epsilon  Stopping criteria
	 */
	public BacktrackingLineSearch(IterativeOptimizerStep step, double epsilon) {
		this(step, DEFAULT_CONTROL_PARAM, DEFAULT_DECAY_RATE, epsilon);
	}

	/**
	 * Constructor.
	 * @param step  Base optimizer step
	 * @param control  Control parameter
	 * @param decay  Decay rate
	 * @param epsilon  Stopping criteria
	 */
	public BacktrackingLineSearch(IterativeOptimizerStep step, double control, double decay, double epsilon) {
		this.control = control;
		this.decay = decay;
		this.step = step;
		this.epsilon = epsilon;
	}
	
	@Override
	public double[] delta(VectorFunction func, double[] curr) {
		double[] x = Arrays.copyOf(curr, curr.length);
		double fx = func.at(x);
		
		double[] gradFx = func.grad(x).getVector();
		double[] dx = this.step.delta(func, x);
		
		double tau = Math.abs(this.control * Dot.prod(gradFx, dx));
		double lambda = 1.0;
		
		double fy = fx;
		int k = 0;
		while(lambda > this.epsilon){
			System.out.println("backtrack " + (k++) + ", worse = " + (fy > fx) + ", lambda=" + lambda);
			if(fy < fx && lambda < 1e-4){
				break;
			}
			double[] y = this.move(curr, lambda, dx);
			fy = func.at(y);
			
			if(fx - fy >= lambda * tau){
				break;
			}
			
			lambda *= this.decay;
		}
		return fy > fx ? new double[curr.length] : this.trim(dx, lambda);
	}
	
	/**
     * Move the position vector by a delta vector and step size
     * @param pos  Position vector
     * @param lambda  Step size
     * @param dx  Delta vector
     * @return  Instance of position vector with values updated
     */
    protected double[] move(double[] pos, double lambda, double[] dx) {
    	double[] q = new double[pos.length];
        for(int i = 0; i < q.length; i++) {
            q[i] = pos[i] + lambda * dx[i];
        }
        return q;
    }
    
    /**
     * Trim the vector by multiplying with a scalar
     * @param dx  Input vector
     * @param lambda  Input scalar
     * @return  Vector trimmed
     */
    protected double[] trim(double[] dx, double lambda) {
    	for(int i = 0; i < dx.length; i++){
    		dx[i] *= lambda;
    	}
    	return dx;
    }

	private IterativeOptimizerStep step;
	private double control, decay, epsilon;
}
