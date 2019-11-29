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
 * Interface for selecting the n-th smallest from a sequence of numbers.
 * 
 * <p>To select the n-th smallest number from a sequence of number, different approaches are
 * optimal to different circumstance, e.g. using a heap, randomized quickselect, or 
 * median-of-medians etc.</p>
 * 
 * <p>This interface is to facilitate the quickselect algorithm. In the quickselect algorithm,
 * a guess is made and the sequence is partitioned. The guess is moved to its rank, and if is what
 * is needed, the answer is found. Otherwise the algorithm binary search through the sub-sequence
 * recursively.</p>
 * 
 * <p>Not all implementation returns the correct index. Some implementations return only a 
 * guess for further processing. These implementations are used by some container to find
 * the correct index.</p>
 * 
 * @author Y.K. Chan
 *
 */
public interface Select {
	
	/**
	 * Tries to select the n-th smallest from a sequence of numbers. 
	 * @param items  Sequence  
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @param target  Target index, i.e. n
	 * @return  The index of the n-th smallest item, 
	 *          or the guess that is moved to the returned position.
	 */
	public int select(double[] items, int begin, int end, int target);
	
	/**
	 * Swap two items in an sequence
	 * @param items  Sequence
	 * @param i  First item to be swapped
	 * @param j  Second item to be swapped
	 */
	public default void swap(double[] items, int i, int j) {
		if(i == j){
			return;
		}
		
		double temp = items[i];
		items[i] = items[j];
		items[j] = temp;
	}

}
