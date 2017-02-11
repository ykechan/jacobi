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
package jacobi.core.prop;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/RankTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class RankTest {
    
    @JacobiInject(1)
    public Matrix input;
    
    @Test
    @JacobiImport("5x5 Rank 3")
    public void test5x5Rank3() {
        Assert.assertEquals(3, new Rank().compute(this.input));
    }
    
    @Test
    @JacobiImport("7x3 Rank 2")
    public void test7x3Rank2() {
        Assert.assertEquals(2, new Rank().compute(this.input));
    }        
    
    @Test
    public void test1x1() {
        Assert.assertEquals(0, new Rank().compute(Matrices.scalar(1e-24)));
        Assert.assertEquals(1, new Rank().compute(Matrices.scalar(1.0)));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullMatrix() {
        new Rank().compute(null);
    }
    
    @Test
    public void testEmptyMatrix() {
        Assert.assertEquals(0, new Rank().compute(Matrices.zeros(0)));
    }
}
