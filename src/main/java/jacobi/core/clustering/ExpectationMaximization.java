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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.unsupervised.Segregation;
import jacobi.core.util.ParallelSupplier;

/**
 * Clustering using Expectation-Maximization algorithm.
 * 
 * <p>
 * The expectation-maximization algorithm consists of two steps: expectation and maximization. Given a estimation
 * of parameters, the expectation step creates the expected value of the underlying model using these parameters, 
 * and then the maximization step estimates new values of the parameters based on the expected model. The steps
 * are repeated until all parameters converge.  
 * </p>
 * 
 * <p>In the context of clustering, expectation is obtaining the cluster models given the memberships of
 * each vectors as parameter, and maximization is assign the vector to the updated cluster models.</p>
 * 
 * @author Y.K. Chan
 * @param T  Type of cluster descriptor
 *
 */
public class ExpectationMaximization<T> implements Clustering {
	
	/**
	 * Constructor.
	 * @param initFunc  Initialize function
	 * @param metric  Cluster metric
	 * @param seg  Matrix segregation
	 * @param flop  Number of FLOPs to start parallelizing
	 */
	public ExpectationMaximization(Function<Matrix, List<T>> initFunc, ClusterMetric<T> metric, long flop) {
		this.initFunc = initFunc;
		this.metric = metric;
		this.flop = flop;
	}
	
	@Override
	public List<int[]> compute(Matrix matrix) {
		List<T> clusters = this.initFunc.apply(matrix);
		Array memberships = this.maximization(matrix, clusters);

		int max = matrix.getRowCount();
		for(int k = 0; k < max; k++){
			clusters = this.expectation(matrix, memberships);
			Array next = this.maximization(matrix, clusters);
			
			if(Arrays.equals(memberships.cuts, next.cuts)
			&& Arrays.equals(memberships.elements, next.elements)){
				// converged
				break;
			}
			
			memberships = next;
		}
		
		Array segments = this.sort(memberships.elements, memberships.cuts);		
		return new AbstractList<int[]>(){

			@Override
			public int[] get(int index) {
				int begin = index == 0 ? 0 : this.ends[index - 1];
				int end = this.ends[index];
				return Arrays.copyOfRange(this.seqs, begin, end);
			}

			@Override
			public int size() {
				return ends.length;
			}
			
			private int[] seqs = segments.elements;
			private int[] ends = segments.cuts;
		};
	}
	
	/**
	 * Expectation step in EM algorithm, i.e. find the cluster descriptors given the membership for
	 * each clusters
	 * @return  List of cluster descriptors
	 */
	protected List<T> expectation(Matrix matrix, Array memberships) {
		Array segments = this.sort(memberships.elements, memberships.cuts);
		
		int[] seq = segments.elements;
		int[] ends = segments.cuts;
		
		Segregation seg = Segregation.getInstance();
		List<Matrix> clusters = new AbstractList<Matrix>() {

			@Override
			public Matrix get(int index) {
				int begin = index == 0 ? 0 : ends[index - 1];
				return seg.toMatrix(matrix, seq, begin, ends[index]);
			}

			@Override
			public int size() {
				return ends.length;
			}
			
		};
		return clusters.stream().map(this.metric::expects).collect(Collectors.toList());
	}
	
	/**
	 * Maximization step in EM algorithm, i.e. find the memberships of each vectors
	 * @param matrix  Input vectors 
	 * @param clusters  List of cluster descriptors
	 * @return  Memberships for each vector by index and cluster sizes
	 */
	protected Array maximization(Matrix matrix, List<T> clusters) {
		int[] memberships = new int[matrix.getRowCount()];
		int[] counts = new int[clusters.size()];
		
		long estCost = (long) clusters.size() * matrix.getRowCount() * matrix.getColCount();
		if(estCost > this.flop){
			ParallelSupplier.cyclic(i -> {
				int c = this.select(clusters, matrix.getRow(i));
				memberships[i] = c;
			}, 0, matrix.getRowCount());
			
			Arrays.fill(counts, 0);
			for(int m : memberships){
				counts[m]++;
			}
		}else{
		
			for(int i = 0; i < matrix.getRowCount(); i++){
				int c = this.select(clusters, matrix.getRow(i));
				memberships[i] = c;
				counts[c]++;
			}
		}
		
		return new Array(memberships, counts);
	}
	
	/**
	 * Select the cluster with the least distance to a certain vector
	 * @param clusters  List of cluster descriptors
	 * @param vector  Input vector
	 * @return  Index of the closest cluster
	 */
	protected int select(List<T> clusters, double[] vector) {
		int min = 0;
		double minDist = this.metric.distanceBetween(clusters.get(0), vector);
		
		for(int i = 1; i < clusters.size(); i++){
			double dist = this.metric.distanceBetween(clusters.get(i), vector);
			if(dist < minDist){
				min = i;
				minDist = dist;
			}
		}
		return min;
	}
	
	/**
	 * Sort the index sequence by cluster index the indexed vector belongs to
	 * @param membership  Vector membership
	 * @param counts  Size of each cluster
	 * @return  Index sequences sorted by cluster index 
	 * 		and end index of each cluster segments
	 */
	protected Array sort(int[] membership, int[] counts) {
		int[] seqs = new int[membership.length];
		int[] starts = new int[counts.length];
		for(int i = 1; i < starts.length; i++){
			starts[i] = starts[i - 1] + counts[i - 1];
		}
		
		for(int i = 0; i < seqs.length; i++){
			int m = membership[i];
			int j = starts[m]++;
			
			seqs[j] = i;
		}
		return new Array(seqs, starts);
	}

	private Function<Matrix, List<T>> initFunc;
	private ClusterMetric<T> metric;
	private long flop;

	/**
	 * Data class for an segmented array
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Array {
		
		/**
		 * Array elements
		 */
		public final int[] elements;
		
		/**
		 * Index of cuts in segmentation
		 */
		public final int[] cuts;

		/**
		 * Constructor.
		 * @param elements  Array elements
		 * @param cuts  Index of cuts in segmentation
		 */
		public Array(int[] elements, int[] cuts) {
			this.elements = elements;
			this.cuts = cuts;
		}
		
	}
}
