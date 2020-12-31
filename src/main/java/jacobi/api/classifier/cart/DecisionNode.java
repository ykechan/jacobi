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
package jacobi.api.classifier.cart;

import java.util.Optional;

import jacobi.api.classifier.Classifier;
import jacobi.api.classifier.Column;

/**
 * Common interface for a decision node in CART model.
 * 
 * @author Y.K. Chan
 *
 */
public interface DecisionNode<T> extends Classifier<T> {
    
    /**
     * Get the column this node depends on when deciding
     * @return  Column this node depends on, or null if this is leaf
     */
    public Column<?> split();
    
    /**
     * Decide the outcome regardless of input
     * @return  Decision value
     */
    public T decide();
    
    /**
     * Decide the outcome given an attribute value.
     * @param value  Attribute value
     * @return  Next decision node to determine the outcome, or empty if this is leaf
     */
    public Optional<DecisionNode<T>> decide(double value);
    
    /**
     * Decide the outcome given the input vector
     * @param inst  Input vector
     * @return  Next decision node to determine the outcome, or empty if this is leaf
     */
    public default Optional<DecisionNode<T>> decide(double[] inst) {
        Column<?> col = this.split();
        return col == null ? Optional.empty() : this.decide(inst[col.getIndex()]);
    }

	@Override
	default T apply(double[] inst) {
		return this.decide(inst).map(n -> n.apply(inst)).orElse(this.decide());
	}

}
