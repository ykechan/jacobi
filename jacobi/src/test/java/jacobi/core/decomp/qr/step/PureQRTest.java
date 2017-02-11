/* 
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/PureQRTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class PureQRTest {
        
    @JacobiInject(-1)
    public Map<Integer, Matrix> steps;
    
    @JacobiResult(100)
    public Matrix partner;
    
    @Test
    @JacobiImport("3x3")
    @JacobiEquals(expected = 100, actual = 100)
    public void test3x3() {
        QRStep qrStep = new PureQR();
        Matrix input = this.steps.get(1);
        this.partner = Matrices.identity(3);
        int k = 1;
        while(steps.containsKey(++k)){
            qrStep.compute(input, partner, 0, input.getRowCount(), true);
            Jacobi.assertEquals(steps.get(k), input);
        }        
    }
    
    @Test
    @JacobiImport("3x3(2)")
    public void test3x3Two() {
        QRStep qrStep = new PureQR();
        Matrix input = this.steps.get(1);
        qrStep.compute(input, null, 0, input.getRowCount(), true);
        Jacobi.assertEquals(steps.get(2), input);
    }

    @Test
    @JacobiImport("4x4")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4() {
        QRStep qrStep = new PureQR();
        Matrix input = this.steps.get(0);
        this.partner = Matrices.identity(4);
        int k = 0;
        while(steps.containsKey(++k)){
            qrStep.compute(input, partner, 0, input.getRowCount(), true);
            Jacobi.assertEquals(steps.get(k), input);
        }
    }
    
}
