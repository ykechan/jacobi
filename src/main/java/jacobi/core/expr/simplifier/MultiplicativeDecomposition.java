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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.TreeMap;

import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Visitor;

/**
 * Decompose an expression into multiplicative components with multiplicity.
 * 
 * @author Y.K. Chan
 *
 */
public class MultiplicativeDecomposition extends DecomposeVisitor {

	/**
	 * Constructor
	 * @param hashFunc
	 */
	public MultiplicativeDecomposition(Visitor<String> hashFunc) {
		super(hashFunc);
	}

	@Override
	public Map<String, Component> visit(Mul expr) {
		Deque<Expression> stack = new ArrayDeque<>();
		stack.push(expr);
		
		Map<String, Component> components = new TreeMap<>();
		while(!stack.isEmpty()){			
			Expression node = stack.pop();
			
			if(node instanceof Mul){
				node.getArgs().forEach(stack::push);
				continue;
			}
			
			Map<String, Component> newComponents = node.accept(this);
			this.join(newComponents, components);
		}
		return components;
	}

	@Override
	public Map<String, Component> visit(Pow expr) {
		if(expr.getRight() instanceof Const){
			
			
			Const<?> c = (Const<?>) expr.getRight();
			Object v = c.get();
			
			if(v instanceof Number){
				Number n = (Number) v;
				return this.indecomposable(n, expr.getLeft());
			}
		}
		return super.visit(expr);
	}
	
}
