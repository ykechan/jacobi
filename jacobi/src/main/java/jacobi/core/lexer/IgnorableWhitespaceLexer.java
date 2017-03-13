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
 * Item lexer in which whitespace that surrounds is ignorable.
 * 
 * Simply put, this class implements the following regex:
 * (\s)*[item](\s)*
 * 
 * where item is the underlying item lexer.
 * 
 * @author Y.K. Chan
 * @param <T>  Item type
 */
public class IgnorableWhitespaceLexer<T> implements ItemLexer<T> {

    public IgnorableWhitespaceLexer(ItemLexer<T> base) {
        this.base = base;
        this.state = State.START;
    }

    @Override
    public Action push(char ch) {
        return (this.state = this.state.jump(this.base, ch)).action;
    }

    @Override
    public Optional<T> get() {
        return this.base.get();
    }

    private State state;
    private ItemLexer<T> base;
    
    protected enum State {
        START {

            @Override
            public <T> State jump(ItemLexer<T> context, char ch) { 
                if(Character.isWhitespace(ch)){
                    return State.START;
                } 
                Action lexerAct = context.push(ch);
                switch(lexerAct){
                    case MOVE:
                        return State.PARSING;
                    case ACCEPT:
                        return State.WAITING.jump(context, ch);
                    case REJECT:
                        return State.REJECT;
                    case FAIL:
                        throw new IllegalStateException("Lexer " + context.toString() + " failed on 1st character.");
                    default:
                        break;
                }
                throw new IllegalStateException(lexerAct.name());
            }
            
        },
        PARSING {

            @Override
            public <T> State jump(ItemLexer<T> context, char ch) {
                Action lexerAct = context.push(ch);
                switch(lexerAct){
                    case MOVE:
                        return State.PARSING;
                    case ACCEPT:
                        return State.WAITING.jump(context, ch);
                    case REJECT:
                        throw new IllegalStateException("Lexer " + context.toString() + " rejected not on 1st character.");
                    case FAIL:
                        return State.FAIL;
                    default:
                        break;
                }
                throw new IllegalStateException(lexerAct.name());
            }
            
        },
        WAITING {

            @Override
            public <T> State jump(ItemLexer<T> context, char ch) {
                return Character.isWhitespace(ch) ? State.WAITING : State.ACCEPT;
            }                        
            
        },
        ACCEPT(Action.ACCEPT), REJECT(Action.REJECT), FAIL(Action.FAIL);

        private State() {
            this(Action.MOVE);
        }
        
        private State(Action action) {
            this.action = action;
        }
        
        public <T> State jump(ItemLexer<T> context, char ch) {
            throw new IllegalStateException();
        }
        
        public Action action;
    }
}
