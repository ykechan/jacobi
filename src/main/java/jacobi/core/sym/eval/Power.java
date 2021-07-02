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

public class Power extends Instruction {

	public Power(int offset) {
		super(offset);
	}

	@Override
	public Object run(List<?> input, List<?> mem) {
		Object arg0 = mem.get(this.offset);
		Object arg1 = mem.get(this.offset + 1);
		
		if(arg0 instanceof Number && arg1 instanceof Number){
			Number a = (Number) arg0;
			Number b = (Number) arg1;
			return this.pow(a, b);
		}
		
		throw new UnsupportedOperationException("Expected number as operand, found "
			+ arg0 + ", " + arg1);
	}
	
	protected Number pow(Number x, Number n) {
		if(n.doubleValue() == 0.0){
			return 1;
		}
		
		if(n.doubleValue() == 1.0){
			return x;
		}
		
		if(n.doubleValue() % 1 == 0.0){
			int index = n.intValue();
			if(x instanceof Double){
				return this.powDouble(x.doubleValue(), index);
			}
			
			if(x instanceof Long){
				return this.powLong(x.longValue(), index);
			}
			
			return this.powInt(x.intValue(), index);
		}
		
		if(n.doubleValue() == 0.5){
			double r = Math.sqrt(x.doubleValue());
			
			if(r % 1 == 0.0 && r < Integer.MAX_VALUE){
				return (int) r;
 			}
			
			if(r % 1 == 0.0){
				return (long) r;
			}
			
			return r;
		}
		
		return Math.pow(x.doubleValue(), n.doubleValue());
	}
	
	protected int powInt(int x, int n) {
		switch(n){
			case 0:
				return 1;
				
			case 1:
				return x;
				
			case 2:
				return x * x;
				
			case 3:
				return x * x * x;
				
			case 4: {
					int tmp = x * x;
					return tmp * tmp;
				}
		
			default:
				break;
		}
		
		int m = 1;
		int t = x;
		
		while(2 * m < n){
			t *= t;
			m *= 2;
		}
		return m < n ? t * this.powInt(x, n - m) : t;
	}
	
	protected long powLong(long x, int n) {
		switch(n){
			case 0:
				return 1;
				
			case 1:
				return x;
				
			case 2:
				return x * x;
				
			case 3:
				return x * x * x;
				
			case 4: {
					long tmp = x * x;
					return tmp * tmp;
				}
		
			default:
				break;
		}
		
		int m = 1;
		long t = x;
		
		while(2 * m < n){
			t *= t;
			m *= 2;
		}
		return m < n ? t * this.powLong(x, n - m) : t;
	}
	
	protected double powDouble(double x, int n) {
		switch(n){
			case 0:
				return 1;
				
			case 1:
				return x;
				
			case 2:
				return x * x;
				
			case 3:
				return x * x * x;
				
			case 4: {
					double tmp = x * x;
					return tmp * tmp;
				}
		
			default:
				break;
		}
		
		int m = 1;
		double t = x;
		
		while(2 * m < n){
			t *= t;
			m *= 2;
		}
		return m < n ? t * this.powDouble(x, n - m) : t;
	}

}
