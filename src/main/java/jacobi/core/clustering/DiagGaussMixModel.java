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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.stats.RowReduce;
import jacobi.core.stats.Variance;

/**
 * Implementation of the Gaussian Mixture Model with only diagonal elements in the co-variance matrix considered.
 * 
 * <p>
 * Assume variables are independent, given a Gaussian distribution N(u, &sigma), <br> 
 * the probability of observing a vector x is given by<br>
 * p(x | N) = (1/&prod;&sigma;<sub>i</sub>&Sqrt;2&pi;) exp( -&frac12; &sum;(x<sub>i</sub> - u<sub>i</sub>/&sigma;<sub>i</sub>)<sup>2</sup> ),<br>
 * where exp is the exponential function and &sigma;<sub>i</sub> is the standard deviation in each variable.<br>
 * <br>
 * A distance function can be defined based on the probability s.t. it is further away when probability is small:<br>
 * d(x | N) = ln[p(x|N)<sup>-1</sup>] = &sum;ln(&sigma;<sub>i</sub>) + &frac12;&sum;(x<sub>i</sub> - u<sub>i</sub>/&sigma;<sub>i</sub>)<sup>2</sup><br>
 * The constant 2&pi; term is dropped since it is constant of x and N.
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class DiagGaussMixModel extends AbstractEMClustering<Matrix> {
	
	public DiagGaussMixModel(Function<Matrix, List<double[]>> initFn, long flop) {
		this(initFn.andThen(DiagGaussMixModel::unitVar), 
			DiagGaussMixModel.meanAndVar(DEFAULT_MEAN_FN, DEFAULT_VAR_FN), 
			flop
		);
	}

	protected DiagGaussMixModel(Function<Matrix, List<Matrix>> initFn,
			Function<Matrix, Matrix> statsFn,
			long flop) {
		super(initFn, flop);
		this.statsFn = statsFn;
	}

	@Override
	protected Matrix expects(Matrix matrix) {
		return this.statsFn.apply(matrix);
	}

	@Override
	protected double distanceBetween(Matrix cluster, double[] vector) {
		double[] mean = cluster.getRow(0);
		double[] var = cluster.getRow(1);
		
		double dist = 0.0;
		for(int i = 0; i < vector.length; i++){
			double dx = vector[i] - mean[i];
			dist += (Math.log(var[i]) + dx * dx / var[i]) / 2;
		}
		return dist;
	}
	
	protected static List<Matrix> unitVar(List<double[]> means) {
		List<Matrix> gmm = new ArrayList<>(means.size());
		for(int i = 0; i < means.size(); i++){
			double[] mean = means.get(i);
			double[] var = new double[mean.length];
			Arrays.fill(var, 1.0);
			gmm.add(Matrices.wrap(new double[][]{mean, var}));
		}
		return gmm;
	}
	
	protected static Function<Matrix, Matrix> meanAndVar(Function<Matrix, double[]> meanFn, Variance varFn) {
		return m -> {
			double[] mean = meanFn.apply(m);
			double[] var = varFn.compute(m, mean);
			return Matrices.wrap(new double[][]{
				mean, var
			});
		};
	}
	
	private Function<Matrix, Matrix> statsFn;
	
	private static final Function<Matrix, double[]> DEFAULT_MEAN_FN = new RowReduce.Mean()::compute;
	
	private static final Variance DEFAULT_VAR_FN = new Variance();
}
