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

package jacobi.core.signal.conv;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@JacobiImport("/jacobi/test/data/CustomKernelTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class CustomKernelTest {

    @JacobiInject(0)
    public Matrix input;

    @JacobiInject(1)
    public Matrix kernel;

    @JacobiResult(10)
    public Matrix output;

    @Test(expected = IllegalArgumentException.class)
    public void testShouldFailIfGivenNullKernel() {
        new CustomKernel(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testShouldFailIfGivenEvenKernel() {
        new CustomKernel(new double[4]);
    }

    @Test
    @JacobiImport("test Signal 13 Kernel 5")
    @JacobiEquals(expected = 10, actual = 10)
    public void testSignal13Kernel5() {
        this.output = Matrices.wrap(new double[][]{
                this.mock().apply(this.input.getRow(0), new double[this.input.getColCount()])
        });
    }

    private CustomKernel mock() {
        return new CustomKernel(this.kernel.getRow(0));
    }

}