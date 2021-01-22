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
import java.util.function.ToDoubleBiFunction;

import jacobi.api.Matrix;
import jacobi.core.util.MapReducer;

/**
 * Pseudo-silhouette coefficient for measuring fitness of clusters.
 * 
 * <p>The silhouette coefficient is a good measure on clustering results. Unlike the within-clusters sum of squared distances,
 * it penalize having more than enough clusters, thus removing the ambiguity of deciding the trade-off of lower measurement
 * or simplier cluster model. However to compute the coefficient precisely all pairwise distances needed to be taken into 
 * account, which can be prohibitively expensive.</p>
 * 
 * <p>The pseudo-silhouette coefficient provides an approximation to the actual silhouette coefficient value. Basically it
 * replaces the mean distance to every members of the cluster by the distance to the centroid of the cluster. Thus the 
 * pseudo-silhouette of a vector amounts to the ratio of the difference of distance between its local cluster and the
 * nearest foreign cluster.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class PseudoSilhouetteCoeff implements ToDoubleBiFunction<Matrix, List<int[]>> {

	/**
	 * Constructor.
	 * @param meanFn  Statistics mean function
	 * @param flop   Number of FLOPs to start parallelizing
	 */
	public PseudoSilhouetteCoeff(ClusterMetric<double[]> em, long flop) {
		this.metric = em;
		this.flop = flop;
	}

	@Override
	public double applyAsDouble(Matrix matrix, List<int[]> clusters) {
		long estCost = (long) matrix.getRowCount() * matrix.getColCount() * clusters.size();
		
		List<double[]> centroids = this.metric.expects(matrix, clusters);
		
		if(estCost < this.flop){
			double sil = this.silhouette(centroids, matrix, 0, matrix.getRowCount());
			return sil / matrix.getRowCount();
		}
		
		return MapReducer.of(0, matrix.getRowCount())
			.flop(clusters.size() * matrix.getColCount())
			.map((begin, end) -> this.silhouette(centroids, matrix, begin, end) / matrix.getRowCount() )
			.reduce((a, b) -> a + b)
			.get();
	}
	
	/**
	 * Compute the sum of the pseudo-silhouette in a range of interest
	 * @param centroids  Cluster centroids
	 * @param matrix   Input matrix
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @return
	 */
	protected double silhouette(List<double[]> centroids, Matrix matrix, int begin, int end) {
		double sum = 0.0;
		for(int i = begin; i < end; i++){
			double[] vector = matrix.getRow(i);
			double sil = this.silhouette(centroids, vector);
			sum += sil;
		}
		return sum;
	}
	
	/**
	 * Compute the pseudo-silhouette of a given vector
	 * @param centroids  Cluster centroids
	 * @param vector  Input vector
	 * @return  Pseudo-silhouette value
	 */
	protected double silhouette(List<double[]> centroids, double[] vector) {
		if(centroids.size() < 2){
			throw new IllegalArgumentException("Unable to measure with only " + centroids.size() + " clusters.");
		}
		
		double iDist = this.metric.distanceBetween(centroids.get(0), vector);
		double jDist = this.metric.distanceBetween(centroids.get(1), vector);
		
		double min0 = Math.min(iDist, jDist);
		double min1 = Math.max(iDist, jDist);
		
		for(int i = 2; i < centroids.size(); i++){
			double[] centroid = centroids.get(i);
			double dist = this.metric.distanceBetween(centroid, vector);

			if(dist < min0){
				min1 = min0;
				min0 = dist;
				continue;
			}
			
			if(dist < min1){
				min1 = dist;
			}
		}
		
		return (min1 - min0) / min1;
	}
	
	private long flop;
	private ClusterMetric<double[]> metric;
}
