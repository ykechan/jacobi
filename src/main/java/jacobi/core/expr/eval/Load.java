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
 * Retrieve an element in input and yields as result.
 * 
 * @author Y.K. Chan
 *
 */
public class Load extends Instruction<Object> {

	/**
	 * Constructor.
	 * @param offset  Memory offset
	 * @param target  Target index of input to be loaded
	 */
	public Load(int offset, int target) {
		super(offset);
		this.target = target;
	}

	@Override
	public Object run(List<?> input, List<?> mem) {
		return input.get(this.target);
	}
	
	@Override
	public String toString() {
		return new StringBuilder().append("load")
			.append(' ').append('#').append(this.target)
			.append(' ').append('$').append(this.offset)
			.toString();
	}

	private int target;
}
