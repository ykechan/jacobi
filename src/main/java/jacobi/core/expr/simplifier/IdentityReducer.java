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
import jacobi.core.expr.Inv;
import jacobi.core.expr.Mul;
import jacobi.core.expr.Neg;
import jacobi.core.expr.Pow;

/**
 * Reduce addition and multiplication with additive identity and multiplicative identity respectively.
 * 
 * @author Y.K. Chan
 *
 */
public class IdentityReducer extends AbstractSimplifierRule {
	
	/**
	 * Default instance
	 */
	public static final IdentityReducer DEFAULT_INSTANCE = new IdentityReducer();
	
	/**
	 * Get the default instance
	 * @return  Default instance
	 */
	public static IdentityReducer getInstance() {
		return DEFAULT_INSTANCE;
	}

	@Override
	public Expression visit(Add expr) {
		if(expr.getLeft() == Const.ZERO){
			return expr.getRight();
		}
		
		if(expr.getRight() == Const.ZERO){
			return expr.getLeft();
		}
		
		return super.visit(expr);
	}

	@Override
	public Expression visit(Mul expr) {
		if(expr.getLeft() == Const.ONE){
			return expr.getRight();
		}
		
		if(expr.getRight() == Const.ONE){
			return expr.getLeft();
		}
		
		if(expr.getLeft() == Const.ZERO
		|| expr.getRight() == Const.ZERO){
			return Const.ZERO;
		}
		
		if(expr instanceof Neg){
			return super.visit(expr);
		}
		
		if(expr.getLeft() == Const.NEG_ONE){
			return new Neg(expr.getRight());
		}
		return super.visit(expr);
	}

	@Override
	public Expression visit(Pow expr) {
		if(expr.getRight() == Const.ZERO){
			return Const.ONE;
		}
		
		if(expr.getRight() == Const.ONE){
			return expr.getLeft();
		}
		
		if(expr instanceof Inv){
			return super.visit(expr);
		}
		
		if(expr.getRight() == Const.NEG_ONE){
			return new Inv(expr.getRight());
		}
		return super.visit(expr);
	}

	@Override
	public <V> Expression visit(Const<V> expr) {
		V val = expr.get();
		if(val instanceof Number){
			double v = ((Number) val).doubleValue();
			
			if(v == 0.0){
				return Const.ZERO;
			}
			
			if(v == 1.0){
				return Const.ONE;
			}
			
			if(v == -1.0){
				return Const.NEG_ONE;
			}
		}
		return super.visit(expr);
	}
	
	

}
