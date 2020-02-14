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
package jacobi.core.spatial.sort;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

/**
 * Implementation of sorting a list of spatial objects using a 2-D space filing fractal.
 * 
 * <p>For higher-dimensional data, it is sorted by 2 specific dimensions.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class FractalSort2D implements SpatialSort {
	
	/**
	 * Constructor.
	 * @param xIdx  Index of x-dimension
	 * @param yIdx  Index of y-dimension
	 * @param fractalFn  Fractal function
	 */
	public FractalSort2D(int xIdx, int yIdx, IntFunction<int[]> fractalFn) {
		this.xIdx = xIdx;
		this.yIdx = yIdx;
		this.fractalFn = fractalFn;
	}

	@Override
	public int[] sort(List<double[]> vectors) {	
		
		return this.sort(
			this.init(vectors),
			IntStream.range(0, vectors.size()).toArray(),
			vectors.size()
		);
	}
	
	/**
	 * Sort the vectors according to the x and y component values
	 * @param values  Component values
	 * @param indices  Indices in pre-sorted order
	 * @param limit  Maximum depth
	 * @return  Indices in sorted order
	 */
	protected int[] sort(double[] values, int[] indices, int limit) {
		int[] temp = new int[indices.length];
		
		Deque<Quadrant> stack = new ArrayDeque<>();
		stack.push(new Quadrant(0, indices.length, this.fractalFn.apply(0)[0], 0));
		
		while(!stack.isEmpty()){			
			Quadrant quad = stack.pop();
			if(quad.end - quad.begin < 2 || quad.depth > limit){
				continue;
			}
			
			int[] counts = this.groupQuad(values, quad, indices, temp);
			int[] enhance = this.fractalFn.apply(quad.parity);
			
			if(counts.length != enhance.length) {
				throw new IllegalStateException("Unable to partition space into "
					+ counts.length + ".");
			}
			
			int[] mapping = new int[4];
			mapping[(quad.parity / Fractal2D.LL) % 4] = enhance[0];
			mapping[(quad.parity / Fractal2D.LU) % 4] = enhance[1];
			mapping[(quad.parity / Fractal2D.UL) % 4] = enhance[2];
			mapping[(quad.parity / Fractal2D.UU) % 4] = enhance[3];
			
			int k = quad.begin;
			for(int i = 0; i < enhance.length; i++) {
				if(counts[i] == quad.end - quad.begin){
					// degenerate
					break;
				}
				
				if(counts[i] > 0) {
					stack.push(new Quadrant(
						k, k + counts[i], mapping[i], quad.depth + 1
					));
					k += counts[i];
				}				
			}						
		}
		return indices;
	}
	
	/**
	 * Group input vectors to 4 quadrants: LL, LU, UL and UU
	 * @param values  Components of vectors
	 * @param quadrant  Current quadrant
	 * @param indices  Indices in sorted order, will be grouped and sorted according to parity
	 * @param temp  Temp buffer
	 * @return  The number of items in each quadrants after grouping
	 */
	protected int[] groupQuad(double[] values, Quadrant quadrant, int[] indices, int[] temp) {
		int head = quadrant.begin;
		int tail = quadrant.end;
		
		int lower = quadrant.begin;
		int upper = quadrant.end;
		
		int parity = quadrant.parity;
		
		double[] mid = this.mean(values, quadrant.begin, quadrant.end, indices);
		double xMid = mid[0];
		double yMid = mid[1];
		
		int i = head;
		while(i < tail){
			int index = 2 * indices[i];			
			int quadBase = (values[index] > xMid ? 16 : 1) * (values[index + 1] > yMid ? 4 : 1);
			int group = (parity / quadBase) % 4;
			
			switch(group) {
				case 0 :
					if(i > head) {
						indices[head] = indices[i];
					}
					head++; i++;
					break;
					
				case 1 :
					temp[lower++] = indices[i++];
					break;
					
				case 2 :
					temp[--upper] = indices[i++];
					break;
					
				case 3 :
					{
						int tmp = indices[i]; indices[i] = indices[--tail]; indices[tail] = tmp;
					}
					break;
					
				default :
					throw new IllegalArgumentException("Invalid group " + group
						+ " found in coefficient of " + quadBase 
						+ " in parity " + parity);
			}
		}
		
		if(lower > quadrant.begin){
			System.arraycopy(temp, quadrant.begin, indices, head, lower - quadrant.begin);
		}
		
		if(upper < quadrant.end){
			int length = quadrant.end - upper;
			System.arraycopy(temp, upper, indices, tail - length, length);
		}
		
		return new int[] {
			head - quadrant.begin,
			lower - quadrant.begin,
			quadrant.end - upper,
			quadrant.end - tail
		};
	}
	
	/**
	 * Find the mean of component values in x and y dimension
	 * @param values  Component values
	 * @param begin  Begin index of interest
	 * @param end  End index of interest
	 * @param indices  Indices of access
	 * @return  Mean in x and y dimension
	 */
	protected double[] mean(double[] values, int begin, int end, int[] indices){
		double sumX = 0.0;
		double sumY = 0.0;
		for(int i = begin; i < end; i++){
			int k = 2 * indices[i];
			sumX += values[k++];
			sumY += values[k];
		}
		return new double[] {sumX / (end - begin), sumY / (end - begin)};
	}
	
	/**
	 * Get the x and y components of all vectors as an array
	 * @param vectors  Input vectors
	 * @return  X and y components
	 */
	protected double[] init(List<double[]> vectors) {
		double[] values = new double[2 * vectors.size()];
		int k = 0;
		for(double[] v : vectors){
			values[k++] = v[this.xIdx];
			values[k++] = v[this.yIdx];
		}
		return values;
	}

	private int xIdx, yIdx;
	private IntFunction<int[]> fractalFn;
	
	/**
	 * Data object for indicating the area currently computing
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Quadrant {
		
		/**
		 * Begin and end index of interest
		 */
		public final int begin, end;
		
		/**
		 * Parity and depth of current area
		 */
		public final int parity, depth;

		/**
		 * Constructor.
		 * @param begin  Begin index of interest
		 * @param end  End index of interest
		 * @param parity  Parity of current area
		 * @param depth  Depth of current area
		 */
		public Quadrant(int begin, int end, int parity, int depth) {
			this.begin = begin;
			this.end = end;
			this.parity = parity;
			this.depth = depth;
		}
		
	}	
}

