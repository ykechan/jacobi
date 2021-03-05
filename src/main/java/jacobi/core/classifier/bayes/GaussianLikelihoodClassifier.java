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
package jacobi.core.classifier.bayes;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jacobi.api.Matrix;
import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.core.clustering.ClusterMetric;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.IntStack;

/**
 * Implementation of Bayesian Classifier assuming the numeric features are under a multi-variate Gaussian distribution.
 * 
 * <p>
 * The maximum likelihood estimate of a conditional probability p(A | B), i.e. having outcome A given
 * features B is the conditional probability p(B | A).
 * 
 * For numeric features, the probability can be computed if the underlying distribution is known. According
 * to the central limit theorem, independent random variables tends toward a Gaussian distribution, thus
 * makes assuming an underlying Gaussian distribution a reasonable guess.
 * </p>
 * 
 * <p>Therefore the sample mean and co-variance matrix can be computed for each outcome. This is equivalent
 * to clustering using the Gaussian Mixture Model, only that the vectors are given to be clustered by its
 * outcome.</p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 * @param <M>  Type of Gaussian model
 */
public class GaussianLikelihoodClassifier<T, M> extends BayesianClassifier<T> {
	
	/**
	 * Factory class for a multi-variate Gaussian likelihood classifier.
	 * 
	 * @author Y.K. Chan
	 * @param <T>  Type of outcome
	 * @param <M>  Type of model
	 */
	public static class Learner<T, M> 
		implements ClassifierLearner<T, GaussianLikelihoodClassifier<T, M>, Void> {
		
		/**
		 * Constructor.
		 * @param metric  Cluster metric
		 */
		public Learner(ClusterMetric<M> metric) {
			this.metric = metric;
		}

		@Override
		public GaussianLikelihoodClassifier<T, M> learn(DataTable<T> dataTab, Void params) {
			Column<T> outCol = dataTab.getOutcomeColumn();
			int[] featCols = dataTab.getColumns().stream()
				.filter(Column::isNumeric)
				.mapToInt(c -> c.getIndex()).toArray();
			
			double[] weights = dataTab.getInstances(outCol)
					.stream().mapToDouble(i -> i.weight).toArray();
			
			List<M> models = this.asClusters(dataTab, outCol).stream()
				.map(s -> this.subMatrix(dataTab, s, featCols, weights))
				.map(this.metric::expects)
				.collect(Collectors.toList());
			
			return new GaussianLikelihoodClassifier<>(outCol, featCols, models, metric);
		}
		
		/**
		 * Group instances with of different outcomes 
		 * @param dataTab  Input data table
		 * @param outCol  Outcome column
		 * @return   List of index sequences of each outcome
		 */
		protected List<int[]> asClusters(DataTable<T> dataTab, Column<T> outCol) {
			IntStack[] buckets = new IntStack[outCol.cardinality()];
			for(int i = 0; i < buckets.length; i++){
				buckets[i] = IntStack.newInstance();
			}
			
			List<Instance> insts = dataTab.getInstances(outCol);
			for(int i = 0; i < insts.size(); i++){
				Instance inst = insts.get(i);
				buckets[inst.outcome].push(i);
			}
			return Arrays.stream(buckets).map(b -> b.toArray()).collect(Collectors.toList());
		}
		
		/**
		 * Get the sub-matrix with given rows in the index sequence and projected columns
		 * @param dataTab  Input data table
		 * @param seq  Input sequence of row index
		 * @param featCols  Indices of projected feature columns
		 * @param weights  Weights of each instances
		 * @return  Sub-matrix
		 */
		protected Matrix subMatrix(DataTable<T> dataTab, int[] seq, int[] featCols, double[] weights) {
			Matrix fullMat = dataTab.getMatrix();
			return new ImmutableMatrix(){

				@Override
				public int getRowCount() {
					return seq.length;
				}

				@Override
				public int getColCount() {
					return featCols.length;
				}

				@Override
				public double[] getRow(int index) {
					double weight = weights[index];
					double[] row = fullMat.getRow(seq[index]);
					return Arrays.stream(featCols).mapToDouble(f -> weight * row[f]).toArray();
				}
						
			};
		}
		
		/**
		 * Normalize the weights to sum to N
		 * @param weights  Array of weights
		 * @return  Normalized weights
		 */
		protected double[] normalize(double[] weights) {
			double norm = Arrays.stream(weights).sum();
			
			for(int i = 0; i < weights.length; i++){				
				double w = weights[i] / norm;
				weights[i] = w * weights.length;
			}
			return weights;
		}

		private ClusterMetric<M> metric;
	}

	/**
	 * Constructor
	 * @param outCol  Outcome column
	 * @param featCols  Column indices of features
	 * @param models  List of GMM for each outcome
	 * @param metric  GMM cluster metric
	 */
	protected GaussianLikelihoodClassifier(Column<T> outCol, int[] featCols, 
			List<M> models, ClusterMetric<M> metric) {
		super(outCol);
		this.featCols = featCols;
		this.models = models;
		this.metric = metric;
	}
	
	@Override
	protected double[] eval(double[] features, Column<T> outCol) {
		double[] vector = Arrays.stream(this.featCols).mapToDouble(f -> features[f]).toArray();
		return this.models.stream()
			.mapToDouble(m -> -this.metric.distanceBetween(m, vector))
			.toArray();
	}

	@Override
	protected double eval(double[] features, int out) {
		double[] lnLikes = this.eval(features, outCol);
		return lnLikes[out];
	}

	private int[] featCols;
	private List<M> models;
	private ClusterMetric<M> metric;
}
