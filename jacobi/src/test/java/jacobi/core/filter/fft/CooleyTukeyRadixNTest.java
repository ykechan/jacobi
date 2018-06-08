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

import jacobi.core.givens.Givens;
import org.junit.Assert;
import org.junit.Test;

import java.util.Random;
import java.util.stream.IntStream;

public class CooleyTukeyRadixNTest {

    @Test
    public void testResultEquivalentWithRadix2Impl() {
        /*
        Random rand = new Random(Double.doubleToLongBits(Math.E));
        ComplexVector vector = ComplexVector.of(
            new double[]{-45.88028833,88.28727398,103.4633919,70.07072038,124.9423701,96.53219416},
            new double[]{-13.16386466,-126.6759578,106.4846306,-23.85915046,18.94474239,109.4681657}
        );

        ComplexVector result2 = vector.slice(0, vector.length());
        ComplexVector resultN = vector.slice(0, vector.length());
        new CooleyTukeyRadix2().merge(result2, 0, result2.length());
        new CooleyTukeyRadixN(2){

            @Override
            protected ComplexVector merge(Slice slice, int mod, Givens giv) {
                System.out.println("Giv=" + giv);
                return super.merge(slice, mod, giv);
            }
        }.merge(resultN, 0, resultN.length());

        Assert.assertArrayEquals(result2.real, resultN.real, 1e-12);
        Assert.assertArrayEquals(result2.imag, resultN.imag, 1e-12);
        */
    }

}