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
package jacobi.core.decomp.gauss;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
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
@JacobiImport("/jacobi/test/data/FullMatrixOperatorTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class FullMatrixOperatorTest {
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiInject(10)
    public Matrix rowOp;
    
    @JacobiInject(2)
    public Matrix expects;
    
    @JacobiResult(1)
    public Matrix actual;
    
    @Test
    @JacobiImport("swapRow3x5")
    @JacobiEquals(expected = 2, actual = 1)
    public void testSwapRow3x5() {
        new FullMatrixOperator(this.mock(this.input), this.input).swapRows(0, 2);
        this.actual = this.input;
    }

    @Test
    @JacobiImport("swapRow5x5")
    @JacobiEquals(expected = 2, actual = 1)
    public void testSwapRow5x5() {
        new FullMatrixOperator(this.mock(this.input), this.input).swapRows(1, 3);
        this.actual = this.input;
    }

    @Test(expected = RuntimeException.class)
    @JacobiImport("swapRow5x5")
    public void testSwapRowNegativeIndex() {
        new FullMatrixOperator(this.mock(this.input), this.input).swapRows(-1, 3);
    }
    
    @Test(expected = RuntimeException.class)
    @JacobiImport("swapRow5x5")
    public void testSwapRowOverflowIndex() {
        new FullMatrixOperator(this.mock(this.input), this.input).swapRows(1, 7);
    }
    
    @Test
    @JacobiImport("rowOp5x5_1")
    @JacobiEquals(expected = 2, actual = 1)
    public void testRowOp5x5Num1() {
        int i = (int) this.rowOp.get(0, 0);
        int j = (int) this.rowOp.get(1, 0);
        double k = this.rowOp.get(2, 0);
        new FullMatrixOperator(this.mock(this.input), this.input).rowOp(i, k, j);
        this.actual = this.input;
    }
    
    @Test
    @JacobiImport("rowOp5x5_2")
    @JacobiEquals(expected = 2, actual = 1)
    public void testRowOp5x5Num2() {
        int i = (int) this.rowOp.get(0, 0);
        int j = (int) this.rowOp.get(1, 0);
        double k =  this.rowOp.get(2, 0);
        new FullMatrixOperator(this.mock(this.input), this.input).rowOp(i, k, j);
        this.actual = this.input;
    }
    
    @Test
    @JacobiImport("rowOp5x5_3")
    @JacobiEquals(expected = 2, actual = 1)
    public void testRowOp5x5Num3() {
        int i = (int) this.rowOp.get(0, 0);
        int j = (int) this.rowOp.get(1, 0);
        double k =  this.rowOp.get(2, 0);
        new FullMatrixOperator(this.mock(this.input), this.input).rowOp(i, k, j);
        this.actual = this.input;
    }
    
    @Test(expected = RuntimeException.class)
    @JacobiImport("rowOp5x5_1")   
    public void testRowOp5x5NegativeIndex() {
        int i = (int) this.rowOp.get(0, 0);
        int j = (int) this.rowOp.get(1, 0);
        double k = this.rowOp.get(2, 0);
        new FullMatrixOperator(this.mock(this.input), this.input).rowOp(-1, k, j);
        this.actual = this.input;
    }
    
    @Test(expected = RuntimeException.class)
    @JacobiImport("rowOp5x5_1")   
    public void testRowOp5x5OverflowIndex() {
        int i = (int) this.rowOp.get(0, 0);
        int j = (int) this.rowOp.get(1, 0);
        double k = this.rowOp.get(2, 0);
        new FullMatrixOperator(this.mock(this.input), this.input).rowOp(i, k, 5);
        this.actual = this.input;
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testNullPartnerMatrix() {
        new FullMatrixOperator(this.mock(Matrices.zeros(3, 2)), null).rowOp(0, 1.0, 0);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMismatchPartnerMatrix() {
        new FullMatrixOperator(this.mock(Matrices.zeros(3, 2)), Matrices.zeros(4, 2)).rowOp(0, 1.0, 0);
    }
    
    private ElementaryOperator mock(Matrix base) {
        return new ElementaryOperator() {

            @Override
            public void swapRows(int i, int j) {                
            }

            @Override
            public void rowOp(int i, double a, int j) {
            }

            @Override
            public Matrix getMatrix() {
                return base;
            }
            
        };
    }
}
