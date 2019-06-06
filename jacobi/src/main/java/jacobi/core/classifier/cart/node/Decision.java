package jacobi.core.classifier.cart.node;

import java.util.Optional;

/**
 * A decision node that make decision regardless of input, i.e. a leaf node in a decision tree.
 * 
 * @author Y.K. Chan
 */
public class Decision implements DecisionNode {
    
    /**
     * Constructor.
     * @param decision  Decision value
     */
    public Decision(int decision) {
        this.decision = decision;
    }

    @Override
    public int splitAt() {
        return -1;
    }

    @Override
    public int decide() {
        return this.decision;
    }

    @Override
    public Optional<DecisionNode> decide(double value) {
        return Optional.empty();
    }

    private int decision;
}
