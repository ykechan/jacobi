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

import java.util.Arrays;
import java.util.function.Function;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Decomp;
import jacobi.core.solver.Substitution;
import jacobi.core.solver.Substitution.Mode;
import jacobi.core.stats.Covar;
import jacobi.core.stats.RowReduce;
import jacobi.core.util.Pair;
import jacobi.core.util.Weighted;

/**
 * Implementation of metric cluster using a Gaussian distribution.
 * 
 * <p>
 * A Gaussian distribution N(&mu;, &Sigma;) and be described by the mean vector &mu; and the
 * co-variance matrix &Sigma;. The probability density function of such a distribution is given 
 * by<br>
 * 
 * p(x) = (2&pi;)<sup>-n/2</sup> det(&Sigma;)<sup>-&frac12;</sup> exp(-&frac12; &Delta;<sup>T</sup>&Sigma;<sup>-1</sup>&Delta;),<br>
 * where n is the number of dimensions,<br>
 * 		exp is the exponential function,<br>
 *      and &Delta; = x - &mu;
 * </p>
 * 
 * <p>
 * The distance between a vector towards a cluster of Gaussian distribution should be inversely proportional
 * to the probability of x generated by the distribution. Also since the exponential function would easily 
 * underflow due to rapid growth rate, it is common to use the log of the probability instead.<br> 
 * 
 * Thus the distance can be defined as:<br>
 * 
 * d(x) = ln[1 / p(x)] = -ln[p(x)] = {nln(2&pi;) + ln[det(&Sigma;)] + &Delta;<sup>T</sup>&Sigma;<sup>-1</sup>&Delta; } / 2
 * </p>
 * 
 * <p>The expression &Delta;<sup>T</sup>&Sigma;<sup>-1</sup>&Delta; can be found by finding the inverse of
 * &Sigma;, or more efficiently, solve the system of linear equation &Sigma;z = &Delta;. Since co-variance
 * matrices are always positive-definite, the Cholesky decomposition LL<sup>T</sup> = &Sigma; and be
 * found, and solve for z by forward-backward substitution.</p>
 * 
 * <p>The cluster descriptor thus contains [ln(det), &mu;, L + L<sup>T</sup> - diag(L)]</p>
 * 
 * @author Y.K. Chan
 */
public class GaussianCluster implements ClusterMetric<Weighted<Pair>> {
	
	/**
	 * Get the default singleton instance.
	 * @return  Default instance
	 */
	public static GaussianCluster getInstance() {
		return INSTANCE;
	}
	
	/**
	 * Constructor.
	 * @param meanFunc  Mean function
	 * @param covarFunc  Co-variance function
	 */
	public GaussianCluster(Function<Matrix, double[]> meanFunc, Function<Matrix, Matrix> covarFunc) {
		this.meanFunc = meanFunc;
		this.covarFunc = covarFunc;
	}
	
	@Override
	public Weighted<Pair> expects(Matrix matrix) {
		double[] u = this.meanFunc.apply(matrix);
		Matrix covar = this.covarFunc.apply(matrix);
		
		Matrix lower = covar.ext(Decomp.class).chol()
			.orElseThrow(() -> new UnsupportedOperationException("Invalid co-variance matrix"));
		
		double lnDet = this.mirror(lower);
		return new Weighted<>(Pair.of(Matrices.wrap(u), lower), lnDet);
	}
	
	@Override
	public double distanceBetween(Weighted<Pair> cluster, double[] vector) {
		double[] u = cluster.item.getLeft().getRow(0);
		Matrix chol = cluster.item.getRight();
		
		double[] dx = new double[u.length];
		for(int i = 0; i < dx.length; i++){
			dx[i] = vector[i] - u[i];
		}
		
		double[] lx = new Substitution(Mode.FORWARD, chol).compute(Arrays.copyOf(dx, dx.length));
		double[] y = new Substitution(Mode.BACKWARD, chol).compute(lx);
		
		double dist = 0.0;
		for(int i = 0; i < dx.length; i++){
			dist += dx[i] * y[i];
		}
		
		return (cluster.weight + dx.length * LN_2PI + dist) / 2;
	}

	/**
	 * Copy the lower triangular entries to the upper triangular side, 
	 * effectively computes L + L<sup>T</sup> - diag(L)
	 * @param lower  Lower triangular matrix
	 * @return  Log of product of diag(L)
	 */
	protected double mirror(Matrix lower) {
		double lnDet = 0.0;
		for(int i = 0; i < lower.getRowCount(); i++){
			double[] row = lower.getRow(i);
			lnDet += Math.log(row[i]);
			
			for(int j = i + 1; j < row.length; j++){
				row[j] = lower.get(j, i);
			}
			lower.setRow(i, row);
		}
		return 2.0 * lnDet;
	}

	private Function<Matrix, double[]> meanFunc;
	private Function<Matrix, Matrix> covarFunc;
	
	private static final double LN_2PI = Math.log(2.0 * Math.PI);
	
	private static final GaussianCluster INSTANCE = new GaussianCluster(
		new RowReduce.Mean()::compute, new Covar()::compute
	);
}
