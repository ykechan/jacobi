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

import java.util.Iterator;
import java.util.List;

import jacobi.api.Matrix;
import jacobi.api.ext.Spatial;
import jacobi.api.spatial.SpatialIndex;
import jacobi.core.util.IntStack;

/**
 * Implementation of Density-based spatial clustering of applications with noise.
 * 
 * @author Y.K. Chan
 *
 */
public class Dbscan implements Clustering {
	
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
		SpatialIndex<Integer> sIndex = matrix.ext(Spatial.class).build();
		
		int[] labels = new int[matrix.getRowCount()];
		
		return null;
	}
	
	protected int[] queryRange(SpatialIndex<Integer> sIndex, double[] query, double dist) {
		Iterator<Integer> iter = sIndex.queryRange(query, dist);
		IntStack stack = IntStack.newInstance();
		while(iter.hasNext()){
			stack.push(iter.next());
		}
		return stack.toArray();
	}

	private int minPts;
	private double epsilon;
}
