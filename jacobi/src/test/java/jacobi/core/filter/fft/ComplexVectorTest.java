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

public class ComplexVectorTest {

    @Test
    public void testMutableByBackingArray() {
        double[] re = {1.0, 2.0, 3.0, 4.0};
        double[] im = {4.0, 3.0, 2.0, 1.0};

        ComplexVector vec = ComplexVector.of(re, im);
        Arrays.fill(re, Math.PI);
        Arrays.fill(im, Math.E);

        Assert.assertArrayEquals(new double[]{Math.PI,Math.PI,Math.PI,Math.PI}, vec.real, 1e-12);
        Assert.assertArrayEquals(new double[]{Math.E,Math.E,Math.E,Math.E}, vec.imag, 1e-12);
    }

    @Test
    public void testSliceWouldCreateNewArray() {
        double[] re = {1.0, 2.0, 3.0, 4.0};
        double[] im = {4.0, 3.0, 2.0, 1.0};

        ComplexVector vec = ComplexVector.of(re, im).slice(1, 3);
        Arrays.fill(re, Math.PI);
        Arrays.fill(im, Math.E);

        Assert.assertArrayEquals(new double[]{2.0, 3.0}, vec.real, 1e-12);
        Assert.assertArrayEquals(new double[]{3.0, 2.0}, vec.imag, 1e-12);
        Assert.assertEquals(2, vec.length());
    }

    @Test
    public void testConjWouldCreateNewArray() {
        double[] re = {1.0, 2.0, 3.0, 4.0};
        double[] im = {4.0, 3.0, 2.0, 1.0};

        ComplexVector vec = ComplexVector.of(re, im).conj();
        Arrays.fill(re, Math.PI);
        Arrays.fill(im, Math.E);

        Assert.assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, vec.real, 1e-12);
        Assert.assertArrayEquals(new double[]{-4.0, -3.0, -2.0, -1.0}, vec.imag, 1e-12);
    }

    @Test
    public void test1To32RootsOfUnityMultConjIsUnit() {
        for(int n = 1; n < 33; n++){
            ComplexVector root = ComplexVector.rootsOfUnity(n);
            ComplexVector conj = root.conj();

            for(int i = 0; i < root.length(); i++) {
                Assert.assertEquals("Degree " + n + " real[" + i + "]",
                        1.0, root.real[i] * conj.real[i] - root.imag[i] * conj.imag[i], 1e-12);
                Assert.assertEquals("Degree " + n + " imag[" + i + "]",
                        0.0, root.real[i] * conj.imag[i] + root.imag[i] * conj.real[i], 1e-12);
            }
        }
    }

    @Test
    public void test1To32RootsOfUnityWouldCreateNewArray() {
        for(int n = 1; n < 33; n++){
            ComplexVector root = ComplexVector.rootsOfUnity(n);
            double[] re = Arrays.copyOf(root.real, root.length());
            double[] im = Arrays.copyOf(root.imag, root.length());
            Arrays.fill(root.real, Math.PI);
            Arrays.fill(root.imag, Math.E);
            root = ComplexVector.rootsOfUnity(n);
            Assert.assertArrayEquals(re, root.real, 1e-12);
            Assert.assertArrayEquals(im, root.imag, 1e-12);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailOnNegDegToRootsOfUnity() {
        ComplexVector.rootsOfUnity(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailOnZeroToRootsOfUnity() {
        ComplexVector v = ComplexVector.rootsOfUnity(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailToConstructWithNullRealPart() {
        ComplexVector.of(null, new double[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailToConstructWithNullImaginaryPart() {
        ComplexVector.of(new double[0], null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailToConstructWithDifferentArrayLength() {
        ComplexVector.of(new double[2], new double[3]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFailToConstructWithSameArrayInstance() {
        double[] array = {1.0, 2.0};
        ComplexVector.of(array, array);
    }

}