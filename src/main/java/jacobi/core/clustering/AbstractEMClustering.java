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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.IntStream;

import jacobi.api.Matrix;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.ParallelSupplier;

/**
 * Abstract class for implementing clustering using expectation-maximization algorithm.
 * 
 * <p>Child class will be providing the function to obtain the cluster descriptor given
 * the member vectors, and the distance function between a cluster descriptor and a vector.
 * A natural measurement of fitness thus arises by taking the negative of the total distance between 
 * the vectors to the cluster it belongs.</p>
 * 
 * @author Y.K. Chan
 * @param <T>  Cluster descriptor
 */
public abstract class AbstractEMClustering<T> implements Clustering, ToDoubleBiFunction<Matrix, List<int[]>> {
	
	/**
	 * Constructor.
	 * @param initFn  Initialize function
	 * @param flop  Number of FLOPs to start parallelizing
	 */
	public AbstractEMClustering(Function<Matrix, List<T>> initFn, long flop) {
		this.initFn = initFn;
		this.flop = flop;
	}
	
	@Override
	public List<int[]> compute(Matrix matrix) {
		List<T> clusters = this.initFn.apply(matrix);
		if(clusters.isEmpty() || matrix.getRowCount() < 1){
			return Collections.emptyList();
		}
		
		if(clusters.size() < 2){
			return Collections.singletonList(IntStream.range(0, matrix.getRowCount()).toArray());
		}
		
		int[] membership = this.maximization(matrix, clusters);
		
		int max = this.limit(matrix.getRowCount());
		for(int n = 0; n < max; n++){
			clusters = this.expectation(matrix, membership);
			int[] next = this.maximization(matrix, clusters);
			
			int delta = 0;
			for(int i = 0; i < membership.length; i++){
				if(membership[i] != next[i]){
					delta++;
					//break;
				}
			}
			
			if(delta < 1){
				break;
			}
			
			membership = next;
		}
		
		int[] seq = new int[membership.length];
		int[] ends = this.groupBy(membership, seq);
		
		return new AbstractList<int[]>() {

			@Override
			public int[] get(int index) {
				return Arrays.copyOfRange(seq, index == 0 ? 0 : ends[index - 1], ends[index]);
			}

			@Override
			public int size() {
				return ends.length;
			}
			
		};
	}
	
	/**
	 * Compute the expectation step, i.e. find the cluster descriptors given its member vectors
	 * @param matrix  Input matrix
	 * @param membership  Membership for each vectors
	 * @return  List of cluster descriptors
	 */
	protected List<T> expectation(Matrix matrix, int[] membership) {
		int[] seq = new int[membership.length];
		int[] ends = this.groupBy(membership, seq);
		
		return this.expectation(matrix, new AbstractList<int[]>(){

			@Override
			public int[] get(int index) {
				int begin = index == 0 ? 0 : ends[index - 1];
				int end = ends[index];
				return Arrays.copyOfRange(seq, begin, end);
			}

			@Override
			public int size() {
				return ends.length;
			}
			
		});
	}
	
	/**
	 * Compute the expectation step, i.e. find the cluster descriptors given its member vectors
	 * @param matrix  Input matrix
	 * @param seqs Index sequence for each clusters
	 * @return  List of cluster descriptors
	 */
	protected List<T> expectation(Matrix matrix, List<int[]> seqs) {
		List<T> clusters = new ArrayList<>(seqs.size());
		for(int i = 0; i < seqs.size(); i++){
			int[] seq = seqs.get(i);
			clusters.add(this.expects(new ImmutableMatrix(){

				@Override
				public int getRowCount() {
					return seq.length;
				}

				@Override
				public int getColCount() {
					return matrix.getColCount();
				}

				@Override
				public double[] getRow(int index) {
					return matrix.getRow(seq[index]);
				}
				
			}));
		}
		return clusters;
	}
	
	/**
	 * Compute the maximization step, i.e. find the membership for each vectors given the cluster descriptors
	 * @param matrix  Input matrix
	 * @param clusters  Cluster descriptors
	 * @return  Membership for each vectors
	 */
	protected int[] maximization(Matrix matrix, List<T> clusters) {
		int[] membership = new int[matrix.getRowCount()];
		
		long estCost = matrix.getRowCount() * this.estimateCost(matrix, clusters);
		if(estCost > this.flop){
			ParallelSupplier.cyclic(i -> {
				double[] vector = matrix.getRow(i);
				membership[i] = this.select(clusters, vector);
			}, 0, matrix.getRowCount());
			
			return membership;
		}
		
		for(int i = 0; i < matrix.getRowCount(); i++){
			double[] vector = matrix.getRow(i);
			membership[i] = this.select(clusters, vector);
		}
		return membership;
	}
	
	/**
	 * Select which cluster a given vector belongs to
	 * @param clusters  List of cluster descriptors
	 * @param vector  Input vector
	 * @return  Index of the cluster this vector belongs to
	 */
	protected int select(List<T> clusters, double[] vector) {
		int min = 0;
		double minDist = this.distanceBetween(clusters.get(0), vector);
		
		for(int i = 1; i < clusters.size(); i++){
			T cluster = clusters.get(i);
			double dist = this.distanceBetween(cluster, vector);
			
			if(dist < minDist){
				min = i;
				minDist = dist;
			}
		}
		return min;
	}
	
	/**
	 * Group an array of membership into a sequence of indices segmented according to the membership
	 * @param membership  Membership for each vectors
	 * @param seq  Output sequence of indices segmented
	 * @return  End indices of each segment
	 */
	protected int[] groupBy(int[] membership, int[] seq) {
		int[] counts = new int[4];
		int step = counts.length / 2;
		
		int max = 0;
		
		for(int m : membership){
			if(m >= counts.length){
				int target = m + 1;
				int next = counts.length;
				
				while(next < target){
					int temp = next; next += step; step = temp;
				}
				
				counts = Arrays.copyOf(counts, next);
			}
			
			counts[m]++;
			if(m > max){
				max = m;
			}
		}
		
		int[] starts = new int[1 + max];
		for(int i = 1; i < starts.length; i++){
			starts[i] = starts[i - 1] + counts[i - 1];
		}
		
		for(int i = 0; i < membership.length; i++){
			int m = membership[i];
			int j = starts[m]++;
			seq[j] = i;
		}
		return starts;
	}
	
	/**
	 * Find the maximum number of iterations by number of instances
	 * @param numRows  Number of instances
	 * @return  Maximum number of iterations
	 */
	protected int limit(int numRows) {
		int log = 0;
		
		while(numRows > 0){
			numRows /= 2;
			log++;
		}
		
		return 4 * log;
	}
	
	/**
	 * Estimate the cost of a processing a single instance in maximization step.
	 * @return  Cost estimation
	 */
	protected long estimateCost(Matrix matrix, List<T> clusters) {
		return matrix.getColCount() * clusters.size();
	}
	
	@Override
	public double applyAsDouble(Matrix matrix, List<int[]> clusters) {
		double dist = 0.0;
		for(int[] cluster : clusters){
			Matrix subMat = new ImmutableMatrix(){

				@Override
				public int getRowCount() {
					return cluster.length;
				}

				@Override
				public int getColCount() {
					return matrix.getColCount();
				}

				@Override
				public double[] getRow(int index) {
					int s = cluster[index];
					return matrix.getRow(s);
				}
				
			};
			
			T desc = this.expects(subMat);
			for(int i = 0; i < subMat.getRowCount(); i++){
				double[] vector = subMat.getRow(i);
				double vDist = this.distanceBetween(desc, vector);
				dist += vDist;
			}
		}
		return -dist;
	}

	/**
	 * Given a collection of row vectors, find the cluster descriptor
	 * @param matrix  Input matrix as collection of row vectors
	 * @return  Cluster descriptor
	 */
	protected abstract T expects(Matrix matrix);
	
	/**
	 * Compute the distance between a cluster and a given vector
	 * @param cluster  Cluster descriptor
	 * @param vector  Input vector
	 * @return  Distance between a cluster and a given vector
	 */
	protected abstract double distanceBetween(T cluster, double[] vector);
	
	private Function<Matrix, List<T>> initFn;
	private long flop;
}
