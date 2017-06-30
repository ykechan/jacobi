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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Supplier;

/**
 * Map-Reduce implementation using Fork-Join Framework.
 * 
 * <p>This implementation is equivalent to 
 * 
 *     IntStream.range(begin, end).parallel().map(mapper).reduce(reduer);</p>
 * 
 * <p>While it is convenient to use IntStream, it lacks an important feature that potentially
 * damage the efficiency: it lacks control on the number of works each thread should do. For example when
 * dealing with matrices that has many more rows than columns, it would be more efficient for a
 * thread to handle more rows than spawning more threads.</p>
 * 
 * <p>This class allows user to specify which to split the task when the range is above a certain
 * limit, to make sure each thread has adequate amount of work to do.</p>
 * 
 * <p>It also skips using parallelism if the amount of work falls under the limit.</p>
 * 
 * @author Y.K. Chan
 * @param <T>  Result type
 */
public class MapReducer<T> implements Supplier<T> {
    
    /**
     * Default maximum number of flop for a single thread.
     */
    public static final int DEFAULT_NUM_FLOP = 2048;
   
    /**
     * Specify a range of indices to begin building a MapReducer.
     * @param begin  Begin index inclusive
     * @param end  End index exclusive
     * @return  Builder helper class
     */
    public static Ranged of(int begin, int end) {
        return (limit) -> new Limited() {

            @Override
            public <T> Mapped<T> map(BiFunction<Integer, Integer, T> mapper) {
                return (reducer) -> new MapReducer<>(mapper, reducer, begin, end, Math.max(limit, 1));
            }
            
        };
    }
    
    /**
     * Constructor.
     * @param mapper Mapper function
     * @param reducer  Reducer function
     * @param begin  Begin of integer range
     * @param end  End of integer range
     * @param limit  Bottom-line range for a single thread to do.
     * @throws  IllegalArgumentException if limit &lt; 2
     */
    public MapReducer(BiFunction<Integer, Integer, T> mapper, BinaryOperator<T> reducer, int begin, int end, int limit) {
        if(limit < 2){
            throw new IllegalArgumentException("Limit too small.");
        }
        this.mapper = mapper;
        this.reducer = reducer;
        this.begin = begin;
        this.end = end;
        this.limit = limit;        
    }
    
    @Override
    public T get() {
        if(end - begin < limit){
            return this.mapper.apply(begin, end);
        }        
        return ForkJoinPool.commonPool().invoke(new Task(begin, end));
    }
    
    private int begin, end, limit;
    private BiFunction<Integer, Integer, T> mapper;
    private BinaryOperator<T> reducer; 
    
    /**
     * Recursive task in Fork-Join Framework
     */
    protected class Task extends RecursiveTask<T> {

        /**
         * Constructor.
         * @param from  Starting index of range, inclusive
         * @param to   Finishing index of range, exclusive
         */
        public Task(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override 
        protected T compute() {
            if(to - from < limit){ 
                return mapper.apply(from, to);
            }
            int mid = (from + to) / 2; 
            Task left = new Task(from, mid);
            Task right = new Task(mid, to);
            left.fork();
            T result = right.compute();
            return reducer.apply(left.join(), result);
        }
        
        private int from, to;
    }    
    
    /**
     * Builder helper class
     */
    public interface Ranged {
        
        /**
         * Specify the maximum number of work a thread should be allocated to.
         * @param num  Maximum number of work for a single thread
         * @return  Builder helper class
         */
        public Limited limit(int num);
        
        /**
         * Specify the number of flop for each index and use the default number of flop
         * to compute the maximum number of work a thread should be allocated to.
         * @param count  Number of flop for each index
         * @return  Builder helper class
         */
        public default Limited flop(int count) {
            return this.limit(Math.max(DEFAULT_NUM_FLOP / count, 2));
        }
        
    }
    
    /**
     * Builder helper class
     */
    public interface Limited {
        
        /**
         * Specify the mapping function from a range of index to a result.
         * @param <T>  Return type
         * @param mapper  Mapping function [a, b] -&gt; T where a and b are begin and end indices
         * @return  Builder helper class
         */
        public <T> Mapped<T> map(BiFunction<Integer, Integer, T> mapper);
        
        /**
         * Compute task and return a given value.
         * @param <T>  Type of return value
         * @param task  Task for a given index range
         * @param returnValue  Value to be returned after completion
         * @return  returnValue
         */
        public default <T> T forEach(BiConsumer<Integer, Integer> task, T returnValue) {
            this.forEach(task);
            return returnValue;
        }
        
        /**
         * Compute task without result.
         * @param task  Task for a given index range
         */
        public default void forEach(BiConsumer<Integer, Integer> task) {
            this.<Void>map((i, j) -> {
                task.accept(i, j);
                return null; 
            }).reduce((a, b) -> null).get();
        }
        
    }
    
    /**
     * Builder helper class
     * @param <T>  Return type
     */
    public interface Mapped<T> {
        
        /**
         * Specify the reducer function to combine result from different ranges.
         * @param reducer  Reducer function
         * @return  Resultant MapReducer object
         */
        public MapReducer<T> reduce(BinaryOperator<T> reducer);
        
    }
        
}
