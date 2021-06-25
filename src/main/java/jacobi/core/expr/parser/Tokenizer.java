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
package jacobi.core.expr.parser;

import java.util.Optional;

import jacobi.core.expr.parser.Token.Type;
import jacobi.core.lexer.ItemLexer;

/**
 * Implementation of a tokenizer of mathematical expression as an item lexer. 
 * 
 * @author Y.K. Chan
 *
 */
public class Tokenizer implements ItemLexer<Token> {
	
	/**
	 * Constructor
	 */
	public Tokenizer() {
		this.state = State.INIT;
	}

	@Override
	public Optional<Token> get() {
		if(this.type == null){
			Optional.empty();
		}
		
		return Optional.of(this.type.of(this.buffer.toString()));
	}

	@Override
	public Action push(char ch) {
		State next = this.state.jump(ch);
		try {
			switch(next){
				case INIT:
					return Action.MOVE;
					
				case END:
					this.type = this.state.type;
					return Action.ACCEPT;
					
				case FAIL:
					return Action.REJECT;
			
				default:
					break;
			}
			
			if(this.buffer == null){
				this.buffer = new StringBuilder();
			}
			
			this.buffer.append(ch);
			return Action.MOVE;
		} finally {
			this.state = next;
		}
	}

	private State state;
	
	private Token.Type type;
	private StringBuilder buffer;
	
	protected enum State {
		INIT {

			@Override
			public State jump(char ch) {
				if(ch == '\0'){
					return INIT;
				}
				
				if(Character.isWhitespace(ch)){
					return INIT;
				}
				
				if(this.isDigit(ch)){
					return INT;
				}
				
				if(this.isLetter(ch) || ch == '_'){
					return IDFR;
				}
				
				switch(ch){
					case '+':
					case '-':
					case '*':
					case '/':
					case '^':
						return OPER;
						
					case '(':
					case ')':
					case ',':
						return DLMR;
						
					default:
						break;
				}
				return FAIL;
			}
			
		},
		
		INT(Token.Type.CONST){

			@Override
			public State jump(char ch) {
				if(this.isDigit(ch)){
					return this;
				}
				
				if(ch == '.'){
					return FLOAT;
				}
				return END;
			}
			
		},
		
		FLOAT(Token.Type.CONST){

			@Override
			public State jump(char ch) {
				if(this.isDigit(ch)){
					return this;
				}
				
				return END;
			}
			
		},
		
		IDFR(Token.Type.IDFR){

			@Override
			public State jump(char ch) {
				if(this.isDigit(ch) || this.isLetter(ch) || ch == '_'){
					return this;
				}
				
				return END;
			}
			
		},
		
		OPER(Token.Type.OPRT){

			@Override
			public State jump(char ch) {
				return END;
			}
			
		},
		
		DLMR(Token.Type.DLMR){

			@Override
			public State jump(char ch) {
				return END;
			}
			
		},
		
		FAIL {

			@Override
			public State jump(char ch) {
				throw new UnsupportedOperationException("Lexer has failed");
			}
			
		},
		
		END {

			@Override
			public State jump(char ch) {
				throw new UnsupportedOperationException("Lexer has ended");
			}
			
		};
		
		public abstract State jump(char ch);
		
		public final Token.Type type;
		
		private State() {
			this(null);
		}

		private State(Type type) {
			this.type = type;
		}
		
		protected boolean isDigit(char ch) {
			return ch >= '0' && ch <= '9';
		}
		
		protected boolean isLetter(char ch) {
			return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
		}
		
	}
}
