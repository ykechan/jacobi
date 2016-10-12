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
package jacobi.api;

import jacobi.core.impl.DefaultMatrix;
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
        Assert.assertFalse(Matrices.copy(Matrices.diag(new double[]{1.0, 2.0, 3.0})) instanceof ImmutableMatrix);
    }
    
}
