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
import java.util.function.Function;
import java.util.function.IntFunction;

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
 * is used. This is because probabilities favor central elements instead of outliers, and 
 * fairly good guesses can be picked without extra cost.</p>
 * 
 * <p>This class is the to-go when general case selection is needed.</p>
 * 
 * @author Y.K. Chan
 */
public class AdaptiveSelect implements Select {	

	public AdaptiveSelect(Select pivoting, int limit) {
		this(new ExtremaSelect(), s -> new DualPivotQuickSelect(s, pivoting, pivoting), limit);
	}
	
	protected AdaptiveSelect(Select extrema, 
			Function<Select, Select> dualQuickFactory, int limit) {
		this.extrema = extrema;
		this.dualQuick = dualQuickFactory.apply(this);
		this.limit = limit;
	}

	@Override
	public int select(double[] items, int begin, int end, int target) {
		int rank = Math.min(target - begin, end - 1 - target);
		if(rank < 4) {
			return this.extrema.select(items, begin, end, target);
		}
		
		if(rank < this.limit){
			return this.heapSelect(items, begin, end, target);
		}
		
		return this.dualQuick.select(items, begin, end, target);
	}
	
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
	
	protected Enque<Entry> enque(int limit, boolean isMin) {
		//int limit = 1 + Math.min(target - begin, end - 1 - target);
		
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

	private int limit;
	private Select extrema, dualQuick;
	
	protected static class Entry {
		
		public final int index;
		
		public final double value;

		public Entry(int index, double value) {
			this.index = index;
			this.value = value;
		}
		
	}
}
