/*
 * The MIT License
 *
 * Copyright 2018 Y.K. Chan
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

/**
 * A stack implementation for integers.
 * 
 * <p>
 * This class is to compensate the lack of a primitive support for stack of integers in which usage is very common.
 * With Java Collection programmer has to use Deque, which is a mixed implementation of Stack and Queue, and have to 
 * use boxed integers, which would took a hit in performance and memory if there is a large amount of integers.
 * </p>
 * 
 * @author Y.K. Chan
 */
public class IntStack {
	
	/**
	 * Factory method to create a new IntStack with default parameters
	 * @return  New instance of IntStack
	 */
	public static final IntStack newInstance() {
		return new IntStack(0);
	}
    
    /**
     * Constructor.
     * @param initialCapacity  Minimum capacity at the start
     */
    public IntStack(int initialCapacity) {
        this.step = Math.max(8 * initialCapacity / 13, 13);
        this.array = new int[Math.max(initialCapacity, 21)];
        this.count = 0;
    }
    
    /**
     * Return if stack is empty.
     * @return  True if empty, false otherwise
     */
    public boolean isEmpty() {
        return this.count == 0;
    }
    
    /**
     * Return the number of elements inside the stack.
     * @return  Number of elements inside the stack
     */
    public int size() {
        return this.count;
    }
    
    /**
     * Push an integer onto the stack. The integer will be available first using the pop operation.
     * @param elem  Integer element
     * @return  This object
     */
    public IntStack push(int elem) {
        if(this.count >= this.capacity()){
        	int current = this.array.length;
            this.array = Arrays.copyOf(this.array, this.array.length + this.step);
            this.step = current;
        }
        this.array[this.count++] = elem;
        return this;
    }
    
    /**
     * Push an array of integers onto the stack by order of the array.
     * @param array  Input array of integers
     * @return  This object
     */
    public IntStack pushAll(int... array) {
    	int ensureCap = this.count + array.length;
    	int current = this.array.length;
    	
    	while(current < ensureCap){
    		int next = current + this.step;
    		this.step = current;
    		current = next;
    	}
    	
    	this.array = current > this.array.length ? Arrays.copyOf(this.array, current) : this.array;
    	System.arraycopy(array, 0, this.array, this.count, array.length);
    	this.count += array.length;
    	return this;
    }
    
    /**
     * Get the top integer in the stack. The integer will not be removed from the stack.
     * @return  Integer element
     */
    public int peek() {
    	return this.array[this.count - 1];
    }
    
    /**
     * Pop an integer from the stack. The integer will be removed from the stack.
     * @return  Integer element
     */
    public int pop() {
        return this.array[--this.count];
    }
    
    /**
     * Get all elements as an array.
     * @return  Integer elements as an array
     */
    public int[] toArray() {
        return Arrays.copyOf(this.array, this.count);
    }
    
    /**
     * Copy all elements to an array
     * @param target  Target array to write
     * @param at  Start index of target array to write
     * @return  Target array
     */
    public int[] toArray(int[] target, int at) {
    	System.arraycopy(this.array, 0, target, at, this.count);
    	return target;
    }
    
    /**
     * Get the capacity of current storage.
     * @return  The length of the current storage.
     */
    protected final int capacity() {
        return this.array.length;
    }

    private int count, step;
    private int[] array;
}
