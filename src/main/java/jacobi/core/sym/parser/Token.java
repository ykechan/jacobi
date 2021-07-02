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
package jacobi.core.sym.parser;

/**
 * Data class for a token in a mathematical expression.
 * 
 * <p>A token can be a constant (e.g. 3.14, [0; 1]), an operator (e.g. +, -),
 * an identifier (e.g. x, y, abc) or a delimiter (e.g. brackets, comma)</p>
 * 
 * @author Y.K. Chan
 *
 */
public class Token {
	
	public static final Token END = Token.Type.DLMR.of("$");
	
	/**
	 * Type of the token
	 */
	public final Type type;
	
	/**
	 * Value of the token
	 */
	public final String value;
	
	/**
	 * Constructor.
	 * @param type  Type of the token
	 * @param value  Value of the token
	 */
	public Token(Type type, String value) {
		this.type = type;
		this.value = value;
	}
	
	@Override
	public String toString() {
		return this.value + "@" + this.type;
	}

	/**
	 * Types of a token
	 * @author Y.K. Chan
	 */
	public enum Type {
		/**
		 * Constant
		 */
		CONST, 
		
		/**
		 * Operator
		 */
		OPRT, 
		
		/**
		 * Identifier
		 */
		IDFR, 
		
		/**
		 * Delimiter
		 */
		DLMR;
		
		/**
		 * Factory method
		 * @param value  Value of the token
		 * @return
		 */
		public Token of(String value){
			return new Token(this, value); 
		}
	}

}
