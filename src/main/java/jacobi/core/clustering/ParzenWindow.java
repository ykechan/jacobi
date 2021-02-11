/*
 * The MIT License
 *
 * Copyright 2021 Y.K. Chan
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
package jacobi.core.clustering;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Parzen window functions in Mean Shift algorithm.
 * 
 * <p>
 * In this context, the Parzen window functions is the weight function W(x, y) = w(||x - y||<sup>2</sup>)
 * used in Mean-Shift algorithm m(x) = &sum;W(x, y)y<sub>i</sub>.
 * </p>
 * 
 * <p>For the process to be a legitimate gradient descent, m(x) - x = &sum;wy - x = -&nabla;K 
 * of some function K, and &sum;W = 1. The totality of w is easily fixed by normalization. 
 * However, what K actually is not always obvious.<br>
 * </p>
 * 
 * <p>
 * Let w(r) = h for some constant h where r = ||x - y||<sup>2</sup>,
 * &sum;w(r)y - x = &sum;hy - x
 * Thus &nabla;K = x - h&sum;y, 
 * 		K = x<sup>T</sup>x - x<sup>T</sup>h&sum;y<sub>i</sub> + C, where C is some constants
 * Let z<sub>i</sub> = y<sub>i</sub>h/2, C = &sum;z<sub>i</sub><sub>T</sub>z<sub>i</sub>,
 *      K = ||x - z||^2.
 * 
 * Thus for a flat Parzen window, the optima is the where the distance to all the points are at a minimum.
 * </p>
 * 
 * <p>It is common to use a Gaussain distribution instead of a flat window. Since the window needed to be
 * normalized, naturally it is done by w(x, y) = g(x, y) / &sum;g(x, y<sub>i</sub>), 
 * where g(x, y) = exp(-r), r = ||x - y||<sup>2</sup>. Integrating this function is hard without a little 
 * intuitive.<br> 
 * 
 * Consider d(lnx)/dx = 1/x, &int;w(x, y) = -ln( &sum;g(x, y<sub>i</sub>) )/2. Therefore by using a Gaussian
 * window it is the log-sum-exp function that is getting optimized.</p>
 * 
 * <p>
 * The implementations take a position vector and a list of neighbour vectors, and assign
 * weights to each of the neighbours. The weights need not to be normalized, and if it is
 * not it would be taken care of by the clustering algorithm.
 * </p>
 *
 * @see jacobi.core.clustering.MeanShift
 * @author Y.K. Chan
 *
 */
public interface ParzenWindow extends BiFunction<double[], List<double[]>, double[]> {
	
	/**
	 * Instance of a flat Parzen window
	 */
	public static ParzenWindow FLAT = (x, n) -> n.stream().mapToDouble(y -> 1.0 / n.size()).toArray();
	
	/**
	 * Factory method of a Gaussian Parzen window
	 * @param sigma  Standard deviation of the window
	 * @return  A Gaussian Parzen window
	 */
	public static ParzenWindow gauss(double sigma) {
		ClusterMetric<double[]> metric = EuclideanCluster.getInstance();
		
		double var = sigma * sigma;
		return (x, n) -> {
			double[] dists = n.stream()
				.mapToDouble(y -> metric.distanceBetween(x, y) / var).toArray();
			
			double[] weights = new double[dists.length];
			for(int i = 0; i < weights.length; i++){
				double denom = 1.0;
				double dx = dists[i];

				for(int j = 1; j < dists.length; j++){
					double dy = dists[(i + j) % dists.length];
					double dist = dx - dy;

					denom += Math.exp(dist);
				}
				
				weights[i] = 1.0 / denom;
			}
			return weights;
		};
	}
	
}
