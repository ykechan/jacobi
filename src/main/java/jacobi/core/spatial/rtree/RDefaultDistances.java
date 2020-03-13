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

/**
 * Implementation of common distance functions for R-Trees.
 * 
 * @author Y.K. Chan
 *
 */
public enum RDefaultDistances {
	
	EUCLIDEAN_SQ {

		@Override
		protected boolean isCloser(Aabb target, double[] qp, double dist) {
			double limit = dist;
			for(int i = 0; i < qp.length; i++){
				double dx = Math.max(target.min(i) - qp[i], qp[i] - target.max(i));
				if(dx > 0.0) {
					limit -= dx * dx;
				}
				
				if(limit <= 0){
					return true;
				}
			}
			return false;
		}

		@Override
		protected double between(Aabb target, double[] qp) {
			double dist = 0.0;
			for(int i = 0; i < qp.length; i++){
				double dx = Math.max(target.min(i) - qp[i], qp[i] - target.max(i));
				if(dx > 0.0) {
					dist += dx * dx;
				}
			}
			
			return dist;
		}

		@Override
		protected boolean isCloser(double[] target, double[] qp, double dist) {
			double limit = dist;
			for(int i = 0; i < qp.length; i++){
				double dx = target[i] - qp[i];
				limit -= dx * dx;				
				
				if(limit <= 0){
					return true;
				}
			}
			
			return false;
		}

		@Override
		protected double between(double[] target, double[] qp) {
			double dist = 0.0;
			for(int i = 0; i < qp.length; i++){
				double dx = target[i] - qp[i];
				dist += dx * dx;				
			}
			
			return dist;
		}
		
	};
	
	public RDistance<Aabb> againstAabb() {
		RDefaultDistances distFn = this;
		return new RDistance<Aabb>() {
			
			@Override
			public boolean isCloser(Aabb target, double[] qp, double dist) {
				return distFn.isCloser(target, qp, dist);
			}

			@Override
			public double between(Aabb target, double[] qp) {
				return distFn.between(target, qp);
			}
			
		};
	}
	
	public RDistance<double[]> againstPoint() {
		RDefaultDistances distFn = this;
		return new RDistance<double[]>() {
			
			@Override
			public boolean isCloser(double[] target, double[] qp, double dist) {
				return distFn.isCloser(target, qp, dist);
			}

			@Override
			public double between(double[] target, double[] qp) {
				return distFn.between(target, qp);
			}
			
		};
	}
	
	/**
	 * 
	 * @param target
	 * @param qp
	 * @param dist
	 * @return
	 */
	protected abstract boolean isCloser(Aabb target, double[] qp, double dist);

	protected abstract double between(Aabb target, double[] qp);
	
	protected abstract boolean isCloser(double[] target, double[] qp, double dist);

	protected abstract double between(double[] target, double[] qp);	
	
}
