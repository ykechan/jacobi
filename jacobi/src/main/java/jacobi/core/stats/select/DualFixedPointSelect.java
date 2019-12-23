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
import java.util.concurrent.ThreadLocalRandom;

/**
 * Linear selection using dual pivot quick select with 2 fixed points after selection.
 * 
 * <p>A fixed point in this context refers to an index k in a sequence of number s[]
 * s.t. s[k] is the k-th smallest number in s[]. Selecting a target order statistics
 * k using this implementation would make both k and k + 1 fixed points.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class DualFixedPointSelect extends DualPivotQuickSelect {		
	
	/**
	 * Multiplier of log(n) for detecting failure to converge in linear time
	 */
	public static final double DEFAULT_DEPTH_MULTIPLIER = 1.25;
	
	/**
	 * Get the default instance 
	 * @return  Default instance
	 */
	public static DualFixedPointSelect getInstance() {
		return INSTANCE;
	}

	/**
	 * Constructor
	 * @param selector0  First pivot selection
	 * @param selector1  Second pivot selection
	 * @param extrema  Extrema selection, must select fixed point
	 * @param base Implementation to fall back to 
	 */
	public DualFixedPointSelect(Select selector0, Select selector1, 
			Select extrema, Select base) {
		super(selector0, selector1);
		this.extrema = extrema;
		this.base = base;
	}
	
	@Override
	public int select(double[] items, int begin, int end, int target) {
		int limit = (int) Math.ceil(DEFAULT_DEPTH_MULTIPLIER * Math.log(end - begin));
		return this.select(items, begin, end, target, Math.max(limit, 1));
	}

	@Override
	public int select(double[] items, int begin, int end, int target, int depth) {
		int dist = Math.min(target - begin, end - 1 - target);
		if(dist > 2) {
			return depth < 0
				? this.base.select(items, begin, end, target)
				: super.select(items, begin, end, target, depth);
		}
		
		if(this.extrema.select(items, begin, end, target) != target) {
			throw new UnsupportedOperationException(
				"Expected fixed point selection using " + this.extrema
			);
		}
		
		if(target == begin + 2 && end - begin > 2) {
			this.swap(items, target + 1, this.minimum(items, target + 1, end));
		}
		return target;
	}
	
	/**
	 * Find the index of the minimum item with specified range 
	 * @param items  Input sequence of items
	 * @param begin  Begin index of range
	 * @param end  End index of range
	 * @return  Index of the minimum item
	 */
	protected int minimum(double[] items, int begin, int end) {
		int min = begin;
		for(int i = begin + 1; i < end; i++) {
			if(items[i] < items[min]) {
				min = i;
			}
		}
		return min;
	}

	private Select extrema, base;
	
	private static final Select RAND = (a, i, j, t) -> i + ThreadLocalRandom.current().nextInt(j - i);
	
	private static final DualFixedPointSelect INSTANCE = new DualFixedPointSelect(
		RAND, RAND, new ExtremaSelect(true), (a, i, j, t) -> {
			Arrays.sort(a, i, j);
			return t;
		}
	);
}
