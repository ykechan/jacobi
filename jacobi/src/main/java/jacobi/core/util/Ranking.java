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

import java.util.Arrays;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntUnaryOperator;

/**
 * Implementation of ranking a sequence of real numbers. 
 * 
 * <p>Sorting is readily available and largely optimized by the standard library. However one
 * case is not covered: sorting primitives by comparators. This is essential if the developer
 * want to obtain the ranking of a sequence of real numbers. Since the sequence can potentially
 * be very long, boxing may impact performance both in memory usage and cache miss, which are
 * totally un-necessary when all of the items are primitives to begin with.</p>
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
	
	/**
	 * Select a pivot within the range.
	 * @param begin  Begin index of range
	 * @param end  End index of range
	 * @return  Pivot index
	 */
	protected int select(int begin, int end) {
		int len = (end - begin) / 2;
		
		if(len < LIMIT_NO_PIVOT) {
			return end - 2;
		}
		
		double val0 = this.entries[begin];
		double val1 = this.entries[(begin + end) / 2];
		double val2 = this.entries[end - 2];
		
		if(len < LIMIT_PIVOT_3) {
			return val0 < val1
				? val1 < val2 ? (begin + end) / 2 : end - 2
				: end - 2;
		}
		
		int idx0 = begin;
		int idx1 = (begin + end) / 2;		
		int idx2 = end - 2;
		
		int idx3 = idx0 + 2 * this.rand.applyAsInt((idx1 - idx0) / 2);
		int idx4 = idx1 + 2 * this.rand.applyAsInt((idx2 - idx1) / 2);
		
		double val3 = this.entries[idx3];
		double val4 = this.entries[idx4];
		
		if(val0 < val1){
			double tmp = val0; val0 = val1; val1 = tmp;
			int temp = idx0; idx0 = idx1; idx1 = temp;
		}
		
		if(val2 < val3){
			double tmp = val2; val2 = val3; val3 = tmp;
			int temp = idx2; idx2 = idx3; idx3 = temp;
		}
		
		if(val0 < val2){
			double tmp = val0; val0 = val2; val2 = tmp;
			int temp = idx0; idx0 = idx2; idx2 = temp;
			
			tmp = val1; val1 = val3; val3 = tmp;
			temp = idx1; idx1 = idx3; idx3 = temp;
		}
		
		if(val1 < val4){
			double tmp = val1; val1 = val4; val4 = tmp;
			int temp = idx1; idx1 = idx4; idx4 = temp;
		}
				
		return val1 > val2 
			? val2 > val4 ? idx2 : idx4
			: val1 > val3 ? idx1 : idx3;
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
