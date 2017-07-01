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
 * Decorator of QR step for 3x3 (sub-)matrices.
 * 
 * <p>For a general size matrix, Francis double-shifted QR algorithm converges
 * faster than the simple shifted QR algorithm. A 3x3 matrix however is a special
 * case. A characteristic equation of a 3x3 matrix is a cubic equation, which
 * always contains a real root that can be computed algebraically. Moreover,
 * using an exact eigenvalue as shift will enable the QR algorithm to deflate
 * at least one sub-diagonal element, thus leaving a 2x2 matrix that can be solved
 * directly. Therefore a 3x3 matrix can be decomposed in exactly two iterations.</p>
 * 
 * <p>Given a 3x3 matrix</p>
 * <pre>
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
 * </pre>
 * <p>For p(H) = 0</p>
 * <pre>
 * x^3 - (a + e + k)*x^2 + [ae + k*(a + e) - bd]*x + (k(bd - ae) + j(fa - cd)) = 0
 * </pre>
 * 
 * <p>
 * With proper substitution, <br>
 * x^3 + c0 * x^2 + c1 * x + c2 = 0<br>
 * Let x = z + h, where h = -c0/3, therefore<br>
 * z^3 + (3h^2 + 2c0*h + c1)z + (c0*h^2 + c1*h + c2) = 0
 * </p>
 * 
 * <p>
 * With proper substitution, z^3 + 3*pz - 2*q = 0
 * </p>
 * 
 * <p>Consider z^3 - v^3 = (z - v)*(z^2 - vz + v^2)</p>
 * <pre>
 * Let z^3 + 3*pz + 2*q = z^3 - v^3 + w*(z - v)
 *                      = (z - v)*(z^2 - vz + v^2) + w*(z - v)
 *                      = (z - v)*(z^2 - (v - w)*z + v^2 + w*v)</pre>
 * Since z^3 - v^3 + w(z - v) = z^3 + w*z - (v^3 + w*v)
 * <p>
 * Comparing coefficient<br>
 * w = 3p<br>
 * v^3 + w*v = 2q<br>
 * </p>
 * 
 * <p>Therefore v^3 + 3p*v = 2q</p>
 * 
 * <p>
 * A miracle solution for v would be<br>
 * v = [q + (p^3 + q^2)^(1/2)]^(1/3) + [q - (p^3 + q^2)^(1/2)]^(1/3)
 * </p>
 * 
 * <p>For our purpose, a real root is obtained and is enough.</p>
 * 
 * @author Y.K. Chan
 */
public class ShiftedQR3x3 implements QRStep {

    /**
     * Constructor.
     * @param base  Base step to fall through with
     */
    public ShiftedQR3x3(QRStep base) {
        this.base = base;
        this.step = new PureQR();
    }

    @Override
    public int compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow != 3){
            // fall through
            return this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
        }
        double shift = this.eig3x3(matrix, beginRow);
        this.shiftDiag(matrix, beginRow, endRow, -shift); 
        try {
            return this.step.compute(matrix, partner, beginRow, endRow, fullUpper);
        } finally {
            this.shiftDiag(matrix, beginRow, endRow, shift);
        }
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
