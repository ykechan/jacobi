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
 * Decision node that split a numeric feature into two parts by a threshold value.
 * 
 * @author Y.K. Chan
 * @param <T>  Type of outcome
 */
public class BinaryNumericSplit<T> implements DecisionNode<T> {
	
	/**
	 * Constructor
	 * @param target  Target numeric feature
	 * @param threshold  Threshold value
	 * @param left  Node for group of having feature value lesser than threshold 
	 * @param right  Node for group of having feature value greater than threshold
	 */
	public BinaryNumericSplit(Column<?> target, 
			double threshold, 
			DecisionNode<T> left, DecisionNode<T> right) {
		this.target = target;
		this.threshold = threshold;
		this.left = left;
		this.right = right;
	}
	
	/**
	 * Get the threshold value of splitting
	 * @return
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * Get the left node which is for cases having the value lesser than the threshold
	 * @return  Left decision node
	 */
	public DecisionNode<T> getLeft() {
		return left;
	}

	/**
	 * Get the right node which is for cases having the value lesser than the threshold
	 * @return  Right decision node
	 */
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
