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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Decorator for single-shifted QR algorithm.
 * 
 * Without going too deep into formal mathematical proof, here is a simple 
 * illustration of how Shifting works.
 * 
 * A "pure" QR algorithm computes A = Q * R, and compute A' = R * Q.
 * A "shifted" QR algorithm computes A - kI = Q * R, and computes A' = R * Q + kI
 * 
 * For any k, if A - kI = Q * R, 
 *                    R = Q^t * (A - kI)
 *                R * Q = Q^t * (A - kI) * Q
 *                      = Q^t * A * Q - k * Q^t * Q
 *                      = Q^t * A * Q - kI
 *      A' = R * Q + kI = Q^t * A * Q
 * 
 * The advantage of using a shift, is that if k is close to a eigenvalue of A,
 * A - kI is not full rank, and Q * R will have a zero element in the diagonal.
 * R * Q will eliminate a sub-diagonal entry in one iteration, thus converges
 * much faster.
 * 
 * Rayleigh-quotient shifting uses the bottom-leftmost element to be a guess
 * to be a eigenvalue. It is proven to have cubic convergence rate. However 
 * Rayleigh-quotient shifts may fail in certain cases, e.g. [0 1; 1 0].
 * Wilkinson shifting uses the eigenvalue of the bottom-leftmost 2x2 submatrix
 * whichever is closer to the Rayleigh-quotient shift. Wilkinson shift ensures
 * convergence but the not always real, and convergence rate is not as good.
 * 
 * This class uses a mixed approach: it uses Rayleigh-quotient shifts for most
 * of the time but randomly switches to using Wilkinson shift, if real, to break
 * symmetry.
 * 
 * @author Y.K. Chan
 */
public class ShiftedQR implements QRStep {

    /**
     * Constructor.
     * @param base  Implementation to be decorated on
     */
    public ShiftedQR(QRStep base) {
        this.base = base;
        this.spin = new AtomicLong(0);
    }

    @Override
    public void compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        double shift = this.getShift(matrix, beginRow, endRow);
        this.shiftDiag(matrix, beginRow, endRow, -shift);
        this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
        this.shiftDiag(matrix, beginRow, endRow, shift);
    }
    
    /**
     * Compute A = A + kI. 
     * @param matrix  Input matrix A
     * @param beginRow  Start of row of interest
     * @param endRow  Finish of row of interest
     * @param shift   Shift value k.
     */
    protected void shiftDiag(Matrix matrix, int beginRow, int endRow, double shift) {
        for(int i = beginRow; i < endRow; i++){
            double elem = matrix.get(i, i);
            matrix.set(i, i, elem + shift);
        }
    }
    
    /**
     * Get shift value.
     * @param matrix  Input matrix
     * @param beginRow  Start of row of interest
     * @param endRow  Finish of row of interest
     * @return  Shift value k
     */
    protected double getShift(Matrix matrix, int beginRow, int endRow) {         
        return this.useWilkinson() 
                ? this.wilkinson(matrix, beginRow, endRow)
                    .orElse(this.rayleighQuotient(matrix, beginRow, endRow))
                : this.rayleighQuotient(matrix, beginRow, endRow);
    }
    
    /**
     * Compute Rayleigh-Quotient shift.
     * @param matrix  Input matrix A
     * @param beginRow  Start of row of interest
     * @param endRow  Finish of row of interest
     * @return  Rayleigh-Quotient shift value
     */
    protected double rayleighQuotient(Matrix matrix, int beginRow, int endRow) {
        return matrix.get(endRow - 1, endRow - 1); 
    }
    
    /**
     * Compute Wilkinson shift.
     * @param matrix  Input matrix A
     * @param beginRow  Start of row of interest
     * @param endRow  Finish of row of interest
     * @return   Wilkinson shift
     */
    protected Optional<Double> wilkinson(Matrix matrix, int beginRow, int endRow) {
        double a = matrix.get(endRow - 2, endRow - 2);
        double b = matrix.get(endRow - 2, endRow - 1);
        double c = matrix.get(endRow - 1, endRow - 2);
        double d = matrix.get(endRow - 1, endRow - 1);
        
        double tr = a + d;
        double det = a * d - b * c;
        
        double delta = tr * tr - 4 * det;
        if(delta < 0.0){
            return Optional.empty();
        }
        delta = Math.sqrt(delta);
        double eig0 = (-b + delta) / 2.0;
        double eig1 = (-b - delta) / 2.0;
        return Optional.of( Math.abs(eig0 - d) < Math.abs(eig1 - d) ? eig0 : eig1 );
    }
    
    /**
     * Determine to use Wilkinson shift or not.
     * @return   True if wilkinson shift should be used, false otherwise.
     */
    protected boolean useWilkinson() {
        return this.spin.incrementAndGet() % PERIOD == 0;
    }

    private AtomicLong spin;
    private QRStep base;
    
    private static final int PERIOD = 3;
}
