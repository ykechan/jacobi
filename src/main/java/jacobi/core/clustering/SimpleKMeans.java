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
import java.util.function.Function;

import jacobi.api.Matrix;
import jacobi.core.stats.RowReduce;

/**
 * Implementation of k-means clustering algorithm.
 * 
 * <p>K-means is an expectation-maximization algorithm. It starts off with k arbitrarily chosen means,
 * cluster data to the closest means, and update the means of the clusters. The process is then repeated
 * until the means converges.</p>
 * 
 * <p>In the context of EM algorithms, k-means is EM algorithm with cluster descriptor being the 
 * centroid/mean, and distance function being euclidean.</p>
 * 
 * @author Y.K. Chan
 */
public class SimpleKMeans extends AbstractEMClustering<double[]> {
	
	/**
	 * Constructor.
	 * @param initFn  Clusters initialization function
	 * @param flop   Number of FLOPs to start parallelizing
	 */
	public SimpleKMeans(Function<Matrix, List<double[]>> initFn, long flop) {
		this(initFn, DEFAULT_MEAN_FUNC, flop);
	}

	/**
	 * Constructor. 
	 * @param initFn  Clusters initialization function
	 * @param meanFn  Mean function
	 * @param flop   Number of FLOPs to start parallelizing
	 */
	public SimpleKMeans(Function<Matrix, List<double[]>> initFn,
			Function<Matrix, double[]> meanFn,
			long flop) {
		super(initFn, flop);
		this.meanFn = meanFn;
	}

	@Override
	protected double[] expects(Matrix matrix) {
		return this.meanFn.apply(matrix);
	}

	@Override
	protected double distanceBetween(double[] cluster, double[] vector) {
		double dist = 0.0;
		for(int i = 0; i < vector.length; i++){
			double dx = cluster[i] - vector[i];
			dist += dx * dx;
		}
		return dist;
	}
	
	private Function<Matrix, double[]> meanFn;
	
	private static final Function<Matrix, double[]> DEFAULT_MEAN_FUNC = new RowReduce.Mean()::compute;
}
