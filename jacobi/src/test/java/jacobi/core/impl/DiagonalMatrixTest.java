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
package jacobi.core.impl;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Op;
import jacobi.api.ext.Prop;
import jacobi.test.annotations.JacobiEquals;
import jacobi.test.annotations.JacobiImport;
import jacobi.test.annotations.JacobiInject;
import jacobi.test.annotations.JacobiResult;
import jacobi.test.util.Jacobi;
import jacobi.test.util.JacobiJUnit4ClassRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Y.K. Chan
 */
@JacobiImport("/jacobi/test/data/DiagonalMatrixTest.xlsx")
@RunWith(JacobiJUnit4ClassRunner.class)
public class DiagonalMatrixTest {
    
    @JacobiInject(1)
    public Matrix input;
    
    @JacobiInject(2)
    public Matrix matrix;
    
    @JacobiResult(3)
    public Matrix result;

    @Test
    public void testConstruct() {
        double[] values = {Math.PI, Math.E, Math.sqrt(2.0)};
        DiagonalMatrix diag = new DiagonalMatrix(values);
        Jacobi.assertEquals(Matrices.of(new double[][]{
            {Math.PI, 0.0, 0.0},
            {0.0, Math.E, 0.0},
            {0.0, 0.0, Math.sqrt(2.0)},
        }), diag);
        
        values[1] = Math.sin(Math.PI / 3.0);
        Jacobi.assertEquals(Matrices.of(new double[][]{
            {Math.PI, 0.0, 0.0},
            {0.0, Math.E, 0.0},
            {0.0, 0.0, Math.sqrt(2.0)},
        }), diag);
    }
    
    @Test
    public void testDet() {
        double[] values = {Math.PI, Math.E, Math.sqrt(2.0)};
        Matrix diag = new DiagonalMatrix(values);
        Assert.assertEquals(Math.PI * Math.E * Math.sqrt(2.0), diag.ext(Prop.class).det(), 1e-14);        
    }
    
    @Test
    public void testInv() {
        double[] values = {Math.PI, Math.E, Math.sqrt(2.0)};
        Matrix diag = new DiagonalMatrix(values);
        Jacobi.assertEquals(Matrices.of(new double[][]{
            {1/Math.PI, 0.0, 0.0},
            {0.0, 1/Math.E, 0.0},
            {0.0, 0.0, 1/Math.sqrt(2.0)},
        }), diag.ext(Prop.class).inv().get());
    }
    
    @Test
    public void testMul() {
        Matrix diag = new DiagonalMatrix(new double[]{Math.PI, Math.E, Math.sqrt(2.0)});
        Matrix column = new ColumnVector(new double[]{ 2.0, 5.0, 7.0 });
        Jacobi.assertEquals(Matrices.of(new double[][]{
            {2.0 * Math.PI},
            {5.0 * Math.E},
            {7.0 * Math.sqrt(2.0)},
        }), diag.ext(Op.class).mul(column).get());
        
        Matrix diag2 = new DiagonalMatrix(new double[]{ 2.0, 5.0, 7.0 });
        Jacobi.assertEquals(Matrices.of(new double[][]{
            {2.0 * Math.PI, 0.0, 0.0},
            {0.0, 5.0 * Math.E, 0.0}, 
            {0.0, 0.0, 7.0 * Math.sqrt(2.0)},
        }), diag.ext(Op.class).mul(diag2).get());
    }
    
    @Test
    @JacobiImport("5x5 mul 5x3")
    @JacobiEquals(expected = 3, actual = 3)
    public void testMulWithNormalMatrix() {
        this.result = new DiagonalMatrix(this.input.getRow(0)).mul(this.matrix);
    }
    
    @Test
    @JacobiImport("4x4 mul 4x7")
    @JacobiEquals(expected = 3, actual = 3)
    public void test4x4Mul4x7() {
        this.result = new DiagonalMatrix(this.input.getRow(0)).mul(this.matrix);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testMulDimensionMismatch() {
        new DiagonalMatrix(new double[]{Math.E, Math.PI}).mul(Matrices.zeros(3, 3));
    }
    
    @Test
    public void testSingularDiagonalMatrix() {
        Assert.assertFalse(new DiagonalMatrix(new double[]{0.0, 1.0}).inv().isPresent());
    }
    
    @Test
    public void testCopy() {
        Matrix diag = new DiagonalMatrix(new double[]{Math.PI, Math.E});
        Matrix copy = diag.copy().set(0, 0, 1.0);
        Jacobi.assertEquals(Matrices.of(new double[][]{
            {1.0, 0.0},
            {0.0, Math.E},
        }), copy);
    }
    
}
