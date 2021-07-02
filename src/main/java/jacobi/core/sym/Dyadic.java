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

import java.util.AbstractList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Base class for symbolic expression that is a connected by a binary operator.
 * 
 * <p>A dyadic &amp; is a binary operation on two operands, A &amp; B.<p>
 * 
 * @author Y.K. Chan
 */
public abstract class Dyadic implements Expr {

	@Override
	public final List<Expr> getArgs() {
		Dyadic op = this;
		return new AbstractList<Expr>(){

			@Override
			public Expr get(int index) {
				switch(index){
					case 0 :
						return op.getX();
						
					case 1 :
						return op.getY();
						
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
	
	/**
	 * Get the left operand
	 * @return  Left operand
	 */
	public abstract Expr getX();
	
	/**
	 * Get the right operand
	 * @return  Right operand
	 */
	public abstract Expr getY();

}
