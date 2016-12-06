/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
package jacobi.core.decomp.qr.step;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/SingleStep2x2Test.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class SingleStep2x2Test {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiResult(1)
    public Matrix output;
    
    @Test
    @JacobiImport("2x2")
    @JacobiEquals(expected = 1, actual = 1)
    public void test2x2() {
        this.mock().compute(input, null, 0, 2, true);
        this.output = this.input;
    }
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 1, actual = 1)
    public void test5x5() {
        this.mock().compute(input, null, 1, 3, true);
        this.output = this.input;
    }
    
    @Test
    @JacobiImport("Patho 2x2")
    @JacobiEquals(expected = 1, actual = 1)
    public void testPatho2x2() {
        this.mock().compute(input, null, 0, 2, true);
        this.output = this.input;
    }
    
    @Test
    @JacobiImport("Complex 2x2")
    @JacobiEquals(expected = 1, actual = 1)
    public void testComplex2x2() {
        this.mock().compute(input, null, 0, 2, true);
        this.output = this.input;
    }

    protected QRStep mock() {
        return new SingleStep2x2(new DefaultQRStep());
    }
}
