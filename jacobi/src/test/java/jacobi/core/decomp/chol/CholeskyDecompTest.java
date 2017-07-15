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
package jacobi.core.decomp.chol;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.prop.Transpose;
import jacobi.core.util.Pair;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/CholeskyDecompTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class CholeskyDecompTest {
    
    @JacobiInject(2)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix lower;
    
    @Test
    @JacobiImport("3x3")
    @JacobiEquals(expected = 1, actual = 1)
    public void test3x3() {
        this.lower = new CholeskyDecomp().compute(this.input).get();
    }

    @Test
    @JacobiImport("4x4")
    @JacobiEquals(expected = 1, actual = 1)
    public void test4x4() {
        this.lower = new CholeskyDecomp().compute(this.input).get();
    }
    
    @Test
    @JacobiImport("4x4")
    @JacobiEquals(expected = 1, actual = 1)
    public void test4x4Both() {
        Pair pair = new CholeskyDecomp().computeBoth(this.input).get();
        this.lower = pair.getLeft();
        Jacobi.assertEquals(new Transpose().compute(pair.getLeft()), pair.getRight());
    }
    
    @Test
    @JacobiImport("4x4")
    public void testPositiveDefinite() {
        Assert.assertTrue(new CholeskyDecomp().isPositiveDefinite(input));
    }
    
    @Test
    @JacobiImport("Not positive definite 5x5")
    public void testNotPositiveDefinite5x5() {
        Assert.assertFalse(new CholeskyDecomp().compute(input).isPresent());
        Assert.assertFalse(new CholeskyDecomp().isPositiveDefinite(input));
    }
    
    @Test
    @JacobiImport("Symmetric 5x5")
    @JacobiEquals(expected = 1, actual = 1)
    public void testSymmetric5x5() {
        double[] ans = new CholeskyDecomp().computeSquared(this.input.getRow(0)).get();
        this.lower = this.front(ans);
    }
    
    @Test
    @JacobiImport("Not positive def tri-diag 5x5")
    public void testNotPositiveDefiniteTriDiag5x5() {
        Assert.assertFalse(new CholeskyDecomp().computeSquared(this.input.getRow(0)).isPresent());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void test5x4() {
        new CholeskyDecomp().compute(Matrices.zeros(5, 4)).get();
    }
    
    protected Matrix front(double[] zElem) {
        return Matrices.wrap(new double[][]{
            IntStream.range(0, zElem.length / 2).mapToDouble((i) -> zElem[4*(i/2) + (i % 2)] ).toArray()
        });
    }
    
}
