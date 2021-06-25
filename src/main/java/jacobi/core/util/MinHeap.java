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
import java.util.NoSuchElementException;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * An implementation of minimum heap for weighted integers.
 * 
 * <p>Using a priority queue for weighted integer incurs much un-necessary overhead in 
 * memory usage and comparison, but in reality only primitives are involved.</p>
 * 
 * <p>This implementation is a heap structure specifically designed to store 
 * weighted integers. The integer are naturally compared by their weights.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class MinHeap implements Enque<Weighted<Integer>> {
	
	/**
	 * Factory method for a min-heap with default parameters
	 * @return  Instance of min-heap
	 */
	public static MinHeap newDefault() {
		return new MinHeap(DEFAULT_INIT_SIZE, DEFAULT_INIT_EXPAND);
	}
	
	/**
	 * Factory method for a min-heap with fixed capacity. Added items when capacity is reached
	 * will drop the minimum item.
	 * @param  capacity  Maximum number of item this heap can hold
	 * @return  Instance of min-heap
	 */
	public static MinHeap ofMax(int capacity) {
		if(capacity < 1){
			throw new IllegalArgumentException("Invalid capacity " + capacity);
		}
		
		return new MinHeap(capacity, 0);
	}
	
	/**
	 * Constructor.
	 * @param initCap  Initial capacity
	 * @param expand  Initial expansion amount
	 */
	public MinHeap(int initCap, int expand) {
		this.length = 0;
		this.expand = expand;
		this.array = new double[2 * initCap];
	}

	@Override
	public int size() {
		return this.length;
	}

	@Override
	public Enque<Weighted<Integer>> push(Weighted<Integer> item) {
		return this.push(item.item, item.weight);
	}
	
	/**
	 * Push an item in this heap
	 * @param item  Integer item
	 * @param weight  Associated weight
	 * @return  This
	 */
	public Enque<Weighted<Integer>> push(int item, double weight) {		
		if(this.expand < 1 && this.array.length == 2 * this.length){
			if(weight < this.min()){
				return this;
			}
			
			this.array[0] = weight;
			this.array[1] = Double.longBitsToDouble((long) item);
			
			this.heapifyDown(this.array, 0);
			return this;
		}
		
		this.array = this.ensureCapacity(this.array, this.length + 1);
		
		this.array[2 * this.length] = weight;
		this.array[2 * this.length + 1] = Double.longBitsToDouble((long) item);
		
		this.heapifyUp(this.array, this.length++);
		return this;
	}

	@Override
	public Weighted<Integer> pop() {
		Weighted<Integer> top = this.peek();
		
		this.swap(this.array, 0, --this.length);
		
		this.heapifyDown(this.array, 0);
		return top;
	}

	@Override
	public Weighted<Integer> peek() {
		if(this.isEmpty()) {
			throw new NoSuchElementException();
		}
		
		return this.get(this.array, 0);
	}
	
	/**
	 * Find the minimum weight in this heap
	 * @return  The minimum weight
	 */
	public double min() {
		if(this.isEmpty()){
			throw new NoSuchElementException();
		}
		
		return this.array[0];
	}
	
	public int[] flush() {
		int[] items = new int[this.size()];		
		int k = 0;
		while(!this.isEmpty()){
			items[k++] = this.pop().item;
		}		
		return items;
	}

	@Override
	public Weighted<Integer>[] toArray(IntFunction<Weighted<Integer>[]> factory) {
		
		return IntStream.range(0, this.length)
				.mapToObj(i -> this.get(this.array, i))
				.toArray(factory);
	}	

	/**
	 * Get an entry from &lt;weight, item&gt; pair array 
	 * @param array  Input array
	 * @param index  Index of the entry
	 * @return  Entry as a weighted integer
	 */
	protected Weighted<Integer> get(double[] array, int index) {
		return new Weighted<>(
			(int) Double.doubleToRawLongBits(array[2 * index + 1]), 
			array[2 * index]
		);
	}
	
	/**
	 * Maintain the heap structure from the misplaced element to the root
	 * @param array  Input array
	 * @param index  Index of the misplaced element
	 */
	protected void heapifyUp(double[] array, int index) {
		int target = index;
		while(target > 0){
			int parent = target / 2;
			if(array[2 * parent] <= array[2 * target]) {
				return;
			}
			
			this.swap(array, target, parent);
			target = parent;
		}
	}
	
	/**
	 * Maintain the heap structure from the misplaced element to the leaf
	 * @param array  Input array
	 * @param from  Index of the misplaced element
	 */
	protected void heapifyDown(double[] array, int from) {
		int target = from;
		int left = 2 * from + 1;
		
		while(left < this.length){
			int lesser = left + 1 < this.length
					? array[2 * left] < array[2 * left + 2] 
						? left : left + 1
					: left;
			
			if(array[2 * target] <= array[2 * lesser]){
				return;
			}
			
			this.swap(array, target, lesser);
			
			target = lesser;
			left = 2 * target + 1;
		}
	}
	
	/**
	 * Swap two entries in an array of &lt;weight, item&gt; pairs
	 * @param array  Input array
	 * @param i  Index of the first entry
	 * @param j  Index of the second entry
	 */
	protected void swap(double[] array, int i, int j) {
		if(i == j){
			return;
		}
		
		double tmp0 = array[2 * i];
		double tmp1 = array[2 * i + 1];
		
		array[2 * i] = array[2 * j];
		array[2 * i + 1] = array[2 * j + 1];
		
		array[2 * j] = tmp0;
		array[2 * j + 1] = tmp1;
	}
	
	/**
	 * Create a new array if capacity of the input array is insufficient for 
	 * the maximum number of elements
	 * @param array  Input array
	 * @param max  Maximum number of elements
	 * @return  A new array if capacity is insufficient, or the original array
	 */
	protected double[] ensureCapacity(double[] array, int max) {
		int next = array.length / 2;
		while(next < max){
			int temp = next;
			next += this.expand;
			this.expand = temp;
		}
		
		next *= 2;
		return next > array.length
			? Arrays.copyOfRange(array, 0, next)
			: array;
	}
	
	private int length, expand;
	private double[] array;
	
	/**
	 * Default initial element capacity
	 */
	protected static final int DEFAULT_INIT_SIZE = 16;
	
	/**
	 * Default initial expand size
	 */
	protected static final int DEFAULT_INIT_EXPAND = 9;
	
}
