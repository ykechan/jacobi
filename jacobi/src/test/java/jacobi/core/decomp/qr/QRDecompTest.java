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

package jacobi.core.decomp.qr;

import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/QRDecompTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class QRDecompTest {
    
    @JacobiInject(0)
    public Matrix input;
    
    @JacobiInject(1)
    public Matrix step1;
    
    @JacobiInject(2)
    public Matrix step2;
    
    @JacobiInject(3)
    public Matrix step3;
    
    @JacobiInject(4)
    public Matrix step4;
    
    @JacobiInject(5)
    public Matrix step5;

    @Test
    @JacobiImport("4x4")
    public void test4x4() {
        this.assertEachStep(Arrays.asList(step1, step2, step3))
                .compute(input, null);
    }
    
    @Test
    @JacobiImport("6x6")
    public void test6x6() {
        this.assertEachStep(Arrays.asList(
            step1, step2, step3,
            step4, step5
        )).compute(input, null);
    }
    
    @Test
    @JacobiImport("9x3")
    public void test9x3() {
        this.assertEachStep(Arrays.asList(
            step1, step2, step3
        )).compute(input, null);
    }
    
    private QRDecomp assertEachStep(List<Matrix> expects) {
        return new QRDecomp(){

            @Override
            protected void eliminate(Matrix matrix, Matrix partner, int j, double[] column) {
                super.eliminate(matrix, partner, j, column);
                for(int i = j + 1; i < matrix.getRowCount(); i++){
                    matrix.set(i, j, 0.0);
                }
                Jacobi.assertEquals(expects.get(j), matrix);
            }
            
        };
    }
}
