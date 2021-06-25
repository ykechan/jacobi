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
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Func;
import jacobi.core.expr.Inv;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Neg;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Var;
import jacobi.core.expr.Visitor;

/**
 * Visitor of a hashing function that maps an expression to a unique string representation.
 * 
 * <p>If the hash value of the expressions are the same, the expressions are equivalent. However
 * not all equivalent expression hash to the same hash value.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class HashVisitor implements Visitor<String> {

	@Override
	public String visit(Add expr) {
		
		List<String> tokens = new ArrayList<>();
		
		Deque<Expression> stack = new ArrayDeque<>();
		stack.push(expr);
		
		while(!stack.isEmpty()){
			Expression node = stack.pop();
			if(node instanceof Add){
				node.getArgs().forEach(stack::push);
				continue;
			}
			
			if(node instanceof Neg){
				Expression arg = node.getArgs().get(1);
				if(arg instanceof Neg){
					stack.push(arg.getArgs().get(1));
				}else if(arg instanceof Add){
					arg.getArgs().forEach(a -> stack.push(new Neg(a)));
				}else{
					String token = arg.accept(this);
					tokens.add("!" + token);
				}
				continue;
			}
			
			String token = node.accept(this);
			tokens.add(token);
		}
		return "{+:" + tokens.stream().sorted().collect(Collectors.joining(",")) + "}";
	}

	@Override
	public String visit(Mul expr) {
		List<String> tokens = new ArrayList<>();
		
		Deque<Expression> stack = new ArrayDeque<>();
		stack.push(expr);
		
		while(!stack.isEmpty()){
			Expression node = stack.pop();
			if(node instanceof Mul){
				node.getArgs().forEach(stack::push);
				continue;
			}
			
			if(node instanceof Inv){
				Expression arg = node.getArgs().get(0);
				if(arg instanceof Inv){
					stack.push(arg.getArgs().get(0));
				}else{
					String token = arg.accept(this);
					tokens.add("/" + token);
				}
				continue;
			}
			
			String token = node.accept(this);
			tokens.add(token);
		}
		return "{*:" + tokens.stream().sorted().collect(Collectors.joining(",")) + "}";
	}

	@Override
	public String visit(Pow expr) {
		
		String base = expr.getLeft().accept(this);
		String idx = expr.getRight().accept(this);
		return "{^:" + base + "," + idx + "}";
	}

	@Override
	public String visit(Func expr) {
		StringBuilder buf = new StringBuilder()
			.append('{').append(expr.getName()).append(':');
		
		int argc = 0;
		
		for(Expression arg : expr.getArgs()){
			String token = arg.accept(this);
			if(argc++ > 0){
				buf.append(',');
			}
			
			buf.append(token);
		}
		
		return buf.append('}').toString();
	}

	@Override
	public String visit(Var expr) {
		return expr.toString();
	}

	@Override
	public <V> String visit(Const<V> expr) {
		V val = expr.get();
		return val == null ? "" : val.toString();
	}

}
