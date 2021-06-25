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
import java.util.function.Function;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Func;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Var;

/**
 * Simplify expressions that contains no degree of freedom, i.e. a constant expression.
 * 
 * @author Y.K. Chan
 *
 */
public class EvaluableRule extends AbstractSimplifierRule {
	
	/**
	 * Constructor.
	 * @param eval  Evaluator of expression
	 */
	protected EvaluableRule(Function<Expression, ?> eval) {
		this.eval = eval;
	}

	@Override
	public Expression visit(Add expr) {
		if(this.deg(expr) < 1){
			return new Const<>(this.eval.apply(expr));
		}
		return super.visit(expr);
	}

	@Override
	public Expression visit(Mul expr) {
		if(this.deg(expr) < 1){
			return new Const<>(this.eval.apply(expr));
		}
		return super.visit(expr);
	}

	@Override
	public Expression visit(Pow expr) {
		if(this.deg(expr) < 1){
			return new Const<>(this.eval.apply(expr));
		}
		return super.visit(expr);
	}

	@Override
	public Expression visit(Func expr) {
		if(this.deg(expr) < 1){
			return new Const<>(this.eval.apply(expr));
		}
		return super.visit(expr);
	}
	
	/**
	 * Find the degree of freedom of an expression
	 * @param expr  Input expression
	 * @return  0 if no degree of freedom, > 0 otherwise.
	 */
	protected int deg(Expression expr) {
		Deque<Expression> stack = new ArrayDeque<>();
		stack.push(expr);
		
		while(!stack.isEmpty()){
			Expression node = stack.pop();
			if(node instanceof Var){
				return 1;
			}
			
			node.getArgs().forEach(stack::push);
		}
		return 0;
	}

	private Function<Expression, ?> eval;
}
