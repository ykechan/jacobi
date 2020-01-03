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

import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;

/**
 * An axis-aligned bounding box.
 * 
 * <p>This class specifies the minimum and maximum bound of a region for each 
 * of the dimension in a hyper-space.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Aabb {
	
	/**
	 * Wrap a point as a degenerate box. The array will be used internally.
	 * @param p  Input point
	 * @return  Degenerate aabb that minimum and maximum both equal to p.
	 */
	public static Aabb wrap(double[] p) {
		return new Aabb(p, p);
	}
	
	/**
	 * Bound a list of point clouds
	 * @param points
	 * @return
	 */
	public static Aabb of(List<double[]> points) {
		double[] first = points.get(0);
		return Aabb.wrap(first).join(new AbstractList<Aabb>() {

			@Override
			public Aabb get(int index) {
				return Aabb.wrap(points.get(1 + index));
			}

			@Override
			public int size() {
				return points.size() - 1;
			}
			
		});
	}
	
	protected Aabb(double[] minBd, double[] maxBd) {
		this.minBd = minBd;
		this.maxBd = maxBd;
	}
	
	public int dim() {
		return this.minBd.length;
	}

	public double min(int xDim) {
		return this.minBd[xDim];
	}
	
	public double max(int xDim) {
		return this.maxBd[xDim];
	}
	
	public Aabb join(List<Aabb> boxes) {
		double[] newMin = Arrays.copyOf(this.minBd, this.dim());
		double[] newMax = Arrays.copyOf(this.maxBd, this.dim());
		
		for(Aabb box : boxes) {
			if(box.dim() != this.dim()){
				throw new IllegalArgumentException("Dimension mismatch. Found "
					+ box.dim() + ", expected " + this.dim());
			}
			
			for(int i = 0; i < newMin.length; i++){
				if(box.min(i) < newMin[i]){
					newMin[i] = box.min(i);
				}
				
				if(box.max(i) < newMax[i]){
					newMax[i] = box.max(i);
				}
			}
		}
		return new Aabb(newMin, newMax);
	}

	private double[] minBd, maxBd;
}