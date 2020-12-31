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
			
			double min = heap.size() < kMax ? Double.MIN_VALUE : heap.peek().weight;
			double dist = 0.0;
			
			for(int j = 0; j < p.length; j++){
				double comp = (query[j] - p[j]) * (query[j] - p[j]);
				min += comp;
				if(min > 0.0){
					break;
				}
				dist += comp;
			}
			
			if(min < 0.0){
				heap.push(i, -dist);
				
				while(heap.size() > kMax){
					heap.pop();
				}
			}
		}
		return null;
	}

	@Override
	public Iterator<Integer> queryRange(double[] query, double dist) {
		IntStack result = new IntStack(4);
		double limit = dist * dist;
		
		for(int i = 0; i < this.matrix.getRowCount(); i++){
			double[] p = this.matrix.getRow(i);
			double res = limit;
			
			for(int j = 0; j < p.length; j++){
				res -= (query[j] - p[j]) * (query[j] - p[j]);
				if(res < 0.0) {
					break;
				}
			}
			
			if(res <= 0.0){
				result.push(i);
			}
		}
		return Arrays.stream(result.toArray()).iterator();
	}

	private Matrix matrix;
}
