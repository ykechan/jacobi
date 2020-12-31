/*
 * The MIT License
 *
 * Copyright 2020 Y.K. Chan
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
package jacobi.core.clustering;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.stats.RowReduce;
import jacobi.core.stats.Variance;

/**
 * Implementation of the Gaussian Mixture Model with only diagonal elements in the co-variance matrix considered.
 * 
 * <p>As the number of dimension increases, the covariance matrix becomes too large to be inferred by the data
 * in any meaningful way. A compromise is to assume the dimensions are independent of each other, thus reducing
 * the covariance matrix into a diagonal matrix.</p>
 * 
 * <p>In this model, the probability of x belongs to a N(u, &Sigma;) is given by the following<br>
 * p(x) = 1/&prod;&sigma;<sub>i</sub>&Sqrt;2&pi;exp( -&sum;[(x<sub>i</sub> - u<sub>i</sub>)/&sigma;<sub>i</sub>]<sup>2</sup> / 2 )<br>
 * Thus the distance function can be defined as<br>
 * d(x) = ln 2&pi; p<sup>-2</sup>(x) = &sum;{ [(x<sub>i</sub> - u<sub>i</sub>)/&sigma;<sub>i</sub>]<sup>2</sup> + ln(&sigma;<sub>i</sub>)}
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class DiagGaussMixModel extends AbstractEMClustering<Matrix> {
	
	/**
	 * Constructor.
	 * @param initFn  Initial function giving the distribution
	 * @param flop  Number of FLOPs to start parallelizing
	 */
	public DiagGaussMixModel(Function<Matrix, List<Matrix>> initFn, long flop) {
		this(initFn, new RowReduce.Mean()::compute, new Variance()::compute, flop);
	}

	/**
	 * Constructor
	 * @param initFn  Initial function giving the distribution
	 * @param meanFn  Mean function
	 * @param varFn  Variance function
	 * @param flop  Number of FLOPs to start parallelizing
	 */
	public DiagGaussMixModel(Function<Matrix, List<Matrix>> initFn,
			Function<Matrix, double[]> meanFn,
			BiFunction<Matrix, double[], double[]> varFn,
			long flop) {
		super(initFn, flop);
		this.meanFn = meanFn;
		this.varFn = varFn;
	}

	@Override
	protected Matrix expects(Matrix matrix) {
		double[] mean = this.meanFn.apply(matrix);
		double[] var = this.varFn.apply(matrix, mean);
		return Matrices.wrap(mean, var);
	}

	@Override
	protected double distanceBetween(Matrix cluster, double[] vector) {
		double[] mean = cluster.getRow(0);
		double[] var = cluster.getRow(1);
		
		double dist = 0.0;
		for(int i = 0; i < vector.length; i++){
			double dx = vector[i] - mean[i];
			dist += (dx * dx) / var[i] + Math.log(var[i]);
		}
		return dist;
	}
	
	private Function<Matrix, double[]> meanFn;
	private BiFunction<Matrix, double[], double[]> varFn;
}
