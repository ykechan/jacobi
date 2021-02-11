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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;

import jacobi.api.Matrix;
import jacobi.core.util.MapReducer;
import jacobi.core.util.MinHeap;
import jacobi.core.util.Throw;

/**
 * Implementation of k-means++ seeding strategy.
 * 
 * <p>K-means algorithm is prone to bad results due to initial conditions. To alleviate this, k-means++ select 
 * the initial centroids carefully that aims to select centroids as far to each other as possible.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class KMeansPP implements Function<Matrix, List<double[]>> {
	
	/**
	 * Constructor.
	 * @param metric  Cluster metric
	 * @param kRand  Random function
	 * @param kMax  Number of centroids
	 * @param flop  Number of FLOPs to start parallelizing
	 */
	public KMeansPP(ClusterMetric<double[]> metric, IntUnaryOperator kRand, int kMax, long flop) {
		this.metric = metric;
		this.kRand = kRand;
		this.kMax = kMax;
		this.flop = flop;
	}
	
	@Override
	public List<double[]> apply(Matrix t) {
		Throw.when()
			.isNull(() -> t, () -> "No input vectors")
			.isTrue(
				() -> this.kMax > t.getRowCount(), 
				() -> "Too few vectors to select. Expected at least " + this.kMax
			);
		
		if(t.getRowCount() == this.kMax){
			return new AbstractList<double[]>(){

				@Override
				public double[] get(int index) {
					return t.getRow(index);
				}

				@Override
				public int size() {
					return t.getRowCount();
				}
				
			};
		}
		
		int start = this.kRand.applyAsInt(t.getRowCount());
		
		double[][] centroids = new double[this.kMax][];
		centroids[0] = t.getRow(start);
		
		for(int i = 1; i < centroids.length; i++){
			int next = this.select(t, Arrays.asList(centroids).subList(0, i));
			centroids[i] = t.getRow(next);
		}
		return Arrays.asList(centroids);
	}
	
	/**
	 * Select the k-th distant vector to a group of centroids, where k is given by the k-rand function. 
	 * @param matrix  Input matrix
	 * @param centroids   Group of centroids
	 * @return  Index of the k-th distant vector
	 */
	protected int select(Matrix matrix, List<double[]> centroids) {
		int maxRank = this.kRand.applyAsInt(0);
		
		int numFlop = centroids.size() * matrix.getColCount() * this.log2(maxRank);
		long estCost = (long) matrix.getRowCount() * numFlop;
		if(estCost > this.flop){
			MinHeap heap = MapReducer.of(0, matrix.getRowCount()).flop(numFlop).map((begin, end) -> {
				MinHeap h = MinHeap.ofMax(1 + maxRank);
				
				for(int i = begin; i < end; i++){
					double[] vector = matrix.getRow(i);
					double dist = this.minDistanceBetween(centroids, vector);
					if(dist > 0.0){
						h.push(i, dist);
					}
				}
				
				return h;
			}).reduce((a, b) -> { 
				while(!b.isEmpty()){
					a.push(b.pop());
				} 
				return a; 
			}).get();
			
			return heap.peek().item;
		}
		
		MinHeap heap = MinHeap.ofMax(1 + maxRank);
		
		for(int i = 0; i < matrix.getRowCount(); i++){
			double[] vector = matrix.getRow(i);
			double dist = this.minDistanceBetween(centroids, vector);
			if(dist > 0.0){
				heap.push(i, dist);
			}
		}
		
		return heap.peek().item;
	}
	
	
	
	/**
	 * Find the minimum distance between a vector and a list of centroids. 
	 * @param centroids  Input centroids
	 * @param vector  Input vector
	 * @return  Minimum distance between a vector and a list of centroids. 
	 */
	protected double minDistanceBetween(List<double[]> centroids, double[] vector) {
		double minDist = this.metric.distanceBetween(centroids.get(0), vector);
		
		for(int i = 1; i < centroids.size(); i++){
			double dist = this.metric.distanceBetween(centroids.get(i), vector);
			if(dist < minDist){
				minDist = dist;
			}
		}
		
		return minDist;
	}
	
	/**
	 * Approximate the base-2 log
	 * @param num  Input value
	 * @return  Approx. log of the input value base-2
	 */
	protected int log2(int num) {
		int lg = 0;
		while(num > 0){
			num /= 2;
			lg++;
		}
		return Math.max(lg, 1);
	}
	
	private ClusterMetric<double[]> metric;
	private IntUnaryOperator kRand;
	private int kMax;
	private long flop;
}
