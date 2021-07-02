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

import java.util.List;
import java.util.function.BinaryOperator;

/**
 * Instruction that involves doing arithmetics on two operands.
 * 
 * @author Y.K. Chan
 *
 */
public class Arithmetic extends Instruction {
	
	/**
	 * Factory method
	 * @param offset  Memory offset
	 * @param oper  Operator, i.e. +, -, * or /. 
	 * @return  Arithmetic instruction
	 */
	public static Arithmetic of(int offset, char oper) {
		switch(oper){
			case '+':
				return new Arithmetic(offset, Arithmetic::add);
				
			case '-':
				return new Arithmetic(offset, Arithmetic::sub);
				
			case '*':
				return new Arithmetic(offset, Arithmetic::mul);
				
			case '/':
				return new Arithmetic(offset, Arithmetic::div);
		
			default:
				break;
		}
		
		throw new UnsupportedOperationException("Unknown operator " + oper);
	}
	
	protected Arithmetic(int offset, BinaryOperator<Number> op) {
		super(offset);
		this.op = op;
	}
	
	@Override
	public Object run(List<?> input, List<?> mem) {
		Object arg0 = mem.get(this.offset);
		Object arg1 = mem.get(this.offset + 1);
		
		if(arg0 instanceof Number && arg1 instanceof Number){
			Number a = (Number) arg0;
			Number b = (Number) arg1;
			return this.op.apply(a, b);
		}
		
		throw new UnsupportedOperationException("Expected number as operand, found "
			+ arg0 + ", " + arg1);
	}
	
	@Override
	public String toString() {
		Number n = this.op.apply(6, 3);
		StringBuilder buf = new StringBuilder();
		
		switch(n.intValue()){
			case 2:
				buf.append("div");
				break;
				
			case 3:
				buf.append("sub");
				break;
				
			case 9:
				buf.append("add");
				break;
				
			case 18:
				buf.append("mul");
				break;
			
			default:
				buf.append("???");
				break;
		}
		
		return buf.append(' ').append('#').append(this.offset)
				.append(' ').append('#').append(this.offset + 1)
				.toString();
	}

	private BinaryOperator<Number> op;

	/**
	 * Addition of two numbers
	 * @param a  First operand
	 * @param b  Second operand
	 * @return  Resultant
	 */
	public static Number add(Number a, Number b) {
		if(a instanceof Double || b instanceof Double){
			return a.doubleValue() + b.doubleValue();
		}
		
		if(a instanceof Long || b instanceof Long){
			return a.longValue() + b.longValue();
		}
		
		return a.intValue() + b.intValue();
	}
	
	/**
	 * Subtraction of two numbers
	 * @param a  First operand
	 * @param b  Second operand
	 * @return  Resultant
	 */
	public static Number sub(Number a, Number b) {
		if(a instanceof Double || b instanceof Double){
			return a.doubleValue() - b.doubleValue();
		}
		
		if(a instanceof Long || b instanceof Long){
			return a.longValue() - b.longValue();
		}
		
		return a.intValue() - b.intValue();
	}
	
	/**
	 * Multiplication of two numbers
	 * @param a  First operand
	 * @param b  Second operand
	 * @return  Resultant
	 */
	public static Number mul(Number a, Number b) {
		if(a instanceof Double || b instanceof Double){
			return a.doubleValue() * b.doubleValue();
		}
		
		if(a instanceof Long || b instanceof Long){
			return a.longValue() * b.longValue();
		}
		
		return a.intValue() * b.intValue();
	}
	
	/**
	 * Division of two numbers
	 * @param a  First operand
	 * @param b  Second operand
	 * @return  Resultant
	 */
	public static Number div(Number a, Number b) {
		if(a instanceof Double || b instanceof Double){
			return a.doubleValue() / b.doubleValue();
		}
		
		if(a instanceof Long || b instanceof Long){
			long p = a.longValue();
			long q = b.longValue();
			
			return p % q == 0 
				? p / q
				: a.doubleValue() / b.doubleValue();
		}
		
		int p = a.intValue();
		int q = b.intValue();
		if(p % q == 0){
			return p / q;
		}
		
		return a.doubleValue() / b.doubleValue();
	}
	
	/**
	 * Negation of a number
	 * @param n  Input number
	 * @return  Resultant
	 */
	public static Number neg(Number n) {
		if(n instanceof Double){
			return -n.doubleValue();
		}
		
		if(n instanceof Long){
			return -n.longValue();
		}
		
		return -n.intValue();
	}

}
