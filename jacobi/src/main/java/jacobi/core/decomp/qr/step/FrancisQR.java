/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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

import jacobi.core.decomp.qr.step.shifts.BulgeChaser;
import jacobi.api.Matrix;
import jacobi.core.decomp.qr.HouseholderReflector;
import jacobi.core.decomp.qr.step.shifts.DoubleShift;

/**
 * Implementation of Francis double-shifted QR algorithm.
 * 
 * Wilkinson shifts ensures convergence albeit having slightly lower convergence
 * rate. The biggest problem is that eigenvalues may not be real, which involve
 * entire layer of complexity. 
 * 
 * However if a complex value z is an eigenvalue of A, z', its complex conjugate
 * must also be an eigenvalue of A since eigenvalues are roots of characteristic
 * polynomials.
 * 
 * Consider
 *  H0 - zI = Q0 * R0
 *       H1 = R0 * Q0 + zI
 * H1 - z'I = Q1 * R1
 *       H2 = R1 * Q1 + z'I
 * 
 * R0 * Q0 + zI - z'I = Q1 * R1
 * Q0*R0*Q0*R0 + (z - z')*Q0*R0 = Q0*Q1*R1*R0
 * (H0 - zI)^2 + (z - z')*(H0 - zI) = Q0*Q1*R1*R0
 * H0^2 - 2*z*H0 + z^2 + (z - z')*H0 - z(z - z')I = Q0*Q1*R1*R0
 * H0^2 - (z + z')*H0 + z*z'I = Q0*Q1*R1*R0
 * H0^2 - 2*Re(z)*H0 + |z|*I = Q0*Q1*R1*R0
 * 
 * Therefore computing H2 = Q0^t*Q1^t*H*Q1*Q0 is equivalent to compute
 * QR decomposition on (H0 - zI)*(H1 - z'I). By implicit-Q theorem, Hessenberg
 * form is unique save for signs. Avoid computing the whole matrix, only Q
 * for reducing the first column of (H0 - zI)*(H1 - z'I) is computed and applied
 * to H, and reducing Q*H*Q (Q = Q^t for Householder) to Hessenberg form
 * will yields H2.
 * 
 * Francis QR step fails when matrix is 2x2 or below.
 * 
 * @author Y.K. Chan
 */
public class FrancisQR implements QRStep {

    /**
     * Constructor.
     * @param base  Base implementation for fall through.
     */
    public FrancisQR(QRStep base) {
        this.bulgeChaser = new BulgeChaser();
        this.base = base;
    }

    @Override
    public int compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow < 4){
            return this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
        }
        this.createBulge(matrix, partner, beginRow, endRow, fullUpper);
        return this.bulgeChaser.compute(matrix, partner, beginRow, endRow, fullUpper);
    }
    
    /**
     * Create initial bulge by computing Q*H*Q.
     * @param matrix  Input matrix H
     * @param partner  Partner matrix
     * @param begin  Begin index of rows of interest
     * @param end  End index of rows of interest
     * @param full   True if full upper triangular matrix needed, false otherwise
     */
    protected void createBulge(Matrix matrix, Matrix partner, int begin, int end, boolean full) {
        HouseholderReflector hh = DoubleShift.of(matrix, end - 2).getImplicitQ(matrix, begin);
        hh.applyLeft(matrix);
        hh.applyRight(matrix); 
        if(partner != null){
            hh.applyRight(partner);
        }
    }
    
    private BulgeChaser bulgeChaser;
    private QRStep base;
}
