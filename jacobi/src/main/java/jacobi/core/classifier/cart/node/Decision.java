package jacobi.core.classifier.cart.node;

import java.util.Optional;

import jacobi.core.classifier.cart.data.Column;

/**
 * A decision node that make decision regardless of input, i.e. a leaf node in a decision tree.
 * 
 * @author Y.K. Chan
 */
public class Decision<T> implements DecisionNode<T> {
    
    /**
     * Constructor.
     * @param decision  Decision value
     */
    public Decision(T decision) {
        this.decision = decision;
    }

    @Override
    public Column<?> split() {
        return null;
    }

    @Override
    public T decide() {
        return this.decision;
    }

    @Override
    public Optional<DecisionNode<T>> decide(double value) {
        return Optional.empty();
    }

    private T decision;
}
