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

package jacobi.test.util;

import jacobi.api.Matrix;
import org.junit.Assert;

/**
 *
 * @author Y.K. Chan
 */
public final class Jacobi {
    
    public static void assertEquals(Matrix expects, Matrix actual) {
        Jacobi.assertEquals(expects, actual, 1e-8);
    }
    
    public static void assertEquals(Matrix expects, Matrix actual, double epilson) {
        Assert.assertNotNull("No expected result.", expects);
        Assert.assertNotNull("No actual result.", actual);
        Assert.assertEquals("Row count mismatch.", expects.getRowCount(), actual.getRowCount());
        Assert.assertEquals("Column count mismatch.", expects.getColCount(), actual.getColCount());
        for(int i = 0; i < actual.getRowCount(); i++){
            for(int j = 0; j < actual.getColCount(); j++){
                Assert.assertEquals(
                    " Element (" + i + "," + j + ") mismatch.",
                    expects.getRow(i)[j],
                    actual.getRow(i)[j],
                    epilson );
            }
        }
    }

}
