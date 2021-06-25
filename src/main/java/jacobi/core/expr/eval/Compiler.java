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
package jacobi.core.expr.eval;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
 * Compiler of an expression to a set of instructions for evaluation.
 * 
 * <p>An expression can be evaluated inline but not efficient enough when it needs to be 
 * evaluated iteratively. This class compiles the expression to a linear order of instructions
 * that can be carried out without recursion or stack call.</p>
 * 
 * <p>This class is a singleton class.</p>
 * 
 * @author Y.K. Chan
 *
 */
public enum Compiler {
	
	/**
	 * Default instance
	 */
	INST;
	
	/**
	 * Get default instance
	 * @return  Default instance
	 */
	public static Compiler getInstance() {
		return INST;
	}
	
	/**
	 * Compile an expression
	 * @param expr  Input expression
	 * @return  An evaluator consists of variable list and procedure
	 */
	public Eval compile(Expression expr) {
		String[] vars = this.findVars(expr).stream().toArray(n -> new String[n]);
		
		Map<String, Integer> varMap = new TreeMap<>();
		for(int i = 0; i < vars.length; i++){
			String var = vars[i];
			varMap.put(var, i);
		}
		
		List<Instruction<?>> proc = expr.accept(new InstructionFactory(varMap, 0));
		int memLength = 1 + proc.stream().mapToInt(i -> i.offset).max().orElse(0);
		
		List<String> varList = Collections.unmodifiableList(Arrays.asList(vars));
		return new Eval(varList, Collections.unmodifiableList(proc), memLength);
	}

	/**
	 * Find all variables within an expression
	 * @param expr  Input expression
	 * @return  Set of all variables
	 */
	protected Set<String> findVars(Expression expr) {
		Deque<Expression> stack = new ArrayDeque<>();
		stack.push(expr);

		Set<String> vars = new TreeSet<>();

		while (!stack.isEmpty()) {
			Expression arg = stack.pop();

			if (arg instanceof Var) {
				vars.add(arg.toString());
				continue;
			}

			arg.getArgs().forEach(stack::push);
		}
		return vars;
	}

	/**
	 * Instruction factory the generates instruction on different expressions.
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class InstructionFactory implements Visitor<List<Instruction<?>>> {
		
		/**
		 * Constructor.
		 * @param varMap  Variable to vector index mapping
		 * @param offset  Memory offset
		 */
		public InstructionFactory(Map<String, Integer> varMap, int offset) {
			super();
			this.varMap = varMap;
			this.offset = offset;
		}

		@Override
		public List<Instruction<?>> visit(Add expr) {
			if(expr.getRight() instanceof Neg){
				Neg neg = (Neg) expr.getRight();
				Instruction<?> i = new Arithmetic(this.offset, '-');
				return this.merge(expr.getLeft(), neg.getArg(), i);
			}
			
			Instruction<?> i = new Arithmetic(this.offset, '+');
			return this.merge(expr.getLeft(), expr.getRight(), i);
		}

		@Override
		public List<Instruction<?>> visit(Mul expr) {
			if(expr.getLeft() == Const.NEG_ONE){
				List<Instruction<?>> sub = new ArrayList<>(expr.getRight().accept(this));
				sub.add(new Negate(this.offset));
				return sub;
			}
			
			if(expr.getRight() instanceof Inv){
				Inv inv = (Inv) expr.getRight();
				Instruction<?> i = new Arithmetic(this.offset, '/');
				return this.merge(expr.getLeft(), inv.getArg(), i);
			}
			
			Instruction<?> i = new Arithmetic(this.offset, '*');
			return this.merge(expr.getLeft(), expr.getRight(), i);
		}

		@Override
		public List<Instruction<?>> visit(Pow expr) {
			return this.merge(expr.getLeft(), expr.getRight(), new Power(this.offset));
		}

		@Override
		public List<Instruction<?>> visit(Func expr) {
			switch(expr.getArgs().size()){
				case 0: // constants
					break;
					
				case 1: 
					try {
						List<Instruction<?>> sub = new ArrayList<>(expr.getArgs().get(0).accept(this));
						
						UnaryFunctions f = UnaryFunctions.valueOf(expr.getName().toUpperCase());
						sub.add(f.apply(this.offset));
						
						return sub;
					}catch(IllegalArgumentException ex){
						throw new UnsupportedOperationException("Function " + expr.getName() + " not found.");
					}
			
				default:
					break;
			}
			
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Instruction<?>> visit(Var expr) {
			int target = this.varMap.getOrDefault(expr.toString(), -1);
			if(target < 0){
				throw new IllegalArgumentException("Variable " + expr.toString() + " was not discovered.");
			}
			
			return Collections.singletonList(new Load(this.offset, 0));
		}

		@Override
		public <V> List<Instruction<?>> visit(Const<V> expr) {
			return Collections.singletonList(new Move<>(this.offset, expr.get()));
		}
		
		protected List<Instruction<?>> merge(Expression left, Expression right, Instruction<?> reducer) {
			List<Instruction<?>> sub = new ArrayList<>(left.accept(this));
			
			InstructionFactory f = new InstructionFactory(this.varMap, this.offset + 1);
			sub.addAll(right.accept(f));
			
			sub.add(reducer);
			return sub;
		}
		
		private Map<String, Integer> varMap;
		private int offset;
	}
}
