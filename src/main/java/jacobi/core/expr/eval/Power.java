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
 * Perform power operation.
 * 
 * @author Y.K. Chan
 *
 */
public class Power extends Instruction<Number> {

	/**
	 * Constructor
	 * @param offset  Memory offset
	 */
	public Power(int offset) {
		super(offset);
	}

	@Override
	public Number run(List<?> input, List<?> mem) {
		Number base = this.getNumber(mem, this.offset);
		Number index = this.getNumber(mem, this.offset + 1);
		
		if(index instanceof Integer || index instanceof Long){
			return this.power(base, index.intValue());
		}
		
		if(index.doubleValue() < 0.0){
			double nBase = 1 / base.doubleValue();
			double nIdx = Math.abs(index.doubleValue());
			
			return Math.pow(nBase, nIdx);
		}
		
		return Math.pow(base.doubleValue(), index.doubleValue());
	}
	
	protected Number getNumber(List<?> mem, int index) {
		try {
			return (Number) mem.get(index);
		} catch(ClassCastException ex) {
			return Double.NaN;
		}
	}
	
	protected Number power(Number base, int index) {
		if(index < 0){
			return this.power(1 / base.doubleValue(), -index);
		}
		
		if(base instanceof Integer && this.isInt(base.intValue(), index)){
			return this.powerInt(base.intValue(), index);
		}
		
		return this.powerLong(base.longValue(), index);
	}
	
	protected int powerInt(int base, int index) {
		if(index < 0){
			throw new IllegalArgumentException();
		}
		
		switch(index){
			case 0 :
				return 1;
				
			case 1 :
				return base;
				
			case 2 :
				return base * base;
				
			case 3 :
				return base * base * base;
				
			default:
				break;
		}
		
		int ans = base;
		int i = 1;
		
		while(2 * i < index){
			ans *= ans;
			i *= 2;
		}
		
		return i == index ? ans : ans * this.powerInt(base, index - i);
	}
	
	protected long powerLong(long base, int index) {
		if(index < 0){
			throw new IllegalArgumentException();
		}
		
		switch(index){
			case 0 :
				return 1;
				
			case 1 :
				return base;
				
			case 2 :
				return base * base;
				
			case 3 :
				return base * base * base;
				
			default:
				break;
		}
		
		long ans = base;
		int i = 1;
		
		while(2 * i < index){
			ans *= ans;
			i *= 2;
		}
		
		return i == index ? ans : ans * this.powerLong(base, index - i);
	}
	
	protected double powerDouble(double base, int index) {
		if(index < 0){
			throw new IllegalArgumentException();
		}
		
		switch(index){
			case 0 :
				return 1.0;
				
			case 1 :
				return base;
				
			case 2 :
				return base * base;
				
			case 3 :
				return base * base * base;
				
			default:
				break;
		}
		
		double ans = base;
		int i = 1;
		
		while(2 * i < index){
			ans *= ans;
			i *= 2;
		}
		
		return i == index ? ans : ans * this.powerDouble(base, index - i);
	}
	
	protected boolean isInt(int base, int index) {
		int lg = 1;
		int pow = base;
		
		while(pow < base){
			pow *= 2;
			lg++;
		}
		
		return index * lg < 30;
	}

	@Override
	public String toString() {
		return new StringBuilder().append("^   ")
			.append(' ').append('$').append(this.offset)
			.append(' ').append('$').append(this.offset + 1)
			.toString();
	}
	
}
