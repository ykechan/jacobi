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
package jacobi.api.classifier.bayes;

import jacobi.core.util.Throw;

/**
 * Data object for parameters to learn Bayesian classifier.
 * 
 * 
 * @author Y.K. Chan
 *
 */
public class BayesianClassifierParams {
	
	/**
	 * Default value for pseudocount
	 */
	public static final double DEFAULT_PSEUDO_COUNT = 1e-6;
	
	private double pseudoCount;
	
	private boolean alwaysUseLaplace;
	
	private NumericColumnsOption numericOption;
	
	private boolean likelihoodOnly;
	
	/**
	 * Constructor.
	 */
	public BayesianClassifierParams() {
		this.pseudoCount = DEFAULT_PSEUDO_COUNT;
		this.alwaysUseLaplace = false;
		this.numericOption = NumericColumnsOption.GAUSS;
		this.likelihoodOnly = false;
	}

	/**
	 * Get the value of pseudo-count for Laplace smoothing
	 * @return  Pseudo-count for Laplace smoothing
	 */
	public double getPseudoCount() {
		return pseudoCount;
	}

	/**
	 * Set the value of pseudo-count for Laplace smoothing
	 * @param pseudoCount  Pseudo-count for Laplace smoothing
	 * @return  this
	 */
	public BayesianClassifierParams setPseudoCount(double pseudoCount) {
		if(pseudoCount < 0.0){
			throw new IllegalArgumentException("Invalid pseudo-count " + pseudoCount);
		}
		
		this.pseudoCount = pseudoCount;
		return this;
	}

	/**
	 * Get the flag to always use Laplace smoothing
	 * @return  True if always use Laplace smoothing, false for using it only when necessary
	 */
	public boolean isAlwaysUseLaplace() {
		return alwaysUseLaplace;
	}

	/**
	 * Set the flag to always use Laplace smoothing
	 * @param alwaysUseLaplace  True if always use Laplace smoothing, false for using it only when necessary
	 * @return  this
	 */
	public BayesianClassifierParams setAlwaysUseLaplace(boolean alwaysUseLaplace) {
		this.alwaysUseLaplace = alwaysUseLaplace;
		return this;
	}

	/**
	 * Get the handling for numeric features
	 * @return  Handling for numeric features
	 */
	public NumericColumnsOption getNumericOption() {
		return numericOption;
	}

	/**
	 * Set the handling for numeric features
	 * @param numericOption  Handling for numeric features
	 * @return  This
	 */
	public BayesianClassifierParams setNumericOption(NumericColumnsOption numericOption) {
		Throw.when().isNull(() -> numericOption, () -> "Missing numeric option");
		
		this.numericOption = numericOption;
		return this;
	}

	/**
	 * Get the flag to classify using likelihood only, i.e. ignore prior probabilities
	 * @return  True to classify using likelihood only, false include prior probabilities
	 */
	public boolean isLikelihoodOnly() {
		return likelihoodOnly;
	}

	/**
	 * Set the flag to classify using likelihood only, i.e. ignore prior probabilities
	 * @param likelihoodOnly  True to classify using likelihood only, false include prior probabilities
	 * @return this
	 */
	public BayesianClassifierParams setLikelihoodOnly(boolean likelihoodOnly) {
		this.likelihoodOnly = likelihoodOnly;
		return this;
	}

}
