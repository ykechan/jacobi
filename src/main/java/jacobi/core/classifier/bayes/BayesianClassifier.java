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

import jacobi.api.classifier.Classifier;
import jacobi.api.classifier.Column;

/**
 * Common superclass for Bayesian Classifiers.
 * 
 * <p>In this context, Bayesian Classifiers refers to classifiers that evaluates the probabilities
 * of different outcomes based on the feature vectors on each case, and thus select the most probable
 * outcome as the answer.</p>
 * 
 * <p>Due to advantages in computation and numerical stability, the evaluations are expected to be
 * log-probabilities.</p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 */
public abstract class BayesianClassifier<T> implements Classifier<T> {
	
	/**
	 * Constructor.
	 * @param outCol  Outcome column
	 */
	public BayesianClassifier(Column<T> outCol) {
		this.outCol = outCol;
	}

	@Override
	public T apply(double[] t) {
		double[] lnProbs = this.eval(t, this.outCol);
		System.out.println(Arrays.toString(t) + " -> " + Arrays.toString(lnProbs));
		int max = 0;
		for(int i = 1; i < lnProbs.length; i++){
			if(lnProbs[i] > lnProbs[max]){
				max = i;
			}
		}
		return this.outCol.valueOf(max);
	}
	
	/**
	 * Evaluate the log-probabilities of each outcome based on the input feature vector
	 * @param features  Input feature vector
	 * @param outCol  Outcome column
	 * @return  Array of probabilities for each outcome
	 */
	protected double[] eval(double[] features, Column<T> outCol) {
		double[] lnProbs = new double[outCol.cardinality()];
		for(int i = 0; i < lnProbs.length; i++){
			lnProbs[i] = this.eval(features, i);
		}
		return lnProbs;
	}
	
	/**
	 * Evaluate the log-probabilities of an outcome based on the input feature vector
	 * @param features  Input feature vector
	 * @param out  Index of outcome
	 * @return  Log-probability of the outcome
	 */
	protected abstract double eval(double[] features, int out);

	/**
	 * Outcome column
	 */
	protected final Column<T> outCol;
}
