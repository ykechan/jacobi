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
package jacobi.core.classifier.cart.node;

import java.util.Optional;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.cart.DecisionNode;

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
