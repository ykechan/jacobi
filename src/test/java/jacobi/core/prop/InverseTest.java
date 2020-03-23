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
@JacobiImport("/jacobi/test/data/InverseTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class InverseTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix ans;
    
    @Test
    @JacobiImport("4x4")
    @JacobiEquals(expected = 1, actual = 1)
    public void test4x4() {
        ans = new Inverse().compute(this.input);
        System.out.println(ans);
    }
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 1, actual = 1)
    public void test5x5() {
        ans = new Inverse().compute(this.input);
    }
    
    @Test
    @JacobiImport("6x6")
    @JacobiEquals(expected = 1, actual = 1)
    public void test6x6() {
        ans = new Inverse().compute(this.input);
    }
    
    @Test
    @JacobiImport("3x3")
    @JacobiEquals(expected = 1, actual = 1)
    public void test3x3() {
        ans = new Inverse().compute(this.input);
    }
    
    @Test
    @JacobiImport("2x2")
    @JacobiEquals(expected = 1, actual = 1)
    public void test2x2() {
        ans = new Inverse().compute(this.input);
    }
    
    @Test
    @JacobiImport("1x1")
    @JacobiEquals(expected = 1, actual = 1)
    public void test1x1() {
        ans = new Inverse().compute(this.input);
    }
    
    @Test
    @JacobiImport("Singular 3x3")
    public void testSingular3x3() {
        Assert.assertFalse(new Inverse().computeMaybe(this.input).isPresent());
    }
    
    @Test
    @JacobiImport("Singular 2x2")
    public void testSingular2x2() {
        Assert.assertFalse(new Inverse().computeMaybe(this.input).isPresent());
    }
    
    @Test
    @JacobiImport("Singular 1x1")
    public void testSingular1x1() {
        Assert.assertFalse(new Inverse().computeMaybe(this.input).isPresent());
    }
    
    @Test
    public void testEmptyMatrix() {
        Assert.assertTrue(new Inverse().compute(Matrices.zeros(0)).getRowCount() == 0);
    }
    
    @Test
    public void testNonSquaredMatrix() {
        new Inverse().compute(Matrices.zeros(3, 2));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullMatrix() {
        new Inverse().compute(null);
    }
}
