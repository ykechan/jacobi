/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
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
package jacobi.core.stats.select;

import java.util.Arrays;

/**
 * Implementation of QuickSelect using two pivots.
 * 
 * <p>This is superior to using single pivot, in which it virtually incur no extra cost,
 * having double the probability of getting the answer, and the next partition is likely
 * to be smaller.</p>
 * 
 * <p>If both pivots are the same, or both pivot having the same element value, a single
 * pivot is used instead. Thus if the pivoting strategy is deterministic and pure, this
 * degenerates into single pivot quick select.</p>
 * 
 * @author Y.K. Chan
 *  
 */
public class DualPivotQuickSelect implements Select {

	/**
	 * Constructor.
	 * @param base  Base selector to fall back to 
	 * @param selector0  Selector for 1st pivot
	 * @param selector1  Selector for 2nd pivot
	 */
	public DualPivotQuickSelect(Select selector0, Select selector1) {
		this.selector0 = selector0;
		this.selector1 = selector1;
	}
	
	@Override
	public int select(double[] items, int begin, int end, int target) {
		
		return this.select(items, begin, end, target, 0);
	}

	public int select(double[] items, int begin, int end, int target, int depth) {
		if(depth > items.length) {
			throw new IllegalStateException(
				"Depth " + depth + " is greater that number of items " + items.length
			);
		}
		
		int lower = this.selector0.select(items, begin, end, target);
		int upper = this.selector1.select(items, begin, end, target);
		
		if(items[lower] > items[upper]){
			int temp = lower; lower = upper; upper = temp;
		}
		
		if(lower == upper || items[lower] == items[upper]){
			int pivot = this.partition(items, begin, end, lower);
			if(pivot == begin
			&& Arrays.stream(items, begin + 1, end).noneMatch(v -> v > items[begin])) {
				// all items are the same
				return target;
			}
			
			return pivot == target 
				? pivot 
				: this.select(items, 
					pivot < target ? pivot + 1 : begin, 
					pivot > target ? pivot : end, 
					target, depth + 1);
		}
		
		this.swap(items, lower, begin);
		this.swap(items, upper, end - 1);
		
		double low = items[begin];
		double high = items[end - 1];
		
		int j = begin;
		int k = end - 1;		
		
		int i = begin + 1;
		while(i < k) {
			if(items[i] > high){
				this.swap(items, i, --k);
				continue;
			}
			
			if(items[i] < low){
				this.swap(items, i, ++j);
			}
			
			i++;
		}
		
		this.swap(items, begin, j);
		this.swap(items, end - 1, k);
		
		return j == target || k == target
			? target
			: this.select(
				items, 
				k < target ? k + 1 : j < target ? j + 1 : begin, 
				j > target ? j : k > target ? k : end, 
				target, depth + 1);
	}
	
	/**
	 * Find the rank of the element in the given pivot, swap the element to its rank, and
	 * partition the sequence into those lesser on the left, and those greater on the right.
	 * @param items  Sequence
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @param pivot  Pivot index
	 * @return  The rank of the pivot index
	 */
	protected int partition(double[] items, int begin, int end, int pivot) {
		this.swap(items, pivot, begin);	
		double value = items[begin];
		
		int j = begin + 1;
		for(int i = begin + 1; i < end; i++) {
			if(items[i] < value){
				this.swap(items, i, j++);
			}
		}
		this.swap(items, begin, --j);
		return j;
	}

	private Select selector0, selector1;
}
