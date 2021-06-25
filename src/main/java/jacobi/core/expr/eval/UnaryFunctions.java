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
import java.util.function.DoubleUnaryOperator;
import java.util.function.IntFunction;

/**
 * Library of supported unary function
 * 
 * @author Y.K. Chan
 *
 */
public enum UnaryFunctions implements IntFunction<Instruction<Double>> {
	
	/**
	 * Identity function f(x) = x
	 */
	ID {

		@Override
		public Instruction<Double> apply(int off) {
			return this.of(off, DoubleUnaryOperator.identity());
		}
		
	},
	
	/**
	 * Natural log
	 */
	LN {

		@Override
		public Instruction<Double> apply(int off) {
			return this.of(off, Math::log);
		}
		
	},
	
	/**
	 * Exponential of natural base
	 */
	EXP {

		@Override
		public Instruction<Double> apply(int off) {
			return this.of(off, Math::exp);
		}
		
	},
	
	/**
	 * Sine function
	 */
	SIN {

		@Override
		public Instruction<Double> apply(int off) {
			return this.of(off, Math::sin);
		}
		
	},
	
	/**
	 * Cosine function
	 */
	COS {

		@Override
		public Instruction<Double> apply(int off) {
			return this.of(off, Math::cos);
		}
		
	},
	
	/**
	 * Tangent function
	 */
	TAN {

		@Override
		public Instruction<Double> apply(int off) {
			return this.of(off, Math::tan);
		}
		
	},
	
	/**
	 * Hyperbolic sine function
	 */
	SINH {

		@Override
		public Instruction<Double> apply(int off) {
			return this.of(off, Math::sinh);
		}
		
	},
	
	/**
	 * Hyperbolic cosine function
	 */
	COSH {

		@Override
		public Instruction<Double> apply(int off) {
			return this.of(off, Math::cosh);
		}
		
	},
	
	/**
	 * Hyperbolic tangent function
	 */
	TANH {

		@Override
		public Instruction<Double> apply(int off) {
			return this.of(off, Math::tanh);
		}
		
	};
	
	/**
	 * Factory method of an instruction that is applying a double unary operator.
	 * @param offset  Memory offset
	 * @param func  Double unary operator
	 * @return  Instruction
	 */
	protected Instruction<Double> of(int offset, DoubleUnaryOperator func) {
		UnaryFunctions f = this;
		return new Instruction<Double>(offset) {

			@Override
			public Double run(List<?> input, List<?> mem) {
				try {
					Number num = (Number) mem.get(this.offset);
					return func.applyAsDouble(num.doubleValue());
				} catch(ClassCastException ex) {
					return Double.NaN;
				}
			}

			@Override
			public String toString() {
				StringBuilder buf = new StringBuilder().append(f.name().toLowerCase());
				for(int i = f.name().length(); i < 4; i++){
					buf.append(' ');
				}
				
				return buf.append(' ')
					.append('$').append(this.offset)
					.toString();
			}
			
		};
	}

}
