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
import jacobi.core.givens.Givens;
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

@JacobiImport("/jacobi/test/data/CooleyTukeyRadix3Test.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class CooleyTukeyRadix3Test {

    @JacobiInject(0)
    public Matrix input;

    @JacobiResult(1)
    public Matrix output;

    @Test
    @JacobiImport("Pure Real 6 DFT")
    @JacobiEquals(expected = 1, actual = 1)
    public void testPureReal6DFT() {
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));
        this.mockNoPivot().merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }

    @Test
    @JacobiImport("Pure Imag 6 DFT")
    @JacobiEquals(expected = 1, actual = 1)
    public void testPureImag6DFT() {
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));
        this.mockNoPivot().merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }

    @Test
    @JacobiImport("Complex 9 DFT")
    @JacobiEquals(expected = 1, actual = 1)
    public void testComplex9DFTNoPivot() {
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));
        this.mockNoPivot().merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }

    @Test
    @JacobiImport("Complex 9 DFT")
    @JacobiEquals(expected = 1, actual = 1)
    public void testComplex9DFTWithPivot() {
        ComplexVector vec = ComplexVector.of(this.input.getRow(0), this.input.getRow(1));
        new CooleyTukeyRadix3().merge(vec, 0, vec.length());
        this.output = Matrices.of(new double[][]{vec.real, vec.imag});
    }

    @Test
    public void testCrossMultIsEquivToGivensRotation() {
        Givens ei2PiOver3 = new Givens(1.0, Math.cos(2*Math.PI / 3), -Math.sin(2*Math.PI / 3));
        Givens ei4PiOver3 = new Givens(1.0, Math.cos(4*Math.PI / 3), -Math.sin(4*Math.PI / 3));

        CooleyTukeyRadix3 fft = new CooleyTukeyRadix3(null, null);

        Random rand = new Random(Double.doubleToLongBits(Math.E));
        for(int i = 0; i < 128; i++){
            double fRe = rand.nextDouble();
            double fIm = rand.nextDouble();

            double gRe = rand.nextDouble();
            double gIm = rand.nextDouble();

            Assert.assertEquals(ei2PiOver3.rotateX(fRe, fIm) + ei4PiOver3.rotateX(gRe, gIm), fft.crossMultRe(fRe, fIm, gRe, gIm), 1e-12);
            Assert.assertEquals(ei2PiOver3.rotateY(fRe, fIm) + ei4PiOver3.rotateY(gRe, gIm), fft.crossMultIm(fRe, fIm, gRe, gIm), 1e-12);
        }
    }

    protected CooleyTukeyRadix3 mockNoPivot() {
        return new CooleyTukeyRadix3(
                ComplexVector.of(new double[]{1.0}, new double[]{0.0}),
                ComplexVector.of(new double[]{1.0}, new double[]{0.0})
        );
    }

}