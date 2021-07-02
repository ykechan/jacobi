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

import java.util.Collections;
import java.util.List;

/**
 * Symbolic expression of a variable.
 * 
 * @author Y.K. Chan
 *
 */
public class Var implements Expr {
	
	/**
	 * Factory method
	 * @param var  Name of the variable
	 * @return  Variable expression
	 */
	public static Var of(String var) {
		if(var == null || var.trim().isEmpty()){
			throw new IllegalArgumentException("Variable name cannot be empty");
		}
		
		return new Var(var.trim());
	}
	
	/**
	 * Constructor.
	 * @param var  Name of the variable
	 */
	protected Var(String var) {
		this.var = var;
	}

	@Override
	public List<Expr> getArgs() {
		return Collections.emptyList();
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public String toString() {
		return this.var;
	}

	private String var;
}
