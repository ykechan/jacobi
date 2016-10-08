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
