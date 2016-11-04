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

import jacobi.core.givens.GivensQR;
import jacobi.api.Matrix;
import jacobi.core.givens.Givens;
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
        List<Givens> givens = this.assertBySteps(step1, step2, step3)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("4x4(2)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4Iter2() {
        List<Givens> givens = this.assertBySteps(step1, step2, step3)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("4x4(3)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4Iter3() {
        List<Givens> givens = this.assertBySteps(step1, step2, step3)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("4x4(4)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4Iter4() {
        List<Givens> givens = this.assertBySteps(step1, step2, step3)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("5x5(1)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test5x5Iter1() {
        List<Givens> givens = this.assertBySteps(step1, step2, step3, step4)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("5x5(2)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test5x5Iter2() {
        List<Givens> givens = this.assertBySteps(step1, step2, step3, step4)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("5x5(3)")
    @JacobiEquals(expected = 100, actual = 100)
    public void test5x5Iter3() {
        List<Givens> givens = this.assertBySteps(step1, step2, step3, step4)
                .computeQR(this.input, 0, this.input.getRowCount(), this.input.getColCount());        
        new GivensQR().computeRQ(this.input, givens, 0, this.input.getRowCount(), 0);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("4x4 in 6x6")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4In6x6() {
        List<Givens> givens = this.assertBySteps(step1, step2, step3).computeQR(this.input, 1, 5, 6);
        new GivensQR().computeRQ(this.input, givens, 0, 5, 1);
        this.rq = this.input;
    }
    
    @Test
    @JacobiImport("4x4 in 6x6 Partial")
    @JacobiEquals(expected = 100, actual = 100)
    public void test4x4In6x6Partial() {
        List<Givens> givens = this.assertBySteps(step1, step2, step3).computeQR(this.input, 2, 6, 6);
        new GivensQR().computeRQ(this.input, givens, 2, 6, 2);
        this.rq = this.input;
    }

    private GivensQR assertBySteps(Matrix... after) {
        return new GivensQR(){

            @Override
            public List<Givens> computeQR(Matrix matrix, int beginRow, int endRow, int endCol) {
                this.matrix = matrix;
                this.index = 0;
                return super.computeQR(matrix, beginRow, endRow, endCol);
            }

            @Override
            public Givens computeQR(double[] upper, double[] lower, int begin, int end) {
                Givens g = super.computeQR(upper, lower, begin, end);
                Jacobi.assertEquals(after[index++], matrix);
                return g;
            }
            
            private int index;
            private Matrix matrix;
        };
    }
}
