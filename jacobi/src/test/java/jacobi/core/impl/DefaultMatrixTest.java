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

import jacobi.api.Matrix;
import jacobi.test.util.Jacobi;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class DefaultMatrixTest {
    
    @Test
    public void testConstruct() {
        double[][] rows = {
            {Math.PI, Math.E, Math.sqrt(2.0)},
            {Math.E, Math.PI, Double.NaN}
        };
        Matrix matrix = new DefaultMatrix(rows);
        for(int i = 0; i < rows.length; i++){
            Assert.assertTrue(matrix.getRow(i) == rows[i]);
        }
        
        Matrix matrix2 = new DefaultMatrix(matrix);
        for(int i = 0; i < rows.length; i++){
            Assert.assertTrue(matrix2.getRow(i) != rows[i]);
        }
        
        Matrix matrix3 = matrix.copy();
        for(int i = 0; i < rows.length; i++){
            Assert.assertTrue(matrix3.getRow(i) != rows[i]);
        }
        
        Jacobi.assertEquals(matrix, matrix2);
        Jacobi.assertEquals(matrix, matrix3);
    }    
    
    @Test
    public void testSetRow() {
        double[][] rows = {
            {Math.PI, Math.E, Math.sqrt(2.0)},
            {Math.E, Math.PI, Math.sqrt(5.0)}
        };
        Matrix matrix = new DefaultMatrix(rows);
        double[] row = matrix.getRow(0);
        row[1] = Math.exp(2.0);
        
        Assert.assertEquals(rows[0][1], Math.exp(2.0), 1e-14);
        
        double[] elem = {1.0, 2.0, 3.0};
        matrix.setRow(1, elem);
        
        Assert.assertTrue(rows[1] == matrix.getRow(1));
        Assert.assertArrayEquals(rows[1], elem, 1e-14);
    }
    
}
