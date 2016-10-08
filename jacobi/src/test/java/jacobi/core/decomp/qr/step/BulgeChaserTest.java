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
@JacobiImport("/jacobi/test/data/BulgeChaserTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class BulgeChaserTest {
    
    @JacobiInject(-1)
    public Map<Integer, Matrix> steps;
    
    @JacobiResult(10)
    public Matrix output;
    
    @Test
    @JacobiImport("By Diag 4x4")
    public void testByDiag4x4() {
        new BulgeChaser(){

            @Override
            protected BulgeChaser.GivensPair pushBulge(Matrix matrix, int col, int endCol, int endRow) {
                BulgeChaser.GivensPair pair = super.pushBulge(matrix, col, endCol, endRow);
                Jacobi.assertEquals(steps.get(col + 1), matrix);
                return pair;
            }
            
        }.compute(steps.get(0), null, 0, 4, true);
    }
    
    @Test
    @JacobiImport("By Diag 6x6")
    public void testByDiag6x6() {
        Matrix input = steps.get(0);
        new BulgeChaser(){

            @Override
            protected BulgeChaser.GivensPair pushBulge(Matrix matrix, int col, int endCol, int endRow) {
                GivensPair pair = super.pushBulge(matrix, col, endCol, endRow);
                Jacobi.assertEquals(steps.get(col + 1), matrix);
                return pair;
            }
            
        }.compute(input, null, 0, input.getRowCount(), true);
    }
    
    @Test
    @JacobiImport("Full 6x6")
    @JacobiEquals(expected = 10, actual = 10)
    public void testFull6x6() {
        Matrix input = steps.get(0);
        new BulgeChaser(){

            @Override
            protected BulgeChaser.GivensPair pushBulge(Matrix matrix, int col, int endCol, int endRow) {
                BulgeChaser.GivensPair pair = super.pushBulge(matrix, col, endCol, endRow);
                Jacobi.assertEquals(steps.get(col + 1), matrix);
                return pair;
            }
            
        }.compute(input, null, 0, input.getRowCount(), true);
        this.output = input;
    }
    
}
