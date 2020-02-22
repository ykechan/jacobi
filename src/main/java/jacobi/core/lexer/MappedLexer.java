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
import java.util.function.Function;

/**
 * Item lexer that maps item result to another type.
 * 
 * @author Y.K. Chan
 * @param <T>  Base item type
 * @param <V>  Result item type
 */
public class MappedLexer<T, V> implements ItemLexer<V> {

    /**
     * Create a mapped lexer from a mapping function.
     * @param <T>  Base item type
     * @param <V>  Result item type
     * @param mapper  Mapping function
     * @return  A mapped lexer
     */
    public static <T, V> Function<ItemLexer<T>, ItemLexer<V>> to(Function<T, V> mapper) {
        return (lexer) -> new MappedLexer<>(lexer, mapper);
    }
    
    /**
     * Constructor.
     * @param base  Base lexer
     * @param mapper  Mapper function
     */
    public MappedLexer(ItemLexer<T> base, Function<T, V> mapper) {
        this.base = base;
        this.mapper = mapper;
    }

    @Override
    public Action push(char ch) {
        return this.base.push(ch);
    }

    @Override
    public Optional<V> get() {
        return this.base.get().map(mapper);
    }

    private ItemLexer<T> base;
    private Function<T, V> mapper;
}
