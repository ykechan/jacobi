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

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleSupplier;

import jacobi.core.spatial.rtree.RPacker.Mbb;
import jacobi.core.util.IntStack;

/**
 * Implementation of packing a list of spatial objects into a R-Tree.
 * 
 * <p></p>
 * 
 * @author Y.K. Chan
 *
 */
public class RPacker {
	
	/**
	 * Pack a list of points into groups preserving the order
	 * @param points  List of points
	 * @param min  Minimum number of points within a group
	 * @param max  Maximum number of points within a group
	 * @param rand  Random function
	 * @return  Number of points for each group
	 */
	protected int[] pack(List<double[]> points, int min, int max, DoubleSupplier rand) {
		if(min >= max) {
			int[] groups = new int[points.size() / min];
			//Arrays.fill(groups, );
			return groups;
		}
		IntStack stack = new IntStack(points.size() / min);
		
		int done = 0;
		while(done < points.size()){
			int num = this.packFront(points.subList(done, 
					Math.min(points.size(), done + max + 1)), min, rand);
			stack.push(num);
			done += num;
		}
		
		return stack.toArray();
	}
	
	/**
	 * Group the first n points, determined by size range, rate of change of volume, and a stochastic function
	 * @param points  List of points
	 * @param min  Minimum number of points to be included
	 * @param rand  Random function
	 * @return  Value of n, i.e. the number of points to pack
	 */
	protected int packFront(List<double[]> points, int min, DoubleSupplier rand){
		if(points.size() < min){
			return Math.min(min, points.size());
		}
		
		int max = points.size();
		Mbb mbb = this.degenerate(points.get(0));
		for(int i = 1; i < points.size(); i++){
			double[] p = points.get(i);
			System.out.println("before = " + mbb);
			double dv = this.updateMbb(mbb, p);
			System.out.println("after = " + mbb + ", dv = " + dv);
			
			if(i < min) {
				continue;
			}
			
			double prob = rand.getAsDouble() * this.rejectProb(i - min, max - min);
			System.out.println("p(" + (i - min) + ", "+ (max - min) + ") = " + prob);
			if(dv < prob) {
				// reject
				return i;
			}
		}
		return max;
	}
	
	/**
	 * Update the mbb to include a given point p, and compute the measure of change in volume.
	 * @param mbb  Mbb to be updated
	 * @param p  Input point p
	 * @return  Measure of change in volume
	 */
	protected double updateMbb(Mbb mbb, double[] p) {
		if(p.length != mbb.length()) {
			
			throw new IllegalArgumentException("Dimension mismatch.");
		}
		
		double rate = 0.0;
		for(int i = 0; i < p.length; i++){
			double span = mbb.max[i] - mbb.min[i];						
			
			if(mbb.min[i] > p[i]) {
				
				rate += span / (mbb.max[i] - p[i]);
				mbb.min[i] = p[i];
			} else if(mbb.max[i] < p[i]) {
				
				rate += span / (p[i] - mbb.min[i]);
				mbb.max[i] = p[i];
			} else {
				
				rate += 1.0;
			}
		}
		return rate / p.length;
	}
	
	/**
	 * Create a degenerate mbb on a given point which has 0 volume
	 * @param p  Input point
	 * @return  A degenerate mbb
	 */
	protected Mbb degenerate(double[] p) {
		
		return new Mbb(Arrays.copyOf(p, p.length), Arrays.copyOf(p, p.length));
	}
	
	/**
	 * Compute the rejection probability according to the logistic function.
	 * @param at  Index of elements to be included
	 * @param range  Range of the number of elements allowed
	 * @return  Rejection probability
	 */
	protected double rejectProb(int at, int range) {
		double x = at - 1;
		double z = (6 * x) / range;
		return 1.0 / (1.0 + Math.exp(-z));
	}
	
	/**
	 * A mutable minimum bounding box.
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Mbb {				
		
		/**
		 * Minimum and maximum bounds
		 */
		public final double[] min, max;

		/**
		 * Constructor.
		 * @param min  Minimum bounds
		 * @param max  Maximum bounds
		 */
		public Mbb(double[] min, double[] max) {
			this.min = min;
			this.max = max;
		}
		
		/**
		 * Number of dimensions of this mbb
		 * @return  Number of dimensions
		 */
		public int length() {
			return this.min.length;
		}
		
		@Override
		public String toString() {
			return Arrays.toString(this.min) 
				+ " x " 
				+ Arrays.toString(this.max);
		}
		
	}
	
}
