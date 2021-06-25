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
 * Abstract base class for instructions to evaluate an expression.
 * 
 * <p>
 * Given an list of input and memory storage, an instruction access the memory to fetch
 * its arguments, perform evaluation, and yields a computation result. Input and memory
 * are read-only.
 * </p>
 * 
 * <p>
 * An instruction should only access memory pass its own offset. Due to performance 
 * concern sub-list is not used.
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public abstract class Instruction<T> {
	
	public final int offset;
	
	/**
	 * Constructor.
	 * @param offset  Index of accessible memory
	 */
	public Instruction(int offset) {
		this.offset = offset;
	}

	/**
	 * Run the instruction
	 * @param input  Input values
	 * @param mem  Memory storage
	 * @return  Instruction result
	 */
	public abstract T run(List<?> input, List<?> mem);

}
