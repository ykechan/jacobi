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
package jacobi.core.sym;

/**
 * Symbolic expression of the addition of two operands.
 * 
 * @author Y.K. Chan
 * 
 */
public abstract class Pow extends Dyadic {
	
	/**
	 * Factory method
	 * @param a  Left operand
	 * @param b  Right operand
	 * @return  Expression of the addition of the operands
	 */
	public static Pow of(Expr a, Expr b) {
		return new Pow() {

			@Override
			public Expr getX() {
				return a;
			}

			@Override
			public Expr getY() {
				return b;
			}
			
		};
	}

	@Override
	public final <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
}
