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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.core.util.Real;
import jacobi.core.util.Throw;

/**
 * Implementation of maximum likelihood classifier based on a single nominal feature.
 * 
 * <p>The maximum likelihood estimate of a conditional probability p(A | B), i.e. having outcome A given
 * features B is the conditional probability p(B | A). 
 * 
 * This class returns the value lnp(B|A) for each outcome A.</p>
 * 
 * <p>
 * By the definition of conditional probability, p(B^A) = p(B|A) * p(A)<br>
 * &there4; p(B|A) = p(B^A) * p(A)
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class NominalLikelihoodClassifier<T> extends BayesianClassifier<T> {
	
	/**
	 * Factory class for a nominal likelihood classifier.
	 * 
	 * <p>This infers the classifier with a parameter that is the laplace pseudo-count. A negative
	 * pseudo-count indicatees laplace counting is always employed, otherwise it would be used only
	 * when necessary. A value of 0 means it would never be used.</p>
	 * 
	 * @author Y.K. Chan
	 *
	 * @param <V>  Type of outcome
	 */
	public static class Learner<V>
		implements ClassifierLearner<V, NominalLikelihoodClassifier<V>, Double> {
		
		/**
		 * Constructor.
		 * @param nomCol  Nominal feature for classification
		 */
		public Learner(Column<?> nomCol) {
			Throw.when()
				.isNull(() -> nomCol, () -> "No feature column")
				.isTrue(nomCol::isNumeric, () -> "Column #" + nomCol.getIndex() + " is numeric.");
			this.nomCol = nomCol;
		}

		@Override
		public NominalLikelihoodClassifier<V> learn(DataTable<V> dataTab, Double pseudoCount) {
			Matrix conj = this.countWeights(dataTab);
			
			boolean hasZero = pseudoCount > 0.0;
			if(!hasZero && pseudoCount != 0.0){
				// detect if conditional probabilities has zero
				for(int i = 0; i < conj.getRowCount(); i++){
					hasZero = conj.getApplySet(i, r -> Arrays.stream(r).anyMatch(w -> w == 0));
					if(hasZero){
						break;
					}
				}
			}
			
			if(hasZero){
				this.laplaceSmoothing(conj, Math.abs(pseudoCount));
			}
			
			Matrix lnLikes = this.toLnLikelihood(conj);
			return new NominalLikelihoodClassifier<>(dataTab.getOutcomeColumn(), this.nomCol, lnLikes);
		}
		
		/**
		 * Count the weights of each feature against each outcome, and the total weights for each outcome
		 * @param dataTab  Input data table
		 * @return  Total weights and weights of each feature against each outcome
		 */
		protected Matrix countWeights(DataTable<V> dataTab) {
			Column<?> outCol = dataTab.getOutcomeColumn();
			Matrix conj = Matrices.zeros(this.nomCol.cardinality(), outCol.cardinality());
			
			List<Instance> insts = dataTab.getInstances(this.nomCol);
			for(Instance i : insts){
				conj.getAndSet(i.feature, r -> r[i.outcome] += i.weight);
			}
			
			return conj;
		}
		
		/**
		 * Compute log-likehoods from conjunctive weights and total weights
		 * @param conj  Conjunctive weights
		 * @return  Log-likehoods of each features against each outcome
		 */
		protected Matrix toLnLikelihood(Matrix conj) {
			double[] lnP = new double[conj.getColCount()];
			for(int i = 0; i < conj.getRowCount(); i++){
				double[] row = conj.getRow(i);
				for(int j = 0; j < lnP.length; j++){
					lnP[j] += row[j];
				}
			}
			
			for(int j = 0; j < lnP.length; j++){
				lnP[j] = Real.pseudoLn(lnP[j]);
			}
			
			for(int i = 0; i < conj.getRowCount(); i++){
				conj.getAndSet(i, r -> {
					for(int j = 0; j < r.length; j++){
						r[j] = Real.pseudoLn(r[j]) - lnP[j];
					}
				});
			}
			return conj;
		}
		
		/**
		 * Apply laplace smoothing, i.e. add pseudo-count to each entry
		 * @param conj  Conjunctive weights
		 * @param pseudoCount  Pseudo-count
		 */
		protected void laplaceSmoothing(Matrix conj, double pseudoCount) {
			if(pseudoCount == 0.0){
				return;
			}
			
			for(int i = 0; i < conj.getRowCount(); i++){
				conj.getAndSet(i, r -> {
					for(int j = 0; j < r.length; j++){
						r[j] += pseudoCount;
					}
				});
			}
		}
		
		private Column<?> nomCol;
	}

	/**
	 * Constructor.
	 * @param outCol  Outcome column
	 * @param featCol  Feature column
	 * @param lnLikes  Log-likehoods
	 */
	public NominalLikelihoodClassifier(Column<T> outCol, Column<?> featCol, Matrix lnLikes) {
		super(outCol);
		this.featCol = featCol;
		this.lnLikes = lnLikes;
	}
	
	@Override
	protected double[] eval(double[] features, Column<T> outCol) {
		int f = this.featureOf(features);
		return this.lnLikes.getRow(f);
	}

	@Override
	protected double eval(double[] features, int out) {
		int f = this.featureOf(features);
		return this.lnLikes.get(f, out);
	}
	
	/**
	 * Get the feature value from feature vector
	 * @param features  Feature vector
	 * @return  Integer feature value
	 */
	protected int featureOf(double[] features) {
		return this.featCol.getMapping().applyAsInt(features[this.featCol.getIndex()]);
	}

	private Column<?> featCol;
	private Matrix lnLikes;
}
