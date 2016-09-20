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
package jacobi.core.decomp.qr.step;

import jacobi.api.Matrix;

/**
 * Common interface for computing an iteration in QR algorithm.
 * 
 * @author Y.K. Chan
 */
public interface QRStep {
    
    /**
     * Compute an iteration of QR algorithm on a sub-matrix.
     * @param matrix  Input matrix
     * @param partner  Partner matrix
     * @param beginRow  Begin index of row of interest
     * @param endRow  End index of row of interest
     * @param fullUpper   True if full upper triangular matrix needed, false otherwise
     */
    public void compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper);
    
}
