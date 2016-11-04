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

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.HouseholderReflector;

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
    public void compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow < 4){
            this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
            return;
        }
        this.createBulge(matrix, partner, beginRow, endRow, fullUpper);
        this.bulgeChaser.compute(matrix, partner, beginRow, endRow, fullUpper);
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
        HouseholderReflector hh = this.getDoubleShift1stCol(matrix, begin, end);
        hh.applyLeft(matrix);
        hh.applyRight(matrix);        
    }

    /**
     * Create Householder reflector Q for double shift.
     * @param matrix  Input matrix H
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @return   Householder reflector Q
     */
    protected HouseholderReflector getDoubleShift1stCol(Matrix matrix, int beginRow, int endRow) {
        double a = matrix.get(endRow - 2, endRow - 2);
        double b = matrix.get(endRow - 2, endRow - 1);
        double c = matrix.get(endRow - 1, endRow - 2);
        double d = matrix.get(endRow - 1, endRow - 1);
        
        double tr = a + d;
        double det = a * d - b * c;
        
        double u = matrix.get(beginRow, beginRow);
        double v = matrix.get(beginRow + 1, beginRow);
        
        double[] col = new double[beginRow + 3];
        col[beginRow    ] = u * u
            + matrix.get(beginRow, beginRow + 1) * v
            - tr * u
            + det;
        col[beginRow + 1] = v * u
            + matrix.get(beginRow + 1, beginRow + 1) * v
            - tr * v;
        col[beginRow + 2] = matrix.get(beginRow + 2, beginRow + 1) * v;
        HouseholderReflector hh = new HouseholderReflector(col, beginRow);
        hh.normalize();
        return hh;
    }    
    
    private BulgeChaser bulgeChaser;
    private QRStep base;
}
