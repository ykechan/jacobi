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
import java.util.function.Supplier;

/**
 * Common interface for performing lexical analysis on getting an item.
 * 
 * <p>All implementations of this class must follow the contract below:</p>
 * <ul>
 *  <li>ItemLexer is stateful containing intermediate result and updates upon push(char).</li>
 *  <li>If MOVE is not returned on push(char ch), the ch is NOT included with this lexer.</li>
 *  <li>Upon encountering '\0', MOVE should NOT be returned, i.e.&nbsp;'\0' is universally un-accepted.</li>
 *  <li>If the lexer is in an intermediate state, i.e.&nbsp;the lexer is not yet parsing any result, 
 *      REJECT is returned upon invalid characters.</li>
 *  <li>If the lexer is not in an intermediate state, i.e.&nbsp;the lexer is in the middle of parsing result,
 *    FAIL is returned upon invalid characters.</li>
 *  <li>Before ACCEPT/REJECT/FAIL is returned, get() returns empty.</li>
 *  <li>After ACCEPT is returned, get() returns with actual result.</li>
 *  <li>After ACCEPT/REJECT/FAIL, successive push(char) throws IllegalStateException.</li>
 * </ul>
 * 
 * @author Y.K. Chan
 * @param <T>  Element item type
 */
public interface ItemLexer<T> extends Supplier<Optional<T>> { 
    
    /**
     * Update internal state upon encountering a character.
     * @param ch  Input character
     * @return  Subsequent action to be taken.
     */
    public Action push(char ch);
    
    /**
     * Action to be taken after encountering an input character.
     */
    public enum Action {
        /**
         * Move on to the next character.
         */
        MOVE, 
        /**
         * Lexer result ready. Character NOT accepted.
         */
        ACCEPT, 
        /**
         * Character NOT accepted. No result.
         */
        REJECT, 
        /**
         * Error in lexing. Character NOT accepted. No result.
         */
        FAIL
    }
    
}
