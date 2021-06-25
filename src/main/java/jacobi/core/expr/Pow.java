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

/**
 * A marker class that represents a expression raised to the power of another expression.
 * 
 * <p>In this context, power is repeated multiplication when the index is a natural number. 
 * When the index is 1/n where n is a natural number, p = pow(a, 1/n) follows pow(p, n) = a.
 * When the index is -1, it follows p = pow(a, -1) = 1 / a.
 * With product of power rule, this operation is extended to all rational index. With limits
 * this operation is extended then to all real number indices.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Pow extends BinaryOperation {

	/**
	 * Constructor.
	 * @param left  Left expression
	 * @param right  Right expression
	 */
	public Pow(Expression left, Expression right) {
		super(left, right);
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}

}
