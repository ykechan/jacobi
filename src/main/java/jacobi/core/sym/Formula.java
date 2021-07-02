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
package jacobi.core.sym;

/**
 * Visitor of expressions that results in a formula of the expression
 * 
 * @author Y.K. Chan
 *
 */
public enum Formula implements Visitor<String> {
	
	/**
	 * Default instance
	 */
	INSTANCE;
	
	/**
	 * Get default instance
	 * @return  Default instance
	 */
	public static Formula getInstance() {
		return INSTANCE;
	}

	@Override
	public String visit(Add expr) {
		Expr b = expr.getY();
		if(b instanceof Neg){
			Expr arg = ((Neg) b).getArg();
			return this.visit(expr.getX(), arg, " - ");
		}
		return this.visit(expr.getX(), expr.getY(), " + ");
	}

	@Override
	public String visit(Mul expr) {
		return this.visit(expr.getX(), expr.getY(), " * ");
	}
	
	@Override
	public String visit(Pow expr) {
		return this.visit(expr.getX(), expr.getY(), "^");
	}

	@Override
	public String visit(Var expr) {
		return expr.toString();
	}

	@Override
	public <V> String visit(Const<V> expr) {
		return expr.toString();
	}
	
	protected String visit(Expr left, Expr right, String op) {
		return this.bracket(left) + op + this.bracket(right);
	}

	protected String bracket(Expr expr) {
		String str = expr.accept(this);
		if(expr instanceof Var || expr instanceof Const){
			return str;
		}
		
		return "(" + str + ")";
	}

}
