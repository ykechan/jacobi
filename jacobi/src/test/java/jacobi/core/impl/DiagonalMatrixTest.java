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
package jacobi.core.impl;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.ext.Op;
import jacobi.api.ext.Prop;
import jacobi.test.util.Jacobi;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class DiagonalMatrixTest {

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
    
}
