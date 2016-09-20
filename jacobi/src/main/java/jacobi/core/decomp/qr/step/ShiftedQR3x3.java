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
 * Decorator of QR step for 3x3 (sub-)matrices.
 * 
 * For a general size matrix, Francis double-shifted QR algorithm converges
 * faster than the simple shifted QR algorithm. A 3x3 matrix however is a special
 * case. A characteristic equation of a 3x3 matrix is a cubic equation, which
 * always contains a real root that can be computed algebraically. Moreover,
 * using an exact eigenvalue as shift will enable the QR algorithm to deflate
 * at least one sub-diagonal element, thus leaving a 2x2 matrix that can be solved
 * directly. Therefore a 3x3 matrix can be decomposed in exactly two iterations.
 * 
 * Given a 3x3 matrix 
 *     [a b c]
 * H = [d e f]  in Hessenberg form
 *     [0 j k]
 * 
 * p(H) = (a - x)*(e - x)*(k - x) + cdj - jfa - bd*(k - x)
 *      = -[x^3 - (a + e + k)*x^2 + (ae + ak + ek)*x + aek ] + bd*x
 *        +(cdj - jfa - bdk)
 *      = -x^3 + (a + e + k)*x^2 - (ae + ak + ek - bd)*x + (-aek + cdj - jfa - bdk)
 *      = -x^3 
 *        + (a + e + k)*x^2 
 *        - [ae + k*(a + e) - db]*x 
 *        + (-aek + cdj - jfa - bdk)
 * 
 * For p(H) = 0
 * x^3 - (a + e + k)*x^2 + [ae + k*(a + e) - bd]*x + (k(bd - ae) + j(fa - cd)) = 0
 * 
 * With proper substitution, 
 * x^3 + c0 * x^2 + c1 * x + c2 = 0
 * Let x = z + h, where h = -c0/3, therefore
 * z^3 + (3h^2 + 2c0*h + c1)z + (c0*h^2 + c1*h + c2) = 0
 * 
 * With proper substitution, 
 * z^3 + 3*pz - 2*q = 0
 * 
 * Consider z^3 - v^3 = (z - v)*(z^2 - vz + v^2)
 * Let z^3 + 3*pz + 2*q = z^3 - v^3 + w*(z - v)
 *                      = (z - v)*(z^2 - vz + v^2) + w*(z - v)
 *                      = (z - v)*(z^2 - (v - w)*z + v^2 + w*v)
 * Since z^3 - v^3 + w(z - v) = z^3 + w*z - (v^3 + w*v)
 * Comparing coefficient
 * w = 3p
 * v^3 + w*v = 2q
 * 
 * Therefore
 * v^3 + 3p*v = 2q
 * 
 * A miracle solution for v would be
 * v = [q + (p^3 + q^2)^(1/2)]^(1/3) + [q - (p^3 + q^2)^(1/2)]^(1/3)
 * 
 * For our purpose, a real root is obtained and is enough.
 * 
 * @author Y.K. Chan
 */
public class ShiftedQR3x3 implements QRStep {

    public ShiftedQR3x3(QRStep base) {
        this.base = base;
        this.step = new PureQR();
    }

    @Override
    public void compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow != 3){
            // fall through
            this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
            return;
        }
        double shift = this.eig3x3(matrix, beginRow);
        this.shiftDiag(matrix, beginRow, endRow, -shift);
        this.step.compute(matrix, partner, beginRow, endRow, fullUpper);
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
     * Find the eigenvalue of a 3x3 hessenberg sub-matrix
     * @param matrix  A n-by-n matrix
     * @param i  Begin index of row and column
     * @return  A real eigenvalue of the 3x3 sub-matrix
     */
    protected double eig3x3(Matrix matrix, int i) {
        double[] r0 = matrix.getRow(i);
        double[] r1 = matrix.getRow(i + 1);
        double[] r2 = matrix.getRow(i + 2);
        // x^3 - (a + e + k)*x^2 + [ae + k*(a + e) - bd - jk]*x + (k(bd - ae) + j(fa - cd)) = 0
        double quad = r0[i] + r1[i + 1] + r2[i + 2];
        double ae = r0[i] * r1[i + 1];
        double bd = r0[i + 1] * r1[i];
        double lin = ae - bd + r2[i + 2]*(r0[i] + r1[i + 1]) - r1[i + 2] * r2[i + 1];
        double con = r2[i + 2]*(bd - ae) + r2[i + 1]*(r1[i + 2]*r0[i] - r0[i + 2]*r1[i]);
        return this.solveCubic(-quad, lin, con);
    }
        
    /**
     * Solve a cubic equation x^3 + a*x^2 + b*x + c = 0.
     * @param a  Quadratic coefficient
     * @param b  Linear coefficient
     * @param c  Constant term
     * @return  A real root of the equation
     */
    protected double solveCubic(double a, double b, double c) {
        double h = -a/3;
        return this.solveDepressedCubic(h*(3*h + 2*a) + b, h*(h*(h + a) + b) + c) + h;
    }
    
    /**
     * Solve a depressed cubic equation x^3 + ax + b = 0
     * @param a  linear coefficient
     * @param b  constant term
     * @return A real root of the equation
     */
    protected double solveDepressedCubic(double a, double b) {
        if(Math.abs(a) < 1e-14){
            return Math.cbrt(-b);
        }
        if(Math.abs(b) < 1e-14){
            return 0.0;
        }
        double p = a/3;
        double q = b/2;
        double p3q2 = p*p*p + q*q;
        if(p3q2 < 0.0){
            double tan = -Math.sqrt(-1.0 - (p/q)*(p/q)*p);
            double x = 2.0 * Math.sqrt(-p) * Math.cos(Math.atan(tan) / 3.0);
            System.out.println("x = " + x);
            if(p > 0.0){
                return b > 0.0 ? x : -x;
            }            
            return Math.signum(-b)*Math.signum(x*x - p)*(x);
        }
        double delta = Math.sqrt(p3q2);
        return Math.cbrt(-q + delta) + Math.cbrt(-q - delta);
    }

    private QRStep base, step;
}
