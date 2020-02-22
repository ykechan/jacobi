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
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
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
        new ShiftedQR((m, p, i, j, up) -> -1, 100.0){

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
        new ShiftedQR((m, p, i, j, up) -> -1, 100.0){

            @Override
            protected void shiftDiag(Matrix matrix, int begin, int end, double shift) {
                super.shiftDiag(matrix, begin, end, shift);
                if(shiftCount.getAndIncrement() == 0){
                    Jacobi.assertEquals(shifted, matrix);
                }
            }

        }.compute(input, null, 1, 5, false);
    }
    
    @Test
    @SuppressWarnings("InfiniteRecursion")
    public void testFallThroughOnSmallMatrices() {
        AtomicInteger call1x1 = new AtomicInteger(0);
        AtomicInteger call2x2 = new AtomicInteger(0);
        new ShiftedQR( (m, p, i, j, up) -> { call1x1.incrementAndGet(); return -1; }, 100.0 )
                .compute(Matrices.zeros(3, 3), null, 2, 3, true);
        new ShiftedQR( (m, p, i, j, up) -> { call2x2.incrementAndGet(); return -1; }, 100.0 )
                .compute(Matrices.zeros(3, 3), null, 1, 3, true);
        Assert.assertEquals(1, call1x1.get());
        Assert.assertEquals(1, call2x2.get());
    }
    
    @Test
    @JacobiImport("Bad shift in 5x5")
    @SuppressWarnings("InfiniteRecursion") // false positive
    public void testBadShiftIn5x5() {
        AtomicBoolean marker = new AtomicBoolean(false);
        new ShiftedQR((m, p, i, j, up) -> {
            marker.set(true);
            return -1;
        }, 10000.0){

            @Override
            protected double getShift(Matrix matrix, int begin, int end) {
                double shift = super.getShift(matrix, begin, end);
                Assert.assertEquals(0.0, shift, 1e-16);
                return shift;
            }
            
        }.compute(input, null, 0, 5, false);
        Assert.assertTrue(marker.get());
    }
    
}
