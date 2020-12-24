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
import jacobi.core.util.ParallelSupplier;

/**
 * Compute the Silhouette coefficient measurement of the fitness of clustering result.
 * 
 * <p>
 * For each vector v[i] &isin; C<sub>k</sub>, define the following qualities: <br>
 * a[i] = sum of d(v[i], v[j]) for all v[j] in C<sub>k</sub>, i &ne; j <br>
 * b[i] = min sum of d(v[i], v[j]) for all v[j] in C<sub>n</sub>, n &ne; k <br>
 * s[i] = (b - a)/max(a, b), where a=a[i], b=b[i] is the silhouette for each vector. <br>
 * <br>
 * For cluster with a single vector, define such s = 0 <br>
 * </p>
 * 
 * <p>The mean s[i] &forall; v[i] &isin; C<sub>k</sub> measures the tightness of the vectors in the cluster
 * are coupled. The maximum of the mean s[i] is the silhouette coefficient.</p>
 * 
 * <p>To precisely compute the silhouette coefficient thus requires O(n<sup>2</sup>) time, which
 * can be prohibitively slow for even modestly large datasets. This implementation serves as a demonstration
 * of concept and not seriously optimized, while in practice pseudo-silhouette coefficient should be used instead.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class SilhouetteCoeff implements ToDoubleBiFunction<Matrix, List<int[]>> {

	@Override
	public double applyAsDouble(Matrix input, List<int[]> clusters) {
		AtomicDouble sil = new AtomicDouble(0.0);
		
		int[] map = this.toHashTable(input.getRowCount(), clusters);
		
		ParallelSupplier.cyclic(i -> {
			double[] dists = this.clusterDists(input, i, map, new double[clusters.size()]);
			double s = this.silhouette(dists, map[i], clusters);
		
			sil.add(s);
		}, 0, input.getRowCount());
		
		return sil.get() / input.getRowCount();
	}
	
	/**
	 * Compute the silhouette given all distances between a target vector against all others
	 * @param dists  All distances between a target vector against all others
	 * @param cluster  Cluster the target vector belongs
	 * @param clusters  Sequence of indices for each clusters
	 * @return  Silhouette value
	 */
	protected double silhouette(double[] dists, int cluster, List<int[]> clusters) {
		int min = (cluster + 1) % dists.length;
		
		dists[cluster] /= (clusters.get(cluster).length - 1);
		
		for(int i = 0; i < dists.length; i++){
			if(i == cluster){
				continue;
			}
			
			dists[i] /= clusters.get(i).length;
			if(dists[min] > dists[i]){
				min = i;
			}
		}
		
		double a = dists[cluster];
		double b = dists[min];
		
		return (b - a) / Math.max(a, b);
	}
	
	/**
	 * Compute the total distance between a given vector and all other vectors grouped by clusters
	 * @param input  Input row vectors
	 * @param index  Index of the given vector
	 * @param clusterMap  Cluster that each vector belongs to
	 * @param dists  Distances buffer for each cluster
	 * @return   Total distance between a given vector and all other vectors grouped by clusters
	 */
	protected double[] clusterDists(Matrix input, int index, int[] clusterMap, double[] dists) {
		double[] u = input.getRow(index);
		for(int i = 0; i < input.getRowCount(); i++){
			if(i == index){
				continue;
			}
			
			double[] v = input.getRow(i);
			int cluster = clusterMap[i];
			dists[cluster] += this.sqDist(u, v);
		}
		
		return dists;
	}
	
	/**
	 * Build a hash table on sequence of indices for each clusters
	 * @param num  Number of vectors
	 * @param clusters  Sequence of indices for each clusters
	 * @return  Hash table as an array
	 */
	protected int[] toHashTable(int num, List<int[]> clusters) {
		int[] map = new int[num];
		for(int c = 0; c < clusters.size(); c++){
			int[] seq = clusters.get(c);
			for(int s : seq){ 
				map[s] = c;
			}
		}
		return map;
	}

	/**
	 * Compute the squared euclidean distance between two vectors
	 * @param u  First vector
	 * @param v  Second vector
	 * @return  Squared euclidean distance
	 */
	protected double sqDist(double[] u, double[] v) {
		double sum = 0.0;
		for(int i = 0; i < u.length; i++){
			double dx = u[i] - v[i];
			sum += dx * dx;
		}
		return sum;
	}
	
	/**
	 * Thread-safe double container
	 * @author Y.K. Chan
	 *
	 */
	protected static class AtomicDouble {
		
		/**
		 * Constructor
		 * @param value  Initial value
		 */
		public AtomicDouble(double value) {
			this.value = value;
		}

		/**
		 * Add to current value
		 * @param val  Value to be added
		 */
		public synchronized void add(double val) {
			this.value += val;
		}
		
		/**
		 * Get the double value
		 * @return  Double value
		 */
		public double get() {
			return this.value;
		}
		
		private double value;
	}
}
