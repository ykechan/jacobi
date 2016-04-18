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

package jacobi.core.op;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Op;
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
@JacobiImport("/jacobi/test/data/MulTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class MulTest {
    
    @JacobiInject(0)
    public Matrix matrixA;
    
    @JacobiInject(1)
    public Matrix matrixB;
    
    @JacobiResult(2)
    public Matrix ans;
    
    @Test
    @JacobiImport("5x5 mul 5x5")
    @JacobiEquals(expected = 2, actual = 2)
    public void test5x5Mul5x5() {
        this.ans = new Mul().compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("7x7 mul 7x7")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x7Mul7x7() {
        this.ans = new Mul().compute(this.matrixA, this.matrixB);
    }

    @Test
    @JacobiImport("7x3 mul 3x5")
    @JacobiEquals(expected = 2, actual = 2)
    public void test7x3Mul3x5() {
        this.ans = new Mul().compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("2x9 mul 9x1")
    @JacobiEquals(expected = 2, actual = 2)
    public void test2x9Mul9x1() {
        this.ans = new Mul().compute(this.matrixA, this.matrixB);
    }
    
    @Test
    @JacobiImport("13x1 mul 1x4")
    @JacobiEquals(expected = 2, actual = 2)
    public void test13x1Mul1x4() {
        this.ans = this.matrixA.ext(Op.class).mul(this.matrixB).get();
    }
    
    @Test(expected = RuntimeException.class)
    public void test5x2Mul4x3() {
        this.matrixA = Matrices.zeros(5, 2);
        this.matrixB = Matrices.zeros(4, 3);
        this.ans = this.matrixA.ext(Op.class).mul(this.matrixB).get();
    }
}
