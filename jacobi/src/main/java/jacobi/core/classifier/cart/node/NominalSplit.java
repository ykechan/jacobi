package jacobi.core.classifier.cart.node;

import java.util.List;
import java.util.Optional;

import jacobi.api.classifier.cart.Column;
import jacobi.api.classifier.cart.DecisionNode;

public class NominalSplit<T> implements DecisionNode<T> {
	
	public NominalSplit(Column<?> target, T majority, List<DecisionNode<T>> children) {
		this.target = target;
		this.majority = majority;
		this.children = children;
	}

	@Override
	public Column<?> split() {
		return this.target;
	}

	@Override
	public T decide() {
		return this.majority;
	}

	@Override
	public Optional<DecisionNode<T>> decide(double value) {
		int nom = this.target.getMapping().applyAsInt(value);
		return Optional.of(this.children.get(nom));
	}

	private Column<?> target;
	private T majority;
	private List<DecisionNode<T>> children;	
}
