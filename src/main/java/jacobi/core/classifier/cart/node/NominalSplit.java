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

import java.util.List;
import java.util.Optional;

import jacobi.api.classifier.Column;
import jacobi.api.classifier.cart.DecisionNode;

/**
 * Decision node that split a nominal feature.
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 */
public class NominalSplit<T> implements DecisionNode<T> {
	
	public NominalSplit(Column<?> target, T majority, List<DecisionNode<T>> children) {
		this.target = target;
		this.majority = majority;
		this.children = children;
	}
	
	/**
	 * Get the list of children nodes
	 * @return  List of children nodes
	 */
	public List<DecisionNode<T>> getChildren() {
		return children;
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
