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

import jacobi.api.Matrix;
import jacobi.core.util.Pair;
import jacobi.test.annotations.*;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@JacobiImport("/jacobi/test/data/DiscreteFourierTransformTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class DiscreteFourierTransformTest {

    @JacobiInject(0)
    public Matrix inRe;

    @JacobiInject(1)
    public Matrix inIm;

    @JacobiResult(10)
    public Matrix outRe;

    @JacobiResult(11)
    public Matrix outIm;

    @Test
    @JacobiImport("Complex 7x3")
    @JacobiEquals(expected =  10, actual = 10)
    @JacobiEquals(expected =  11, actual = 11)
    public void testComplex7x3() {
        //this.compute(new DiscreteFourierTransform.Forward());
    }

    public void compute(DiscreteFourierTransform dft) {
        Pair out = this.inIm == null ? dft.compute(this.inRe) : dft.compute(this.inRe, this.inIm);
        this.outRe = out.getLeft();
        this.outIm = out.getRight();
    }

}