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
package jacobi.core.sym;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Symbolic expression of a constant value.
 * 
 * @author Y.K. Chan
 * @param <V>  Type of constant value
 */
public class Const<V> implements Expr, Supplier<V> {
	
	/**
	 * Default instance of constant 0
	 */
	public static final Const<Number> ZERO = new Const<>(0);
	
	/**
	 * Default instance of constant 1
	 */
	public static final Const<Number> ONE = new Const<>(1);
	
	/**
	 * Default instance of constant -1
	 */
	public static final Const<Number> NEG_ONE = new Const<>(-1);
	
	/**
	 * Visitor to check the expression is a constant of a certain type
	 * @param type  Type of constant value
	 * @return  Visitor checker
	 */
	public static <T> Visitor<Optional<T>> is(Class<T> type) {
		return new Visitor<Optional<T>>(){

			@Override
			public Optional<T> visit(Add expr) {
				return Optional.empty();
			}

			@Override
			public Optional<T> visit(Mul expr) {
				return Optional.empty();
			}

			@Override
			public Optional<T> visit(Pow expr) {
				return Optional.empty();
			}

			@Override
			public Optional<T> visit(Var expr) {
				return Optional.empty();
			}

			@Override
			public <V> Optional<T> visit(Const<V> expr) {
				return expr.as(type);
			}
			
		};
	}
	
	/**
	 * Factory method
	 * @return  Constant expression
	 */
	@SuppressWarnings("unchecked")
	public static <V> Const<V> of(V value) {
		if(value == null){
			throw new IllegalArgumentException("Constant value must not be null.");
		}
		
		if(value instanceof Number){
			Number n = (Number) value;
			
			if(n.doubleValue() == 0.0){
				return (Const<V>) ZERO;
			}
			
			if(n.doubleValue() == 1.0){
				return (Const<V>) ONE;
			}
			
			if(n.doubleValue() == -1.0){
				return (Const<V>) NEG_ONE;
			}
		}
		return new Const<>(value);
	}
	
	/**
	 * Constructor.
	 * @param value  Constant value
	 */
	protected Const(V value) {
		this.value = value;
	}

	@Override
	public List<Expr> getArgs() {
		return Collections.emptyList();
	}

	@Override
	public V get() {
		return this.value;
	}
	
	/**
	 * Get the constant value as a particular type, or empty
	 * @param clazz  Type of the return value
	 * @return  Constant value as a particular type, or empty
	 */
	public <T> Optional<T> as(Class<T> clazz) {
		if(clazz.isInstance(this.value)){
			return Optional.of(clazz.cast(this.value));
		}
		
		return Optional.empty();
	}
	
	@Override
	public <T> T accept(Visitor<T> visitor) {
		return visitor.visit(this);
	}
	
	@Override
	public String toString() {
		return this.value.toString();
	}

	private V value;
}
