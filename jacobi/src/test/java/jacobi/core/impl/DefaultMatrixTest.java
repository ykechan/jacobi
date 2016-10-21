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
    
    @Test
    public void testCopy() {
        double[][] rows = {
            {Math.PI, Math.E, Math.sqrt(2.0)},
            {Math.E, Math.PI, Math.sqrt(5.0)}
        };
        Matrix matrix = new DefaultMatrix(rows);
        Matrix copy = new DefaultMatrix(matrix.toArray());
        
        Jacobi.assertEquals(matrix, copy);
        Jacobi.assertEquals(matrix, matrix.copy());
    }
    
}
