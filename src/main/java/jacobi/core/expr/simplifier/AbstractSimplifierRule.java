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

import jacobi.core.expr.Add;
import jacobi.core.expr.Const;
import jacobi.core.expr.Expression;
import jacobi.core.expr.Func;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Pow;
import jacobi.core.expr.Var;
import jacobi.core.expr.Visitor;

/**
 * Abstract base class of a simplifier rule.
 * 
 * @author Y.K. Chan
 *
 */
public abstract class AbstractSimplifierRule implements Visitor<Expression> {

	@Override
	public Expression visit(Add expr) {
		return expr;
	}

	@Override
	public Expression visit(Mul expr) {
		return expr;
	}

	@Override
	public Expression visit(Pow expr) {
		return expr;
	}

	@Override
	public Expression visit(Func expr) {
		return expr;
	}

	@Override
	public Expression visit(Var expr) {
		return expr;
	}

	@Override
	public <V> Expression visit(Const<V> expr) {
		return expr;
	}

}
