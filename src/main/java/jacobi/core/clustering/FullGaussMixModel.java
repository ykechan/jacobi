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
package jacobi.core.clustering;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.chol.CholeskyDecomp;
import jacobi.core.solver.Substitution;
import jacobi.core.solver.Substitution.Mode;
import jacobi.core.stats.Covar;
import jacobi.core.stats.RowReduce;
import jacobi.core.util.MapReducer;
import jacobi.core.util.Pair;

/**
 * Implementation of the Gaussian Mixture Model with full co-variance matrix.
 * 
 * <p>
 * Given a Gaussian distribution N(u, &Sigma;), the probability of observing a vector x is given by<br>
 * p(x) = (2&pi;)<sup>n/2</sup> det(&Sigma;)<sup>-&frac12;</sup>exp(-&frac12; (x - u)<sup>T</sup>&Sigma;<sup>-1</sup>(x - u) )<br>
 * <br>
 * A distance function can be constructed with observations with lower probability being further away as follows:<br>
 * d(x) = -2ln p(x) = ln det(&Sigma;) + (x - u)<sup>T</sup>&Sigma;<sup>-1</sup>(x - u)
 * </p>
 * 
 * <p>
 * The distance function contains the Mahalanobis distance &Delta;<sup>T</sup>&Sigma;<sup>-1</sup>&Delta;, where &Delta; = (x - u).<br>
 * To compute this value obtaining the inverse of &Sigma; is not actually necessary. 
 * Since &Sigma; is positive-definite, by Cholesky decomposition,<br>
 * &Sigma; = LL<sup>T</sup>, for some lower triangular matrix L. <br>
 * 
 * Let y = &Sigma;<sup>-1</sup>&Delta;, solving &Sigma;y = &Delta; &harr; LL<sup>T</sup>y = &Delta; 
 * by backward-forward substitution yields the result.
 * </p>
 * 
 * <p>
 * The cluster descriptor of GMM is a pair of matrices with left being the mean as a single row matrix, 
 * and the right being the L + L<sup>T</sup> - diag(L), which captures the Cholesky decomposition of the covariance matrix.
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class FullGaussMixModel extends AbstractEMClustering<Pair> {
	
	/**
	 * Constructor
	 * @param initFn  Initial function giving the cluster centroids
	 * @param flop  Number of FLOPs to start parallelizing
	 */
	public FullGaussMixModel(Function<Matrix, List<double[]>> initFn, long flop) {
		this(
			initFn.andThen(FullGaussMixModel::novar),
			new RowReduce.Mean()::compute,
			new Covar()::compute,
			flop
		);
	}
	
	/**
	 * Contructor
	 * @param initFn  Initialize cluster function
	 * @param meanFn  Mean function
	 * @param covarFn  Covariance function
	 * @param flop  Number of FLOPs to start parallelizing
	 */
	protected FullGaussMixModel(Function<Matrix, List<Pair>> initFn,
			Function<Matrix, double[]> meanFn,
			Function<Matrix, Matrix> covarFn, long flop) {
		super(initFn, flop);
		this.meanFn = meanFn;
		this.covarFn = covarFn;
		this.defaultEm = DEFAULT_EM_INST;
	}
	
	@Override
	protected Pair expects(Matrix matrix) {
		double[] mean = this.meanFn.apply(matrix);
		Matrix covar = this.covarFn.apply(matrix);
		
		Matrix chol = new CholeskyDecomp().compute(covar).map(this::reflect).orElse(null);
		if(chol == null){
			// fall-back to unit var matrix
		}
		
		return Pair.of(Matrices.wrap(new double[][]{mean}), chol);
	}

	@Override
	protected double distanceBetween(Pair cluster, double[] vector) {
		double[] mean = cluster.getLeft().getRow(0);
		if(cluster.getRight() == null){
			return this.defaultEm.distanceBetween(mean, vector);
		}
		
		double[] delta = new double[mean.length];
		for(int i = 0; i < delta.length; i++){
			delta[i] = vector[i] - mean[i];
		}
		
		Matrix chol = cluster.getRight();
		double[] lx = new Substitution(Mode.FORWARD, chol).compute(Arrays.copyOf(delta, delta.length));
		double[] y = new Substitution(Mode.BACKWARD, chol).compute(lx);
		
		double dist = 0.0;
		for(int i = 0; i < delta.length; i++){
			double diag = chol.get(i, i);
			dist += (y[i] * delta[i]) / 2 + Math.log(diag);
		}
		
		return dist;
	}
	
	/**
	 * Fill the upper square entries with the transpose of lower square entries
	 * @param lower  Input lower square matrix
	 * @return  Input matrix with upper square entries filled
	 */
	protected Matrix reflect(Matrix lower) {
		for(int i = 0; i < lower.getRowCount(); i++){
			double[] row = lower.getRow(i);
			for(int j = i + 1; j < row.length; j++){
				row[j] = lower.get(j, i);
			}
			lower.setRow(i, row);
		}
		return lower;
	}
	
	@Override
	protected long estimateCost(Matrix matrix, List<Pair> clusters) {
		return 2L * matrix.getColCount() * matrix.getColCount() * clusters.size();
	}

	/**
	 * Pairing a list of positions with null covariance matrix
	 * @param pos  List of positions
	 * @return  List of matrix pair as cluster descriptors
	 */
	protected static List<Pair> novar(List<double[]> pos) {
		return new AbstractList<Pair>(){

			@Override
			public Pair get(int index) {
				double[] p = pos.get(index);
				return Pair.of(Matrices.wrap(new double[][]{p}), (Matrix) null);
			}

			@Override
			public int size() {
				return pos.size();
			}
			
		};
	}

	private Function<Matrix, double[]> meanFn;
	private Function<Matrix, Matrix> covarFn;
	private AbstractEMClustering<double[]> defaultEm;
	
	protected static final AbstractEMClustering<double[]> DEFAULT_EM_INST = new SimpleKMeans(
		m -> Collections.emptyList(), MapReducer.DEFAULT_NUM_FLOP
	);
}
