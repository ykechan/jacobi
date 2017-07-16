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
package jacobi.core.stats;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/HigherMomentTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class HigherMomentTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix skewness;
    
    @JacobiResult(2)
    public Matrix kurtosis;
    
    @Test
    @JacobiImport("17x7")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    public void test17x7() {
        this.skewness = Matrices.wrap(new double[][]{ new HigherMoment.Skewness().compute(input) });
        this.kurtosis = Matrices.wrap(new double[][]{ new HigherMoment.Kurtosis().compute(input) });
    }
    
    @Test
    @JacobiImport("4x9")
    @JacobiEquals(expected = 1, actual = 1)
    @JacobiEquals(expected = 2, actual = 2)
    public void test4x9() {
        this.skewness = Matrices.wrap(new double[][]{ new HigherMoment.Skewness().compute(input) });
        this.kurtosis = Matrices.wrap(new double[][]{ new HigherMoment.Kurtosis().compute(input) });
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNull() {
        new HigherMoment((d) -> d, (v) -> v).compute(null);
    }
    
    @Test
    public void testEmpty() {
        Assert.assertEquals(0, new HigherMoment((d) -> d, (v) -> v).compute(Matrices.zeros(0)).length);
    }
    
}
