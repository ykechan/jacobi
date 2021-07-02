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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class Procedure implements Function<List<?>, Object> {
	
	/**
	 * Constructor
	 * @param steps  List of instructions
	 * @param mem  Memory capacity
	 */
	protected Procedure(List<Instruction> steps, int mem) {
		this.steps = steps;
		this.mem = mem;
	}
	
	@Override
	public Object apply(List<?> t) {
		Object[] memory = new Object[this.mem];
		List<?> view = Collections.unmodifiableList(Arrays.asList(memory));
		
		for(Instruction i : this.steps){
			Object result = i.run(t, view);
			memory[i.offset] = result;
		}
		
		return memory[0];
	}

	private List<Instruction> steps;
	private int mem;
}
