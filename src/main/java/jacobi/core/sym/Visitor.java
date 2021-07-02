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
 * Common interface of visitor of a symbolic expression
 * 
 * @author Y.K. Chan
 * @param <T>  Type of visitor result
 */
public interface Visitor<T> {
	
	/**
	 * Visitor a addition expression
	 * @param expr  Variable expression
	 * @return  Visitor result
	 */
	public T visit(Add expr);
	
	/**
	 * Visitor a exponentiation expression
	 * @param expr  Constant expression
	 * @return  Visitor result
	 */
	public T visit(Mul expr);
	
	/**
	 * Visitor a multiplication expression
	 * @param expr  Constant expression
	 * @return  Visitor result
	 */
	public T visit(Pow expr);
	
	/**
	 * Visitor a variable expression
	 * @param expr  Variable expression
	 * @return  Visitor result
	 */
	public T visit(Var expr);
	
	/**
	 * Visitor a constant expression
	 * @param expr  Constant expression
	 * @return  Visitor result
	 */
	public <V> T visit(Const<V> expr);

}
