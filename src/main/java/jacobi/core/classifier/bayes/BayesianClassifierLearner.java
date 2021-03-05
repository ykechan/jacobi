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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.Column;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.Instance;
import jacobi.api.classifier.bayes.BayesianClassifierParams;
import jacobi.core.clustering.GaussianCluster;
import jacobi.core.clustering.StandardScoreCluster;
import jacobi.core.util.Real;

/**
 * Learn a Bayesian Classifier on a dataset.
 * 
 * <p>
 * Given a feature vector x, the conditional probability of the instance belongs to y is p(y|x).<br>
 * 
 * By Bayes' Theorem, p(y|x) = p(x|y) * p(y).<br>
 * 
 * A Bayesian Classifer infers parameters on estimating of p(x|y) by training data set. The type
 * of estimation (Naive? Normal? Nominal only?) can be configured in learning parameters.<br>
 * 
 * In this context, p(y) is called prior probabilities, which depends on the statistics of y only. This
 * term can be removed by setting likelihoodOnly to true.<br>
 * </p>
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 */
public class BayesianClassifierLearner<T> 
	implements ClassifierLearner<T, BayesianClassifier<T>, BayesianClassifierParams> {

	@Override
	public BayesianClassifier<T> learn(DataTable<T> dataTab, BayesianClassifierParams params) {
		Column<T> outCol = dataTab.getOutcomeColumn();
		double[] bias = params.isLikelihoodOnly() 
			? new double[outCol.cardinality()] 
			: this.prior(dataTab);
			
		List<BayesianClassifier<T>> classifiers = new ArrayList<>();
		classifiers.addAll(this.byNominals(dataTab, params));
	
		BayesianClassifier<T> numClassifier = this.byNumerics(dataTab, params);
		if(numClassifier != null){
			classifiers.add(numClassifier);
		}
		return new NaiveBayesClassifier<>(outCol, bias, classifiers);
	}
	
	/**
	 * Obtain a list of classifiers on the nominal features
	 * @param dataTab  Input data table
	 * @param params  Learning parameters
	 * @return  List of Bayesian classifier on the nominal features
	 */
	protected List<BayesianClassifier<T>> byNominals(DataTable<T> dataTab, BayesianClassifierParams params) {
		double pseudoCount = (params.isAlwaysUseLaplace() ? -1 : 1) * params.getPseudoCount();
		
		return dataTab.getColumns().stream()
			.filter(c -> !c.isNumeric())
			.map(c -> new NominalLikelihoodClassifier.Learner<T>(c))
			.map(learner -> learner.learn(dataTab, pseudoCount))
			.collect(Collectors.toList());
	}
	
	/**
	 * Obtain a bayesian classifier based on the numeric features 
	 * @param dataTab  Input data table
	 * @param params  Learning parameters
	 * @return  Bayesian classifer on the numeric features, 
	 * 		or null if numeric features are ignored / not found.
	 */
	protected BayesianClassifier<T> byNumerics(DataTable<T> dataTab, BayesianClassifierParams params) {
		GaussianLikelihoodClassifier.Learner<T, ?> learner = null;
		if(dataTab.getColumns().stream().noneMatch(c -> c.isNumeric())){
			return null;
		}
		
		switch(params.getNumericOption()){
			case IGNORE:
				break;
				
			case GAUSS:
				learner = new GaussianLikelihoodClassifier.Learner<>(GaussianCluster.getInstance());
				break;
				
			case NAIVE:
				learner = new GaussianLikelihoodClassifier.Learner<>(StandardScoreCluster.getInstance());
				break;
				
			default:
				break;
		}
		return learner == null ? null : learner.learn(dataTab, null);
	}
	
	/**
	 * Compute the prior ln-probabilities for each outcome.
	 * @param dataTab  Input data table
	 * @return  Prior ln-probabilities
	 */
	protected double[] prior(DataTable<?> dataTab) {
		Column<?> outCol = dataTab.getOutcomeColumn();
		List<Instance> insts = dataTab.getInstances(outCol);
		
		double[] weights = new double[outCol.cardinality()];
		for(Instance i : insts){
			weights[i.outcome] += i.weight;
		}
		
		double total = Real.pseudoLn(Arrays.stream(weights).sum());
		return Arrays.stream(weights).map(w -> Real.pseudoLn(w) - total).toArray();
	}
	
}
