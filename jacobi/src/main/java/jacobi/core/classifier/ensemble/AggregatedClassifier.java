package jacobi.core.classifier.ensemble;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jacobi.api.classifier.Classifier;
import jacobi.core.util.Weighted;

public class AggregatedClassifier<T> implements Classifier<T> {
	
	public AggregatedClassifier(List<Weighted<Classifier<T>>> classifiers) {
		this.classifiers = classifiers;
	}

	@Override
	public T apply(double[] features) {
		Map<T, Double> map = new HashMap<>();
		
		for(Weighted<Classifier<T>> classifier : this.classifiers){
			T guess = classifier.item.apply(features);			
			map.put(guess, map.getOrDefault(guess, 0.0) + classifier.weight);
		}
		
		T best = null;
		double max = 0.0;
		for(Map.Entry<T, Double> entry : map.entrySet()) {
			if(best == null || entry.getValue() > max){
				best = entry.getKey();
				max = entry.getValue();
			}
		}
		
		return best;
	}

	public List<Weighted<Classifier<T>>> classifiers;
}
