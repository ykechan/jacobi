/* 
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package jacobi.core.decomp.qr.step;

import jacobi.api.Matrix;

/**
 * Decorator for single-shifted QR algorithm.
 * 
 * <p>Without going too deep into formal mathematical proof, here is a simple 
 * illustration of how shifting works.</p>
 * 
 * <p>
 * A "pure" QR algorithm computes A = Q * R, and compute A' = R * Q.<br>
 * A "shifted" QR algorithm computes A - kI = Q * R, and computes A' = R * Q + kI<br>
 * </p>
 * 
 * <p>
 * For any k, <pre>
 *            if A - kI = Q * R,
 *                    R = Q^t * (A - kI)
 *                R * Q = Q^t * (A - kI) * Q
 *                      = Q^t * A * Q - k * Q^t * Q
 *                      = Q^t * A * Q - kI
 *      A' = R * Q + kI = Q^t * A * Q
 * </pre>
 * 
 * <p>The advantage of using a shift, is that if k is close to a eigenvalue of A,
 * A - kI is not full rank, and Q * R will have a zero element in the diagonal.
 * R * Q will eliminate a sub-diagonal entry in one iteration, thus converges
 * much faster.</p>
 * 
 * <p>Rayleigh-quotient shifting uses the bottom-leftmost element to be a guess
 * to be a eigenvalue. It is proven to have cubic convergence rate. However 
 * Rayleigh-quotient shifts may fail in certain cases, e.g. [0 1; 1 0].
 * Wilkinson shifting uses the eigenvalue of the bottom-leftmost 2x2 sub-matrix
 * whichever is closer to the Rayleigh-quotient shift. Wilkinson shift ensures
 * convergence but the not always real, and convergence rate is not as good.</p>
 * 
 * <p>Normally a Francis QR double-shift step is more preferable (see FrancisQR), but it may
 * converge very slowly when the first column is almost deflated. In that case
 * double-shifts was reverted to a pure QR algorithm. However in such cases a 
 * good candidate of a real shift is found: the 1st element.</p>
 * 
 * <p>This class picks up such cases and perform single-shift instead of double-shifts.</p>
 * 
 * @author Y.K. Chan
 */
public class ShiftedQR implements QRStep {    

    /**
     * Constructor.
     * @param base  Base implementation to fall through
     */
    public ShiftedQR(QRStep base) {
        this(base, 1e5);
    }

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
    public int compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow < 3){
            return this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
        }
        double shift = this.getShift(matrix, beginRow, endRow);
        if(shift == 0.0){
            return this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
        }
        this.shiftDiag(matrix, beginRow, endRow, -shift);
        try {
            return this.impl.compute(matrix, partner, beginRow, endRow, fullUpper);
        } finally {
            this.shiftDiag(matrix, beginRow, endRow, shift);
        }
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
