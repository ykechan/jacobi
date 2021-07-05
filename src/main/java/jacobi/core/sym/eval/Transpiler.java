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
package jacobi.core.sym.eval;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import jacobi.core.sym.Add;
import jacobi.core.sym.Const;
import jacobi.core.sym.Dyadic;
import jacobi.core.sym.Expr;
import jacobi.core.sym.Inv;
import jacobi.core.sym.Mul;
import jacobi.core.sym.Neg;
import jacobi.core.sym.Pow;
import jacobi.core.sym.Var;
import jacobi.core.sym.Visitor;

public class Transpiler {
	
	protected static final Transpiler DEFAULT_INSTANCE = new Transpiler();
	
	public static Transpiler getInstance() {
		return DEFAULT_INSTANCE;
	}
	
	public Procedure compile(Expr expr) {
		Set<String> vars = this.discover(expr);
		
		Map<String, Integer> varMap = new TreeMap<>();
		int i = 0;
		for(String v : vars){
			varMap.put(v, i++);
		}
		
		Visitor<List<Instruction>> compiler = this.getCompiler(0, varMap);
		List<Instruction> procedure = expr.accept(compiler);
		
		int mem = procedure.stream().mapToInt(j -> j.offset).max().orElse(0);
		List<String> varList = vars.stream().collect(Collectors.toList());
		return new Procedure(
			Collections.unmodifiableList(varList),
			Collections.unmodifiableList(procedure), 
			mem);
	}
	
	protected Set<String> discover(Expr expr) {
		Set<String> vars = new TreeSet<>();
		
		Deque<Expr> stack = new ArrayDeque<>();
		stack.push(expr);
		
		while(!stack.isEmpty()){
			Expr node = stack.pop();
			if(node instanceof Var){
				vars.add(node.toString());
				continue;
			}
			
			node.getArgs().forEach(stack::push);
		}

		return vars;
	}
	
	protected Visitor<List<Instruction>> getCompiler(int offset, Map<String, Integer> varMap) {
		Transpiler self = this;
		return new Visitor<List<Instruction>>() {

			@Override
			public List<Instruction> visit(Add expr) {
				if(expr.getY() instanceof Neg){
					Neg neg = (Neg) expr.getY();
					Instruction i = Arithmetic.of(offset, '-');
					return this.visit(expr.getX(), neg.getArg(), i);
				}
				
				Instruction i = Arithmetic.of(offset, '+');
				return this.visit(expr.getX(), expr.getY(), i);
			}

			@Override
			public List<Instruction> visit(Mul expr) {
				if(expr.getY() instanceof Inv){
					Instruction i = Arithmetic.of(offset, '/');
					Inv inv = (Inv) expr.getY();
					return this.visit(expr.getX(), inv.getArg(), i);
				}
				
				Instruction i = Arithmetic.of(offset, '*');
				return this.visit(expr.getX(), expr.getY(), i);
			}

			@Override
			public List<Instruction> visit(Pow expr) {
				
				Instruction i = new Power(offset);
				return this.visit(expr.getX(), expr.getY(), i);
			}

			@Override
			public List<Instruction> visit(Var expr) {
				String var = expr.toString();
				int t = varMap.getOrDefault(var, -1);
				if(t < 0){
					throw new UnsupportedOperationException("Variable " + var + " not discovered");
				}
				
				Instruction i = Swap.move(offset, t);
				return Collections.singletonList(i);
			}

			@Override
			public <V> List<Instruction> visit(Const<V> expr) {
				V val = expr.get();
				Instruction i = Swap.load(offset, val);
				return Collections.singletonList(i);
			}
			
			protected List<Instruction> visit(Expr x, Expr y, Instruction join) {
				Visitor<List<Instruction>> visitor = self.getCompiler(offset + 1, varMap);
				List<Instruction> commands = y.accept(visitor);
				
				List<Instruction> sub = x.accept(this);
				if(sub.size() < 2){
					sub = new ArrayList<>(sub);
				}
				
				sub.addAll(commands);
				sub.add(join);
				return sub;
			}
		};
	}
	
}
