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
package jacobi.core.data;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class PaddingPlanTest {
    
    @Test
    public void testApply() {
        // transfrorm <x, y> into <1.0, x, x^2>
        PaddingPlan pp = PaddingPlan.builder(2)
                .prepend((r) -> 1.0)
                .prepend((r) -> r.get(1) * r.get(1))
                .select(1, 2, 0)
                .build();
        double[] u = {3.0, 4.0};
        double[] v = pp.apply(pp.createBuffer(), u);
        Assert.assertArrayEquals(new double[]{1.0, 3.0, 3.0 * 3.0}, v, 1e-16);
        
        // transform <a, b, y> into <1.0, a, b, a*b, a^2, b^2>
        pp = PaddingPlan.builder(3)
                .append((r) -> 1.0)
                .append((r) -> r.get(0) * r.get(1))
                .append((r) -> r.get(0) * r.get(0))
                .append((r) -> r.get(1) * r.get(1))                
                .select(3, 0, 1, 4, 5, 6)
                .build();
        u = new double[]{2.0, 5.0, 9.0};
        v = pp.apply(pp.createBuffer(), u);
        Assert.assertArrayEquals(new double[]{1.0, 2.0, 5.0, 2.0 * 5.0, 2.0 * 2.0, 5.0 * 5.0}, v, 1e-16);
    }
    
    @Test
    public void testAppendPrependSelect() {
        // transfrorm <x, y> into <1.0, x, x^2>
        PaddingPlan pp = PaddingPlan.builder(2)
                .append((r) -> r.get(0) * r.get(0))
                .prepend((r) -> 1.0)
                .select(0, 1, 3)
                .build();
        double[] u = {3.0, 4.0};
        double[] v = pp.apply(pp.createBuffer(), u);
        Assert.assertArrayEquals(new double[]{1.0, 3.0, 3.0 * 3.0}, v, 1e-16);        
    }
    
    @Test
    public void testPrepareAppendPrependSelect() {
        PaddingPlan pp = PaddingPlan.builder(3)
                .append((r) -> 1.0)
                .prepend((r) -> 2.0)
                .append((r) -> 3.0)
                .prepend((r) -> 4.0)
                .insert(3, (r) -> 5.0)
                .select(2, 3)
                .build();
        // 4 2 ! 5 @ # 1 3 -> ! 5
        Buffer buffer = pp.createBuffer();
        Assert.assertEquals(2, buffer.getStartingPosition());
        Assert.assertEquals(8, buffer.getMaximumLength());
    }

    @Test
    public void testPrepareMultiSelect() {
        PaddingPlan pp = PaddingPlan.builder(3)
                .append((r) -> 1.0)
                .prepend((r) -> 2.0)
                .insert(3, (r) -> 5.0)
                .select(2, 4, 3)
                .append((r) -> 3.0)
                .prepend((r) -> 4.0)                
                .prepend((r) -> 5.0)
                .prepend((r) -> 6.0)
                .select(2, 3)
                .build();
        // 2 ! 5 @ # 1 -> 6 5 4 5 # @ 3 -> # @
        Buffer buffer = pp.createBuffer();
        Assert.assertEquals(3, buffer.getStartingPosition());
        Assert.assertEquals(7, buffer.getMaximumLength());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSelectNull() {
        PaddingPlan.builder(3)
                .append((r) -> 1.0)
                .prepend((r) -> 2.0)
                .insert(3, (r) -> 5.0)
                .select(null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSelectEmpty() {
        PaddingPlan.builder(3)
                .append((r) -> 1.0)
                .prepend((r) -> 2.0)
                .insert(3, (r) -> 5.0)
                .select(new int[0]);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testSelectInvalid() {
        PaddingPlan.builder(3)
                .append((r) -> 1.0)
                .prepend((r) -> 2.0)
                .insert(3, (r) -> 5.0)
                .select(100);
    }
}
