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

import java.util.List;

/**
 * Perform arithmetic operation, i.e. addition, subtraction, multiplication and division. 
 * 
 * @author Y.K. Chan
 *
 */
public class Arithmetic extends Instruction<Number> {

	/**
	 * Constructor.
	 * @param offset  Memory offset
	 * @param op  Operator
	 */
	public Arithmetic(int offset, char op) {
		super(offset);
		this.op = op;
	}

	@Override
	public Number run(List<?> input, List<?> mem) {
		Number arg0 = this.getNumber(mem, this.offset);
		Number arg1 = this.getNumber(mem, this.offset + 1);
		
		switch(this.op){
			case '+':
				if(arg0 instanceof Integer && arg1 instanceof Integer){
					return arg0.intValue() + arg1.intValue();
				}
				
				if(arg0 instanceof Long && arg1 instanceof Long){
					return arg0.longValue() + arg1.longValue();
				}
				
				return arg0.doubleValue() + arg1.doubleValue();
				
			case '-':
				if(arg0 instanceof Integer && arg1 instanceof Integer){
					return arg0.intValue() - arg1.intValue();
				}
				
				if(arg0 instanceof Long && arg1 instanceof Long){
					return arg0.longValue() - arg1.longValue();
				}
				
				return arg0.doubleValue() - arg1.doubleValue();
				
			case '*':
				if(arg0 instanceof Integer && arg1 instanceof Integer){
					return arg0.intValue() * arg1.intValue();
				}
				
				if(arg0 instanceof Long && arg1 instanceof Long){
					return arg0.longValue() * arg1.longValue();
				}
				return arg0.doubleValue() * arg1.doubleValue();
			
			case '/':
				if(arg0 instanceof Integer 
				&& arg1 instanceof Integer
				&& arg0.intValue() % arg1.intValue() == 0){
					return arg0.intValue() / arg1.intValue();
				}
				
				if(arg0 instanceof Long 
				&& arg1 instanceof Long
				&& arg0.longValue() % arg1.longValue() == 0){
					return arg0.longValue() / arg1.longValue();
				}
				
				return arg0.doubleValue() / arg1.doubleValue();
				
			default:
				break;
		}
		
		throw new UnsupportedOperationException("Unsupported operator " + this.op);
	}
	
	protected Number getNumber(List<?> mem, int index) {
		Object val = mem.get(index);
		if(val instanceof Number){
			return (Number) val;
		}
		
		return Double.NaN;
	}
	
	@Override
	public String toString() {
		return new StringBuilder()
			.append(this.op).append("   ")
			.append(' ').append('$').append(this.offset)
			.append(' ').append('$').append(this.offset + 1)
			.toString();
	}

	private char op;
}
