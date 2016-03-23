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
import jacobi.core.util.Triplet;

/**
 * Hessenberg decomposition is decompose a matrix A into Q * U * Q^t, where
 * Q is orthogonal and U is almost upper triangular. Almost triangular here refers to
 * almost upper triangular, in that all elements under sub-diagonal are zero. 
 * Sub-diagonal is not necessarily zeroes though.
 * 
 * Hessenberg decomposition may not have much interest invested in itself, but
 * it is the first step of optimization of QR algorithm.
 * 
 * Hessenberg decomposition can be easily computed by Householder reflection.
 * 
 * @author Y.K. Chan
 */
public class Hessenberg {
    
    public Matrix compute(Matrix a) {
        return null;
    }
    
    public Triplet compute3(Matrix matrix) {
        return null;
    }

}
