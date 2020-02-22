/* 
 * The MIT License
 *
 * Copyright 2019 Y.K. Chan
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
package jacobi.core.classifier.ensemble;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jacobi.api.classifier.Classifier;
import jacobi.core.util.Weighted;

/**
 * An aggregated classifier is a group of classifiers that accepts the same feature vectors 
 * and classifier to the same outcome type. Each classifier is associated with a positive 
 * importance value. The aggregated classifiers make the decision by the answer having the 
 * highest sum of importance.
 * 
 * <p>When all classifiers are of equal importance, it is equivalent to voting.</p>
 * 
 * @author Y.K. Chan
 *
 * @param <T>  Type of outcome
 * @param <C>  Type of sub-classifiers
 */
public class AggregatedClassifier<T, C extends Classifier<T>> implements Classifier<T> {
	
	/**
	 * Constructor
	 * @param classifiers  List of classifiers with weight as importance
	 */
	public AggregatedClassifier(List<Weighted<C>> classifiers) {
		this.classifiers = classifiers;
	}

	@Override
	public T apply(double[] featureVector) {
		Map<T, Double> conf = this.classify(featureVector);
		
		T ans = null;
		double max = 0.0;
		for(Map.Entry<T, Double> entry : conf.entrySet()){
			if(ans == null || entry.getValue() > max){
				ans = entry.getKey();
				max = entry.getValue();
			}
		}
		return ans;
	}
	
	/**
	 * Classify the input feature vector by each classifier. For answer given by multiple classifiers,
	 * the value is their sum of importance.
	 * @param featureVector  Input feature vector
	 * @return  Map of answers and associated importance
	 */
	public Map<T, Double> classify(double[] featureVector) {
		Map<T, Double> conf = new HashMap<>();
		for(Weighted<C> classifier : classifiers) {
			T ans = classifier.item.apply(featureVector);
			conf.put(ans, classifier.weight + conf.getOrDefault(ans, 0.0));
		}
		return conf;
	}
	
	private List<Weighted<C>> classifiers;
}
