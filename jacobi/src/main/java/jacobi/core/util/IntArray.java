/*
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
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
 * A thin wrapper on a integer array to make it immutable.
 * 
 * @author Y.K. Chan
 */
public class IntArray {
    
    /**
     * Singleton instance of an empty array.
     */
    public static final IntArray EMPTY = new IntArray(new int[0]);        

    /**
     * Constructor.
     * @param array  
     */
    public IntArray(int... array) { // NOPMD - intended
        this.array = array;
    }
    
    /**
     * Get the length of the array.
     * @return  Length of the array
     */
    public int length() {
        return this.array.length;
    }
    
    /**
     * Get an array element.
     * @param index  Index of the array element
     * @return  Array element
     */
    public int get(int index) {
        return this.array[index];
    }
    
    /**
     * Create a copy of the underlying array.
     * @return  A copy of the underlying array
     */
    public int[] toArray() {
        return Arrays.copyOf(array, this.array.length);
    } 

    private int[] array;
}
