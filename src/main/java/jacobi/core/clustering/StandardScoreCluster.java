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
package jacobi.core.clustering;

import java.util.function.BiFunction;
import java.util.function.Function;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.stats.RowReduce;
import jacobi.core.stats.Variance;

/**
 * Implementation of the metric cluster by using the standard score as the distance function.
 * 
 * <p>
 * Given the mean &mu; and variance &sigma;<sup>2</sup>, the standard score is defined as 
 * z = (x - &mu;)/&sigma;.<br>
 * 
 * This implementation uses the value ||z||<sup>2</sup> as the metric distance.
 * </p>
 * 
 * <p>
 * This can be considered a Gaussian distribution with diagonal co-variance matrix, i.e. 
 * all dimensions are independent of each other.  
 * </p>
 * 
 * <p>The cluster descriptor is represented as a matrix [&mu;<sup>T</sup>; &sigma;2<sup>T</sup>]</p>
 * 
 * @author Y.K. Chan
 *
 */
public class StandardScoreCluster implements ClusterMetric<Matrix> {
	
	/**
	 * Get the default singleton instance
	 * @return  Default instance
	 */
	public static StandardScoreCluster getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Constructor.
	 * @param meanFunc  Mean function
	 * @param varFunc  Variance function
	 */
	public StandardScoreCluster(Function<Matrix, double[]> meanFunc, 
			BiFunction<Matrix, double[], double[]> varFunc) {
		this.meanFunc = meanFunc;
		this.varFunc = varFunc;
	}

	@Override
	public Matrix expects(Matrix matrix) {
		double[] u = this.meanFunc.apply(matrix);
		double[] sig = this.varFunc.apply(matrix, u);
		return Matrices.wrap(u, sig);
	}

	@Override
	public double distanceBetween(Matrix cluster, double[] vector) {
		double[] u = cluster.getRow(0);
		double[] sig = cluster.getRow(1);
		
		double dist = 0.0;
		for(int i = 0; i < u.length; i++){
			double dx = u[i] - vector[i];
			dist += dx * dx / sig[i];
		}
		return dist;
	}

	private Function<Matrix, double[]> meanFunc;
	private BiFunction<Matrix, double[], double[]> varFunc;
	
	private static final StandardScoreCluster INSTANCE = new StandardScoreCluster(
		new RowReduce.Mean()::compute,
		new Variance()::compute
	); 
}
