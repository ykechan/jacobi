package jacobi.core.classifier.cart.node;

import java.util.Optional;

import jacobi.core.classifier.cart.data.Column;

public class BinaryNumericSplit<T> implements DecisionNode<T> {
	
	public BinaryNumericSplit(Column<?> target, 
			double threshold, 
			DecisionNode<T> left, DecisionNode<T> right) {
		this.target = target;
		this.threshold = threshold;
		this.left = left;
		this.right = right;
	}
	
	public double getThreshold() {
		return threshold;
	}

	public DecisionNode<T> getLeft() {
		return left;
	}

	public DecisionNode<T> getRight() {
		return right;
	}

	@Override
	public Column<?> split() {
		return this.target;
	}

	@Override
	public T decide() {
		return null;
	}

	@Override
	public Optional<DecisionNode<T>> decide(double value) {
		return Optional.of(value < this.threshold ? this.left : this.right);
	}
	
	private Column<?> target;
	private double threshold;
	private DecisionNode<T> left, right;
}
