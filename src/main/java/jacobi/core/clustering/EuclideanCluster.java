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

import java.util.function.Function;

import jacobi.api.Matrix;
import jacobi.core.stats.RowReduce;

/**
 * Implementation of metric cluster using the mean as cluster descriptor 
 * and squared Euclidean distance as metric function.
 * 
 * <p>This model is used in K-means clustering.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class EuclideanCluster implements ClusterMetric<double[]> {
	
	/**
	 * Get the default instance
	 * @return  Default instance
	 */
	public static EuclideanCluster getInstance() {
		return DEFAULT_INSTANCE;
	}
	
	/**
	 * Constructor
	 * @param meanFunc  Mean function
	 */
	public EuclideanCluster(Function<Matrix, double[]> meanFunc) {
		this.meanFunc = meanFunc;
	}

	@Override
	public double[] expects(Matrix matrix) {
		return this.meanFunc.apply(matrix);
	}

	@Override
	public double distanceBetween(double[] cluster, double[] vector) {
		double dist = 0.0;
		for(int i = 0; i < vector.length; i++){
			double dx = cluster[i] - vector[i];
			dist += dx * dx;
		}
		return dist;
	}

	private Function<Matrix, double[]> meanFunc;
	
	/**
	 * Default instance
	 */
	protected static final EuclideanCluster DEFAULT_INSTANCE = new EuclideanCluster(new RowReduce.Mean()::compute);
}
