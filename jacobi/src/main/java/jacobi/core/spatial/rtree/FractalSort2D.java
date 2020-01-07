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

import java.util.Arrays;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

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
	
	public static final double EPSILON = 1e-5;
	
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
		if(begin > 0) {
			return this.apply(Arrays.copyOfRange(vectors, begin, end), 0, end - begin);
		}
		
		Aabb2 mbr = this.mbrOf(vectors, begin, end);		
		return this.sort(vectors, 
			mbr, 
			Buffer.init(end - begin), 
			Math.max(mbr.xMax - mbr.xMin, mbr.yMax - mbr.yMin) * EPSILON
		).array;
	}
	
	protected Buffer sort(double[][] vectors, Aabb2 aabb, Buffer buffer, double epsilon) {
		if(aabb.area() < epsilon){
			// ignore negligible / de-generate regions
			return buffer;
		}
		
		double xMid = aabb.xMid();
		double yMid = aabb.yMid();
		
		int[] counts = this.group(buffer, i -> {
			double[] pos = vectors[i];
			int xUpper = pos[xIdx] > xMid ? 4 : 0;
			int yUpper = pos[yIdx] > yMid ? 2 : 0;
			return (aabb.parity << (xUpper + yUpper) ) % 4;
		});
		
		Aabb2[] quadrants = this.partition(aabb);
		if(counts.length != quadrants.length) {
			throw new UnsupportedOperationException(
				"Partition count " + quadrants.length + " mismatch with group count " + counts.length 
			);
		}
		
		int begin = buffer.begin;
		for(int i = 0; i < counts.length; i++){
			if(counts[i] > 1) {
				this.sort(vectors, 
					quadrants[i], 
					buffer.rangeOf(begin, begin + counts[i]), 
					epsilon
				);
			}
			begin += counts[i];
		}
		return buffer;
	}
	
	protected Aabb2[] partition(Aabb2 aabb) {
		double xMid = aabb.xMid();
		double yMid = aabb.yMid();
		
		int[] curve = this.fnFrac.apply(aabb.parity);
		return new Aabb2[] {
			new Aabb2(aabb.xMin, xMid, aabb.yMin, yMid, curve[0]), // LL
			new Aabb2(aabb.xMin, xMid, yMid, aabb.yMax, curve[1]), // LU
			new Aabb2(xMid, aabb.xMax, aabb.yMin, yMid, curve[2]), // UL
			new Aabb2(xMid, aabb.xMax, yMid, aabb.yMax, curve[3])  // UU
		};
	}
	
	/**
	 * Find the minimum bounding rectangle of a set of vectors
	 * @param vectors  Input vectors
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @return  Minimum bounding rectangle of a set of vectors
	 */
	protected Aabb2 mbrOf(double[][] vectors, int begin, int end) {
		double xMin = vectors[begin][this.xIdx];
		double yMin = vectors[begin][this.yIdx];
		double xMax = xMin;
		double yMax = yMin;
		
		for(int i = begin; i < end; i++){
			double[] pos = vectors[i];
			if(xMin > pos[this.xIdx]){
				xMin = pos[this.xIdx];
			}
			
			if(xMax < pos[this.xIdx]){
				xMax = pos[this.xIdx];
			}
			
			if(yMin > pos[this.yIdx]){
				yMin = pos[this.yIdx];
			}
			
			if(yMax < pos[this.yIdx]){
				yMax = pos[this.yIdx];
			}
		}
		return new Aabb2(xMin, yMin, xMax, yMax, this.fnFrac.apply(0)[0]);
	}
	
	/**
	 * Group items in buffer into 4 categories in ascending order.
	 * @param buffer  Input buffer
	 * @param gFn  Grouping function for item, returns an integer [0, 4) to denote the category
	 * @return  Number of items for each categories
	 */
	protected int[] group(Buffer buffer, IntUnaryOperator gFn) {
		int begin = buffer.begin;
		int end = buffer.end;
		
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
	
	protected static class Aabb2 {
		
		public final double xMin, yMin, xMax, yMax;
		
		public final int parity;

		public Aabb2(double xMin, double yMin, double xMax, double yMax, int parity) {
			this.xMin = xMin;
			this.yMin = yMin;
			this.xMax = xMax;
			this.yMax = yMax;
			this.parity = parity;
		}
		
		public double xMid() {
			return (this.xMin + this.xMax) / 2.0;
		}
		
		public double yMid() {
			return (this.yMin + this.yMax) / 2.0;
		}
		
		public double area() {
			return (this.xMax - this.xMin) * (this.yMax - this.xMin);
		}
		
	}
	
	protected static class Buffer {
		
		public static Buffer init(int length) {
			return new Buffer(
				IntStream.range(0, length).toArray(), 
				new int[length], 
				0, length
			);
		}
		
		public final int[] array, temp;
		
		public final int begin, end;

		public Buffer(int[] array, int[] temp, int begin, int end) {
			this.array = array;
			this.temp = temp;
			this.begin = begin;
			this.end = end;
		}
		
		public Buffer rangeOf(int start, int finish) {
			
			return new Buffer(this.array, this.temp, start, finish);
		}
		
	}
}
