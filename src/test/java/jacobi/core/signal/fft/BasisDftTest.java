/*
 * The MIT License
 *
 * Copyright (c) 2018 Y.K. Chan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jacobi.core.signal.fft;


import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.signal.ComplexVector;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@JacobiImport("/jacobi/test/data/BasisDftTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class BasisDftTest {

    @JacobiInject(0)
    public Matrix input;

    @JacobiResult(1)
    public Matrix output;


    @Test
    @JacobiImport("Complex DFT 7")
    @JacobiEquals(expected = 1, actual = 1)
    public void testComplexDFT7() {
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));
        this.mockNoSpecialCase().merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }

    @Test
    public void testLength2ShouldBeASpecialCase(){
        AtomicInteger counter = new AtomicInteger(0);
        Random rand = new Random(Double.doubleToLongBits(Math.E));

        ComplexVector vec = ComplexVector.of(
                new double[]{rand.nextDouble(), rand.nextDouble()},
                new double[]{rand.nextDouble(), rand.nextDouble()}
        );

        ComplexVector out0 = vec.slice(0, vec.length());
        this.mockNoSpecialCase().merge(out0, 0, out0.length());
        ComplexVector out1 = vec.slice(0, vec.length());
        this.mock(counter).merge(out1, 0, out1.length());

        Assert.assertEquals(0, counter.get());
        Assert.assertArrayEquals(out0.real, out1.real, 1e-12);
        Assert.assertArrayEquals(out0.imag, out1.imag, 1e-12);
    }

    @Test
    @JacobiImport("Complex DFT 3")
    @JacobiEquals(expected = 1, actual = 1)
    public void testLength3ShouldBeASpecialCase(){
        AtomicInteger counter = new AtomicInteger(0);
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));
        this.mock(counter).merge(vec, 0, vec.length());
        Assert.assertEquals(0, counter.get());

        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }

    @Test
    @JacobiImport("Complex DFT 6")
    @JacobiEquals(expected = 1, actual = 1)
    public void testLength6ShouldBeASpecialCase(){
        AtomicInteger counter = new AtomicInteger(0);
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));
        this.mock(counter).merge(vec, 0, vec.length());
        Assert.assertEquals(0, counter.get());

        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testErrorWhenVectorExceedLimit() {
        new BasisDft(10).merge(ComplexVector.of(new double[20], new double[20]), 0, 20);
    }

    protected BasisDft mockNoSpecialCase() {
        return new BasisDft(1024){

            @Override
            public void merge(ComplexVector vector, int offset, int length) {
                this.computeN(vector, offset, length);
            }
        };
    }

    protected BasisDft mock(AtomicInteger counter) {
        return new BasisDft(1024){

            @Override
            protected void computeN(ComplexVector vector, int offset, int length) {
                counter.incrementAndGet();
                super.computeN(vector, offset, length);
            }
        };
    }

}
