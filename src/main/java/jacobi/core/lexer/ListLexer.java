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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A lexer for a list of basic items.
 * 
 * Simply put, this class implements the following regex
 * 
 * &lt;item&gt;(,&lt;item&gt;)+|(&lt;item&gt;)?
 * 
 * which , can be replaced with other delimiter characters.
 * 
 * Since it makes harder and error-prone to generate a list of items exactly without a trailing delimiter, e.g.
 * a common mistake is 1,2,3,4,. Thus a strict flag is supplied, and if set to false, such trailing delimiter
 * is accepted.
 * 
 * Another common option is no delimiter. Such case is represented by a '\0' delimiter. However in this scenario
 * the basic item lexer is responsible for the ending of previous item and starting of the next item.
 * 
 * @author Y.K. Chan
 * @param <T>  Item type
 */
public class ListLexer<T> implements ItemLexer<List<T>> {
    
    public static <T> Function<Supplier<ItemLexer<T>>, ItemLexer<List<T>>> by(char delimiter) {
        return (factory) -> new ListLexer<>(factory, delimiter, true);
    }

    /**
     * Constructor.
     * @param factory  Factory of basic item lexer
     * @param delimiter  Delimiter character
     * @param strict  True to accept optional extra delimiter character at the end, false otherwise
     */
    public ListLexer(Supplier<ItemLexer<T>> factory, char delimiter, boolean strict) {
        this.context = new Context<>(factory, delimiter, strict);
        this.state = State.START;
    }

    @Override
    public Action push(char ch) {
        return (this.state = this.state.jump(context, ch)).action;
    }

    @Override
    public Optional<List<T>> get() {
        return this.state == State.ACCEPT ? Optional.of(this.context.results) : Optional.empty();
    }

    private State state;
    private Context<T> context;
    
    /**
     * States this lexer can be in.
     */
    protected enum State {
        /**
         * Initial state.
         */
        START {

            @Override
            public <T> State jump(Context<T> context, char ch) {
                context.lexer = context.factory.get();
                context.results = new ArrayList<>();
                return State.PARSING.jump(context, ch);
            }
            
        }, 
        /**
         * Parsing an item.
         */
        PARSING {

            @Override
            public <T> State jump(Context<T> context, char ch) {
                return this.jump(context, ch, 0);
            }
            
            protected <T> State jump(Context<T> context, char ch, int depth) {
                if(depth > 2){
                    throw new IllegalStateException(context.lexer + " accepted upon first character.");
                }
                Action result = context.lexer.push(ch);
                switch(result){
                    case MOVE:
                        return State.PARSING;
                    case ACCEPT:
                        context.results.add(
                            context.lexer.get()
                                .orElseThrow(() -> new IllegalStateException(context.lexer + " accepted without result."))
                        ); 
                        context.lexer = context.factory.get();
                        return context.delimiter == '\0'
                                ? this.jump(context, ch, depth + 1)
                                : ch == context.delimiter 
                                    ? State.PARSING 
                                    : State.ACCEPT;
                    case REJECT:
                        return context.results.isEmpty() 
                            || context.delimiter == '\0'
                            || !context.strict ? State.ACCEPT : State.FAIL;
                    case FAIL:
                        return State.FAIL;
                    default:
                        break;
                }
                throw new IllegalStateException(result.name());
            }
        },
        ACCEPT(Action.ACCEPT), REJECT(Action.REJECT), FAIL(Action.FAIL);
        
        public final Action action;

        private State() {
            this(Action.MOVE);
        }
        
        private State(Action action) {
            this.action = action;
        }
        
        public <T> State jump(Context<T> context, char ch) {
            throw new IllegalStateException();
        }
    }
    
    /**
     * Lexical context.
     * @param <T>  Item type
     */
    protected static class Context<T> {
        
        /**
         * Current basic item lexer, to be replaced upon new item.
         */
        public ItemLexer<T> lexer;
        
        /**
         * Current item list.
         */
        public List<T> results; 
        
        /**
         * Basic item lexer factory.
         */
        public final Supplier<ItemLexer<T>> factory;
        
        /**
         * Delimiter character, or '\0' for no delimiter.
         */
        public final char delimiter;
        
        /**
         * True to accept optional extra delimiter character at the end, false otherwise
         */
        public final boolean strict;

        /**
         * Constructor.
         * @param factory  Basic item lexer factory
         * @param delimiter  Delimiter character, or '\0' for no delimiter
         * @param strict   True to accept optional extra delimiter character at the end, false otherwise
         */
        public Context(Supplier<ItemLexer<T>> factory, char delimiter, boolean strict) {
            this.factory = factory;
            this.delimiter = delimiter;
            this.strict = strict;
        }
        
    }
}
