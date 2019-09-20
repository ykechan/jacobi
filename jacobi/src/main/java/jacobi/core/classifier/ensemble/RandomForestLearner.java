package jacobi.core.classifier.ensemble;

import jacobi.api.classifier.ClassifierLearner;
import jacobi.api.classifier.DataTable;
import jacobi.api.classifier.cart.DecisionNode;
import jacobi.api.classifier.ensemble.RandomForestParams;

public class RandomForestLearner<T> 
		implements ClassifierLearner<T, DecisionNode<T>, RandomForestParams> {

	@Override
	public DecisionNode<T> learn(DataTable<T> dataTab, RandomForestParams params) {
		// ...
		return null;
	}
	
}
