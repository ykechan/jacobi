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
import jacobi.api.ext.Prop;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/TraceTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class TraceTest {
    
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiResult(2)
    public Matrix ans;
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x5() {
        this.ans = Matrices.scalar(new Trace().compute(this.input));
    }
    
    @Test
    @JacobiImport("7x7")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x7() {
        this.ans = Matrices.scalar(new Trace().compute(this.input));
    }
    
    @Test
    @JacobiImport("Diag 6x6")
    @JacobiEquals(expected = 2, actual = 2)
    public void testDiag6x6() {
        this.ans = Matrices.scalar(new Trace().compute(Matrices.diag(this.input.getRow(0))));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNotSquareMatrix() {
        new Trace().compute(Matrices.zeros(3, 2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullMatrix() {
        new Trace().compute(null);
    }
    
    @Test
    public void testEmptyMatrix() {
        Assert.assertEquals(0.0, new Trace().compute(Matrices.zeros(0)), 1e-24);
    }
}
