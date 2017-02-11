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

package jacobi.core.decomp.qr.step.shifts;

import jacobi.core.givens.GivensPair;
import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.qr.Householder;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Pair;

/**
 * Representation of double-shift. 
 * 
 * A double-shift is a pair of shifts that are both real or conjugate to each other.
 * 
 * Often double-shifts based on the eigenvalues of a 2x2 matrix, which instead of explicitly
 * compute the eigenvalues, the characteristic polynomial is suffice.
 * 
 * The characteristic polynomial of a 2x2 matrix A is given by
 * x^2 - tr(A)*x + det(A) = 0
 * 
 * @author Y.K. Chan
 */
public class DoubleShift {
    
    /**
     * Find double-shift using the eigenvalue of a 2x2 sub-matrix of input matrix A.
     * @param matrix  Input matrix A
     * @param at  Row/Column index of the sub-matrix
     * @return  Double-shift instance
     */
    public static DoubleShift of(Matrix matrix, int at) {
        double a = matrix.get(at, at);
        double b = matrix.get(at, at + 1);
        double c = matrix.get(at + 1, at);
        double d = matrix.get(at + 1, at + 1);        
        return new DoubleShift(a + d, a*d - b*c);
    }

    /**
     * Constructor. 
     * @param tr  Sum of the shift pair
     * @param det  Product of the shift pair
     */
    public DoubleShift(double tr, double det) {
        this.tr = tr;
        this.det = det;
        this.delta = tr * tr - 4.0 * det;
    }

    /**
     * Get the linear term of the characteristic polynomial.
     * @return  Linear term
     */
    public double getTr() {
        return tr;
    }

    /**
     * Get the constant term of the characteristic polynomial.
     * @return  Constant term
     */
    public double getDet() {
        return det;
    }    
    
    /**
     * Check if the eigenvalues are both real or complex.
     * @return  True if both complex, false otherwise.
     */
    public boolean isComplex() {
        return this.delta < 0.0;
    }
    
    /**
     * Compute the pair of eigenvalues.
     * @return A pair of column vectors &lt;A, B&gt; s.t. a[k] + b[k]i is an eigenvalue for k = 0, 1.
     */
    public Pair eig() {
        double del = Math.sqrt(Math.abs(this.delta));
        return this.delta < 0.0
                ? Pair.of(new ColumnVector(tr/2.0, tr/2.0), new ColumnVector(del/2.0, -del/2.0))
                : Pair.of(new ColumnVector((tr + del)/2.0, (tr - del)/2.0), Matrices.zeros(2, 1));
    }
    
    /**
     * Get Householder reflector for creating bulge on sub-matrix of input Hessenberg matrix A using this shift.
     * This method does not check if A is really Hessenberg.
     * @param matrix  Input matrix A
     * @param at  Row/Column index of the sub-matrix
     * @return  Householder reflector for creating bulge.
     */
    public Householder getImplicitQ(Matrix matrix, int at) {
        double u = matrix.get(at, at);
        double v = matrix.get(at + 1, at);
        
        double[] col = new double[at + 3];
        col[at    ] = u * u
            + matrix.get(at, at + 1) * v
            - tr * u
            + det;
        col[at + 1] = v * u
            + matrix.get(at + 1, at + 1) * v
            - tr * v;
        col[at + 2] = matrix.get(at + 2, at + 1) * v;
        Householder hh = new Householder(col, at);
        hh.normalize();
        return hh;
    }    
    
    /**
     * Get a pair of Givens rotation for creating bulge on sub-matrix of input Hessenberg matrix A using this shift.
     * This method does not check if A is really Hessenberg.
     * @param matrix  Input matrix A
     * @param at  Row/Column index of the sub-matrix
     * @return  A pair of Givens rotation for creating this bulge
     */
    public GivensPair getImplicitG(Matrix matrix, int at) {
        double u = matrix.get(at, at);
        double v = matrix.get(at + 1, at);
        return GivensPair.of(u * u
            + matrix.get(at, at + 1) * v
            - tr * u
            + det, 
                
            v * u
            + matrix.get(at + 1, at + 1) * v
            - tr * v, 
            
            matrix.get(at + 2, at + 1) * v);
    }
    
    private double tr, det;
    protected final double delta;
}
