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

package jacobi.core.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.IntBinaryOperator;

/**
 * Implementation of divide-and-conquer algorithm.
 * 
 * <p>The divide-and-conquer algorithm is generalized as follows:
 * Given a range [a, b), find a &lt; p &lt; b s.t.&nbsp;[a, p) and [p, b) can be handled independently, until
 * there is only a single element within the range.</p>
 * 
 * <p>In numerical methods, it is often that the splitting point p can only be found after several iteration.
 * This class also supports when such splitting function returns out-of-range splitting point as no splitting point
 * found. To vaoid indefinite computation, this class poses a limit of iterations on finding a single p w.r.t. the 
 * length of the range.</p>
 * 
 * @author Y.K. Chan
 */
public class Divider {
    
    /**
     * Default multiplier of iteration limit.
     */
    public static final int DEFAULT_ITER_LIMIT = 16;
    
    /**
     * Construct a divider with repeating splitting function until a splitting point is found, with default 
     * iteration limit.
     * @param splitter  Attempt splitting function
     * @return  Divider object
     */
    public static Divider repeats(IntBinaryOperator splitter) {
        return Divider.repeats(splitter, DEFAULT_ITER_LIMIT);
    }
    
    /**
     * Construct a divider with repeating splitting function until a splitting point is found.
     * @param splitter  Attempt splitting function
     * @param limit  Multiplier of iteration limit
     * @return  Divider object
     */
    public static Divider repeats(IntBinaryOperator splitter, int limit){
        return new Divider((begin, end) -> {
            int max = limit * (end - begin);
            for(int i = 0; i < max; i++){
                int at = splitter.applyAsInt(begin, end);
                if(at > begin && at < end){
                    return at;
                }
            }
            throw new IllegalStateException("Unable to split in " + max + " iterations.");
        });
    }

    /**
     * Constructor.
     * @param splitter  Splitting function
     */
    public Divider(IntBinaryOperator splitter) {
        this.splitter = splitter;
    }
    
    /**
     * Divide-and-conquer the given range.
     * @param begin  Begin index
     * @param end   End index
     * @return  An identity function for fluent interface
     */
    public Echoer visit(int begin, int end) {
        Deque<Range> stack = new ArrayDeque<>(16);
        stack.push(new Range(begin, end));
        while(!stack.isEmpty()){
            Range range = stack.pop();
            if(range.end - range.begin < 2){
                continue;
            }
            int at = this.splitter.applyAsInt(range.begin, range.end);
            if(at <= range.begin || at >= range.end){
                throw new UnsupportedOperationException("Unable to split at " + at + " in " + range);
            }
            if(at - range.begin > 1){
                stack.push(new Range(range.begin, at));
            }
            if(range.end - at > 1){
                stack.push(new Range(at, range.end));
            }
        }
        return INSTANCE;
    }

    private IntBinaryOperator splitter;
    
    /**
     * A type-safe identity function
     */
    public static final class Echoer {
        
        /**
         * Return the argument.
         * @param <T>  Argument and return type
         * @param t  Argument
         * @return   Argument
         */
        public <T> T echo(T t) {
            return t;
        }
        
    }
    
    /**
     * Data object for range.
     */
    protected static final class Range {
        
        /**
         * Begin and end index.
         */
        public final int begin, end;

        /**
         * Constructor.
         * @param begin  Begin index
         * @param end  End index
         */
        public Range(int begin, int end) {
            this.begin = begin;
            this.end = end;
        }

        @Override
        public String toString() {
            return "[" + this.begin + "," + this.end + ")";
        }
        
    }
    
    private static final Echoer INSTANCE = new Echoer();
    
}
