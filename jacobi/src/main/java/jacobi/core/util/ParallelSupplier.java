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

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Composite suppliers that will be run in parallel, each with its own thread.
 * 
 * In some scenarios, especially works are closely dependent of each other and synchronizing is frequent, fork-join 
 * model is not suitable since it requires spawning threads each time of synchronizing. Synchronization in ExecutorService
 * is hard since newFixedThreadPool guarantees maximum thread limit, but lesser threads may be spawned instead.
 * 
 * This class uses one thread per supplier so the number of threads is predictable.
 * 
 * @author Y.K. Chan
 * @param <T>  Individual return result
 */
public class ParallelSupplier<T> implements Supplier<List<T>> { 
    
    /**
     * Default number of threads. It is defined as a multiple of available processors, to obtain a higher performance
     * for processors supporting hyper-threading and a lower probability that most threads run on the same processor.
     */
    public static final int DEFAULT_NUM_THREADS = 4 * Runtime.getRuntime().availableProcessors();
    
    /**
     * Run a task multiple times in multiple threads, with default number of threads.
     * @param task 
     */
    public static void run(Runnable task) {
        ParallelSupplier.of(() -> { task.run(); return null; }).get();
    }
    
    /**
     * Run a task multiple times in multiple threads.
     * @param task  Task
     * @param numThreads  Number of times to run this task.
     */
    public static void run(Runnable task, int numThreads) {
        ParallelSupplier.of(() -> { task.run(); return null; }, numThreads).get();
    }
    
    /**
     * Create parallel supplier with single base supplier but run multiple times, with default number of threads.
     * @param <T>  Individual return result
     * @param supplier  Supplier
     * @return  Parallel supplier
     */
    public static <T> ParallelSupplier<T> of(Supplier<T> supplier) {
        return ParallelSupplier.of(supplier, DEFAULT_NUM_THREADS);
    }
    
    /**
     * Create parallel supplier from a list of suppliers.
     * @param <T>  Individual return result
     * @param suppliers  List of suppliers
     * @return  Parallel supplier
     */
    public static <T> ParallelSupplier<T> of(List<Supplier<T>> suppliers) {
        return new ParallelSupplier<>(suppliers);
    } 
   
    /**
     * Create parallel supplier with single base supplier but run multiple times.
     * @param <T>  Individual return result
     * @param supplier  Base supplier
     * @param numThreads  Number of times to run this supplier.
     * @return  Parallel supplier
     */
    public static <T> ParallelSupplier<T> of(Supplier<T> supplier, int numThreads) {
        return ParallelSupplier.of(IntStream.range(0, numThreads).mapToObj((i) -> supplier).collect(Collectors.toList()));
    }    

    /**
     * Constructor. 
     * @param suppliers  List of base suppliers
     */
    public ParallelSupplier(List<Supplier<T>> suppliers) {
        this.suppliers = suppliers.stream()
                .map((s) -> new Task<>(s))
                .collect(Collectors.toList());
    }

    @Override
    public List<T> get() {
        List<Thread> threads = this.suppliers.stream()
                .sequential()
                .skip(1)
                .map((t) -> new Thread(t))
                .collect(Collectors.toList());
        Task<T> first = this.suppliers.iterator().next();
        threads.forEach((t) -> t.start());
        try {
            first.run();
        } catch(RuntimeException ex) {
            threads.forEach((t) -> t.interrupt());
            throw ex;
        }
        threads.forEach((t) -> {
            try { 
                t.join(DEFAULT_TIMEOUT); 
            } catch (InterruptedException ex) { 
                throw new IllegalStateException(ex);
            } 
        }); 
        return this.suppliers.stream()
                .map((t) -> t.get())
                .collect(Collectors.toList());
    }    

    private List<Task<T>> suppliers;
    
    private static final long DEFAULT_TIMEOUT = 60 * 1000L;        
    
    /**
     * Supplier and Runnable with cached return result.
     * @param <T>  Individual return result
     */
    protected class Task<T> implements Supplier<T>, Runnable {

        /**
         * Constructor.
         * @param supplier  Base supplier
         */
        public Task(Supplier<T> supplier) {
            this.supplier = supplier;            
        }        

        @Override
        public T get() {
            return this.result;
        }                

        @Override
        public void run() {
            this.result = this.supplier.get();
        }
        
        private T result;
        private Supplier<T> supplier;
    }
}
