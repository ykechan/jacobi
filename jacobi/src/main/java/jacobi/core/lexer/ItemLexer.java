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
 * All implementations of this class must follow the contract below:
 * 
 * 1. ItemLexer is stateful containing intermediate result and updates upon push(char).
 * 2. If MOVE is not returned on push(char ch), the ch is NOT included with this lexer.
 * 3. Upon encountering '\0', MOVE should NOT be returned, i.e. '\0' is universally 
 *    un-accepted.
 * 4. If the lexer is in an intermediate state, i.e. the lexer is not yet parsing any result, 
 *    REJECT is returned upon invalid characters.
 * 5. If the lexer is not in an intermediate state, i.e. the lexer is in the middle of parsing result,
 *    FAIL is returned upon invalid characters.
 * 6. Before ACCEPT/REJECT/FAIL is returned, get() returns empty.
 * 7. After ACCEPT is returned, get() returns with actual result.
 * 8. After ACCEPT/REJECT/FAIL, successive push(char) throws IllegalStateException.
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
     * MOVE - Move on to the next character
     * ACCEPT - Lexing result ready. Character NOT accepted.
     * REJECT - Character NOT accepted. No result.
     * FAIL - Error in lexing. Character NOT accepted. No result.
     */
    public enum Action {
        MOVE, ACCEPT, REJECT, FAIL
    }
    
}
