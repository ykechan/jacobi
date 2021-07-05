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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.spatial.SpatialIndex;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.IntStack;
import jacobi.core.util.MinHeap;
import jacobi.core.util.Ranking;
import jacobi.core.util.Throw;

/**
 * Spatial index that uses binary search for scalars.
 * 
 * <p>For indexing scalars, a sorted array is suffices to avoid the overhead brought by other
 * data structures, such as trees.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class BinarySearchIndex implements SpatialIndex<Integer> {
	
	/**
	 * Factory method
	 * @param matrix  Input column vector
	 * @return  Spatial index on row indices
	 */ 
	public static SpatialIndex<Integer> of(Matrix matrix) {
		Throw.when()
			.isNull(() -> matrix, () -> "No input matrix")
			.isTrue(() -> matrix.getColCount() != 1, () -> "This class is for column vector only.");
		
		double[] index = new double[2 * matrix.getRowCount()];
		Ranking ranking = new Ranking(index, n -> ThreadLocalRandom.current().nextInt(n), 0){

			@Override
			protected int[] toArray() {
				return null;
			}
			
		};
		
		if(matrix instanceof ColumnVector){
			double[] vector = ((ColumnVector) matrix).getVector();
			ranking.init(i -> vector[i]);
		} else {
			ranking.init(i -> matrix.get(i, 0));
		}
		
		ranking.sort();
		return new BinarySearchIndex(index);
	}
	
	/**
	 * Constructor.
	 * @param sorted  Index that is sorted
	 */
	protected BinarySearchIndex(double[] sorted) {
		this.sorted = sorted;
	}

	@Override
	public List<Integer> queryKNN(double[] query, int kMax) {
		if(query == null || query.length != 1){
			throw new IllegalArgumentException("Dimension mismatch.");
		}
		
		if(kMax < 1){
			return Collections.emptyList();
		}
		
		int len = this.sorted.length / 2;
		int pos = this.search(query[0], 0, len, true);
		
		MinHeap heap = MinHeap.ofMax(kMax);
		
		
		return Arrays.stream(heap.flush()).boxed().collect(Collectors.toList());
	}

	@Override
	public Iterator<Integer> queryRange(double[] query, double dist) {
		if(query == null || query.length != 1){
			throw new IllegalArgumentException("Dimension mismatch.");
		}
		
		if(dist < 0.0){
			return Collections.emptyIterator();
		}
		
		int len = this.sorted.length / 2;
		
		int begin = this.search(query[0] - dist, 0, len, true);
		int end = this.search(query[0] + dist, begin, len, false);
		
		IntStack stack = new IntStack(end - begin);
		for(int i = begin; i < end; i++){
			double value = this.sorted[2 * i];
			if(Math.abs(query[0] - value) > dist){
				break;
			}
			
			int idx = (int) Double.doubleToLongBits(this.sorted[2 * i + 1]);
			stack.push(idx);
		}
		return Arrays.stream(stack.toArray()).iterator();
	}
	
	protected int search(double target, int begin, int end, boolean lower){
		while(end - begin > 1){
			int mid = (begin + end) / 2;
			
			double value = this.sorted[2 * mid];
			if(value < target){
				begin = mid;
			}else if(value > target){
				end = mid;
			}else{ 
				int stride = lower ? -1 : 1;
				while(this.sorted[2 * mid] == target){
					mid += stride;
					if(mid < begin || mid >= end){
						break;
					}
				}
				
				return mid - stride;
			}
		}
		return lower ? begin : end;
	}

	private double[] sorted;
}
