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

/**
 * 
 * @author Y.K. Chan
 *
 */
public class Swap extends Instruction {
	
	public static <T> Swap load(int offset, T value) {
		if(value == null){
			throw new UnsupportedOperationException("Must not swap in null");
		}
		return new Swap(offset, -1, value);
	}
	
	public static <T> Swap move(int offset, int target) {
		if(target < 0){
			throw new UnsupportedOperationException("Target index out of bound");
		}
		
		return new Swap(offset, target, null);
	}
	
	protected Swap(int offset, int target, Object value) {
		super(offset);
		this.target = target;
		this.value = value;
	}
	
	@Override
	public Object run(List<?> input, List<?> mem) {
		return this.value == null ? input.get(this.target) : this.value;
	}

	private int target;
	private Object value;
}
