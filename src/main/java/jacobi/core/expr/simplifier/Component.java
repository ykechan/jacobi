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
package jacobi.core.expr.simplifier;

import jacobi.core.expr.Expression;

/**
 * Data class for a indecomposable expression with its multiplicity lambda.
 * 
 * @author Y.K. Chan
 *
 */
public class Component {
	
	/**
	 * Multiplicity of this component
	 */
	public final Number lambda;
	
	/**
	 * Expression of this component
	 */
	public final Expression expr;

	/**
	 * Constructor
	 * @param lambda  Multiplicity lambda
	 * @param expr  Component expression
	 */
	public Component(Number lambda, Expression expr) {
		this.lambda = lambda;
		this.expr = expr;
	}
	
	/**
	 * Get a component with multiplicity inverted
	 * @return  Component with multiplicity inverted
	 */
	public Component inverse() {
		if(this.lambda instanceof Integer){
			return new Component(-this.lambda.intValue(), expr);
		}
		
		if(this.lambda instanceof Long){
			return new Component(-this.lambda.longValue(), expr);
		}
		
		return new Component(-this.lambda.doubleValue(), expr);
	}

}
