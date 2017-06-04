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

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
@SuppressWarnings("InfiniteRecursion") // false positive
public class DividerTest {
    
    @Test    
    public void testDivideInHalf() {
        int n = 128;
        boolean[] mark = new boolean[n];
        Arrays.fill(mark, false);
        new Divider((a, b) -> {
            if(b - a == 2){
                mark[a] = true;
                mark[a + 1] = true;
            }
            return (a + b) / 2;
        }).visit(0, n);
        Assert.assertFalse(IntStream.range(0, n).mapToObj((i) -> mark[i]).filter((b) -> !b).findAny().isPresent());
    }
    
    @Test
    public void testDivideLinear() {
        int n = 128;
        boolean[] mark = new boolean[n];
        Arrays.fill(mark, false);
        new Divider((a, b) -> {
            if(b - a == 2){
                mark[a] = true;
            }
            mark[b - 1] = true;
            return b - 1;
        }).visit(0, n);
        Assert.assertFalse(IntStream.range(0, n).mapToObj((i) -> mark[i]).filter((b) -> !b).findAny().isPresent());
    }
    
    @Test
    public void testDivideLinearFromBegin() {
        int n = 128;
        boolean[] mark = new boolean[n];
        Arrays.fill(mark, false);        
        new Divider((a, b) -> {
            if(b - a == 2){
                mark[b - 1] = true;
            }
            mark[a] = true;
            return a + 1;
        }).visit(0, n);
        Assert.assertFalse(IntStream.range(0, n).mapToObj((i) -> mark[i]).filter((b) -> !b).findAny().isPresent());
    }
    
    @Test
    public void testLaggedDivideFromMiddle() {
        AtomicInteger tick = new AtomicInteger(0);
        int n = 64;
        boolean[] mark = new boolean[n];
        Arrays.fill(mark, false);
        Divider.repeats((a, b) -> {
            if(tick.incrementAndGet() % 3 == 0){
                if(b - a == 2){
                    mark[a] = true;
                }
                mark[b - 1] = true;
                return (a + b) / 2;
            }
            return -1;
        }).visit(0, n);
        Assert.assertFalse(IntStream.range(0, n).mapToObj((i) -> mark[i]).filter((b) -> !b).findAny().isPresent());
    }
    
    @Test(expected = IllegalStateException.class)
    public void testAttemptDivideNeverSplit() {
        //MapReducer.divide((a, b) -> -1, 0, 8);
        Divider.repeats((a, b) -> -1).visit(0, 8);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testDivideBeyond() {
        new Divider((a, b) -> b).visit(0, 8);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testDivideBefore() {
        new Divider((a, b) -> a).visit(0, 8);
    }
    
}
