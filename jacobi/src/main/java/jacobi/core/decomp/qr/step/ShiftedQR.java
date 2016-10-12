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
 * Decorator for single-shifted QR algorithm.
 * 
 * Without going too deep into formal mathematical proof, here is a simple 
 * illustration of how shifting works.
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
 * Wilkinson shifting uses the eigenvalue of the bottom-leftmost 2x2 sub-matrix
 * whichever is closer to the Rayleigh-quotient shift. Wilkinson shift ensures
 * convergence but the not always real, and convergence rate is not as good.
 * 
 * Normal a Francis QR double-shift step is superior (see FrancisQR), but it can
 * converge very slowly when the first column is almost converged. In that case
 * double-shifts was deflated to a pure QR algorithm. However in such cases a 
 * good candidate of real shift is found: the 1st element. 
 * 
 * This class picks up such cases and perform single-shift instead of double-shifts.
 * 
 * @author Y.K. Chan
 */
public class ShiftedQR implements QRStep {

    /**
     * Constructor. 
     * @param base  Base implementation to fall through
     * @param threshold  Threshold on accepting the shift value.
     */
    public ShiftedQR(QRStep base, double threshold) {
        this.base = base;
        this.impl = new PureQR();
        this.threshold = threshold;
    }

    @Override
    public void compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow < 3){
            this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
            return;
        }
        double shift = this.getShift(matrix, beginRow, endRow);
        if(shift == 0.0){
            this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
            return;
        }
        this.shiftDiag(matrix, beginRow, endRow, -shift);
        this.impl.compute(matrix, partner, beginRow, endRow, fullUpper);
        this.shiftDiag(matrix, beginRow, endRow, shift);
    }        
    
    /**
     * Shift diagonal elements by given value
     * @param matrix  Input matrix
     * @param begin  Begin index of rows of interest
     * @param end  End index of rows of interest
     * @param shift  Shift value
     */
    protected void shiftDiag(Matrix matrix, int begin, int end, double shift) {
        for(int i = begin; i < end; i++){
            double elem = matrix.get(i, i);
            matrix.set(i, i, elem + shift);
        }
    }
    
    /**
     * Find shift value.
     * @param matrix  Input matrix
     * @param begin  Begin index of rows of interest
     * @param end  End index of rows of interest
     * @return  Shift value, or 0 if criteria is not met
     */
    protected double getShift(Matrix matrix, int begin, int end) {
        double lastBulge = matrix.get(end - 1, end - 2);
        if(Math.abs(matrix.get(end - 1, end - 1) / lastBulge) > this.threshold){
            return matrix.get(end - 1, end - 1);
        }
        double firstBulge = matrix.get(begin + 1, begin);
        if(Math.abs(matrix.get(begin, begin) / firstBulge) > this.threshold){
            return matrix.get(begin, begin);
        }
        return 0.0;
    }
    
    private QRStep base, impl;
    private double threshold;
    
}
