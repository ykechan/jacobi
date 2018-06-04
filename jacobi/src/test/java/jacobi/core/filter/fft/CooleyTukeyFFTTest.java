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

package jacobi.core.filter.fft;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class CooleyTukeyFFTTest {

    @Test
    public void testSplitRadix2() {
        ComplexVector in = ComplexVector.of(
                new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0},
                new double[]{0.0, -1.0, -2.0, -3.0, -4.0, -5.0}
        );
        ComplexVector out = ComplexVector.of(new double[6], new double[6]);

        new CooleyTukeyFFT().split(in, out, 0, in.length(), 2);

        Assert.assertArrayEquals(new double[]{0.0, 2.0, 4.0, 1.0, 3.0, 5.0}, out.real, 1e-12);
        Assert.assertArrayEquals(new double[]{0.0, -2.0, -4.0, -1.0, -3.0, -5.0}, out.imag, 1e-12);
    }

    @Test
    public void testSplitRadix3() {
        ComplexVector in = ComplexVector.of(
                new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0},
                new double[]{0.0, -1.0, -2.0, -3.0, -4.0, -5.0}
        );
        ComplexVector out = ComplexVector.of(new double[6], new double[6]);

        new CooleyTukeyFFT().split(in, out, 0, in.length(), 3);

        Assert.assertArrayEquals(new double[]{0.0, 3.0, 1.0, 4.0, 2.0, 5.0}, out.real, 1e-12);
        Assert.assertArrayEquals(new double[]{0.0, -3.0, -1.0, -4.0, -2.0, -5.0}, out.imag, 1e-12);
    }

    @Test
    public void testSplitLength8Radix2() {
        ComplexVector in = ComplexVector.of(
                new double[]{0.0, 2.0, 4.0, 6.0, 1.0, 3.0, 5.0, 7.0},
                new double[]{0.0, -2.0, -4.0, -6.0, -1.0, -3.0, -5.0, -7.0}
        );
        ComplexVector out = ComplexVector.of(new double[8], new double[8]);

        new CooleyTukeyFFT().split(in, out, 0, 4, 2).split(in, out, 4, 4, 2);

        Assert.assertArrayEquals(new double[]{
                0.0, 4.0, 2.0, 6.0, 1.0, 5.0, 3.0, 7.0
        }, out.real, 1e-12);
        Assert.assertArrayEquals(new double[]{
                0.0, -4.0, -2.0, -6.0, -1.0, -5.0, -3.0, -7.0
        }, out.imag, 1e-12);
    }

    @Test
    public void testSplitLength8() {
        ComplexVector result = new CooleyTukeyFFT().split(
                ComplexVector.of(
                        new double[]{0.0, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0},
                        new double[]{0.0, -1.0, -2.0, -3.0, -4.0, -5.0, -6.0, -7.0}
                ),
                ComplexVector.of(new double[8], new double[8]),
                new int[]{2, 2});
        // 1st split: 0 2 4 6 1 3 5 7
        // 2nd split: 0 4 2 6 1 5 3 7
        Assert.assertArrayEquals(new double[]{0.0, 4.0, 2.0, 6.0, 1.0, 5.0, 3.0, 7.0}, result.real, 1e-12);
        Assert.assertArrayEquals(new double[]{0.0, -4.0, -2.0, -6.0, -1.0, -5.0, -3.0, -7.0}, result.imag, 1e-12);
    }

    @Test
    public void testFactorizeLength16Radix2Baseline1(){
        Assert.assertArrayEquals(
                new int[]{2, 2, 2, 2, 1},
                new CooleyTukeyFFT().factorize(16, new int[]{2}, new int[]{1}));
    }

    @Test
    public void testFactorizeLength6Radix2And3Baseline1(){
        Assert.assertArrayEquals(
                new int[]{2, 3, 1},
                new CooleyTukeyFFT().factorize(6, new int[]{2, 3}, new int[]{1}));
    }

    @Test
    public void testFactorizeShouldPreserveThePriorityOfRadixArray() {
        Assert.assertArrayEquals(
                new int[]{3, 2, 1},
                new CooleyTukeyFFT().factorize(6, new int[]{3, 2}, new int[]{1}));
    }

    @Test
    public void test1ShouldBeBaselineWithoutSpecifying(){
        Assert.assertArrayEquals(
                new int[]{2, 3, 1},
                new CooleyTukeyFFT().factorize(6, new int[]{2, 3}, new int[]{5}));
        Assert.assertArrayEquals(
                new int[]{2, 3, 1},
                new CooleyTukeyFFT().factorize(6, new int[]{2, 3}, new int[0]));
    }

    @Test
    public void testLengthAsBaselineWhenNoRadixSpecified() {
        Assert.assertArrayEquals(
                new int[]{6},
                new CooleyTukeyFFT().factorize(6, new int[0], new int[]{5}));
    }

    @Test
    public void testLengthAsBaselineWhenNotDecomposible() {
        Assert.assertArrayEquals(
                new int[]{7},
                new CooleyTukeyFFT().factorize(7, new int[]{2, 3}, new int[]{5}));
    }

    @Test
    public void testBaselineShouldBePreferredOverFactorizing() {
        Assert.assertArrayEquals(
                new int[]{3, 6},
                new CooleyTukeyFFT().factorize(18, new int[]{2, 3}, new int[]{6}));
    }

    @Test
    public void testFactorizingShouldBePreferredWhenPrimedAfterBaseline() {
        Assert.assertArrayEquals(
                new int[]{2, 3, 7},
                new CooleyTukeyFFT().factorize(7 * 6, new int[]{2, 3}, new int[]{6}));
    }

    @Test
    public void testFactorize1920() {
        Assert.assertArrayEquals(
                new int[]{2, 2, 2, 2, 2, 2, 2, 3, 5, 1},
                new CooleyTukeyFFT().factorize(1920,
                    new int[]{2, 3, 5},
                    new int[0]));
    }

}