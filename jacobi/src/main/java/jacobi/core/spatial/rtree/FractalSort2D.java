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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.IntFunction;

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
	public int[] apply(double[][] vectors, int begin, int end, int[] result) {
		
		int[] buffer = new int[result.length];
		
		Deque<Arguments> queue = new ArrayDeque<>();
		queue.addLast(new Arguments(this.mbrOf(vectors, begin, end), begin, end, 0));
		
		while(!queue.isEmpty()){
			Arguments args = queue.removeLast();
			if(args.end - args.begin < 2) {
				continue;
			}
			
			int[] quadrants = this.groupQuadrants(vectors, args, result, buffer);
			Arguments[] argList = this.split(args, quadrants);
			for(Arguments arg : argList) {
				if(arg != null) {
					queue.addLast(arg);
				}
			}
		}
		return result;
	}
	
	protected Arguments[] split(Arguments args, int[] parts) {
		int parity = args.aabb.parity;
		int[] mapping = new int[4];
		mapping[(parity / Fractal2D.LL) % 4] = 0;
		mapping[(parity / Fractal2D.LU) % 4] = 1;
		mapping[(parity / Fractal2D.UL) % 4] = 2;
		mapping[(parity / Fractal2D.UU) % 4] = 3;
		
		Aabb2[] aabbs = this.partition(args.aabb);
		if(aabbs.length != mapping.length || parts.length != mapping.length){
			throw new UnsupportedOperationException();
		}				
		
		int k = args.begin;
		Arguments[] argList = new Arguments[parts.length];
		for(int i = 0; i < argList.length; i++){
			argList[i] = parts[i] < 2
					? null : new Arguments(aabbs[mapping[i]], k, k + parts[i], args.depth + 1);
			k += parts[i];
		}
		
		if(k != args.end){
			throw new UnsupportedOperationException("Number of elements mismatch. "
				+ "Expected [" + args.begin + "," + args.end +"), "
				+ "found [" + args.begin + "," + k + ").");
		}
		return argList;
	}
	
	/**
	 * Group the result array according to the order of the 4 quadrants, given by the encoded
	 * parity as the order
	 * @param vectors  Input spatial vectors
	 * @param args  Arguments of the current region
	 * @param result  Array to be grouped
	 * @param buffer  Array as temp buffer
	 * @return
	 */
	protected int[] groupQuadrants(double[][] vectors, Arguments args, int[] result, int[] buffer) {
		double xMid = args.aabb.xMid();
		double yMid = args.aabb.yMid();

		int begin = args.begin;
		int end = args.end;
		
		int head = begin; 
		int tail = end;
		int low = begin;
		int high = end;
		
		int i = begin;
		while(i < tail){
			double[] pos = vectors[result[i]];	
			int xUpper = pos[xIdx] > xMid ? 16 : 1;
			int yUpper = pos[yIdx] > yMid ? 4 : 1;			
			switch((args.aabb.parity / xUpper / yUpper) % 4) {
				case 0 :
					result[head++] = result[i++];
					break;
					
				case 1 :
					buffer[low++] = result[i++];
					break;
					
				case 2 :
					buffer[--high] = result[i++];
					break;
					
				case 3 : {
					tail = tail - 1;
					int tmp = result[tail]; 
					result[tail] = result[i]; 
					result[i] = tmp; 
					}
					break;
					
				default :
					throw new UnsupportedOperationException();
			}			
		}
		
		if(low > begin) {
			System.arraycopy(buffer, begin, result, head, low - begin);
		}
		
		if(high < end){			
			System.arraycopy(buffer, high, result, tail - (end - high), end - high);
		}
		return new int[] { head - begin, low - begin, end - high, end - tail };
	}
	
	/**
	 * Partition a region defined by an aabb into 4 quadrants
	 * @param aabb  Input region
	 * @return  Array of aabbs for the 4 quadrants in the order of 
	 *          (lower, lower), (lower, upper), (upper, lower) and (upper, upper). 
	 */
	protected Aabb2[] partition(Aabb2 aabb) {
		double xMid = aabb.xMid();
		double yMid = aabb.yMid();
		
		int[] curve = this.fnFrac.apply(aabb.parity);
		return new Aabb2[] {
			new Aabb2(aabb.xMin, aabb.yMin, xMid, yMid, curve[0]), // LL
			new Aabb2(aabb.xMin, yMid, xMid, aabb.yMax, curve[1]), // LU
			new Aabb2(xMid, aabb.yMin, aabb.xMax, yMid, curve[2]), // UL
			new Aabb2(xMid, yMid, aabb.xMax, aabb.yMax, curve[3])  // UU
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
	
	private int xIdx, yIdx;
	private IntFunction<int[]> fnFrac;
	private Params params;
	
	public static class Params {
		
		public double epsilon;
		
		public double maxDepth;
		
		public double minEntropy;

		public Params(double epsilon, double maxDepth, double minEntropy) {
			this.epsilon = epsilon;
			this.maxDepth = maxDepth;
			this.minEntropy = minEntropy;
		}
		
	}
	
	/**
	 * Data class for arguments in an iteration
	 * @author Y.K. Chan
	 *
	 */
	protected static class Arguments {
		
		/**
		 * Aabb to define region of interest
		 */
		public final Aabb2 aabb;
		
		/**
		 * Begin and end index of interest
		 */
		public final int begin, end;
		
		/**
		 * Depth of current iteration
		 */
		public final int depth;

		/**
		 * Constructor.
		 * @param aabb  Region defined by an aabb.
		 * @param begin  Begin index of interest
		 * @param end  End index of interest
		 * @param depth  Depth of current iteration
		 */
		public Arguments(Aabb2 aabb, int begin, int end, int depth) {
			this.aabb = aabb;
			this.begin = begin;
			this.end = end;
			this.depth = depth;
		}
		
	}
	
	/**
	 * An axis-aligned bounding box in 2-D space
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Aabb2 {
		
		/**
		 * Minimum and maximum bound in x and y dimensions
		 */
		public final double xMin, yMin, xMax, yMax;
		
		/**
		 * Parity information of the fractal in current region
		 */
		public final int parity;

		public Aabb2(double xMin, double yMin, double xMax, double yMax, int parity) {
			this.xMin = xMin;
			this.yMin = yMin;
			this.xMax = xMax;
			this.yMax = yMax;
			this.parity = parity;
		}
		
		/**
		 * Mid value in x-dimension
		 * @return  Mid value in x-dimension
		 */
		public double xMid() {
			return (this.xMin + this.xMax) / 2.0;
		}
		
		/**
		 * Mid value in y-dimension
		 * @return  Mid value in y-dimension
		 */
		public double yMid() {
			return (this.yMin + this.yMax) / 2.0;
		}
		
		/**
		 * Get the area of the aabb.
		 */
		public double area() {
			return (this.xMax - this.xMin) * (this.yMax - this.xMin);
		}
		
	}	

}
