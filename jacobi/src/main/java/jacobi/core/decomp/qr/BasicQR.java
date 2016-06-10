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

import jacobi.core.decomp.qr.step.QRStep;
import jacobi.api.Matrix;
import jacobi.core.util.Throw;

/**
 * Basic QR algorithm implementation with given iteration implementation.
 * 
 * Basic QR algorithm goes as the following: 
 * 
 * Given a Hessenberg matrix A, find f(A) with some shifting strategy
 * Find Q*R = f(A), s.t. Q is orthogonal and R upper triangular 
 * Compute A' = R*Q, and ~A = f^-1(A')
 * Repeat until ~A is upper triangular.
 * 
 * When a sub-diagonal entry of A is close to zero, A can be deflated into
 * two separate matrices, and perform the iteration isolated. Depending on
 * whether whole Schur form is required or only eigenvalues, computes are
 * not necessary for columns beyond the upper left corner.
 * 
 * This class is to perform the iteration given the step of each iteration.
 * 
 * @author Y.K. Chan
 */
public class BasicQR implements QRStrategy {

    /**
     * Constructor.
     * @param step  Implementation to perform an iteration.
     */
    public BasicQR(QRStep step) {
        this.step = step;
    }

    @Override
    public Matrix compute(Matrix matrix, Matrix partner, boolean fullUpper) {
        Throw.when()
            .isNull(() -> matrix, () -> "No matrix to compute.")
            .isTrue(
                () -> matrix.getRowCount() == 0, 
                () -> "Unable to compute an empty matrix."
            )
            .isTrue(
                () -> matrix.getRowCount() != matrix.getColCount(), 
                () -> "Unable to compute a non-square "
                    + matrix.getRowCount() + "x" + matrix.getColCount()
                    + " matrix.")
            .isTrue(
                () -> partner != null && matrix.getRowCount() != partner.getRowCount(), 
                () -> "Mismatch partner matrix having " + partner.getRowCount() + " rows.");
        this.compute(matrix, partner, 0, matrix.getRowCount(), fullUpper);
        return matrix;
    }
    
    /**
     * Iterate using QR step within the limited range until it converges or exhausted.
     * @param matrix  Input matrix A
     * @param partner  Partner matrix
     * @param beginRow  Begin index of row of interest
     * @param endRow  End index of row of interest
     * @param fullUpper   True is full upper triangular matrix required, false otherwise
     */
    protected void compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow < 2){
            return;
        }
        int limit = LIMIT * (endRow - beginRow);
        int end = this.deflate(matrix, beginRow, endRow);
        for(int k = 0; k < limit; k++){
            this.step.compute(matrix, partner, beginRow, end, fullUpper);
            int conv = this.getConverged(matrix, beginRow, endRow);
            if (conv >= 0) {
                this.compute(matrix, partner, beginRow, conv, fullUpper);
                this.compute(matrix, partner, conv, endRow, fullUpper);
                return;
            }
        }
        throw new UnsupportedOperationException("Exhaused to converge any entry in " + limit + " iterations.");
    }
    
    /**
     * Find the index of the first zero entry in sub-diagonal.
     * @param matrix  Input matrix
     * @param begin  Begin index of row of interest
     * @param end  End index of row of interest
     * @return   Index of first zero entry, -1 if none was found.
     */
    protected int getConverged(Matrix matrix, int begin, int end) {
        for(int i = end - 1; i > begin; i--){
            if(Math.abs(matrix.get(i, i - 1)) < EPSILON){                
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Find the index of the last non-zero entry in sub-diagonal
     * @param matrix  Input matrix
     * @param begin  Begin index of row of interest
     * @param end  End index of row of interest
     * @return   Index of last zero entry, end - 1 if none was found.
     */
    protected int deflate(Matrix matrix, int begin, int end) {
        int k = end - 1;
        while(k > begin){
            if(Math.abs(matrix.get(k, k - 1)) > EPSILON){
                break;
            }
        }
        return k + 1;
    }

    private QRStep step;
    
    private static final double EPSILON = 1e-12;
    private static final int LIMIT = 128;
}
