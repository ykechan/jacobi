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
import org.junit.Test;

/**
 *
 * @author Y.K. Chan
 */
public class CopyOnWriteMatrixTest {
    
    @Test
    public void testNormal() {
        Matrix base = new DiagonalMatrix(new double[]{Math.E, Math.PI});
        Matrix diag = new CopyOnWriteMatrix(base);
        Jacobi.assertEquals(base, diag);
        
        diag.getApplySet(1, (r) -> r[0] = Math.sqrt(2.0));
         
        Jacobi.assertEquals(new DefaultMatrix(new double[][]{
            {Math.E, 0.0},
            {Math.sqrt(2.0), Math.PI}
        }), diag);
        
        Jacobi.assertEquals(new DefaultMatrix(new double[][]{
            {Math.E, 0.0},
            {0.0, Math.PI}
        }), base);
    }

    
}
