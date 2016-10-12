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

package jacobi.core.prop;

import jacobi.api.Matrix;
import jacobi.core.util.Throw;

/**
 * Find the trace of an input matrix A.
 * 
 * The trace of a matrix A tr(A) is the sum of its diagonal elements.
 * 
 * @author Y.K. Chan
 */
public class Trace {
    
    /**
     * Find the trace of an input matrix A.
     * @param matrix  Input matrix A
     * @return  tr(A)
     * @throws  IllegalArgumentException if A is null or A is not a square matrix.
     */
    public double compute(Matrix matrix) {
        Throw.when()
            .isNull(() -> matrix, () -> "No matrix to compute.")
            .isFalse(
                () -> matrix.getRowCount() == matrix.getColCount(), 
                () -> "Trace not exists for non-square matrices");
        double tr = 0.0;
        for(int i = 0; i < matrix.getRowCount(); i++){
            tr += matrix.get(i, i);
        }
        return tr;
    }
    
}
