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
package jacobi.core.spatial;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.spatial.SpatialIndex;
import jacobi.core.util.IntStack;
import jacobi.core.util.MinHeap;

/**
 * Perform spatial query linearly without index.
 * 
 * @author Y.K. Chan
 *
 */
public class DirectQuery implements SpatialIndex<Integer> {
	
	/**
	 * Constructor
	 * @param matrix  Spatial data in matrix form
	 */
	public DirectQuery(Matrix matrix) {
		this.matrix = matrix;
	}

	@Override
	public List<Integer> queryKNN(double[] query, int kMax) {
		MinHeap heap = MinHeap.ofMax(kMax);
		for(int i = 0; i < this.matrix.getRowCount(); i++){
			double[] p = this.matrix.getRow(i);
			double pDist = sqDist(query, p);
			if(heap.size() < kMax || heap.min() > pDist){
				heap.push(i, pDist);
			}
		}
		return Arrays.stream(heap.flush()).boxed().collect(Collectors.toList());
	}

	@Override
	public Iterator<Integer> queryRange(double[] query, double dist) {
		IntStack result = new IntStack(4);
		double qDist = dist * dist;
		
		for(int i = 0; i < this.matrix.getRowCount(); i++){
			double[] p = this.matrix.getRow(i);
			double pDist = sqDist(query, p);
			
			if(pDist < qDist){
				result.push(i);
			}
		}
		return Arrays.stream(result.toArray()).iterator();
	}
	
	/**
	 * Compute the squared euclidean distance between two vectors
	 * @param u  First vector
	 * @param v  Second vector
	 * @return  Squared euclidean distance
	 */
	protected static double sqDist(double[] u, double[] v) {
		double dist = 0.0;
		for(int i = 0; i < u.length; i++){
			double dx = u[i] - v[i];
			dist += dx * dx;
		}
		return dist;
	}

	private Matrix matrix;
}
