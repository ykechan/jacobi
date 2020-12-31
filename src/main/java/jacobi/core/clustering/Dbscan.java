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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import jacobi.api.Matrix;
import jacobi.api.ext.Spatial;
import jacobi.api.spatial.SpatialIndex;
import jacobi.core.util.IntStack;
import jacobi.core.util.Real;
import jacobi.core.util.Throw;

/**
 * Implementation of Density-based spatial clustering of applications with noise.
 * 
 * @author Y.K. Chan
 *
 */
public class Dbscan implements Clustering {
	
	/**
	 * Proxy class for method handle
	 * @author Y.K. Chan
	 *
	 */
	public static class Proxy {
		
		/**
		 * Obtain clusters of a matrix as a collection of row vectors by DBSCAN
		 * @param matrix  Input matrix
		 * @param minPts  Minimum number of points a cluster should contain
		 * @param epsilon  Reach distance of neighbourhood
		 * @return  Sequence of indices for each clusters
		 */
		public List<int[]> compute(Matrix matrix, int minPts, double epsilon) {
			Throw.when()
				.isNull(() -> matrix, () -> "No matrix to cluster")
				.isTrue(() -> minPts < 1, () -> "Invalid minimum number of points " + minPts)
				.isTrue(() -> epsilon < Real.TOLERANCE, () -> "Invalid reach distance " + epsilon);
			
			return new Dbscan(minPts, epsilon).compute(matrix);
		}
		
	}
	
	/**
	 * Constructor.
	 * @param minPts  Minimum number of points to form a cluster
	 * @param epsilon  Minimum distance that a point is considered reachable
	 */
	public Dbscan(int minPts, double epsilon) {
		this.minPts = minPts;
		this.epsilon = epsilon;
	}
	
	@Override
	public List<int[]> compute(Matrix matrix) {
		SpatialIndex<Integer> sIndex = this.buildIndex(matrix);
		
		List<int[]> clusters = new ArrayList<>();

		byte[] flags = new byte[matrix.getRowCount()];
		Arrays.fill(flags, EMPTY);
		
		for(int i = 0; i < matrix.getRowCount(); i++){
			if(flags[i] != EMPTY){
				continue;
			}
			
			int[] cluster = this.expand(matrix, sIndex, flags, i);
			if(cluster.length > 0){
				clusters.add(cluster);
			}
		}
		return clusters;
	}
	
	/**
	 * Expand a cluster on a starting point
	 * @param input  Input matrix
	 * @param sIndex  Spatial index
	 * @param flags  Flags of each points
	 * @param start  Starting point
	 * @return  Sequence of indices of this cluster, or empty if starting point is already visited
	 */
	protected int[] expand(Matrix input, SpatialIndex<Integer> sIndex, byte[] flags, int start) {
		if(flags[start] == VISITED){
			return NONE;
		}
		
		double[] query = input.getRow(start);
		int[] neighbors = this.queryRange(sIndex, query);
		if(neighbors.length < this.minPts){
			flags[start] = NOISE;
			return NONE;
		}
		
		flags[start] = VISITED;
		
		IntStack cluster = IntStack.newInstance();
		cluster.push(start);
		
		IntStack core = IntStack.newInstance();
		for(int n : neighbors){
			core.push(n);
		}
		
		while(!core.isEmpty()){
			int next = core.pop();
			if(flags[next] == VISITED){
				continue;
			}
			
			flags[next] = VISITED;
			cluster.push(next);
			
			neighbors = this.queryRange(sIndex, input.getRow(next));
			if(neighbors.length < this.minPts){
				continue;
			}
			
			for(int n : neighbors){
				core.push(n);
			}
		}
		
		return cluster.toArray();
	}
	
	/**
	 * Query all neighbours within reaching distance from a query point
	 * @param sIndex  Spatial index
	 * @param query  Query point
	 * @return  Array of indices of neighbours
	 */
	protected int[] queryRange(SpatialIndex<Integer> sIndex, double[] query) {
		Iterator<Integer> iter = sIndex.queryRange(query, this.epsilon);
		IntStack stack = IntStack.newInstance();
		while(iter.hasNext()){
			stack.push(iter.next());
		}
		return stack.toArray();
	}
	
	/**
	 * Build spatial index on a collection of vectors
	 * @param matrix  Input matrix
	 * @return  Spatial index
	 */
	protected SpatialIndex<Integer> buildIndex(Matrix matrix) {
		return matrix.ext(Spatial.class).build();
	}

	private int minPts;
	private double epsilon;
	
	private static final byte EMPTY = '\0';
	
	private static final byte NOISE = 'n';
	
	private static final byte VISITED = 'v';
	
	private static final int[] NONE = new int[0];
}
