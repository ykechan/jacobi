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

import jacobi.core.decomp.qr.step.GivensQR;
import jacobi.api.Matrix;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/GivensQRTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class GivensQRTest {
    
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
    
    @JacobiResult(100)
    public Matrix rq;
    
    @Test
    @JacobiImport("4x4(1)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4Iter1() {
        List<GivensQR.Givens> givens = this.assertBySteps(step1, step2, step3)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("4x4(2)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4Iter2() {
        List<GivensQR.Givens> givens = this.assertBySteps(step1, step2, step3)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("4x4(3)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4Iter3() {
        List<GivensQR.Givens> givens = this.assertBySteps(step1, step2, step3)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("4x4(4)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4Iter4() {
        List<GivensQR.Givens> givens = this.assertBySteps(step1, step2, step3)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("5x5(1)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test5x5Iter1() {
        List<GivensQR.Givens> givens = this.assertBySteps(step1, step2, step3, step4)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("5x5(2)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test5x5Iter2() {
        List<GivensQR.Givens> givens = this.assertBySteps(step1, step2, step3, step4)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("5x5(3)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test5x5Iter3() {
        List<GivensQR.Givens> givens = this.assertBySteps(step1, step2, step3, step4)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("4x4 in 6x6")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4In6x6() {
        List<GivensQR.Givens> givens = this.assertBySteps(step1, step2, step3).computeQR(this.input, 1, 5, 6);
        new GivensQR().computeRQ(this.input, givens, 0, 5, 1);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("4x4 in 6x6 Partial")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4In6x6Partial() {
        List<GivensQR.Givens> givens = this.assertBySteps(step1, step2, step3).computeQR(this.input, 2, 6, 6);
        new GivensQR().computeRQ(this.input, givens, 2, 6, 2);
        this.rq = this.input;
    }

    private GivensQR assertBySteps(Matrix... after) {
        return new GivensQR(){

            @Override
            public List<GivensQR.Givens> computeQR(Matrix matrix, int beginRow, int endRow, int endCol) {
                this.matrix = matrix;
                this.index = 0;
                return super.computeQR(matrix, beginRow, endRow, endCol);
            }

            @Override
            public GivensQR.Givens computeQR(double[] upper, double[] lower, int begin, int end) {
                GivensQR.Givens g = super.computeQR(upper, lower, begin, end);
                Jacobi.assertEquals(after[index++], matrix);
                return g;
            }
            
            private int index;
            private Matrix matrix;
        };
    }
}
