/*
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan.
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Simple utility class for executing a number of tasks concurrently each with a single thread allocated.
 * 
 * This class is suited for computing a set of highly co-ordinated tasks that Fork-Join model and ExecutorService
 * is not suitable: both are unable to specifically set the number of threads running. (Executors.newFixedThreadPool
 * will create a thread pool with a limit of n threads, not guarantee that exactly n threads are used, making 
 * synchronization difficult.)
 * 
 * @author Y.K. Chan
 */
public abstract class Threads {

    /**
     * Constructor. Do not instantiate
     */
    private Threads() {
    }
    
    /**
     * Spawn multiple threads running the same work. 
     * @param <T>  Work result
     * @param work  Same instruction that will be run in parallel
     * @param numThreads  Number of threads to run
     * @param timeout  Time limit for each work
     * @return  A list of work result
     */
    public static <T> List<T> invoke(Supplier<T> work, int numThreads) { 
        return invokeAll(IntStream.range(0, numThreads).mapToObj((i) -> work).collect(Collectors.toList()));
    }
    
    /**
     * Do a group of works in parallel, each with its own thread, and collect the result.
     * @param <T> Work result
     * @param works  A group of works that will be done in parallel
     * @param timeout  Time limit for each work
     * @return  A list of work result
     */
    public static <T> List<T> invokeAll(Collection<? extends Supplier<T>> works) {
        if(works.isEmpty()){
            return Collections.emptyList();
        }
        if(works.size() < 2){
            return Collections.singletonList(works.iterator().next().get());
        }
        List<Task<T>> tasks = works.stream().map((w) -> new Task<>(w)).collect(Collectors.toList());
        List<Thread> threads = tasks.stream().sequential().skip(1).map((t) -> new Thread(t)).collect(Collectors.toList());
        threads.forEach((t) -> t.start());
        tasks.iterator().next().run();
        for(Thread t : threads){
            try {
                t.join(DEFAULT_TIMEOUT);
            } catch (InterruptedException ex) {
                throw new IllegalStateException(ex);
            }
        }
        return tasks.stream().map((t) -> t.get()).collect(Collectors.toList());
    }
    
    /**
     * A decorate of Supplier that is Runnable and cache result.
     * @param <T> Work result
     */
    protected static class Task<T> implements Runnable, Supplier<T> { 

        /**
         * Constructor.
         * @param supplier  Work to get the result
         */
        public Task(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        @Override
        public void run() {
            this.result = supplier.get();
        } 

        @Override
        public T get() {
            return this.result;
        }
        
        private T result;
        private Supplier<T> supplier;
    }
    
    private static final long DEFAULT_TIMEOUT = 30 * 60 * 1000L;
}
