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

package jacobi.core.decomp.gauss;

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
