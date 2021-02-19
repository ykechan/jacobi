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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import jacobi.api.Matrix;
import jacobi.api.ext.Spatial;
import jacobi.api.spatial.SpatialIndex;
import jacobi.core.util.IntStack;
import jacobi.core.util.MinHeap;
import jacobi.core.util.Real;

/**
 * Implementation of Mean-Shift clustering.
 * 
 * <p>
 * Given a set of vectors {y<sub>i</sub>}, let K(x) be the density estimation
 * function at position x.<br>
 * Typically K(x) = &sum;k(||x - y<sub>i</sub>||<sup>2</sup>), i.e. the density
 * depends on the distance between x and the data vectors y<sub>i</sub>.
 * Optimizing K means finding the local density center, which is what the
 * mean-shift algorithm looking after.
 * </p>
 * 
 * <p>
 * By Gradient descent, to optimize K, x[n + 1] = x[n] - &gamma;&nabla;K for
 * some learning rate &gamma;. &nabla;K(x) = &sum; 2(x - y<sub>i</sub>) k'(||x -
 * y<sub>i</sub>||<sup>2</sup>). By fixing &sum;k' = 1 and &gamma; = 1/2, it
 * gives x[n + 1] = &sum;y<sub>i</sub>k'(||x - y<sub>i</sub>||<sup>2</sup>),
 * which is conceptually an iterative re-weighted mean tracking algorithm.
 * </p>
 * 
 * <p>
 * Each vector in the data set is then traced to a stationary point, and vectors
 * with the same destination is considered to be within a same cluster.
 * </p>
 * 
 * <p>
 * In practical implementation, instead of covering the whole search space, a
 * query distance is given such that vectors further than this distance is
 * ignored. Furthermore, a epsilon distance is also provided such that vectors
 * within the epsilon-distance of the tracing path is considered to be having
 * the same destination.
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class MeanShift implements Clustering {
	
	/**
	 * Default value for minimum points as cluster
	 */
	public static final int DEFAULT_MIN_PTS = 3;
	
	/**
	 * Constructor.
	 * @param window  Parzen window function
	 * @param radius  Window radius
	 * @param epsilon  Tolerance radius
	 * @param minPts  Minimum points as cluster
	 */
	public MeanShift(ParzenWindow window, double radius, double epsilon) {
		this(window, radius, epsilon, DEFAULT_MIN_PTS);
	}
	
	/**
	 * Constructor.
	 * @param window  Parzen window function
	 * @param radius  Window radius
	 * @param epsilon  Tolerance radius
	 * @param minPts  Minimum points as cluster
	 */
	public MeanShift(ParzenWindow window, double radius, double epsilon, int minPts) {
		this.window = window;
		this.radius = radius;
		this.epsilon = epsilon;
		this.minPts = minPts;
	}

	@Override
	public List<int[]> compute(Matrix matrix) {
		SpatialIndex<Tuple> sIndex = this.createIndex(matrix);
		
		Context context = this.initContext(matrix);
		Arrays.fill(context.memberships, -10);
		
		for(int i = 0; i < matrix.getRowCount(); i++){
			if(context.memberships[i] >= 0){
				continue;
			}
			
			Tuple start = new Tuple(i, matrix.getRow(i));
			Tuple dest = this.trace(sIndex, context, start);
			
			if(dest.index == i){
				// the vector has its own cluster
				context.destinations.add(dest);
			}
			
			context.memberships[i] = dest.index;
		}
		
		return this.collapse(context.memberships);
	}
	
	/**
	 * Trace the path of mean-shifting from a starting vector
	 * @param sIndex  Spatial index
	 * @param context  Clustering context
	 * @param start  Starting index and vector
	 * @return  Destination cluster, or -1 if classified as an outlier
	 */
	protected Tuple trace(SpatialIndex<Tuple> sIndex, Context context, Tuple start) {
		int[] memberships = context.memberships;
		Neighbourhood current = new Neighbourhood(start.index, new int[0], start.vector);
		int max = memberships.length;
		
		if(memberships[start.index] >= 0){
			return new Tuple(memberships[start.index], new double[0]);
		}
		
		ClusterMetric<double[]> metric = EuclideanCluster.getInstance();
		// by default is its own cluster
		memberships[start.index] = start.index;
		
		for(int k = 0; k < max; k++){
			Neighbourhood next = this.shift(sIndex, current.mean, context, start.index);
			
			if(this.converged(context, current, next)){
				current = next;
				break;
			}
			
			int hash = this.hashCode(next, next.elements.length);
			Tuple hit = context.cache.get(hash);
			if(hit == null){
				context.cache.put(hash, new Tuple(start.index, next.mean));
			}else if(metric.distanceBetween(hit.vector, next.mean) < context.eps){
				memberships[start.index] = hit.index;
				break;
			}
			
			current = next;
		}
		
		int dest = memberships[start.index];
		return new Tuple(dest, current.mean);
	}
	
	/**
	 * Check if the tracing is converged to a stationary position
	 * @param context  Clustering context
	 * @param current  Current neighbourhood
	 * @param next  Shifted neighbourhood
	 * @return  True if the shifted distance is smaller than epsilon, or it is traced to a destination
	 *     previously found.
	 */
	protected boolean converged(Context context, Neighbourhood current, Neighbourhood next) {
		if(next.elements.length < 1){
			return true;
		}
		
		ClusterMetric<double[]> metric = EuclideanCluster.getInstance();
		
		for(Tuple t : context.destinations){
			if(metric.distanceBetween(next.mean, t.vector) < context.eps){
				context.memberships[current.index] = t.index;
				return true;
			}
		}
		
		if(!Arrays.equals(current.elements, next.elements)){
			return false;
		}
		
		return metric.distanceBetween(next.mean, current.mean) < context.eps;
	}
	
	/**
	 * Find the shifted mean given the current mean vector 
	 * @param sIndex  Spatial index
	 * @param mean  Current mean vector
	 * @param memberships  Memberships of each vectors
	 * @param start  Starting position
	 * @return  Shifted mean, or empty array if no vector found within query window.
	 */
	protected Neighbourhood shift(SpatialIndex<Tuple> sIndex, double[] mean, Context context, int start) {
		int[] memberships = context.memberships;
		
		List<Tuple> neighbours = new ArrayList<>();
		Iterator<Tuple> iter = sIndex.queryRange(mean, this.radius);
		
		ClusterMetric<double[]> metric = EuclideanCluster.getInstance();
		
		while(iter.hasNext()){
			Tuple tuple = iter.next();
			neighbours.add(tuple);
			
			double dist = metric.distanceBetween(mean, tuple.vector);
			if(tuple.index == start || dist > context.eps){
				continue;
			}

			int cluster = memberships[tuple.index];
			if(cluster < 0){
				memberships[tuple.index] = start;
			}else if(cluster != start){
				memberships[start] = cluster;
				return EMPTY;
			}
		}
		
		if(neighbours.size() < this.minPts){
			memberships[start] = -1;
			return EMPTY;
		}
		
		double[] weights = this.window.apply(mean, 
			neighbours.stream().map(t -> t.vector).collect(Collectors.toList()));
		
		return this.weightedMean(neighbours, weights, start);
	}
	
	/**
	 * Compute the weighted mean of a list of vectors
	 * @param tuples  List of tuple with vectors
	 * @param weights  Weights of each vector
	 * @return  Weighted mean of the vectors
	 */
	protected Neighbourhood weightedMean(List<Tuple> tuples, double[] weights, int cluster) {
		if(tuples.isEmpty()){
			return EMPTY;
		}
		
		double norm = 0.0;
		double[] next = new double[tuples.get(0).vector.length];
		
		MinHeap heap = MinHeap.ofMax(weights.length);
		for(int i = 0; i < weights.length; i++){
			Tuple tuple = tuples.get(i);
			double w = weights[i];
			double[] v = tuple.vector;
			
			for(int j = 0; j < next.length; j++){
				next[j] += w * v[j];
			}
			
			norm += w;
			heap.push(tuple.index, -w);
		} 
		
		int[] elements = new int[weights.length];
		int k = elements.length;
		while(!heap.isEmpty()){
			elements[--k] = heap.pop().item;
		}
		
		if(Real.isNegl(norm - 1.0)){
			return new Neighbourhood(cluster, elements, next);
		}
		
		for(int i = 0; i < next.length; i++){
			next[i] /= norm;
		}
		return new Neighbourhood(cluster, elements, next);
	}
	
	/**
	 * Build a spatial index on the given vectors
	 * @param matrix  Collection of row vectors
	 * @return  Spatial index
	 */
	protected SpatialIndex<Tuple> createIndex(Matrix matrix) {
		return matrix.ext(Spatial.class).build().map(i -> new Tuple(i, matrix.getRow(i)));
	}
	
	/**
	 * Merge chain of clusters that leads to the same destination
	 * @param memberships  Membership of each vectors
	 * @return  Index sequences of each cluster
	 */
	protected List<int[]> collapse(int[] memberships) {
		IntStack[] buckets = new IntStack[memberships.length];
		for(int i = 0; i < memberships.length; i++){
			if(memberships[i] == i){
				buckets[i] = IntStack.newInstance();
				buckets[i].push(i);
			}
		}
		
		for(int i = 0; i < memberships.length; i++){
			if(memberships[i] == i || memberships[i] < 0){
				continue;
			}
			
			int k = i;
			while(k != memberships[k]){
				if(memberships[k] < 0){
					k = -1;
					break;
				}
				
				k = memberships[k];
			}
			
			if(k < 0){
				continue;
			}
			
			memberships[i] = k;
			buckets[k].push(i);
		}
		return Arrays.stream(buckets)
			.filter(b -> b != null && b.size() >= this.minPts)
			.map(b -> b.toArray()).collect(Collectors.toList());
	}
	
	/**
	 * Hash function for a neighbourhood on its members
	 * @param neighbourhood  Neighbourhood
	 * @param len  Number of members to include in the hash value
	 * @return  Hash value based on index of the neighbourhood members. If all members are included,
	 *    the indices are sorted s.t. the hash value is based on inclusion not ordering.
	 */
	protected int hashCode(Neighbourhood neighbourhood, int len) {
		int[] elem = neighbourhood.elements;
		int num = Math.min(elem.length, len);
		
		if(num == elem.length){
			elem = Arrays.copyOf(elem, elem.length);
			Arrays.sort(elem);
		}
		
		Checksum checksum = new CRC32();
		for(int i = 0; i < num; i++){
			int k = neighbourhood.elements[i];
			for(int j = 0; j < 4; j++){
				checksum.update((k >> j * 8) & 0xff);
			}
		}
		return (int) checksum.getValue();
	}
	
	/**
	 * 
	 * @return
	 */
	protected Context initContext(Matrix matrix) {
		int[] membership = new int[matrix.getRowCount()];
		Arrays.fill(membership, -1);
		
		double eps = this.epsilon * this.epsilon;
		return new Context(membership, new ArrayList<>(), new HashMap<>(), eps);
	}

	private ParzenWindow window;
	private double radius;
	private double epsilon;
	private int minPts;

	/**
	 * Data object for a tuple of row index and row vector.
	 * 
	 * @author Y.K. Chan
	 */
	protected static class Tuple {

		/**
		 * Row index
		 */
		public final int index;

		/**
		 * Row vector
		 */
		public final double[] vector;
		

		/**
		 * Constructor.
		 * @param index  Row index
		 * @param vector  Row vector
		 */
		public Tuple(int index, double[] vector) {
			this.index = index;
			this.vector = vector;
		}

	}
	
	/**
	 * Data object for a neighbourhood of a vector
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Neighbourhood {
		
		/**
		 * Index of the starting vector
		 */
		public int index;
		
		/**
		 * Index of the vectors in the neighbourhood
		 */
		public int[] elements;
		
		/**
		 * Mean vector of the neighbourhood
		 */
		public double[] mean;

		/**
		 * Constructor.
		 * @param index   Index of the starting vector
		 * @param elements  Index of the vectors in the neighbourhood
		 * @param mean  Mean vector of the neighbourhood
		 */
		public Neighbourhood(int index, int[] elements, double[] mean) {
			this.index = index;
			this.elements = elements;
			this.mean = mean;
		}
		
	}
	
	/**
	 * Data class for clustering context
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Context {

		/**
		 * Membership of each vectors
		 */
		public final int[] memberships;
		
		/**
		 * Destinations of found clusters
		 */
		public final List<Tuple> destinations;
		
		/**
		 * Cache of intermediate steps
		 */
		public final Map<Integer, Tuple> cache;
		
		/**
		 * Epsilon for collapsing
		 */
		public final double eps;

		/**
		 * Constructor.
		 * @param memberships  Array of memberships
		 * @param destinations  Destinations of found clusters
		 * @param cache  Cache of intermediate steps
		 * @param eps  Epsilon distance for collapsing
		 */
		public Context(int[] memberships, List<Tuple> destinations, Map<Integer, Tuple> cache, double eps) {
			this.memberships = memberships;
			this.destinations = destinations;
			this.cache = cache;
			this.eps = eps;
		}
	}
	
	protected static final Neighbourhood EMPTY = new Neighbourhood(-1, new int[0], new double[0]);
	
}
