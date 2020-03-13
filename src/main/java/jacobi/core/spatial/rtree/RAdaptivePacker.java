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

import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * Implementation of packer for R-Objects which rejects based on current minimum bounding box. 
 * 
 * @author Y.K. Chan
 *
 */
public class RAdaptivePacker implements RPacker {
	
	/**
	 * Constructor
	 * @param rand  Random function
	 */
	public RAdaptivePacker(DoubleSupplier rand) {
		this.rand = rand;
	}

	@Override
	public int spanFront(List<? extends RObject<?>> rObjs, int minItems) {
		if(rObjs.size() <= minItems) {
			
			return Math.min(minItems, rObjs.size());
		}
		
		Mbb mbb = this.toMbb(rObjs.get(0).minBoundBox());
		int k = 0;
		for(RObject<?> rObj : rObjs){
			double delta = this.updateMbb(mbb, rObj.minBoundBox());
			int i = k++;
			if(i < minItems){
				continue;
			}
			
			double reject = this.rand.getAsDouble() * this.rejectProb(
					i - minItems, 
					rObjs.size() - minItems);
			
			if(delta < reject){
				return i;
			}
		}
		return rObjs.size();
	}
	
	/**
	 * Update the minimum bounding box to include a new aabb.
	 * @param mbb  Input mbb to be updated
	 * @param aabb  Aabb to be included
	 * @return  Measure in [0, 1) of volume increase
	 */
	protected double updateMbb(Mbb mbb, Aabb aabb) {
		if(mbb.length() != aabb.dim()) {
			throw new IllegalArgumentException("Dimension mismatch");
		}
		
		double delta = 0.0;
		for(int i = 0; i < aabb.dim(); i++){ 
			double span = mbb.max[i] - mbb.min[i];
			if(aabb.max(i) > mbb.max[i]){
				mbb.max[i] = aabb.max(i);
			}
			
			if(aabb.min(i) < mbb.min[i]){
				mbb.min[i] = aabb.min(i);
			}
			
			double width = mbb.max[i] - mbb.min[i];
			delta += width > span ? span / width : 1.0;
		}
		return delta / aabb.dim();
	}
	
	protected Mbb toMbb(Aabb aabb) {
		Mbb mbb = new Mbb(new double[aabb.dim()], new double[aabb.dim()]);
		for(int i = 0; i < aabb.dim(); i++){
			mbb.min[i] = aabb.min(i);
			mbb.max[i] = aabb.max(i);
		}
		return mbb;
	}
	
	protected double rejectProb(int num, int length) {
		double x = 4.0 * num / length;
		return 1 / ( 1 + Math.exp(-x) );
	}
	
	private DoubleSupplier rand;
	
	/**
	 * Data object for minimum bounding box.
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
		 * Constructor
		 * @param min  Minimum bounds
		 * @param max  Maximum bounds
		 */
		public Mbb(double[] min, double[] max) {
			this.min = min;
			this.max = max;
		}
		
		/**
		 * Get the number of dimensions of bound
		 * @return  Number of dimensions of bound
		 */
		public int length() {
			return this.min.length;
		}
		
	}

}
