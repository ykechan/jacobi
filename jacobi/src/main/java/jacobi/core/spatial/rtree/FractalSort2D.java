/* 
 * The MIT License
 *
 * Copyright 2020 Y.K. Chan
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
package jacobi.core.spatial.rtree;

import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

/**
 * Sorting spatial objects by a 2-D space filing fractal.
 * 
 * <p>A fractal is this context is a space-filing curve with structure that is 
 * largely scale-invariant. Due to this properties, visiting order of points in 
 * a coarse scale is unchanged in a finer scale of rendering of the fractal.</p>
 * 
 * <p>This implementation groups the spatial objects of the 4 quadrants and rank
 * them in the current scale, and further sort within each group in a finer scale.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class FractalSort2D implements SpatialSort {
	
	/**
	 * Constructor.
	 * @param xIdx  Index of x-axis
	 * @param yIdx  Index of y-axis
	 * @param fnFrac  Fractal function
	 */
	public FractalSort2D(int xIdx, int yIdx, IntFunction<int[]> fnFrac) {
		this.xIdx = xIdx;
		this.yIdx = yIdx;
		this.fnFrac = fnFrac;
	}	
	
	@Override
	public int[] apply(double[][] vectors, int begin, int end) {
		
		return null;
	}
	
	protected int[] group(Buffer buffer, int begin, int end, IntUnaryOperator gFn) {
		int head = begin, tail = end;
		int low = begin, high = end;
		
		int i = begin;
		while(i < tail){
			int gNum = gFn.applyAsInt(buffer.array[i]);
			switch(gNum) {
				case 0 :
					buffer.array[head++] = buffer.array[i++];
					break;
					
				case 1 :
					buffer.temp[low++] = buffer.array[i++];
					break;
					
				case 2 :
					buffer.temp[--high] = buffer.array[i++];
					break;
					
				case 3 : { 
					int tmp = buffer.array[--tail]; 
					buffer.array[tail] = buffer.array[i]; 
					buffer.array[i] = tmp; 
					}
					break;
					
				default :
					throw new UnsupportedOperationException("Support grouping into [0-4) only, found " + gNum);
			}
		}
		
		if(low > begin) {
			System.arraycopy(buffer.temp, begin, buffer.array, head, low - begin);
		}
		
		if(high < end){			
			System.arraycopy(buffer.temp, 
				high, 
				buffer.array, tail - (end - high), end - high);
		}
		return new int[] { head - begin, low - begin, end - high, end - tail };
	}
	
	private int xIdx, yIdx;
	private IntFunction<int[]> fnFrac;
	
	protected static class Buffer {
		
		public final int[] array, temp;

		public Buffer(int[] array, int[] temp) {
			this.array = array;
			this.temp = temp;
		}
		
	}
}
