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
package jacobi.api.unsupervised;

import java.util.List;

import jacobi.api.Matrix;
import jacobi.core.clustering.MeanShift;
import jacobi.core.clustering.ParzenWindow;

/**
 * Clustering using the mean-shift algorithms.
 * 
 * <p>This is an API class to conveniently invoke the Mean-Shift algorithm with different parameters.</p>
 * 
 * <p>
 * For example, when using a flat kernel window, the window size and tolerance is directly supplied.
 * When a Gaussian kernel is used, this class provides invoking by speaking in probability theory terms, 
 * supplying the standard deviation and specify the tolerance by significance level. This would provide
 * theoretical background on the basis of choosing those particular value, making the decision less
 * arbitrary.
 * 
 * In this context, the statistics test is simplied s.t. the significance level is value of the 
 * cumulative distribution function.
 * </p>
 * 
 * <p>
 * This amounts to finding the value of the Gauss Error function 
 * erf(z) = 2/&Sqrt;&pi;&int;<sub>0</sub><sup>z</sup>exp(-t<sup>2</sup>)dt.
 * 
 * An approximation by Abramowitz and Stegun is employed here:<br>
 * 
 * erf(z) ~ 1 - (&sum;a<sub>i</sub>t<sup>i</sup>)exp(-z<sup>2</sup>),
 * where t = 1 / 1 + pz,
 *       p = 0.3275911,
 *       a1 = 0.254829592,
 *       a2 = −0.284496736,
 *       a3 = 1.421413741,
 *       a4 = −1.453152027,
 *       a5 = 1.061405429
 *       
 * @see <a href="https://en.wikipedia.org/wiki/Error_function#Approximation_with_elementary_functions">Error function</a>
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class MeanShifts {
	
	/**
	 * Default significant level of collapsing nearby points for Gaussian window
	 */
	public static final double DEFAULT_SIGNIFICANT_LEVEL = 0.15;
	
	/**
	 * Proxy class for extension facade
	 * @author Y.K. Chan
	 *
	 */
	public static class Proxy {
		
		/**
		 * Factory method for mean-shifts interface
		 * @param matrix  Input data matrix
		 * @return  Mean shifts
		 */
		public MeanShifts of(Matrix matrix) {
			return new MeanShifts(matrix, DEFAULT_MIN_SIMPLEX, DEFAULT_NUM_SIGMA);
		}
		
	}
	
	/**
	 * Constructor
	 * @param matrix  Data matrix
	 * @param minPts  Minimum points to be considered forming a cluster
	 * @param numSig  Number of std. dev. for the query for Gaussian window
	 */
	public MeanShifts(Matrix matrix, int minPts, double numSig) {
		this.matrix = matrix;
		this.minPts = minPts;
		this.numSig = numSig;
	}
	
	/**
	 * Set the minimum points to be considered forming a cluster
	 * @param minPts  Minimum points to be considered forming a cluster
	 * @return  This
	 */
	public MeanShifts setMinPts(int minPts) {
		this.minPoints(1, minPts);
		this.minPts = minPts;
		return this;
	}

	/**
	 * Set the number of standard deviation to cutoff for a Gaussian parzen window
	 * @param numSig  Number of standard deviation 
	 */
	public MeanShifts setNumSig(double numSig) {
		if(numSig < 0.0){
			throw new IllegalArgumentException("Invalid number of std. dev. " + numSig);
		}
		
		this.numSig = numSig;
		return this;
	}

	/**
	 * Perform mean shift algorithm using a flat parzen window
	 * @param windowRadius  Radius of the window
	 * @param epsilon  Epsilon distance to collapse nearby points
	 * @return  Index sequences for each clusters
	 */
	public List<int[]> flat(double windowRadius, double epsilon) {
		int kMin = this.minPoints(this.matrix.getColCount(), this.minPts);
		MeanShift ms = new MeanShift(ParzenWindow.FLAT, windowRadius, epsilon, kMin);
		return ms.compute(this.matrix);
	}
	
	/**
	 * Perform mean shift using a Gaussian parzen window
	 * @param sigma  Standard deviation of the window
	 * @param sigLv  Significance level for collapsing nearby points
	 * @return  Index sequences for each clusters
	 */
	public List<int[]> gauss(double sigma, double sigLv) {
		double qDist = this.numSig * sigma;
		
		ParzenWindow win = ParzenWindow.gauss(sigma);
		double eps = 2 * this.erf(sigma, sigLv);
		
		int kMin = this.minPoints(this.matrix.getColCount(), this.minPts);
		
		MeanShift ms = new MeanShift(win, qDist, eps, kMin);
		return ms.compute(matrix);
	}
	
	/**
	 * Perform mean shift using a Gaussian parzen window
	 * @param sigma  Standard deviation of the window
	 * @return  Index sequences for each clusters
	 */
	public List<int[]> gauss(double sigma) {
		return this.gauss(sigma, DEFAULT_SIGNIFICANT_LEVEL);
	}
	
	/**
	 * Approximation value of the Error function.
	 * @param sigma  Standard deviation of the distribution
	 * @param z  Standard score
	 * @return  Approx. value of erf(z)
	 */
	protected double erf(double sigma, double z) {
		double t = 1 / (1 + APPROX_COEFF_P * z);
		
		double y = APPROX_COEFF_A5;
		y = t * y + APPROX_COEFF_A4;
		y = t * y + APPROX_COEFF_A3;
		y = t * y + APPROX_COEFF_A2;
		y = t * y + APPROX_COEFF_A1;
		y = t * y;
		
		return sigma * (1.0 - y * Math.exp(-z * z));
	}
	
	/**
	 * Get the minimum number of points to form a cluster
	 * @param numDim  Number of dimension
	 * @param minPts  Minimum number of points, negative for special formula
	 * @return  Minimum number of points to form a cluster
	 */
	protected int minPoints(int numDim, int minPts) {
		if(minPts >= 0){
			return minPts;
		}
		
		switch(minPts){
			case DEFAULT_MIN_SIMPLEX :
				return 1 + numDim;
				
			default:
				break;
		}
		
		throw new IllegalArgumentException("Invalid minimum cluster population " + minPts);
	}

	private Matrix matrix;
	private int minPts;
	private double numSig;
	
	/**
	 * Use the minimum number to form a simplex as minimum points to be considered forming a cluster
	 */
	protected static final int DEFAULT_MIN_SIMPLEX = -1;
	
	/**
	 * Default number of sigma to cutoff
	 */
	protected static final double DEFAULT_NUM_SIGMA = 3;
	
	/**
	 * Coefficient for approximating erf(z), refers to above
	 */
	protected static final double APPROX_COEFF_P = 0.3275911;
	
	/**
	 * Coefficient for approximating erf(z), refers to above
	 */
	protected static final double APPROX_COEFF_A1 = 0.254829592;
	
	/**
	 * Coefficient for approximating erf(z), refers to above
	 */
	protected static final double APPROX_COEFF_A2 = -0.284496736;
	
	/**
	 * Coefficient for approximating erf(z), refers to above
	 */
	protected static final double APPROX_COEFF_A3 = 1.421413741;
	
	/**
	 * Coefficient for approximating erf(z), refers to above
	 */
	protected static final double APPROX_COEFF_A4 = -1.453152027;
	
	/**
	 * Coefficient for approximating erf(z), refers to above
	 */
	protected static final double APPROX_COEFF_A5 = 1.061405429;
}
