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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

import jacobi.api.Matrix;
import jacobi.core.impl.ArrayMatrix;
import jacobi.core.util.MapReducer;
import jacobi.core.util.MinHeap;
import jacobi.core.util.Weighted;

/**
 * Implementation of k-means++ seeding strategy.
 * 
 * <p>K-means algorithm is prone to bad results due to initial conditions. To alleviate this, k-means++ select 
 * the initial centroids carefully that aims to select centroids as far to each other as possible.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class KMeansPP implements Function<Matrix, Matrix> {
	
	/**
	 * Constructor
	 * @param randFn  Random function
	 * @param kMax  Number of initial centroids to be selected
	 * @param minRank  The number of vectors to be considered as centroids ordered by distance, 0 would consider all vectors
	 * @param numFlop  Minimum number of FLOP to start parallelizing 
	 */
	public KMeansPP(DoubleSupplier randFn, int kMax, int minRank, long numFlop) {
		this.randFn = randFn;
		this.kMax = kMax;
		this.minRank = minRank;
		this.numFlop = numFlop;
	}
	
	@Override
	public Matrix apply(Matrix t) {
		int numCols = t.getColCount();
		
		double[] centroids = new double[numCols * this.kMax];
		int start = (int) (this.randFn.getAsDouble() * t.getRowCount());
		
		System.arraycopy(t.getRow(start), 0, centroids, 0, t.getColCount());
		
		for(int i = 1; i < this.kMax; i++){
			Weighted<double[]> dists = this.computeDistances(t, centroids, i);
			int next = this.minRank < 1 
				? this.select(dists.item, dists.weight)
				: this.select(dists.item, this.minRank);
			
			System.arraycopy(t.getRow(next), 0, centroids, i * numCols, numCols);
		}
		return ArrayMatrix.wrap(numCols, centroids);
	}
	
	/**
	 * Select the next centroids given the distances with all vectors considered
	 * @param dists  Distances for each vector
	 * @param sum  Sum of all distances
	 * @return  Index of next centroid
	 */
	protected int select(double[] dists, double sum) {
		
		double prob = this.randFn.getAsDouble() * sum;
		
		double q = 0.0;
		double maxDist = 0.0;
		int max = -1;
		for(int i = 0; i < dists.length; i++){
			q += dists[i];
			if(q > prob){
				return i;
			}
			
			if(max < 0 || dists[i] > maxDist){
				max = i;
				maxDist = dists[i];
			}
		}
		
		return max;
	}
	
	/**
	 * Select the next centroid given the distances and maximum number of points with largest distance to consider
	 * @param dists  Distances for each vector
	 * @param rank  Maximum number of points with the largest distance to consider selecting
	 * @return  Index of next centroid
	 */
	protected int select(double[] dists, int rank) {
		MinHeap heap = MinHeap.ofMax(rank);
		for(int i = 0; i < dists.length; i++){
			heap.push(i, dists[i]);
		}
		
		List<Weighted<Integer>> weights = new ArrayList<>(rank);
		double sum = 0.0;
		while(!heap.isEmpty()){
			Weighted<Integer> w = heap.pop();
			weights.add(w);
			sum += w.weight;
		}
		
		Collections.reverse(weights);
		
		double prob = this.randFn.getAsDouble() * sum;
		double q = 0.0;
		for(Weighted<Integer> w : weights){
			q += w.weight;
			if(q > prob){
				return w.item;
			}
		}
		return 0;
	}
	
	/**
	 * Compute the distances of all vectors to its corresponding closest centroids, with the total distance
	 * @param input  Input vector
	 * @param centroids  Centroids
	 * @param num  Number of centroids
	 * @return  Distances of all vectors to its corresponding closest centroids, with the total distance
	 */
	protected Weighted<double[]> computeDistances(Matrix input, double[] centroids, int num) {
		double[] dists = new double[input.getRowCount()];
		
		long flop = (long) dists.length * input.getColCount() * num;
		if(flop > this.numFlop){
			double totalDist = MapReducer.of(0, input.getRowCount())
				.flop(num * input.getColCount())
				.map((begin, end) -> {
					double sum = 0.0;
					for(int i = begin; i < end; i++){
						double[] vector = input.getRow(i);
						dists[i] = this.closestDistance(vector, centroids, num) / dists.length;
						sum += dists[i];
					}
					return sum;
				})
				.reduce((a, b) -> a + b).get();
			
			return new Weighted<>(dists, totalDist);
		}
		
		double sum = 0.0;
		for(int i = 0; i < input.getRowCount(); i++){
			double[] vector = input.getRow(i);
			dists[i] = this.closestDistance(vector, centroids, num) / dists.length;
			sum += dists[i];
		}
		return new Weighted<>(dists, sum);
	}
	
	/**
	 * Compute the distance between a vector to its closest centroid 
	 * @param vector  Input vector
	 * @param centroids  Centroids
	 * @param num  Number of centroids
	 * @return  Distance between a vector to its closest centroid 
	 */
	protected double closestDistance(double[] vector, double[] centroids, int num) {
		double minDist = -1.0;
		for(int i = 0; i < num; i++){
			int begin = i * vector.length;
			double dist = 0.0;
			for(int j = 0; j < vector.length; j++){
				double dx = vector[j] - centroids[begin + j];
				dist += dx * dx;
			}
			
			if(i == 0 || dist < minDist){
				minDist = dist;
			}
		}
		return minDist;
	}
	
	private DoubleSupplier randFn;
	private int kMax, minRank;
	private long numFlop;
}
