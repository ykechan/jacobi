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

import java.text.ParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import jacobi.core.lexer.ItemLexer;
import jacobi.core.sym.Add;
import jacobi.core.sym.Const;
import jacobi.core.sym.Expr;
import jacobi.core.sym.Inv;
import jacobi.core.sym.Mul;
import jacobi.core.sym.Neg;
import jacobi.core.sym.Pow;
import jacobi.core.sym.Var;

/**
 * 
 * Implementation of parser of mathematical Expr.
 * 
 * <p>
 * This class accept the most basic and common form of syntax for mathematical expression.<br>
 * The syntax can be defined in ENBF:<br>
 * <br>
 * &lt;element&gt; := number|idfr|(&lt;expr&gt;)|idfr(&lt;expr&gt;{,&lt;expr&gt;})<br>
 * &lt;exp&gt; := &lt;element&gt;{^&lt;element&gt;}<br>
 * &lt;product&gt := &lt;exp&gt;{*&lt;exp&gt; | /&lt;exp&gt;}<br>
 * &lt;expr&gt := &lt;exp&gt;{+&lt;exp&gt; | -&lt;exp&gt;}<br>
 * </p>
 * 
 * @author Y.K. Chan
 *
 */
public class Parser implements ItemLexer<Expr> {
	
	/**
	 * Parsing a string of symbolic expression
	 * @param expr  String of symbolic expression
	 * @return  Symbolic expression
	 * @throws ParseException  when syntax error is found
	 */
	public static Expr parse(String expr) throws ParseException {
		Parser parser = new Parser(Tokenizer::new);
		for(int i = 0; i < expr.length(); i++){
			char ch = expr.charAt(i);
			
			Action action = parser.push(ch);
			switch(action){
				case ACCEPT:
					throw new ParseException("Expr has ended", i);

				case MOVE:
					break;

				default:
					throw new ParseException("Error found at " + i + " in " + expr, i);
			}
		}
		
		if(parser.push('\0') != Action.ACCEPT){
			throw new ParseException("Un-expected end of Expr", expr.length());
		}
		return parser.get()
			.orElseThrow(() -> new IllegalStateException("Invalid parser state " + parser.state));
	}
	
	/**
	 * Constructor.
	 * @param factory  Tokenizer factory
	 */
	public Parser(Supplier<ItemLexer<Token>> factory) {
		this.factory = factory;
		this.state = State.INIT;
	}

	@Override
	public Optional<Expr> get() {
		if(this.state != State.END){
			return Optional.empty();
		}
		
		if(this.stack.isEmpty()){
			throw new IllegalStateException("No Expr parsed.");
		}
		
		if(this.stack.size() > 1){
			throw new IllegalStateException("Un-expected end of Expr.");
		}
		
		Expr expr = this.stack.peek().expr;
		return Optional.of(expr);
	}

	@Override
	public Action push(char ch) {
		if(this.lexer == null){
			this.lexer = this.factory.get();
		}
		
		Action action = this.lexer.push(ch);
		if(action != Action.ACCEPT){
			return action;
		}
		
		Token token = this.lexer.get()
			.orElseThrow(() -> new IllegalStateException("Tokenizer " + this.lexer + "  accepted with no token"));
		
		Action next = this.push(token);
		switch(next){
			case MOVE:
				break;
			
			case FAIL:
				return next;
				
			default:
				throw new IllegalStateException();
		}
		
		if(ch == '\0'){
			return this.push(Token.END);
		}
		
		this.lexer = this.factory.get();
		this.lexer.push(ch);
		return next;
	}
	
	/**
	 * Push down a token
	 * @param token  Input token
	 * @return  Action to take
	 */
	protected Action push(Token token) {
		State next = this.state.jump(this, token);
		this.state = next;
		return next == State.END ? Action.ACCEPT : Action.MOVE;
	}
	
	/**
	 * Convert a token to a constant
	 * @param token  Input token
	 * @return  Constant value
	 */
	protected Const<?> toConst(Token token) {
		if(token.type != Token.Type.CONST){
			throw new IllegalArgumentException("Token " + token + " is not a constant.");
		}
		
		String val = token.value;
		if(val.indexOf('.') >= 0){
			double v = Double.parseDouble(val);
			return v == 0.0 ? Const.ZERO : v == 1.0 ? Const.ONE : Const.of(v);
		}
		
		if(val.length() < 10){
			int v = Integer.parseInt(val);
			return v == 0 ? Const.ZERO : v == 1 ? Const.ONE : Const.of(v);
		}
		
		long v = Long.parseLong(val);
		return v == 0L ? Const.ZERO : v == 1L ? Const.ONE : Const.of(v);
	}
	
	/**
	 * Get the top element of the current stack
	 * @return  Top element
	 */
	protected Element peek() {
		return this.stack.peek();
	}
	
	/**
	 * Get the current state of the parser
	 * @return  Current state
	 */
	protected State getState() {
		return this.state;
	}
	
	private Supplier<ItemLexer<Token>> factory;
	
	private ItemLexer<Token> lexer;
	private Deque<Element> stack;
	private State state;

	/**
	 * Parser state
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected enum State {
		
		/**
		 * Initializing
		 */
		INIT {

			@Override
			public State jump(Parser parser, Token token) {
				parser.stack = new ArrayDeque<>();
				return BEGIN_ELEMENT.jump(parser, token);
			}
			
		}, 
		
		/**
		 * Before a new element
		 */
		BEGIN_ELEMENT {

			@Override
			public State jump(Parser parser, Token token) {
				Deque<Element> stack = parser.stack;
				
				switch(token.type){
					case CONST: {
							Const<?> item = parser.toConst(token);
							stack.push(new Element('\0', item));
						}
						return END_ELEMENT;
						
					case IDFR: {
							Var var = Var.of(token.value);
							stack.push(new Element('\0', var));
						}
						return INVOKE_FUNC;
						
					case DLMR:
						if("(".equals(token.value)){
							stack.push(new Element('(', null));
							return BEGIN_SUM;
						}
						break;
						
					case OPRT:
						if("+".equals(token.value)){
							// ignore positive sign
							return this;
						}
						
						if("-".equals(token.value)){
							// negation operator
							stack.push(new Element('!', null));
							return this;
						}
						break;
						
					default:
						break;
				}
				return FAIL;
			}			
		}, 
		
		/**
		 * After an identifier, determine if element is a variable or function call
		 */
		INVOKE_FUNC {

			@Override
			public State jump(Parser parser, Token token) {
				if(!"(".equals(token.value)){
					return END_ELEMENT.jump(parser, token);
				}
				
				Deque<Element> stack = parser.stack;
				Element elem = stack.pop();
				
				stack.push(new Element('(', elem.expr));
				return BEGIN_SUM;
			}
			
		},
		
		/**
		 * After the end of an element
		 */
		END_ELEMENT {

			@Override
			public State jump(Parser parser, Token token) {
				Deque<Element> stack = parser.stack;
				Expr expr = stack.pop().expr;
				
				if("^".equals(token.value)){
					stack.push(new Element('^', expr));
					return BEGIN_ELEMENT;
				}
				
				stack.push(new Element('\0', expr));
				return END_EXPONENTIALS.jump(parser, token);
			}
			
		},
		
		/**
		 * Before a composite exponential expression
		 */
		BEGIN_EXPONENTIALS {

			@Override
			public State jump(Parser parser, Token token) {
				return BEGIN_ELEMENT.jump(parser, token);
			}
			
		},
		
		/**
		 * After a composite exponential expression
		 */
		END_EXPONENTIALS {

			@Override
			public State jump(Parser parser, Token token) {
				Deque<Element> stack = parser.stack;
				Expr expr = stack.pop().expr;
				
				while(!stack.isEmpty()){
					char suffix = stack.peek().suffix;
					
					if(suffix != '^' && suffix != '!'){
						break;
					}
					
					Element elem = stack.pop();
					if(suffix == '^'){
						expr = Pow.of(elem.expr, expr);
					}
					
					if(suffix == '!'){
						expr = new Neg(expr);
					}
				}
				
				expr = this.pop(stack, expr);
				
				if("*".equals(token.value) || "/".equals(token.value)){
					char suffix = token.value.charAt(0);
					stack.push(new Element(suffix, expr));
					return BEGIN_EXPONENTIALS;
				}
				
				stack.push(new Element('\0', expr));
				return END_PRODUCT.jump(parser, token);
			}
			
			protected Expr pop(Deque<Element> stack, Expr right) {
				if(stack.isEmpty()){
					return right;
				}
				
				if(stack.peek().suffix == '*'){
					Expr left = stack.pop().expr;
					return Mul.of(left, right);
				}
				
				if(stack.peek().suffix == '/'){
					Expr left = stack.pop().expr;
					return Mul.of(left, new Inv(right));
				}
				return right;
			}
			
		},
		
		/**
		 * Before a composite product
		 */
		BEGIN_PRODUCT {

			@Override
			public State jump(Parser parser, Token token) {
				return BEGIN_EXPONENTIALS.jump(parser, token);
			}
			
		},
		
		/**
		 * After a composite product
		 */
		END_PRODUCT {

			@Override
			public State jump(Parser parser, Token token) {
				Deque<Element> stack = parser.stack;
				Expr expr = stack.pop().expr;

				expr = this.pop(stack, expr);
				
				if("+".equals(token.value) || "-".equals(token.value)){
					char op = token.value.charAt(0);
					stack.push(new Element(op, expr));
					return BEGIN_PRODUCT;
				}
				
				stack.push(new Element('\0', expr));
				return END_SUM.jump(parser, token);
			}
			
			protected Expr pop(Deque<Element> stack, Expr right) {
				if(stack.isEmpty()){
					return right;
				}
				
				if(stack.peek().suffix == '+'){
					Expr left = stack.pop().expr;
					return Add.of(left, right);
				}
				
				if(stack.peek().suffix == '-'){
					Expr left = stack.pop().expr;
					return Add.of(left, new Neg(right));
				}
				return right;
			}
			
		},
		
		/**
		 * Before a composite sum
		 */
		BEGIN_SUM {

			@Override
			public State jump(Parser parser, Token token) {
				return BEGIN_PRODUCT.jump(parser, token);
			}
			
		},
		
		/**
		 * After a composite product
		 */
		END_SUM {

			@Override
			public State jump(Parser parser, Token token) {
				Deque<Element> stack = parser.stack;
				Expr expr = stack.pop().expr;
				
				if(",".equals(token.value)){
					stack.push(new Element(',', expr));
					return BEGIN_SUM;
				}
				
				if(")".equals(token.value)){
					expr = this.popAll(stack, expr);
					stack.push(new Element('\0', expr));
					return expr == null ? FAIL : END_ELEMENT;
				}
				
				stack.push(new Element('\0', expr));
				return END;
			}
			
			protected Expr popAll(Deque<Element> stack, Expr right) {
				List<Expr> args = new ArrayList<>();
				args.add(right);
				
				Element bracket = null;
				while(!stack.isEmpty()){
					Element elem = stack.pop();
					if(elem.suffix == '('){
						bracket = elem;
						break;
					}
					
					args.add(elem.expr);
				}
				
				if(bracket == null){
					return null;
				}
				
				if(bracket.expr instanceof Var){
					// function call
					Collections.reverse(args);
					// ...
				}
				
				if(args.size() > 1){
					return null;
				}
				
				return right;
			}
			
		},
		
		/**
		 * Parser has failed
		 */
		FAIL {

			@Override
			public State jump(Parser parser, Token token) {
				throw new UnsupportedOperationException("Parser has failed.");
			}
			
		},
		
		/**
		 * Parser is ended
		 */
		END {

			@Override
			public State jump(Parser parser, Token token) {
				throw new UnsupportedOperationException("Parser is ended.");
			}
			
		};
		
		public abstract State jump(Parser parser, Token token);
	}
	
	/**
	 * Data class for an element in the parsing stack
	 * 
	 * @author Y.K. Chan
	 *
	 */
	protected static class Element {
		
		/**
		 * Operator following the expression
		 */
		public final char suffix;
		
		/**
		 * Symbolic expression
		 */
		public final Expr expr;

		/**
		 * Constructor.
		 * @param suffix  Operator following the expression
		 * @param expr  Symbolic expression
		 */
		public Element(char suffix, Expr expr) {
			this.suffix = suffix;
			this.expr = expr;
		}
		
	}
	
}
