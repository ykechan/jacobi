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

/**
 * A lexer for analyze a numerical number.
 * 
 * @author Y.K. Chan
 */
public class DoubleLexer implements ItemLexer<Double> {

    /**
     * Constructor.
     */
    public DoubleLexer() {
        this.state = State.START;
        this.buffer = new StringBuilder();
    }

    @Override
    public Action push(char ch) {
        return (this.state = this.state.jump(buffer, ch)).action;
    }

    @Override
    public Optional<Double> get() {        
        return this.state == State.ACCEPT ? Optional.of(Double.valueOf(this.buffer.toString())) : Optional.empty();
    }

    private State state;
    private StringBuilder buffer;
    
    /**
     * States this lexer could be in.
     */
    protected enum State {
        /**
         * Initial state.
         */
        START {

            @Override
            public State jump(StringBuilder context, char ch) {
                switch(ch){
                    case '+' :
                        return State.SIGNED;
                    case '-' :
                        return this.append(context, '-').to(State.SIGNED);
                    case '.' :
                        return this.append(context, '0').append(context, '.').to(State.DOTTED);                                            
                    default :
                        if(ch >= '0' && ch <= '9'){
                            return this.append(context, ch).to(State.INT);
                        }
                        break;
                }
                return State.REJECT;
            }
            
        }, 
        /**
         * A sign (+|-) has been encountered. Need a number or dot for fractional part.
         */
        SIGNED {

            @Override
            public State jump(StringBuilder context, char ch) {
                return ch == '.'
                        ? this.append(context, '0').append(context, '.').to(State.DOTTED)
                        : ch >= '0' && ch <= '9' 
                            ? this.append(context, ch).to(State.INT) 
                            : ch == '+' || ch == '-' 
                                ? State.FAIL
                                : State.ACCEPT;
            }
            
        },
        /**
         * Within the integral part of the number
         */
        INT {

            @Override
            public State jump(StringBuilder context, char ch) {
                return ch == '.'
                        ? this.append(context, '.').to(State.DOTTED)
                        : ch >= '0' && ch <= '9' 
                            ? this.append(context, ch)
                            : State.ACCEPT;
            }
            
        },
        /**
         * Encountered decimal dot, need at least 1 digit decimal.
         */
        DOTTED {

            @Override
            public State jump(StringBuilder context, char ch) {
                return ch >= '0' && ch <= '9' 
                        ? this.append(context, ch).to(State.FRAC)
                        : State.FAIL;
            }
            
        },
        /**
         * Within the decimal part, terminal upon any non-numerical character.
         */
        FRAC {

            @Override
            public State jump(StringBuilder context, char ch) {
                return ch >= '0' && ch <= '9' 
                        ? this.append(context, ch)
                        : ch == '.' ? State.FAIL : State.ACCEPT;
            }                        
            
        },
        ACCEPT(Action.ACCEPT), 
        REJECT(Action.REJECT),
        FAIL(Action.FAIL)
        ;

        private State() {
            this(Action.MOVE);
        }

        private State(Action action) {
            this.action = action;
        }
        
        public State jump(StringBuilder context, char ch) {
            throw new IllegalStateException(this.name());
        }
        
        protected final State append(StringBuilder context, char ch) {
            context.append(ch);
            return this;
        }
        
        protected final State to(State state) {
            return state;
        }
        
        public final Action action;
    }
}
