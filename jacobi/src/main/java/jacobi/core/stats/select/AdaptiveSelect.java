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

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

import jacobi.core.util.Enque;

/**
 * Implementation of select that is adaptive to the input.
 * 
 * <p>This implementation uses a hybrid approach to select the target element.</p>
 * 
 * <p>When the target is an extrema, ExtremaSelect is used to directly find the element.</p>
 * 
 * <p>When the target is close to an extrema (but not an extrema), a PriorityQueue is used
 * to directly find the element.</p>
 * 
 * <p>When the sequence is very long, DualPivotQuickSelect with a randomized pivoting selection
 * is used. The probability of keep hitting the extrema is fairly slim. However if things go
 * wrong and the recursive call is nested too depth, a median-of-medians together with another
 * random pivot is used to guarantee time complexity.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class AdaptiveSelect implements Select {
	
	/**
	 * Factory method
	 * @param lowerLimit  Number of element that between extrema to target to consider using a heap
	 * @param constFactor  Constant factor. If the depth is greater that constFactor * ln(n) 
	 * 					   it is considered too deep 
	 * @return Instance of AdaptiveSelect
	 */
	public static AdaptiveSelect of(int lowerLimit, double constFactor) {
		
		return new AdaptiveSelect(lowerLimit, constFactor, 
			ExtremaSelect.getInstance(),
			ThreadLocalRandom.current()::nextInt
		);
	}
	
	/**
	 * Constructor.s
	 * @param lowerLimit  Number of element that between extrema to target to consider using a heap
	 * @param constFactor  Constant factor. If the depth is greater that constFactor * ln(n) 
	 * 					   it is considered too deep 
	 * @param extremaSelect  Implementation of selecting extrema
	 * @param randFn  Random function to be used in the worst case
	 */
	protected AdaptiveSelect(int lowerLimit, double constFactor, 
			Select extremaSelect, 
			IntUnaryOperator randFn) {
		this.lowerLimit = lowerLimit;
		this.constFactor = constFactor;
		this.extremaSelect = extremaSelect;
		this.base = new MedianOfMediansSelect(new RandomSelect(randFn));
	}

	@Override
	public int select(double[] items, int begin, int end, int target) {
		int rank = Math.min(target - begin, end - 1 - target);
		if(rank < 3) {
			return extremaSelect.select(items, begin, end, target);
		}
		
		if(rank < this.lowerLimit) {	
			return heapSelect(items, begin, end, target);
		}
		
		int limit = (int) Math.ceil(this.constFactor * Math.log(end - begin));
		return new RandomDualPivotSelect(limit,
			this.newRandomSelect(),
			this.newRandomSelect()
		).select(items, begin, end, target);
	}	
	
	/**
	 * Select the target directly by a heap.
	 * @param items  Sequence  
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @param target  Target index, i.e. n
	 * @return  The index of the n-th smallest item
	 */
	protected int heapSelect(double[] items, int begin, int end, int target) {		
		int limit = 1 + Math.min(target - begin, end - 1 - target);		
		double sign = target - begin < end - 1 - target ? 1 : -1;
		
		Enque<Entry> heap = this.enque(limit, sign > 0);
		heap.push(new Entry(begin, items[begin]));
		for(int i = begin + 1; i < end; i++) {				
			
			if(heap.size() >= limit
			&& sign * (items[i] - heap.peek().value) > 0) {
				continue;
			}
			
			heap.push(new Entry(i, items[i]));
		}
		
		return heap.peek().index;		
	}
	
	/**
	 * Create a max/min heap that at most stores a limited number of elements.
	 * @param limit  Element limit
	 * @param isMin  True for min heap, false otherwise
	 * @return  A max/min heap
	 */
	protected Enque<Entry> enque(int limit, boolean isMin) {
		Comparator<Entry> cmp = Comparator.comparingDouble(v -> v.value);
		
		PriorityQueue<Entry> queue = new PriorityQueue<>(isMin ? cmp.reversed() : cmp);
		return new Enque<Entry>() {

			@Override
			public int size() {
				
				return queue.size();
			}

			@Override
			public Enque<Entry> push(Entry item) {
				queue.offer(item);
				while(queue.size() > limit) {
					queue.remove();
				}
				return this;
			}

			@Override
			public Entry pop() {
				
				return queue.remove();
			}

			@Override
			public Entry peek() {
				
				return queue.peek();
			}

			@Override
			public Entry[] toArray(IntFunction<Entry[]> factory) {
				
				throw new UnsupportedOperationException();
			}
			
			
		};
	}
	
	/**
	 * Create a new selector that picks randomly
	 * @return  Selector that returns randomly
	 */
	protected Select newRandomSelect() {
		
		return RandomSelect.ofDefault();
	}
	
	private int lowerLimit;
	private double constFactor;
	private Select extremaSelect, base;
	
	/**
	 * Random Select, i.e. a wrapper of a random function to the Select interface
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class RandomSelect implements Select {
		
		/**
		 * Factory method using ThreadLocalRandom as random function
		 * @return Instance of RandomSelect
		 */
		public static RandomSelect ofDefault() {
			
			return new RandomSelect(ThreadLocalRandom.current()::nextInt);
		}
		
		/**
		 * Constructor.
		 * @param rand  Random function
		 */
		public RandomSelect(IntUnaryOperator rand) {
			this.rand = rand;
		}

		@Override
		public int select(double[] items, int begin, int end, int target) {
			
			return begin + this.rand.applyAsInt(end - begin);
		}
		
		private IntUnaryOperator rand;
	}
	
	/**
	 * Randomized dual pivot quick select.
	 * 
	 * If the call depth exceed a certain limit it falls back to base implementation.
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected class RandomDualPivotSelect extends DualPivotQuickSelect {

		/**
		 * Constructor.
		 * @param max  Maximum depth limit
		 * @param rand0  First random select
		 * @param rand1  Second random select
		 */
		public RandomDualPivotSelect(int max, Select rand0, Select rand1) {
			super(rand0, rand1);
			this.max = max;
		}

		@Override
		public int select(double[] items, int begin, int end, int target, int depth) {
			if(depth > this.max){
				return base.select(items, begin, end, target);
			}
			
			int rank = Math.min(target - begin, end - 1 - target);
			if(rank < 3) {
				return extremaSelect.select(items, begin, end, target);
			}
			
			if(rank < lowerLimit) {
				
				return heapSelect(items, begin, end, target);
			}
			return super.select(items, begin, end, target, depth);
		}
		
		private int max;
	}
	
	/**
	 * Dual pivot quick select using 1 random pivot and uses median-of-medians for another.
	 * 
	 * <p>Using median-of-medians guarantees </p>
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected class MedianOfMediansSelect implements Select {
		
		public MedianOfMediansSelect(Select rand) {
			this.quickSelect = new DualPivotQuickSelect(rand, new MedianOfMedians(this));
		}

		@Override
		public int select(double[] items, int begin, int end, int target) {
			
			int rank = Math.min(target - begin, end - 1 - target);
			if(rank < 3) {
				return extremaSelect.select(items, begin, end, target);
			}
			
			if(rank < lowerLimit) {
				
				return heapSelect(items, begin, end, target);
			}
			return this.quickSelect.select(items, begin, end, target);
		}
		
		private Select quickSelect;
	}
	
	protected static class Entry {
		
		public final int index;
		
		public final double value;

		public Entry(int index, double value) {
			this.index = index;
			this.value = value;
		}
		
	}
	
}
