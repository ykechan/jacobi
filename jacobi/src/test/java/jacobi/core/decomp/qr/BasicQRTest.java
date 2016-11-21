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
package jacobi.core.decomp.qr;

import jacobi.api.Matrices;
import jacobi.core.decomp.qr.step.QRStep;
import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.DefaultQRStep;
import jacobi.core.impl.Empty;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/BasicQRTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class BasicQRTest {
    
    @JacobiInject(1)
    public Matrix input5x5;
    
    @JacobiInject(2)
    public Matrix input7x7;
    
    @Test
    @JacobiImport("Data")
    public void test5x5() {
        QRStrategy impl = new BasicQR(this.mockStep(
           1, 2, 3, 4
        ));
        impl.compute(input5x5, null, true);
    }
   
    @Test
    public void testEmptyMatrix() {
        new BasicQR(new DefaultQRStep()).compute(Empty.getInstance(), null, true);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullMatrix() {
        new BasicQR(new DefaultQRStep()).compute(null, null, true);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNonSquareMatrix() {
        new BasicQR(new DefaultQRStep()).compute(Matrices.zeros(3, 4), null, true);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMismatchParnerMatrix() {
        new BasicQR(new DefaultQRStep()).compute(Matrices.zeros(3, 3), Matrices.zeros(4, 4), true);
    }

    private QRStep mockStep(int... order) {
        AtomicInteger i = new AtomicInteger(0);
        return (m, p, begin, end, full) -> order[i.getAndIncrement()];
    }
}
