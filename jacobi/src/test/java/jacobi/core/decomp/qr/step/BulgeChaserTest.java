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
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.Map;
import org.junit.Assert;
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
    
    @Test
    @JacobiImport("Full 6x6 Deflated At 4")
    @JacobiEquals(expected = 10, actual = 10)
    public void testFull6x6DeflatedAt4() {
        Matrix input = steps.get(0);
        int next = new BulgeChaser(){

            @Override
            protected BulgeChaser.GivensPair pushBulge(Matrix matrix, int col, int endCol, int endRow) {
                BulgeChaser.GivensPair pair = super.pushBulge(matrix, col, endCol, endRow);
                Jacobi.assertEquals(steps.get(col + 1), matrix);
                return pair;
            }
            
        }.compute(input, null, 0, input.getRowCount(), true);
        Assert.assertEquals(4, next);
        this.output = input;
    }
    
}
