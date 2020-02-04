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
import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * Implementation of packing a list of spatial objects into a R-Tree.
 * 
 * <p></p>
 * 
 * @author Y.K. Chan
 *
 */
public class RPacker {
	
	protected int accept(List<double[]> points, int min, int max, DoubleSupplier rand) {
		if(min == max || points.size() < min){
			return Math.min(points.size(), min);		
		}
		
		double[] first = points.get(0);
		double[] minBd = Arrays.copyOf(first, first.length);
		double[] maxBd = Arrays.copyOf(first, first.length);
		
		for(int i = 1; i < max + 1; i++){
			double dx = this.updateDelta(minBd, maxBd, points.get(i));
			if(i <= min || dx == 0.0) {
				continue;
			}
									
			double limit = rand.getAsDouble() * this.acceptProb(min, max, i);
			if(1.0 - dx > limit) {
				return i;
			}
		}
		return max;
	}	
	
	/**
	 * Update the aabb represented by min and max bounds to include a point p, and return
	 * the measure of volume increase due to the update. The measure is the arithmetic mean
	 * of the change in span in each dimension.
	 * @param minBd  Minimum bounds
	 * @param maxBd  Maximum bounds
	 * @param p  Point to be included
	 * @return  Measure of volume increase due to the update
	 */
	protected double updateDelta(double[] minBd, double[] maxBd, double[] p) {
		double dx = 0.0;
		for(int i = 0; i < p.length; i++){
			double span = maxBd[i] - minBd[i];
			if(p[i] < minBd[i]){
				dx += span / (maxBd[i] - p[i]);
				minBd[i] = p[i];
			}
			
			if(p[i] > maxBd[i]){
				dx += span / (p[i] - minBd[i]);
				maxBd[i] = p[i];
			}
		}
		return dx / p.length;
	}
	
	/**
	 * Compute the acceptance probability based on number of points included.
	 * @param min  Minimum size
	 * @param max  Maximum size
	 * @param at  Number of points included
	 * @return  An acceptance probability that is descending[]
	 */
	protected double acceptProb(int min, int max, int at) {
		int x = at - (min + max) / 2;
		double y = 6.0 * x / (max - min);
		return 1.0 / (1 + Math.exp(y));
	}
	
}
