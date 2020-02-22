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
 * Implementation of Median-of-Medians algorithm for pivot selection.
 * 
 * <p>A sequence is divided by groups of 5, and select the median of each group. The 
 * algorithm then return the median of these medians within each group.</p>
 * 
 * <p>Using this pivot selecting strategy together with QuickSelect achieves linear
 * running time.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class MedianOfMedians implements Select {
	
	/**
	 * Constructor.
	 * @param selector  Selector for median
	 */
	public MedianOfMedians(Select selector) {
		this.selector = selector;
	}

	@Override
	public int select(double[] items, int begin, int end, int target) {
		if(end - begin < 6){
			return this.select(items, begin, end, target);
		}
		
		this.medianToFront(items, begin, end);
		int length = this.groupFronts(items, begin, end);
		return this.selector.select(items, begin, begin + length, begin + length / 2);
	}
	
	/**
	 * Move the median of each group of 5 to the first element of each group
	 * @param items  Sequence of numbers
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 */
	protected void medianToFront(double[] items, int begin, int end) {
		for(int i = begin; i < end; i += 5) {
			int length = Math.min(end - i, 5);
			if(length < 3) {
				// meaningful median doesn't exists for 1 or 2 items
				continue;
			}
			int median = this.selector
				.select(items, i, i + length, i + length / 2);
			this.swap(items, i, median);
		}
	}
	
	/**
	 * Move all first elements of each group to the front of the sequence
	 * @param items  Sequence of numbers
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @return  End index of elements moved
	 */
	protected int groupFronts(double[] items, int begin, int end) {
		int k = begin + 1;
		
		for(int i = begin + 5; i < end; i += 5) {
			this.swap(items, i, k++);
		}
		
		return k;
	}
	
	private Select selector;
}
