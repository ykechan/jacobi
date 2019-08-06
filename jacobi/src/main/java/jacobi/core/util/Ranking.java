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
package jacobi.core.util;

import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;

/**
 * Implementation of ranking a sequence of real numbers. 
 * 
 * <p>Sorting is a readily available and largely optimized function provided by the standard library. 
 * However one case is not covered: sorting primitives by comparators. This is essential if the developer
 * want to obtain the ranking of a sequence of real numbers.</p>
 * 
 * <p>This class is to prevent the un-necessary boxing or memory usage for using the out-of-the-box
 * sorting implementation.</p>
 * 
 * <p>This class implements sorting on a double array with each number having a payload attached.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Ranking {	
	
	/**
	 * Constructor.
	 * @param entries  Array of entries
	 * @param rand  Random function that accepts an integer n and returns a random integer in [0, n).
	 */
	protected Ranking(double[] entries, IntUnaryOperator rand) {
		this.rand = rand;
		this.entries = entries;
	}
	
	/**
	 * Initialize the buffer
	 * @param valFn  Function get the real value given the index
	 * @return  this
	 */
	public Ranking init(IntToDoubleFunction valFn) {
		
		int len = this.entries.length / 2;
		for(int i = 0; i < len; i++) {
			this.entries[2 * i] = valFn.applyAsDouble(i);
			this.entries[2 * i + 1] = Double.longBitsToDouble((long) i);
		}
		return this;
	}
	
	/**
	 * Sort the buffer
	 * @return  Ranking of each values
	 */
	public int[] sort() {
		
		return this.toArray();
	}
	
	protected void introsort(int begin, int end, int limit) {
		int len = end - begin;
		
		if(len < 2) {
			return;
		}
		
		if(limit < 0) {
			this.heapsort(begin, end);
			return;
		}
		
		int upper = begin;
		int lower = end - 1;
		
		int[] piv = this.pivoting3(begin, end, lower, upper);
		
		if(piv[0] == begin 
		&& piv[1] == end - 1 
		&& this.entries[2 * begin] == this.entries[2 * end - 2]) {
			// degenerate case that all elements are the same
			return;
		}
		
		this.introsort(begin, piv[0] - 1, limit - 1);
		this.introsort(piv[0] + 1, piv[1] - 1, limit - 1);
		this.introsort(piv[1] + 1, end, limit - 1);
	}
	
	/**
	 * Perform pivoting using 2 pivots. The range would be partitioned into lesser than both pivots,
	 * between the pivots and greater than both pivots, in sequence.
	 * @param begin  Begin index of range
	 * @param end  End index of range
	 * @param lower  Index of 1st pivot. If this is greater than the 2nd pivot, the pivots are swapped.
	 * @param upper  Index of 2nd pivot. If this is lesser than the 2nd pivot, the pivots are swapped.
	 * @return  Array of two integer of the pivots final position.
	 */
	protected int[] pivoting3(int begin, int end, int lower, int upper) {		
		if(upper == lower) {
			throw new IllegalArgumentException("Pivots #" 
				+ lower + " and #" + upper + " duplicated.");
		}
		
		if(this.entries[2 * lower] > this.entries[2 * upper]) {
			return this.pivoting3(begin, end, upper, lower);					
		}
		
		this.swap(begin, lower);
		this.swap(end - 1, upper == begin ? lower : upper);
		
		double lowerPivot = this.entries[2 * begin];
		double upperPivot = this.entries[2 * end - 2];
		
		if(lowerPivot > upperPivot) {
			throw new IllegalArgumentException("Lower pivot " 
				+ lowerPivot + " is larger than upper pivot " 
				+ upperPivot );
		}
		
		int j = end - 1;
		int i = begin + 1;
		int k = begin + 1;
		while(k < j){
			if(this.entries[2 * k] > upperPivot) {
				this.swap(k, --j);
				continue;
			}
			
			if(this.entries[2 * k] < lowerPivot) {
				this.swap(k, i++);
			}
			
			k++;
		}
		
		this.swap(--i, begin);
		this.swap(j, end - 1);
		return new int[] {i, j};
	}	
	
	/**
	 * Perform heap sort on range. This method accepts entry index instead of array index.
	 * @param begin  Begin index of range of entries
	 * @param end  End index of range of entries
	 */
	protected void heapsort(int begin, int end) {
		int last = end - 1;
		
		// heapify
		if(last % 2 > 0 && this.entries[2 * last] > this.entries[last - 1]) {
			this.swap(last, last / 2);
		}
		
		for(int i = (last / 2) - 1; i >= 0; i--){						
			this.heapify(i, end);
		}
		
		// extract max
		int len = end - begin;
		for(int i = 0; i < len; i++){
			this.swap(begin, end - 1 - i);
			this.heapify(begin, end - 1 - i);
		}
	}
	
	/**
	 * Re-construct a branch of heap while only the root is out-of-place
	 * @param target  Root index of branch
	 * @param end  End index of heap
	 */
	protected void heapify(int target, int end) {
		int prev = target;
		int next = 2 * target + 1;
		
		while(next < end){
			if(next + 1 < end && this.entries[2 * next + 2] > this.entries[2 * next]){
				next++;
			}
			
			if(this.entries[2 * prev] > this.entries[2 * next]){
				break;
			}
			
			this.swap(prev, next);
			prev = next;
			next = 2 * prev + 1;
		}
	}
	
	/**
	 * Swap two entries. This method accepts entry index instead of array index.
	 * @param i  Index of first entry
	 * @param j  Index of second entry.
	 */
	protected void swap(int i, int j) {
		if(i == j){
			return;
		}
		
		double tmp0 = this.entries[2 * i];
		double tmp1 = this.entries[2 * i + 1];
		
		this.entries[2 * i] = this.entries[2 * j];
		this.entries[2 * i + 1] = this.entries[2 * j + 1];
		
		this.entries[2 * j] = tmp0;
		this.entries[2 * j + 1] = tmp1;
	}	
	
	/**
	 * Extract the ranking of each entry
	 * @return  Ranking of each entry
	 */
	protected int[] toArray() {
		int[] array = new int[this.entries.length / 2];
		for(int i = 1; i < this.entries.length; i += 2) {
			array[i / 2] = (int) Double.doubleToRawLongBits(this.entries[i]);
		}
		return array;
	}
	
	private IntUnaryOperator rand;
	private double[] entries;
	
	protected static final int LIMIT_NO_PIVOT = 4;
	
	protected static final int LIMIT_PIVOT_3 = 9;
}
