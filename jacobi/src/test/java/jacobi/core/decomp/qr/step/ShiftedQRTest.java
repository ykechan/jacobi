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
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/ShiftedQRTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class ShiftedQRTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Matrix shifted;
    
    @Test
    @JacobiImport("Shift last element 4x4")
    public void testShiftLastElement4x4() {
        AtomicInteger shiftCount = new AtomicInteger(0);
        new ShiftedQR((m, p, i, j, up) -> {}, 100.0){

            @Override
            protected void shiftDiag(Matrix matrix, int begin, int end, double shift) {
                super.shiftDiag(matrix, begin, end, shift);
                if(shiftCount.getAndIncrement() == 0){
                    Jacobi.assertEquals(shifted, matrix);
                }
            }

        }.compute(input, null, 0, 4, false);
    }
    
    @Test
    @JacobiImport("Shift first element 4x4 in 6x6")
    public void testShiftFirstElement4x4In6x6() {
        AtomicInteger shiftCount = new AtomicInteger(0);
        new ShiftedQR((m, p, i, j, up) -> {}, 100.0){

            @Override
            protected void shiftDiag(Matrix matrix, int begin, int end, double shift) {
                super.shiftDiag(matrix, begin, end, shift);
                if(shiftCount.getAndIncrement() == 0){
                    Jacobi.assertEquals(shifted, matrix);
                }
            }

        }.compute(input, null, 1, 5, false);
    }
    
}
