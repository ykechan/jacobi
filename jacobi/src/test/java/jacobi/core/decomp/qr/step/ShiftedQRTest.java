/*
 * Copyright (C) 2016 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
