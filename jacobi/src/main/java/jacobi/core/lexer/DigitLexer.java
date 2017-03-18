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
 * Item lexer for a single digit. Only a digit is accepted, and multiple continuous digit is not accepted.
 * 
 * This class is mainly used for testing purposes.
 * 
 * @author Y.K. Chan
 */
public class DigitLexer implements ItemLexer<Integer> {

    /**
     * Constructor
     */
    public DigitLexer() {        
        this.state = State.START;
    }

    @Override
    public Action push(char ch) {
        switch(this.state){
            case START:
                if(ch >= '0' && ch <= '9'){
                    this.result = ch - '0';
                    return (this.state = State.READ).action;
                }
                return (this.state = State.REJECT).action;
            case READ:
                if(ch >= '0' && ch <= '9'){
                    return (this.state = State.FAIL).action;
                }
                return (this.state = State.ACCEPT).action;
            case ACCEPT:                
            case REJECT:                
            case FAIL:
                throw new IllegalStateException();
            default:
                break;
        }
        throw new IllegalStateException(this.state.name());
    }

    @Override
    public Optional<Integer> get() {
        return this.state == State.ACCEPT ? Optional.of(this.result) : Optional.empty();
    }
    
    private State state;
    private int result;

    /**
     * State this lexer could be in
     */
    protected enum State {
        START(Action.MOVE), READ(Action.MOVE), ACCEPT(Action.ACCEPT), REJECT(Action.REJECT), FAIL(Action.FAIL);

        private State(Action action) {
            this.action = action;
        }
        
        public final Action action;
    }
}
