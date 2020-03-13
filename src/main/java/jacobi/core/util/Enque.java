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

import java.util.Deque;
import java.util.Queue;
import java.util.function.IntFunction;

/**
 * Common interface of both Stack and Queue data structure.
 * 
 * <p>Stack and queue are simple data structures which differ only on the order 
 * of removing elements: LIFO and FIFO respectively. However, it is very confusing
 * in Java Collections API to switch between them in different situation.</p>
 * 
 * <p>For example, java.util.Stack is mainly obsolete due to default synchronization. 
 * java.util.Deque can be accessed by both LIFO and FIFO and it has to be manually tracked 
 * which pair of methods was used. Queue interface extends Collections which makes it
 * way more verbose then necessary to instantiate.</p>
 * 
 * <p>This interface attempts to provide a bare minimum skeleton for a LIFO/FIFO/others data structure.
 * The order depends on the implementation.</p>
 * 
 * @author Y.K. Chan
 *
 */
public interface Enque<T> {
	
	/**
	 * Factory method for creation using a queue 
	 * @param queue  Backing queue
	 * @return  An enque
	 */
	public static <T> Enque<T> of(Queue<T> queue) {
		return new Enque<T>() {

			@Override
			public int size() {
				return queue.size();
			}

			@Override
			public Enque<T> push(T item) {
				queue.add(item);
				return this;
			}

			@Override
			public T pop() {
				return queue.remove();
			}

			@Override
			public T peek() {
				return queue.poll();
			}

			@Override
			public T[] toArray(IntFunction<T[]> factory) {
				T[] array = factory.apply(this.size());
				int k = 0;
				while(!queue.isEmpty()){
					array[k++] = queue.remove();
				}
				return array;
			}
			
		};
	}
	
	/**
	 * Factory method for creating a Stack (LIFO) using a Deque
	 * @param deque  Deque instance
	 * @return  A stack data structure
	 */
	public static <T> Enque<T> stackOf(Deque<T> deque) {
		return new Enque<T>() {

			@Override
			public int size() {
				return deque.size();
			}

			@Override
			public Enque<T> push(T item) {
				deque.push(item);
				return this;
			}

			@Override
			public T pop() {
				return deque.pop();
			}

			@Override
			public T peek() {
				return deque.peek();
			}

			@Override
			public T[] toArray(IntFunction<T[]> factory) {
				T[] array = deque.toArray(factory.apply(this.size()));
				for(int i = 0, j = array.length - 1; i < j; i++, j--) {
					T temp = array[i]; 
					array[i] = array[j];
					array[j] = temp;
				}
				return array;
			}
			
		};
	}
	
	/**
	 * Factory method for creating a Queue (FIFO) using a Deque
	 * @param deque  Deque instance
	 * @return  A Queue data structure
	 */
	public static <T> Enque<T> queueOf(Deque<T> deque) {
		return new Enque<T>() {

			@Override
			public int size() {
				return deque.size();
			}

			@Override
			public Enque<T> push(T item) {
				deque.offer(item);
				return this;
			}

			@Override
			public T pop() {
				return deque.poll();
			}

			@Override
			public T peek() {
				return deque.peek();
			}
			
			@Override
			public T[] toArray(IntFunction<T[]> factory) {
				return deque.toArray(factory.apply(this.size()));
			}
			
		};
	}
	
	/**
	 * Remove all items in this data structure
	 * @return  This object
	 */
	public default Enque<T> clear() {
		while(this.size() > 0) {
			this.pop();
		}
		return this;
	}
	
	/**
	 * Check if this data structure is empty
	 * @return  True if empty, false otherwise
	 */
	public default boolean isEmpty() {
		return this.size() == 0;
	}
	
	/**
	 * Get the number of items
	 * @return  Number of items
	 */
	public int size();
	
	/**
	 * Push an item into this data structure
	 * @param item  Item to be stored
	 * @return  This object
	 */
	public Enque<T> push(T item);
	
	/**
	 * Retrieve and remove the first item in this data structure
	 * @return  Item to be removed
	 */
	public T pop();
	
	/**
	 * Retrieve the first item in this data structure
	 * @return  The first item
	 */
	public T peek();
	
	/**
	 * Get all items in this data structure
	 * @param factory  Factory method for creating array for items
	 * @return  An array containing all items
	 */
	public T[] toArray(IntFunction<T[]> factory);	
	
}
