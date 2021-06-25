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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Perform of a set of instructions.
 * 
 * <p>This class is a interpreter of a set of instructions. It would provide a controlled memory
 * entries, and for each instruction the result yielded would be put back to the offset entry
 * in the memory. After all instructions are done, the first entry in the memory would be returned.</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Eval implements Function<List<?>, Object> {
	
	
	
	/**
	 * Constructor
	 * @param vars  List of free variables
	 * @param procedure  Procedure as list of instructions
	 * @param memLength  Length of memory entries required
	 */
	protected Eval(List<String> vars, List<Instruction<?>> procedure, int memLength) {
		this.vars = vars;
		this.procedure = procedure;
		this.memLength = memLength;
	}
	
	/**
	 * Get the list of free variables
	 * @return  List of free variables
	 */
	public List<String> getVars() {
		return vars;
	}
	
	/**
	 * Get the procedure i.e. list of instructions
	 * @return  List of instructions
	 */
	public List<Instruction<?>> getProcedure() {
		return procedure;
	}
	
	@Override
	public Object apply(List<?> t) {
		if(t.size() != this.vars.size()){
			throw new IllegalArgumentException("Number of variables not match");
		}
		
		List<?> input = Collections.unmodifiableList(t);
		Object[] mem = new Object[this.memLength];
		List<?> view = Collections.unmodifiableList(Arrays.asList(mem));
		
		for(Instruction<?> i : this.procedure){
			Object result = i.run(input, view);
			mem[i.offset] = result;
		}
		
		return mem[0];
	}

	private List<String> vars;
	private List<Instruction<?>> procedure;
	private int memLength;
}
