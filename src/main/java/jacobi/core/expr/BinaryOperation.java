/*
 * The MIT License
 *
 * Copyright 2021 Y.K. Chan
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
package jacobi.core.expr;

import java.util.AbstractList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Common abstract class for a binary operator that acts on its left and right expressions.
 * 
 * @author Y.K. Chan
 *
 */
public abstract class BinaryOperation implements Expression {

	/**
	 * Constructor
	 * @param left  Left argument
	 * @param right  Right argument
	 */
	public BinaryOperation(Expression left, Expression right) {
		this.left = left;
		this.right = right;
	}

	/**
	 * Get the left argument
	 * @return  Left argument
	 */
	public Expression getLeft() {
		return left;
	}

	/**
	 * Get the right argument
	 * @return  Right argument
	 */
	public Expression getRight() {
		return right;
	}

	@Override
	public List<Expression> getArgs() {
		BinaryOperation f = this;
		return new AbstractList<Expression>(){

			@Override
			public Expression get(int index) {
				switch(index){
					case 0:
						return f.getLeft();
						
					case 1:
						return f.getRight();
						
					default:
						break;
				}
				throw new NoSuchElementException();
			}

			@Override
			public int size() {
				return 2;
			}
			
		};
	}

	private Expression left, right;
}
