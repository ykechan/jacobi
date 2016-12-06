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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.shifts.DoubleShift;
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
@JacobiImport("/jacobi/test/data/FrancisQRCreateBulgeTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class FrancisQRCreateBulgeTest {
    
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiResult(2)
    public Matrix output;
    
    @JacobiResult(3)
    public Matrix partner;
    
    @Test
    @JacobiImport("5x5")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x5() {
        //this.output = new FrancisQR(new DefaultQRStep()).getDoubleShift1stCol(input, 0, input.getRowCount());
        this.output = DoubleShift.of(input, input.getRowCount() - 2).getImplicitQ(input, 0);
    }

    @Test
    @JacobiImport("5x5(2)")
    @JacobiEquals(expected = 2, actual = 2)
    public void test2nd5x5() {
        this.output = DoubleShift.of(input, input.getRowCount() - 2).getImplicitQ(input, 0);
    }
    
    @Test
    @JacobiImport("Step2 6x6")
    @JacobiEquals(expected = 2, actual = 2)
    public void testStepTwo6x6() {
        this.output = DoubleShift.of(input, input.getRowCount() - 3).getImplicitQ(input, 1);
    }
    
    @Test
    @JacobiImport("Bulge 5x5")
    @JacobiEquals(expected = 2, actual = 2)
    @JacobiEquals(expected = 3, actual = 3)
    public void testBulge5x5() {
        this.partner = Matrices.identity(5);
        new FrancisQR(new DefaultQRStep()).createBulge(input, partner, 0, input.getRowCount(), true);
        this.output = this.input;        
    }
    
    @Test
    @JacobiImport("Bulge Step 2 6x6")
    @JacobiEquals(expected = 2, actual = 2)
    public void testBulgeStepTwo6x6() {
        this.partner = Matrices.identity(6);
        new FrancisQR(new DefaultQRStep()).createBulge(input, partner, 1, input.getRowCount() - 1, true);
        this.output = this.input;
    }
}
