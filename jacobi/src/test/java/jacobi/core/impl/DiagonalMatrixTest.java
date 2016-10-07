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
