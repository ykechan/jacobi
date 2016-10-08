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

package jacobi.core.decomp.qr;

import jacobi.api.Matrix;

/**
 * Common strategy interface for QR algorithm.
 * 
 * The basic idea of QR algorithm is referred to as follows: 
 * 
 * Given a Hessenberg matrix H, find H = Q * R, 
 * where Q is orthogonal, and R is upper triangular.
 * 
 * Compute H2 = R * Q, and until H2 is upper triangular, repeat the process.
 * 
 * The vanilla QR algorithm, stated above, though works, takes large number 
 * of iterations to converge. Many enhancements are available, and their
 * efficiency increases with their complexity. Instead of implementing the
 * state-of-the-art QR algorithm, lesser sophisticated but simpler versions
 * are implemented experimentally.
 * 
 * @author Y.K. Chan
 */
public interface QRStrategy {
    
    /**
     * Perform QR algorithm on a Hessenberg matrix H, with a partner matrix B.
     * Matrix H will be transformed to a upper triangular matrix, and B will be transformed
     * to Q * B, where H = Q * D * Q^t.
     * @param matrix  Hessenberg matrix H
     * @param partner  Partner matrix B
     * @param fullUpper True if whole upper triangular matrix is needed, false if only
     *          diagonal entries, which are eigenvalues, are interested.
     * @return Upper triangular matrix D
     */
    public Matrix compute(Matrix matrix, Matrix partner, boolean fullUpper);
    
}
