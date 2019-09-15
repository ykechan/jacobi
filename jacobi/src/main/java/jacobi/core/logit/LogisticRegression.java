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
import java.util.function.BiFunction;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

import jacobi.api.Matrix;
import jacobi.core.solver.nonlin.IterativeOptimizer;
import jacobi.core.solver.nonlin.VectorFunction;
import jacobi.core.stats.RowReduce;
import jacobi.core.stats.Variance;
import jacobi.core.util.Real;

/**
 * Implementation of Logistic Regression.
 * 
 * <p>Logistic Regression is to fit a Sigmoid curve S on a set of data {X<sup>i</sup>, y<sup>i</sup>}
 * where y<sup>i</sup> &isin; {0, 1}.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class LogisticRegression {
	
	/**
	 * Constructor.
	 * @param optimizer  Function optimizer
	 * @param sigmoidFactory  Factory of creating loss function
	 */
	protected LogisticRegression(IterativeOptimizer optimizer,
			BiFunction<Matrix, double[], VectorFunction> sigmoidFactory) {
		this.optimizer = optimizer;
		this.sigmoidFactory = sigmoidFactory;
	}
	
	/**
	 * Compute the logistic regression on the input data with associated outcome
	 * @param matrix  Input data i.e. X = {x<sup>i</sup>}
	 * @param outcomes Outcome of each x<sup>i</sup>, i.e. y<sup>i</sup> 
	 *                 s.t. true represents 1 and false represents 0.
	 * @return  Coefficient of the Sigmoid function
	 */
	public double[] compute(Matrix matrix, boolean[] outcomes) {
		double[] weights = new double[outcomes.length];
		
		for(int i = 0; i < weights.length; i++) {
			weights[i] = outcomes[i] ? 1.0 : -1.0;
		}
		
		return this.compute(matrix, weights);
	}
	
	/**
	 * Compute the logistic regression on the input data with given weights, in which a positive
	 * weight indicates a positive result and a negative weight indicates a negative result.
	 * @param matrix  Input data i.e. X = {x<sup>i</sup>}
	 * @param weights  Weights of each row
	 * @return  Coefficient of the Sigmoid function
	 */
	public double[] compute(Matrix matrix, double[] weights) {
		
		VectorFunction func = this.sigmoidFactory.apply(matrix, weights);
		
		return this.optimizer.optimize(func,
			() -> new double[matrix.getColCount()],
			iterMult * matrix.getColCount(), Real.TOLERANCE
		).item.getVector();
	}
	
	protected Supplier<double[]> init(Matrix matrix, DoubleSupplier rand) {
		double[] mean = new RowReduce.Mean().compute(matrix);
		double[] stdDev = new Variance.StdDev().compute(matrix);
		
		int biasIndex = this.detectBias(stdDev);
		System.out.println(Arrays.toString(mean));
		System.out.println(Arrays.toString(stdDev));
		
		return () -> {
			double[] vector = new double[matrix.getColCount()];
			double offset = 0.0;
			for(int i = 0; i < vector.length; i++) {
				if(i == biasIndex) {
					continue;
				}
				vector[i] = (rand.getAsDouble() * stdDev[i]) / mean[i];
				offset += vector[i] * mean[i];
			}
			
			if(biasIndex >= 0) {
				vector[biasIndex] = rand.getAsDouble() * -offset;
			}
			return vector;
		};
	}
	
	protected int detectBias(double[] stdDev) {
		int bias = -1;
		for(int i = 0; i < stdDev.length; i++){
			if(stdDev[i] > Real.EPSILON){
				continue;
			}
			
			if(bias >= 0) {
				throw new IllegalArgumentException("Co-linearity detected between column "
						+ bias + " and column " + i);
			}
			
			bias = i;
		}
		return bias;
	}

	private IterativeOptimizer optimizer;
	private BiFunction<Matrix, double[], VectorFunction> sigmoidFactory;
	private int iterMult;
	
	protected static final long DEFAULT_ITER_MULT = 128L;
}
