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
 * A marker class that represents the additive inverse, i.e. negation.
 * 
 * <p>The additive inverse of an expression a is -1 * a. Thus this is a sub-class of multiplication,
 * with left expression the constant -1.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Neg extends Mul {

	/**
	 * Constructor
	 * @param arg  Argument of the inverse element
	 */
	public Neg(Expression arg) {
		super(Const.NEG_ONE, arg);
	}
	
	/**
	 * Get the argument of the inverse element
	 * @return  Argument of the inverse element
	 */
	public Expression getArg() {
		return this.getRight();
	}

}
