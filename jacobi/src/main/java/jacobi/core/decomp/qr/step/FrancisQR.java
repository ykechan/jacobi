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
import jacobi.core.decomp.qr.step.GivensQR.Givens;
import java.util.Arrays;
import java.util.List;

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

    public FrancisQR() {
        this.givensQR = new GivensQR();
    }

    @Override
    public void compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow < 3){
            throw new UnsupportedOperationException("Unable to double-shift of 2x2 or smaller matrices.");
        }
        
    }
    
    protected void createBulge(Matrix matrix, Matrix partner, int begin, int end, boolean full) {
        HouseholderReflector hh = this.getDoubleShift1stCol(matrix, begin, end);
        Matrix subMat = this.subMatrix(matrix, begin, end, full);
        hh.applyLeft(subMat);
        hh.applyRight(subMat);
    }
    
    protected void chaseBulge(Matrix matrix, Matrix partner, int begin, int end, boolean full) {
        //
        //  x x x x x
        //  x x x x x
        //  x x x x x
        //  x x x x x
        //  0 0 0 x x
        //
        
    }
    
    protected List<Givens> chaseBulgeByDiag(Matrix matrix, int begin, int end, boolean full) {
        int last = end - 2;
        Givens[] gs = new Givens[last - begin];
        int endCol = full ? matrix.getColCount() : end;
        for(int i = begin; i < last; i++){
            double[] upper = matrix.getRow(i + 1);
            double[] lower = matrix.getRow(i + 2);
            
            gs[i] = this.givensQR.computeQR(upper, lower, i, endCol);
            matrix.setRow(i + 1, upper);
            matrix.setRow(i + 2, lower);
            
            if(i + 3 >= end){
                continue;
            }
            double[] next = matrix.getRow(i + 3);
            double a = -next[i + 2] * gs[i].getSin();
            double b =  next[i + 2] * gs[i].getCos();
            next[i + 1] = a;
            next[i + 2] = b;
            matrix.setRow(i + 3, next);
        }
        return Arrays.asList(gs);
    }

    protected HouseholderReflector getDoubleShift1stCol(Matrix matrix, int beginRow, int endRow) {
        double a = matrix.get(endRow - 2, endRow - 2);
        double b = matrix.get(endRow - 2, endRow - 1);
        double c = matrix.get(endRow - 1, endRow - 2);
        double d = matrix.get(endRow - 1, endRow - 1);
        
        double tr = a + d;
        double det = a * d - b * c;
        
        double u = matrix.get(beginRow, beginRow);
        double v = matrix.get(beginRow + 1, beginRow);
        
        double[] col = {
              u * u
            + matrix.get(beginRow, beginRow + 1) * v
            - tr * u
            + det,
            
              v * u
            + matrix.get(beginRow + 1, beginRow + 1) * v
            - tr * v,
              
              matrix.get(beginRow + 2, beginRow + 1) * v
        };
        HouseholderReflector hh = new HouseholderReflector(col, 0);
        hh.normalize();
        return hh;
    }
    
    protected Matrix subMatrix(Matrix matrix, int begin, int end, boolean fullUpper) {
        return new Matrix() {

            @Override
            public int getRowCount() {
                return 3;
            }

            @Override
            public int getColCount() {
                return fullUpper ? matrix.getColCount() : (end - begin);
            }

            @Override
            public double[] getRow(int index) {
                return matrix.getRow(begin + index);
            }

            @Override
            public Matrix setRow(int index, double[] values) {
                matrix.setRow(begin + index, values);
                return this;
            }

            @Override
            public Matrix swapRow(int i, int j) {
                throw new UnsupportedOperationException();
            }

            @Override
            public <T> T ext(Class<T> clazz) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Matrix copy() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    private GivensQR givensQR;
}
