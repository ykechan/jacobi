package jacobi.api.classifier.cart;

import jacobi.core.classifier.cart.measure.Impurity;

public class DecisionTreeParams {
	
	public static final DecisionTreeParams DEFAULT = new DecisionTreeParams(
		Impurity.ENTROPY, Integer.MAX_VALUE
	);
	
	public final Impurity impurityMeasure;
	
	public final int maxHeight;

	public DecisionTreeParams(Impurity impurityMeasure, int maxHeight) {
		this.impurityMeasure = impurityMeasure;
		this.maxHeight = maxHeight;
	}	
	
}
