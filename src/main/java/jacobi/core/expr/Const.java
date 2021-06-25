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

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * A marker class that represents an expression of a constant value.
 * 
 * @author Y.K. Chan
 * @param <V>  Type of constant value
 */
public class Const<V> implements Expression, Supplier<V> {
	
	/**
	 * Constant number 0
	 */
	public static final Const<Number> ZERO = new Const<>(0);
	
	/**
	 * Constant number 1
	 */
	public static final Const<Number> ONE = new Const<>(1);
	
	/**
	 * Constant number -1
	 */
	public static final Const<Number> NEG_ONE = new Const<>(-1);
	
	/**
	 * Constructor
	 * @param value
	 */
	public Const(V value) {
		this.value = value;
	}
	
	@Override
	public V get() {
		return this.value;
	}

	@Override
	public List<Expression> getArgs() {
		return Collections.emptyList();
	}

	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}

	private V value;

}
