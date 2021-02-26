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

import jacobi.api.classifier.Column;
import jacobi.core.util.Throw;

/**
 * Implementation of the Naive-Bayes Classifier.
 * 
 * <p>
 * Consider the Bayes Theorem,
 * p(A | B) = p(B | A) * p(A) / p(B), where, in this context, A is the event of having a certain
 * outcome, and B is the event of observing a certain feature vector. Since p(B) is constant
 * for all As, it can be ignored by the classifier.
 * </p>
 * 
 * <p>
 * The Naive-Bayes classifier is "naive" in the sense that it assumes features to be independent to each other.
 * Thus p(A | B) = p(A) * p(&cup; B<sub>i</sub> | A) = p(A) * &prod;p(B<sub>i</sub> | A).
 * 
 * However this class is "naive" that it assumes the underlying Bayesian classifiers are independent
 * to each other, instead of the features. It sums all the log-probabilities evaluated by the underlying
 * classifiers and add the prior probabilities lnp(A) to be its evaluation.
 * </p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 */
public class NaiveBayesClassifier<T> extends BayesianClassifier<T> {

	/**
	 * Constructor.
	 * @param outCol  Outcome column
	 * @param prior  Prior probabilities
	 * @param conds  Classifiers to obtain conditional probabilities
	 */
	public NaiveBayesClassifier(Column<T> outCol, double[] prior, List<BayesianClassifier<T>> conds) {
		super(outCol);
		Throw.when()
			.isNull(() -> prior, () -> "Missing prior probabilities")
			.isNull(() -> conds, () -> "Missing conditional classifiers")
			.isTrue(
				() -> outCol.cardinality() != prior.length, 
				() -> "Number of prior probabilities does not match with outcome cardinality."
			)
			.isTrue(
				() -> conds.stream().map(c -> c.outCol).anyMatch(out -> !outCol.equals(out)), 
				() -> "Found different outcome column in conditional classifiers"
			);
		
		this.prior = prior;
		this.conds = conds;
	}
	
	@Override
	protected double[] eval(double[] features, Column<T> outCol) {
		double[] lnProbs = Arrays.copyOf(this.prior, this.prior.length);
		for(BayesianClassifier<T> b : this.conds){
			double[] lnP = b.eval(features, outCol);
			for(int i = 0; i < lnProbs.length; i++){
				lnProbs[i] += lnP[i];
			}
		}
		return lnProbs;
	}

	@Override
	protected double eval(double[] features, int out) {		
		return prior[out] + this.conds.stream().mapToDouble(c -> c.eval(features, out)).sum();
	}

	private double[] prior;
	private List<BayesianClassifier<T>> conds;
}
