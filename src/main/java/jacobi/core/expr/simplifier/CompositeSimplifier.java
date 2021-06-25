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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

import jacobi.core.expr.Add;
import jacobi.core.expr.BinaryOperation;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Formula;
import jacobi.core.expr.Func;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Var;
import jacobi.core.expr.Visitor;

/**
 * Composite expression simplifier. 
 * 
 * @author Y.K. Chan
 *
 */
public class CompositeSimplifier implements Visitor<Expression> {
	
	/**
	 * Constructor
	 * @param ruleList  List of simplifier rules
	 */
	public CompositeSimplifier(List<Visitor<Expression>> ruleList) {
		this.ruleList = ruleList;
	}

	@Override
	public Expression visit(Add expr) {
		return this.simplify(expr, Add::new);
	}

	@Override
	public Expression visit(Mul expr) {
		return this.simplify(expr, Mul::new);
	}

	@Override
	public Expression visit(Pow expr) {
		return this.simplify(expr, Pow::new);
	}

	@Override
	public Expression visit(Func expr) {
		List<Expression> args = new ArrayList<>();
		int done = 0;
		
		for(Expression arg : expr.getArgs()){
			Expression simplified = arg.accept(this);
			args.add(simplified);
			
			if(simplified == arg){
				continue;
			}
			
			done++;
		}
		
		if(done < 1){
			return this.simplify(expr);
		}
		
		Func f = Func.of(expr.getName(), args);
		return this.simplify(f);
	}

	@Override
	public Expression visit(Var expr) {
		return expr;
	}

	@Override
	public <V> Expression visit(Const<V> expr) {
		return expr;
	}
	
	protected <T extends BinaryOperation> Expression simplify(T func, 
			BiFunction<Expression, Expression, T> factory) {
		Expression left = func.getLeft().accept(this);
		Expression right = func.getRight().accept(this);
		
		if(left == func.getLeft() && right == func.getRight()){
			return this.simplify(func);
		}
		
		return this.simplify(factory.apply(left, right));
	}
	
	/**
	 * Simplify an expression by applying rules iteratively. The process is stopped when 
	 * no rule is applicable.
	 * @param expr  Input expression
	 * @return  
	 */
	protected Expression simplify(Expression expr) {
		Expression result = expr;
		int max = this.ruleList.size();
		
		for(int t = 0; t < max; t++){
			int done = 0;
			for(Visitor<Expression> rule : this.ruleList){
				Expression simplified = result.accept(rule);
				if(simplified == result){
					continue;
				}
				
				System.out.println(result.accept(Formula.INST)
					+ " -> " + simplified.accept(Formula.INST)
					+ " by " + rule);
				
				result = simplified;
				done++;
			}
			
			if(done < 1){
				break;
			}
		}
		return result;
	}

	private List<Visitor<Expression>> ruleList;
}
