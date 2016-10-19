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
package jacobi.api;

import jacobi.core.impl.DefaultMatrix;
import jacobi.core.impl.DiagonalMatrix;
import jacobi.core.impl.Empty;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.test.util.Jacobi;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class MatricesTest {
    
    @Test(expected = InvocationTargetException.class)
    public void testConstructor() throws Exception {
        Constructor<Matrices> cons = Matrices.class.getDeclaredConstructor();
        cons.setAccessible(true);
        Matrices matrices = cons.newInstance();
    }
    
    @Test
    public void testCreateEmptyMatrix() {
        Assert.assertTrue(Matrices.of(null) == Empty.getInstance());
        Assert.assertTrue(Matrices.of(new double[0][]) == Empty.getInstance());
        Assert.assertTrue(Matrices.of(new double[10][0]) == Empty.getInstance());
        Assert.assertTrue(Empty.getInstance().copy() == Empty.getInstance());  
    }        
    
    @Test(expected = RuntimeException.class)
    public void testCreateJaggedMatrix() {
        Matrices.of(new double[][]{
            {1.0, 2.0, 3.0} ,
            {1.0, 2.0, 3.0, 4.0} ,
            {1.0, 2.0} 
        });
    }
    
    @Test(expected = RuntimeException.class)
    public void testNormalMatrix() {
        Matrix m = Matrices.of(new double[][]{
            {1.0, 2.0, 3.0},
            {4.0, 5.0, 6.0},
            {7.0, 8.0, 8.0},
        });
        Matrix mat = m.copy();
        Assert.assertFalse(m == mat);
        Jacobi.assertEquals(m, mat);
        
        Matrix data = new DefaultMatrix(mat);
                
        mat.setRow(0, new double[]{0.0});
    }
    
    @Test(expected = RuntimeException.class)
    public void testInvalidRow() {
        Matrices.zeros(-1, 5);
    }
    
    @Test(expected = RuntimeException.class)
    public void testInvalidColumn() {
        Matrices.zeros(1, -5);
    }
    
    @Test
    public void testCopyNull() {
        Assert.assertNull(Matrices.copy(null));
    }
    
    @Test
    public void testCopyImmutableMatrix() {
        Assert.assertFalse(Matrices.copy(new DiagonalMatrix(new double[]{1.0, 2.0, 3.0})) instanceof ImmutableMatrix);
    }
    
}
