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

import java.util.List;
import java.util.function.ToDoubleBiFunction;

import jacobi.api.Matrix;
import jacobi.api.unsupervised.Segregation;

/**
 * Measurement of cluster fitness by the average sum of distances between all vectors to its clusters.
 * 
 * @author Y.K. Chan
 *
 */
public class WithinClusterDistance implements ToDoubleBiFunction<Matrix, List<int[]>> {
	
	/**
	 * Factory method
	 * @param metric  Cluster metric
	 * @return  New instance of this class
	 */
	@SuppressWarnings("unchecked")
	public static <T> WithinClusterDistance of(ClusterMetric<T> metric) {
		return new WithinClusterDistance((ClusterMetric<Object>) metric, Segregation.getInstance());
	}
	
	/**
	 * Constructor
	 * @param metric  Cluster metric function
	 */
	public WithinClusterDistance(ClusterMetric<Object> metric, Segregation seg) {
		this.metric = metric;
	}

	@Override
	public double applyAsDouble(Matrix t, List<int[]> u) {
		return this.seg.compute(t, u).stream()
			.sequential()
			.mapToDouble(this::allDistances).sum() / u.size();
	}
	
	/**
	 * Compute the mean distance from all vectors in a cluster to the cluster itself
	 * @param cluster  Cluster members
	 * @return  Mean distance
	 */
	protected double allDistances(Matrix cluster) {
		Object clusterDesc = this.metric.expects(cluster);
		
		double dist = 0.0;
		for(int i = 0; i < cluster.getRowCount(); i++){
			double[] v = cluster.getRow(i);
			dist += this.metric.distanceBetween(clusterDesc, v);
		}
		return dist / cluster.getRowCount();
	}

	private ClusterMetric<Object> metric;
	private Segregation seg;
}
