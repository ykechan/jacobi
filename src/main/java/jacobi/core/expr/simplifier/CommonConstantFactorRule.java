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
import jacobi.core.expr.Mul;
import jacobi.core.expr.Neg;
import jacobi.core.expr.Visitor;

/**
 * Simplifier rule that factors out common constant
 * 
 * @author Y.K. Chan
 *
 */
public class CommonConstantFactorRule extends AbstractSimplifierRule {
	
	/**
	 * Constructor.
	 * @param decomposer  Additive decomposition
	 */
	public CommonConstantFactorRule(Visitor<Map<String, Component>> decomposer) {
		this.decomposer = decomposer;
	}
	
	@Override
	public Expression visit(Add expr) {
		Map<String, Component> components = this.decomposer.visit(expr);
		if(components.isEmpty()){
			return super.visit(expr);
		}
		
		double factor = 0.0;
		for(Component component : components.values()){
			double lambda = component.lambda.doubleValue();
			if(lambda == 0.0){
				continue;
			}
			
			if(factor == 0.0){
				factor = Math.abs(lambda);
				continue;
			}
			
			if(factor != lambda && factor != -lambda){
				return super.visit(expr);
			}
		}
		if(factor == 1.0){
			return expr;
		}
		
		Expression sum = components.values().stream()
			.filter(c -> c.lambda.doubleValue() != 0.0)
			.map(c -> c.lambda.doubleValue() < 0 ? new Neg(c.expr) : c.expr)
			.reduce(Const.ZERO, (a, b) -> a == Const.ZERO ? b : b == Const.ZERO ? a : new Add(a, b));
		
		return this.scale(factor, sum);
	}
	
	/**
	 * Composite an expression multiplying with a scale factor
	 * @param factor  Scale factor
	 * @param expr  Input expression
	 * @return  Scaled expression
	 */
	protected Expression scale(double factor, Expression expr) {
		if(factor == 1.0){
			return expr;
		}
		
		if(factor == -1.0){
			return new Neg(expr);
		}
		
		if(factor == 0.0){
			return Const.ZERO;
		}
		
		if(factor % 1 == 0){
			if(factor > Integer.MIN_VALUE && factor < Integer.MAX_VALUE){
				int i = (int) factor;
				return new Mul(new Const<>(i), expr);
			}
			
			long v = (long) factor;
			return new Mul(new Const<>(v), expr);
		}
		return new Mul(new Const<>(factor), expr);
	}

	private Visitor<Map<String, Component>> decomposer;
}
