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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;
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
    
}
