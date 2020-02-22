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

/**
 * Implementation of selecting an extrema.
 * 
 * <p>An extrema refers to the 1st, 2nd, and 3rd largest or smallest element in a sequence.</p>
 * 
 * <p>In such cases the element can be picked in a single pass while maintaining the 3 elements
 * that are most extreme. The number of elements to maintain is so small that special data
 * structure poses no benefit if not detrimental to the performance.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class ExtremaSelect implements Select {
	
	/**
	 * Get the common default instance
	 * @return  Default instance
	 */
	public static ExtremaSelect getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Constructor.
	 */
	public ExtremaSelect() {
		this(false);
	}

	/**
	 * Constructor.
	 * @param fix  True for fixing the extrema elements, false otherwise
	 */
	public ExtremaSelect(boolean fix) {
		this.fix = fix;
	}

	@Override
	public int select(double[] items, int begin, int end, int target) {
		
		switch(end - begin) {
			case 0 :
				throw new IllegalArgumentException("Invalid range [" + begin + "," + end + ").");
				
			case 1 :
				return begin;
				
			case 2 :
				if(this.fix && items[begin] > items[begin + 1]){
					this.swap(items, begin, begin + 1);
					return target;
				}
				return target == begin
					? items[begin] < items[begin + 1] ? begin : begin + 1
					: items[begin] > items[begin + 1] ? begin : begin + 1;
				
			default :
				break;
		}
		
		int minRank = target - begin;
		int maxRank = end - 1 - target;
		
		int rank = Math.min(minRank, maxRank);
		
		if(rank < 0) {
			throw new IllegalArgumentException(
				"Invalid target " + target + " in [" + begin + "," + end + ").");
		}
		
		if(rank > 2) {
			throw new UnsupportedOperationException(
				"Rank " + rank + " is not considered an extrema."
			);
		}
		
		int[] extrema = minRank < maxRank 
				? this.minima(items, begin, end) 
				: this.maxima(items, begin, end);
		return extrema == null 
			? minRank < maxRank ? begin + rank : end - 1 - rank
			: extrema[rank];
	}
	
	/**
	 * Finding the minimum 3 elements in order in a sequence, and put the 3 elements 
	 * at the beginning of the sequence if fix is set to true
	 * @param items  Sequence
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @return  3 elements with minimum values sorted ascendingly, or null if fixed
	 */
	protected int[] minima(double[] items, int begin, int end) {
		int min0 = -1, min1 = -1, min2 = -1;
		for(int i = begin; i < end; i++) {
			if(min0 < 0 || items[i] < items[min0]) {
				min2 = min1; min1 = min0; min0 = i;
				continue;
			}
			
			if(min1 < 0 || items[i] < items[min1]) {
				min2 = min1; min1 = i;
				continue;
			}
			
			if(min2 < 0 || items[i] < items[min2]) {
				min2 = i;
			}
		}
		
		if(!this.fix) {
			return new int[] {min0, min1, min2};
		}
		
		this.swap(items, begin, min0);
		if(min1 >= 0) {
			this.swap(items, begin + 1, min1 == begin ? min0 : min1);
		}
		if(min2 >= 0){
			this.swap(items, 
				begin + 2, 
				min2 == begin ? min0 : min2 == begin + 1 ? min1 : min2
			);
		}
		return null;
	}
	
	/**
	 * Finding the maximum 3 elements in order in a sequence, and put the 3 elements 
	 * at the end of the sequence if fix is set to true
	 * @param items  Sequence
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @return  3 elements with maximum values sorted descendingly, or null if fixed
	 */
	protected int[] maxima(double[] items, int begin, int end) {
		int max0 = -1, max1 = -1, max2 = -1;
		for(int i = begin; i < end; i++) {
			if(max0 < 0 || items[i] > items[max0]) {
				max2 = max1; max1 = max0; max0 = i;
				continue;
			}
			
			if(max1 < 0 || items[i] > items[max1]) {
				max2 = max1; max1 = i;
				continue;
			}
			
			if(max2 < 0 || items[i] > items[max2]) {
				max2 = i;
			}
		}
		
		if(!this.fix) {
			return new int[] {max0, max1, max2};
		}
		
		this.swap(items, end - 1, max0);
		if(max1 >= 0) {
			this.swap(items, end - 2, max1 == end - 1 ? max0 : max1);
		}
		if(max2 >= 0) {
			this.swap(items, end - 3, max2 == end - 1 ? max0 : max2 == end - 2 ? max1 : max2);
		}
		
		return null;
	}
	
	private boolean fix;

	private static final ExtremaSelect INSTANCE = new ExtremaSelect(false);
}
