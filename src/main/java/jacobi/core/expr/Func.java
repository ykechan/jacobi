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
package jacobi.core.expr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A marker class that represents a function call with argument expressions.
 * 
 * @author Y.K. Chan
 *
 */
public class Func implements Expression {
	
	public static Func of(String name, List<Expression> args) {
		if(name == null || name.trim().isEmpty()){
			throw new IllegalArgumentException("Function name must not be empty.");
		}
		
		if(args == null){
			return new Func(name, Collections.emptyList());
		}
		
		Expression[] array = args.toArray(new Expression[0]);
		return new Func(name, Collections.unmodifiableList(Arrays.asList(array)));
	}
	
	/**
	 * Constructor.
	 * @param name  Name of the function 
	 * @param args  List of arguments
	 */
	protected Func(String name, List<Expression> args) {
		this.name = name;
		this.args = args;
	}
	
	/**
	 * Get the name of the function
	 * @return  Name of the function
	 */
	public String getName() {
		return name;
	}

	@Override
	public List<Expression> getArgs() {
		return this.args;
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	private String name;
	private List<Expression> args;
}
