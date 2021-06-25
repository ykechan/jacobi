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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Inv;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Neg;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Visitor;

/**
 * Decompose an expression into additive components with multiplicity.
 * 
 * @author Y.K. Chan
 *
 */
public class AdditiveDecomposition extends DecomposeVisitor {

	/**
	 * Constructor.
	 * @param hashFunc  Hash function
	 */
	public AdditiveDecomposition(Visitor<String> hashFunc) {
		super(hashFunc);
	}

	@Override
	public Map<String, Component> visit(Add expr) {
		Deque<Expression> stack = new ArrayDeque<>();
		stack.push(expr);
		
		Map<String, Component> components = new TreeMap<>();
		
		while(!stack.isEmpty()){
			Expression node = stack.pop();
			if(node instanceof Add){
				node.getArgs().forEach(stack::push);
				continue;
			}
			
			Map<String, Component> newComponents = node.accept(this);
			this.join(newComponents, components);
		}
		return components;
	}

	@Override
	public Map<String, Component> visit(Mul expr) {
		if(expr instanceof Neg){
			Expression arg = ((Neg) expr).getArg();
			Map<String, Component> components = arg.accept(this);
			return this.inverse(components);
		}
		
		Component component = this.decomposeCoeff(expr);
		String key = component.expr.accept(this.hashFunc);
		return Collections.singletonMap(key, component);
	}
	
	
	
	@Override
	public Map<String, Component> visit(Pow expr) {
		if(expr instanceof Inv){
			Component component = this.decomposeCoeff((Inv) expr);
			String key = component.expr.accept(this.hashFunc);
			return Collections.singletonMap(key, component);
		}
		
		return super.visit(expr);
	}
	
	protected Component decomposeCoeff(Mul expr) {
		Number coeff = 1;
		
		List<Expression> items = new ArrayList<>();
		
		Deque<Expression> stack = new ArrayDeque<>();
		stack.push(expr);
		while(!stack.isEmpty()){
			Expression node = stack.pop();
			
			if(node instanceof Mul){
				node.getArgs().forEach(stack::push);
				continue;
			}
			
			if(node instanceof Inv){
				Component inv = this.decomposeCoeff((Inv) node);
				coeff = this.mul(coeff, inv.lambda);
				items.add(inv.expr);
				continue;
			}
			
			if(node instanceof Const){
				Const<?> c = (Const<?>) node;
				Object val = c.get();
				if(val instanceof Number){
					coeff = this.mul(coeff, (Number) val);
					continue;
				}
			}
			
			items.add(node);
		}
		
		Expression product = items.stream().reduce(Const.ONE, 
			(a, b) -> a == Const.ONE ? b : b == Const.ONE ? a : new Mul(a, b)
		);
		
		return new Component(coeff, product);
	}
	
	protected Component decomposeCoeff(Inv expr) {
		Expression arg = ((Inv) expr).getArg();
		
		Component comp = this.decomposeCoeff(new Mul(Const.ONE, arg));
		return new Component(1 / comp.lambda.doubleValue(), 
			comp.expr == Const.ONE ? Const.ONE :new Inv(comp.expr)
		);
	}

	protected Number mul(Number a, Number b) {
		if(a instanceof Double || b instanceof Double){
			return a.doubleValue() * b.doubleValue();
		}
		
		if(a instanceof Long || b instanceof Long){
			return a.longValue() * b.longValue();
		}
		
		return a.intValue() * b.intValue();
	}
	
}
