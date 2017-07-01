/*
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
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

package jacobi.core.lexer;

import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Item lexer in which item is ended by a trailing delimiter.
 * 
 * Simply put, this class implements the following regex:
 * [item];
 * 
 * where item is the underlying item lexer, ; is the character of delimiter.
 * 
 * @author Y.K. Chan
 * @param <T>  Item type
 */
public class DelimitedLexer<T> implements ItemLexer<T> {
    
    public static <T> UnaryOperator<ItemLexer<T>> by(char ch) {
        return (lexer) -> new DelimitedLexer<>(lexer, ch);
    }

    /**
     * Constructor.
     * @param base  Base lexer
     * @param delimiter  Delimiter character
     */
    public DelimitedLexer(ItemLexer<T> base, char delimiter) {
        this.base = base;
        this.delimiter = delimiter;
        this.state = State.START;
    }

    @Override
    public Action push(char ch) {
        return (this.state = this.state.jump(this, ch)).action;
    }

    @Override
    public Optional<T> get() {
        return this.state == State.ACCEPT ? this.base.get() : Optional.empty();
    }

    private State state;
    private ItemLexer<T> base;
    private char delimiter;    
    
    /**
     * States this lexer can be in.
     */
    protected enum State {
        /**
         * Initial state.
         */
        START {

            @Override
            public <T> State jump(DelimitedLexer<T> lexer, char ch) {
                Action result = lexer.base.push(ch);
                switch(result){
                    case MOVE:
                        return State.PARSING;
                    case ACCEPT:                        
                        throw new IllegalStateException(lexer.base + " unexpected to accept.");
                    case REJECT:
                        return State.REJECT;
                    case FAIL:
                        throw new IllegalStateException(lexer.base + " unexpected to fail.");
                    default :
                        break;
                }
                throw new IllegalStateException(result.name());
            }
            
        }, 
        /**
         * Parsing by base lexer.
         */
        PARSING {

            @Override
            public <T> State jump(DelimitedLexer<T> lexer, char ch) {                
                Action result = lexer.base.push(ch);
                switch(result){
                    case MOVE:
                        return State.PARSING;
                    case ACCEPT:
                        return ch == lexer.delimiter ? State.WAITING : State.FAIL;
                    case REJECT:
                        throw new IllegalStateException(lexer.base + " unexpected to reject.");
                    case FAIL:
                        return State.FAIL;
                    default :
                        break;
                }
                throw new IllegalStateException(result.name());
            }                        
            
        },
        /**
         * Encountered delimiter, waiting to end at the next character.
         */
        WAITING {

            @Override
            public <T> State jump(DelimitedLexer<T> lexer, char ch) {
                return State.ACCEPT;
            }
            
        },
        /**
         * Result accepted.
         */
        ACCEPT(Action.ACCEPT), 
        /**
         * Input rejected.
         */
        REJECT(Action.REJECT), 
        /**
         * Lexer failed.
         */
        FAIL(Action.FAIL);
        
        private State() {
            this(Action.MOVE);
        }

        private State(Action action) {
            this.action = action;
        }
        
        /**
         * Jump to another state upon receiving a character.
         * @param <T>  Result type
         * @param lexer  Lexer 
         * @param ch  Character received.
         * @return  Next state
         */
        public <T> State jump(DelimitedLexer<T> lexer, char ch) {
            throw new IllegalStateException();
        }
        
        /**
         * Action to be returned in this state.
         */
        public final Action action;
    }
}
