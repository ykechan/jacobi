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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class ParallelSupplierTest {
    
    @Test
    public void test() { 
        List<String> results = ParallelSupplier.of(() -> Thread.currentThread().getName(), 10).get();
        Set<String> set = new TreeSet<>(results);
        Assert.assertEquals(10, set.size());
    }
    
    @Test
    public void testJoin() {
        int n = 10;
        List<Supplier<String>> works = new ArrayList<>();
        for(int i = 0; i < n; i++){
            long t = i * 100L;
            works.add(() -> {
                try {
                    Thread.sleep(t);
                } catch (InterruptedException ex) {
                    throw new IllegalStateException(ex);
                }
               return Thread.currentThread().getName();
            });
        }
        List<String> results = ParallelSupplier.of(works).get();
        Set<String> set = new TreeSet<>(results);
        Assert.assertEquals(works.size(), set.size());
    }    
    
    @Test
    public void testCyclic() {
        int num = 128;
        AtomicInteger[] marker = IntStream.range(0, num).mapToObj((i) -> new AtomicInteger(0)).toArray((n) -> new AtomicInteger[n]);
        ParallelSupplier.cyclic((i) -> Assert.assertEquals(0, marker[i].getAndIncrement()), 0, num);
        Assert.assertFalse(Arrays.stream(marker).filter((i) -> i.get() == 0).findAny().isPresent());
    }
    
    @Test(expected = RuntimeException.class)
    public void testExceptionThrown() {
        AtomicInteger count = new AtomicInteger(0);
        Supplier<Void> wait = () -> {
                try {
                    Thread.sleep(300L);
                } catch (InterruptedException ex) {                    
                }
                count.incrementAndGet();
                return null;
            };
        ParallelSupplier.of(Arrays.asList( 
            () -> {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException ex) {
                }
                throw new RuntimeException();
            },
            wait,
            wait,
            wait
        )).get(); 
    }
    
    @Test(expected = RuntimeException.class)
    public void testExceptionThrownInCyclic() {
        int n = 128;
        AtomicInteger count = new AtomicInteger(0);
        try {
            ParallelSupplier.cyclic((i) -> {
                if(i == 32){
                    throw new RuntimeException();
                }
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException ex) {

                }
                count.getAndIncrement();
            }, 0, n);
        } catch(RuntimeException ex) {
            Assert.assertEquals(n - 1, count.get());
            throw ex;
        }
    }
    
}
