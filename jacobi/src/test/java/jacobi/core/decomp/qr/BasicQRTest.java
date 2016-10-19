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

import jacobi.core.decomp.qr.step.QRStep;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.stream.IntStream;
import org.junit.Assert;
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
        for(int i = 1; i < this.input5x5.getRowCount(); i++){
            Assert.assertEquals(0.0, this.input5x5.get(i, i - 1), 1e-12);
        }
    }
   

    private QRStep mockStep(int... order) {
        return (m, p, begin, end, full) -> {
            for(int i = 0; i < order.length; i++){
                if(order[i] >= begin && order[i] < end){ 
                    int k = order[i];
                    order[i] = -1;
                    m.set(k, k - 1, 0.0);
                    return;
                }
            }
            throw new UnsupportedOperationException("Unable to find converge point in " 
                    + Arrays.toString(order)
                    + " within [" + begin + "," + end + ").");
        };
    }
}
