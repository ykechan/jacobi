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
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * Item lexer that tests and filter un-qualified items at the end.
 * 
 * If item fails the test, an FAIL would return instead of ACCEPT.
 * 
 * @author Y.K. Chan
 * @param <T>  Item type
 */
public class FilteredLexer<T> implements ItemLexer<T> {
    
    /**
     * Create a decorating function mapping to lexer with filter.
     * @param <T>  Item type
     * @param filter  Testing predicate
     * @return  A filtered lexer
     */
    public static <T> UnaryOperator<ItemLexer<T>> by(Predicate<T> filter) {
        return (lexer) -> new FilteredLexer<>(lexer, filter);
    }

    /**
     * Constructor.
     * @param base  Base lexer
     * @param filter   Item predicate
     */
    public FilteredLexer(ItemLexer<T> base, Predicate<T> filter) {        
        this.base = base;
        this.filter = filter;        
        this.lastAction = Action.MOVE;
    }

    @Override
    public Action push(char ch) {
        if(this.lastAction != Action.MOVE){
            throw new IllegalStateException();
        }
        this.lastAction = this.base.push(ch); 
        if(this.lastAction == Action.ACCEPT){
            this.item = this.base.get().orElseThrow(() -> new IllegalStateException());
            if(!this.filter.test(item)){
                return this.lastAction = Action.FAIL;
            }
        }
        return this.lastAction;
    }

    @Override
    public Optional<T> get() {
        return this.lastAction == Action.ACCEPT ? this.base.get() : Optional.empty();
    }

    private ItemLexer<T> base;
    private Predicate<T> filter;
    private T item;
    private Action lastAction;
}
