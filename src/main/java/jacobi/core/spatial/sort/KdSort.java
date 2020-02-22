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
package jacobi.core.spatial.sort;

import java.util.List;
import java.util.function.Function;

import jacobi.core.util.IntStack;

/**
 * Implementation of spatial sorting in the order of a kd-tree.
 * 
 * <p>Fractal sort utilize all of the dimensions which in turn generates good result,
 * but it does not scale well with the number of dimensions (especially Hilbert sort).
 * Only sorting in a few dimensions may miss out a lot of information from the missing
 * dimensions. Dimension reduction techniques like PCA work but this library would leave
 * that choice to the data analyst.</p>
 * 
 * <p>K-d sort is provided for sorting data in high dimension. As in a kd-tree, the spatial 
 * data are divided in half by a single dimension, then in turn sorting the two halves
 * in other dimensions in the same manner.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class KdSort implements SpatialSort {

	@Override
	public int[] sort(List<double[]> vectors) {
		// ...
		return null;
	}	
	
	protected int[] groupBy(List<double[]> vectors, Buffer buffer, int... orderBy) {
		if(orderBy.length > 4) {
			throw new UnsupportedOperationException("Number of dimensions too large in single batch.");
		}
		
		double[] mean = this.meanOf(vectors, buffer, orderBy);
		IntStack[] groups = new IntStack[1 << orderBy.length];
		
		int begin = buffer.begin;
		int end = buffer.end;
		
		for(int i = begin; i < end; i++) {
			int k = buffer.array[i];
			double[] v = vectors.get(k);
			
			int hash = 0;
			for(int j = 0; j < mean.length; j++){
				hash += v[orderBy[j]] < mean[j] ? 0 : 1;
				hash *= 2;
			}
			
			if(groups[hash] == null) {
				groups[hash] = new IntStack(4);
			}
			
			groups[hash].push(k);
		}
		
		return this.merge(groups, buffer);
	}
	
	protected double[] meanOf(List<double[]> vectors, Buffer buffer, int... orderBy) {
		double[] mean = new double[orderBy.length];
		
		int begin = buffer.begin;
		int end = buffer.end;
		
		for(int i = begin; i < end; i++){
			double[] v = vectors.get(buffer.array[i]);
			for(int j = 0; j < mean.length; j++) {
				mean[j] += v[orderBy[j]];
			}
		}
		
		for(int j = 0; j < mean.length; j++) {
			mean[j] /= end - begin;
		}
		return mean;
	}
	
	protected int[] merge(IntStack[] groups, Buffer buffer) {
		int[] nums = new int[groups.length];
		int start = buffer.begin;
		for(int i = 0; i < groups.length; i++){
			if(start + groups[i].size() > buffer.end) {
				throw new IllegalArgumentException(
					"Number of items out of bounds [" + buffer.begin + "," + buffer.end + "),"
				);
			}
			
			groups[i].toArray(buffer.array, start);
			nums[i] = groups[i].size();
			start += groups[i].size();
		}
				
		return nums;
	}	
	
	protected List<int[]> histo() {
		return null;
	}
	
	private Function<int[], SpatialSort> sortFactory;
	private Function<List<double[]>, double[]> meanFn, stdDevFn;
		
	protected static class Buffer {
		
		public final int[] array;
		
		public final int begin, end;

		public Buffer(int[] array, int begin, int end) {
			this.array = array;
			this.begin = begin;
			this.end = end;
		}
		
	}
}
