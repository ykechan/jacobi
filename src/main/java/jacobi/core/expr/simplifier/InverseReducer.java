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

import java.util.Map;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Inv;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Neg;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Visitor;

/**
 * 
 * Reduce expression by joining equivalent components or eliminating inverses.
 * 
 * @author Y.K. Chan
 *
 */
public class InverseReducer extends AbstractSimplifierRule {
	
	/**
	 * Constructor.
	 * @param hashFunc  Hash function on expressions
	 * @param addDecomp  Additive decomposition
	 * @param mulDecomp  Multiplicative decomposition
	 */
	public InverseReducer(
			Visitor<String> hashFunc,
			Visitor<Map<String, Component>> addDecomp,
			Visitor<Map<String, Component>> mulDecomp) {
		this.hashFunc = hashFunc;
		this.addDecomp = addDecomp;
		this.mulDecomp = mulDecomp;
	}

	@Override
	public Expression visit(Add expr) {
		String hash = expr.accept(this.hashFunc);
		
		Map<String, Component> components = expr.accept(this.addDecomp);
		
		Expression result = Const.ZERO;
		for(Component component : components.values()){
			if(component.lambda.doubleValue() == 0.0){
				continue;
			}
			
			Expression item = component.lambda.doubleValue() == 1.0
				? component.expr
				: component.lambda.doubleValue() == -1.0
					? new Neg(component.expr)
					: new Mul(new Const<>(component.lambda), component.expr);
					
			result = result == Const.ZERO ? item : new Add(result, item);
		}
		
		String sign = result.accept(this.hashFunc);
		return hash.equals(sign) ? expr : result;
	}

	@Override
	public Expression visit(Mul expr) {
		String hash = expr.accept(this.hashFunc);
		
		Map<String, Component> components = expr.accept(this.mulDecomp);
		
		Expression result = Const.ONE;
		Number coeff = 1;
		for(Component component : components.values()){
			if(component.lambda.doubleValue() == 0.0){
				continue;
			}
			
			if(component.expr == Const.ONE){
				coeff = component.lambda;
				continue;
			}
			
			Expression item = component.lambda.doubleValue() == 1.0
				? component.expr
				: component.lambda.doubleValue() == -1.0
					? new Inv(component.expr)
					: new Pow(component.expr, new Const<>(component.lambda));
					
			result = result == Const.ONE ? item : new Mul(result, item);
		}
		
		if(coeff.doubleValue() != 1){
			result = coeff.doubleValue() == -1.0 
				? new Neg(result) 
				: new Mul(new Const<>(coeff), result);
		}
		
		String sign = result.accept(this.hashFunc);
		return hash.equals(sign) ? expr : result;
	}

	private Visitor<String> hashFunc;
	private Visitor<Map<String, Component>> addDecomp, mulDecomp;
}
