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
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Func;
import jacobi.core.expr.Functional;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Var;
import jacobi.core.expr.Visitor;
import jacobi.core.expr.eval.Compiler;

/**
 * Default implementation of a comprehensive simplifier.
 * 
 * <p>This class is the connector of different simplifier rules and visitor implementations.</p>
 * 
 * <p>This class is a singleton.</p>
 * 
 * @author Y.K. Chan
 *
 */
public enum DefaultSimplifier implements Functional {
	
	/**
	 * Default instance
	 */
	INST;
	
	/**
	 * Get the default instance
	 * @return   Default instance
	 */
	public static DefaultSimplifier getInstance() {
		return INST;
	}
	
	@Override
	public Expression apply(Expression t) {
		return t.accept(this.getVisitor());
	}

	
	/**
	 * Create a potentially stateful simplifier visitor 
	 * @return  Simplifier visitor
	 */
	protected Visitor<Expression> getVisitor() {
		Visitor<String> hashFunc = new CachedVisitor<>(new HashVisitor(), new IdentityHashMap<>());
		
		Visitor<Map<String, Component>> addComp = new CachedVisitor<>(
			new AdditiveDecomposition(hashFunc), new IdentityHashMap<>()
		);
		Visitor<Map<String, Component>> mulComp = new CachedVisitor<>(
			new MultiplicativeDecomposition(hashFunc), new IdentityHashMap<>()
		);
		
		List<Visitor<Expression>> rules = new ArrayList<>();
		rules.add(IdentityReducer.DEFAULT_INSTANCE);
		rules.add(DEFAULT_EVAL_RULE);
		rules.add(new InverseReducer(hashFunc, addComp, mulComp));
		rules.add(new CommonConstantFactorRule(addComp));
		
		return new CompositeSimplifier(rules);
	}
	
	/**
	 * Default evaluator
	 */
	protected static final Function<Expression, ?> DEFAULT_EVAL = f -> 
		Compiler.getInstance().compile(f).apply(Collections.emptyList());
		
	/**
	 * Default instance for evaluation rule
	 */
	protected static final Visitor<Expression> DEFAULT_EVAL_RULE = new EvaluableRule(DEFAULT_EVAL);
}
