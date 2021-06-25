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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Func;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Var;
import jacobi.core.expr.Visitor;

/**
 * Base class for a visitor that returns the components of an expression.
 * 
 * @author Y.K. Chan
 *
 */
public abstract class DecomposeVisitor implements Visitor<Map<String, Component>> {
	
	/**
	 * Constructor
	 * @param hashFunc  Hash function
	 */
	public DecomposeVisitor(Visitor<String> hashFunc) {
		this.hashFunc = hashFunc;
	}

	@Override
	public Map<String, Component> visit(Add expr) {
		return this.indecomposable(1, expr);
	}

	@Override
	public Map<String, Component> visit(Mul expr) {
		return this.indecomposable(1, expr);
	}

	@Override
	public Map<String, Component> visit(Pow expr) {
		return this.indecomposable(1, expr);
	}

	@Override
	public Map<String, Component> visit(Func expr) {
		return this.indecomposable(1, expr);
	}

	@Override
	public Map<String, Component> visit(Var expr) {
		return this.indecomposable(1, expr);
	}

	@Override
	public <V> Map<String, Component> visit(Const<V> expr) {
		V val = expr.get();
		if(val instanceof Number){
			return this.indecomposable((Number) val, Const.ONE);
		}
		
		return this.indecomposable(1, expr);
	}
	
	/**
	 * Join new components into existing components with added multiplicity
	 * @param newComponents  New components
	 * @param components  Existing components
	 * @return  Number of components reduced
	 */
	protected int join(Map<String, Component> newComponents, Map<String, Component> components) {
		int num = 0;
		
		for(Map.Entry<String, Component> entry : newComponents.entrySet()){
			String key = entry.getKey();
			Component value = entry.getValue();
			
			if(!components.containsKey(key)){
				components.put(key, value);
				continue;
			}
			
			num++;
			
			Component component = components.get(key);
			Number n = this.add(component.lambda, value.lambda);
			
			if(n.doubleValue() == 0.0){
				components.remove(key);
				continue;
			}
			
			Expression expr = component.expr;
			
			if(n.doubleValue() == 1.0){
				components.put(key, new Component(1, expr));
				continue;
			}
			
			components.put(key, new Component(n, expr));
		}
		return num;
	}
	
	protected Map<String, Component> inverse(Map<String, Component> components) {
		if(components.isEmpty()){
			return Collections.emptyMap();
		}
		
		Map<String, Component> map = new TreeMap<>();
		
		for(Map.Entry<String, Component> entry : components.entrySet()){
			String key = entry.getKey();
			Component comp = entry.getValue();
			Number lambda = comp.lambda;
			Expression expr = comp.expr;
			
			if(lambda instanceof Integer){
				map.put(key, new Component(-lambda.intValue(), expr));
				continue;
			}
			
			if(lambda instanceof Long){
				map.put(key, new Component(-lambda.longValue(), expr));
				continue;
			}
			
			map.put(key, new Component(-lambda.doubleValue(), expr));
		}
		return map;
	}
	
	/**
	 * Get a singleton component map given the expression and multiplicity
	 * @param lambda  Component multiplicity
	 * @param expr  Component expression
	 * @return  Singleton map, or empty if multiplicity is zero
	 */
	protected Map<String, Component> indecomposable(Number lambda, Expression expr){
		if(lambda.doubleValue() == 0.0){
			return Collections.emptyMap();
		}
		
		String key = expr.accept(this.hashFunc);
		Component value = new Component(lambda, expr);
		
		return Collections.singletonMap(key, value);
	}
	
	protected Number add(Number a, Number b) {
		if(a instanceof Double || b instanceof Double){
			return a.doubleValue() + b.doubleValue();
		}
		
		if(a instanceof Long || b instanceof Long){
			return a.longValue() + b.longValue();
		}
		
		return a.intValue() + b.intValue();
	}

	protected final Visitor<String> hashFunc;
}
