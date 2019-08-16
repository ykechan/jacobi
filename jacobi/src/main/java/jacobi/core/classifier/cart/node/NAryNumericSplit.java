package jacobi.core.classifier.cart.node;

import java.util.Optional;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.cart.DecisionNode;

public class NAryNumericSplit<T> implements DecisionNode<T> {

	@Override
	public Column<?> split() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T decide() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<DecisionNode<T>> decide(double value) {
		// TODO Auto-generated method stub
		return null;
	}

	private double[] bounds;
}
