/*
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class PairTest {
    
    @Test
    public void testEager() {
        Matrix a = Matrices.identity(2);
        Matrix b = Matrices.identity(3);
        Pair pair = Pair.of(a, b);
        Assert.assertTrue(pair.getLeft() == a);
        Assert.assertTrue(pair.getRight() == b);
    }
    
    @Test
    public void testLazyRight() {
        Matrix a = Matrices.identity(2);
        Matrix b = Matrices.identity(3);
        AtomicInteger count = new AtomicInteger(0);
        Pair pair = Pair.of(a, () -> { count.incrementAndGet(); return b; });        
        Assert.assertTrue(pair.getLeft() == a);
        Assert.assertEquals(0, count.get());
        Assert.assertTrue(pair.getRight() == b);
        Assert.assertEquals(1, count.get());
        Assert.assertTrue(pair.getRight() == b);
        Assert.assertEquals(1, count.get());
    }
    
    @Test
    public void testLazyLeft() {
        Matrix a = Matrices.identity(2);
        Matrix b = Matrices.identity(3);
        AtomicInteger count = new AtomicInteger(0);
        Pair pair = Pair.of(() -> { count.incrementAndGet(); return a; }, b);
        Assert.assertTrue(pair.getRight() == b);
        Assert.assertEquals(0, count.get());
        Assert.assertTrue(pair.getLeft() == a);        
        Assert.assertEquals(1, count.get());
        Assert.assertTrue(pair.getLeft() == a);
        Assert.assertEquals(1, count.get());
    }
}
