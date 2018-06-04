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

public class CooleyTukeyRadix3Test {

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

}