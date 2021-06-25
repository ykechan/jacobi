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
 * Move in a constant value and yields as result
 * 
 * @author Y.K. Chan
 * @param T  Type of constant value
 *
 */
public class Move<T> extends Instruction<T> {

	/**
	 * Constructor.
	 * @param offset  Memory offset
	 * @param value  Constant value
	 */
	public Move(int offset, T value) {
		super(offset);
		this.value = value;
	}

	@Override
	public T run(List<?> input, List<?> mem) {
		return this.value;
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("move")
			.append(' ').append(this.value)
			.append(' ').append('$').append(this.offset)
			.toString();
	}

	private T value;
}
