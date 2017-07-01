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
import jacobi.core.decomp.qr.step.shifts.DoubleShift;
import jacobi.core.util.Pair;

/**
 * Compute Schur decomposition of a 2x2 sub-matrix in a single step.
 * 
 * <p>For a 2x2 matrix, A = [a  b;c  d], the eigenvalues can be found by solving
 * the characteristic polynomial det(A - kI) = 0.</p>
 * 
 * <p>Therefore</p>
 * <pre>
 * (a - k) * (d - k) - b * c = 0
 * k^2 - (a + d) * k + (a*d - b*c) = 0
 * k^2 - tr(A)*k + det(A) = 0
 * </pre>
 * 
 * <p>The roots can be easily solved by the quadratic formula</p>
 * <pre>
 * delta = tr(A)*tr(A) - 4*det(A)
 * k = [tr(A) +- sqrt(delta)]/2
 * </pre>
 * 
 * <p>If delta &lt; 0, further decomposition into real upper triangular form is not possible.</p>
 * 
 * <p>If full decomposition is need, i.e.&nbsp;Q is needed, the eigenvector can be found.</p>
 * 
 * <p>WLOG, assume k be the larger eigenvalue.<br>
 * Find eigenvector by solving Av = k*v<br>
 * a*x + b*y = k*x, where v = [x ; y]<br>
 * y = [(k - a)/b] * x<br>
 * </p>
 * 
 * <p>Imposing v to be a unit vector,<p>
 * <pre>
 * x^2 + y^2 = 1
 * x^2 + (k - a)^2/b^2 * x^2 = 1
 * x^2 ( k^2 - 2*a*k + a^2 + b^2 )/b^2 = 1
 * x = +- b / sqrt( k^2 - 2*a*k + a^2 + b^2 )
 * </pre>
 * 
 * <p>In general the two eigenvectors are not orthogonal, thus to introduce an 
 * orthogonal basis, find a vector w that is orthongal to v.</p>
 * 
 * <p>A trivial choice would be w = [+-y ; -+x]. And the schur decomposition can be 
 * obtained by A = [ v w ] U [ v w ]^t. As the signs are freely chosen, a clever
 * choice of w would be [y ; -x] s.t.&nbsp;Q = [ x y ; y -x ] is orthogonal and
 * symmetric, thus no need to worry multiplying Q or Q^t, and therefore less
 * error-prone.</p>
 * 
 * <p>This class is a decorator on a more general class for computing Schur
 * decomposition on a general matrix, and handles the 2-by-2 case.</p>
 * 
 * @author Y.K. Chan
 */
public class SingleStep2x2 implements QRStep {

    /**
     * Constructor with no fall through.
     */
    public SingleStep2x2() {
        this(new DefaultQRStep());
    }

    /**
     * Constructor. 
     * @param base  Implementation to be decorated on
     */
    public SingleStep2x2(QRStep base) {
        this.base = base;
    }

    @Override
    public int compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow < 2){
            return beginRow + 1;
        }
        if(endRow - beginRow == 2){
            this.compute2x2(matrix, partner, beginRow, fullUpper);
            return beginRow + 1;
        }        
        return this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
    }
    
    /**
     * Compute the eigenvalues of a 2x2 sub-matrix of input matrix A.
     * @param matrix  Input matrix A
     * @param at  Row index of the 2x2 sub-matrix.
     * @return  &lt;X, Y&gt; s.t.&nbsp;x[k] + iy[k] is an eigenvalue of A
     */
    public Pair computeEig(Matrix matrix, int at) {
        return DoubleShift.of(matrix, at).eig();
    }
    
    /**
     * Compute Schur decomposition for a 2x2 sub-matrix.
     * @param matrix  Input matrix
     * @param partner  Partner matrix
     * @param at  Row index of the 2x2 sub-matrix
     * @param fullUpper  Full upper matrix needed
     */
    protected void compute2x2(Matrix matrix, Matrix partner, int at, boolean fullUpper) {        
        DoubleShift shift = DoubleShift.of(matrix, at);
        if(shift.isComplex()){
            return;
        }
        Matrix eigs = shift.eig().getLeft();
        Vector2 eigVec = this.computeEigenvector(matrix, at, eigs.get(0, 0), eigs.get(1, 0));
        this.leftMultQ(eigVec, matrix, at, at).rightMultQ(eigVec, matrix, 0, at + 2, at);
        if(partner != null){
            this.rightMultQ(eigVec, partner, 0, partner.getRowCount(), at);
        }
    }    
    
    /**
     * Compute an eigen-vector of a 2x2 sub-matrix of input matrix A.
     * @param matrix  Input matrix A
     * @param at  Row index of the 2x2 sub-matrix
     * @param eig0  First eigenvalue
     * @param eig1  Second eigenvalue
     * @return  An eigen-vector of the 2x2 sub-matrix.
     */
    protected Vector2 computeEigenvector(Matrix matrix, int at, double eig0, double eig1) {        
        //double a = Math.abs(state.b) > EPSILON ? state.a : state.c;
        //double b = Math.abs(state.b) > EPSILON ? state.b : state.d;
        double a = Math.abs(matrix.get(at, at + 1)) > EPS ? matrix.get(at, at) : matrix.get(at + 1, at);
        double b = Math.abs(matrix.get(at, at + 1)) > EPS ? matrix.get(at, at + 1) : matrix.get(at + 1, at + 1);
        
        double eig = Math.abs(eig0) > Math.abs(eig1) ? eig0 : eig1;
        double denom = eig*eig - 2*a*eig + a*a + b*b;
        
        double x = b / Math.sqrt(denom);
        double y = x*(eig - a)/b;
        return new Vector2(x, y);
    }    
    
    /**
     * Multiply the eigenvector v to partner matrix Q, i.e.&nbsp; v^t * Q.
     * @param eigVec  Eigenvector
     * @param matrix  Partner matrix Q
     * @param i  Row index
     * @param beginCol  Begin index of columns of interest
     * @return  This object
     */
    protected SingleStep2x2 leftMultQ(Vector2 eigVec, Matrix matrix, int i, int beginCol) {
        double[] upper = matrix.getRow(i);
        double[] lower = matrix.getRow(i + 1);
        this.leftMultQ(eigVec, upper, lower, beginCol);
        matrix.setRow(i, upper).setRow(i + 1, lower);
        return this;
    }
    
    /**
     *  Multiply the eigenvector matrix v to 2 rows of partner matrix Q, i.e.&nbsp; v^t * Q[i:i+1].
     * @param eigVec  2x2 Eigenvector matrix V
     * @param upper  Upper row
     * @param lower  Lower row
     * @param begin  Begin index of columns of interest
     */
    protected void leftMultQ(Vector2 eigVec, double[] upper, double[] lower, int begin) {
        for(int i = begin; i < upper.length; i++){
            double p = eigVec.x * upper[i] + eigVec.y * lower[i];
            double q = eigVec.y * upper[i] - eigVec.x * lower[i];
            upper[i] = p;
            lower[i] = q;
        }
    }
    
    /**
     * Multiply partner matrix Q to the eigenvector v, i.e.&nbsp; Q * v.
     * @param eigVec  Eigenvector
     * @param matrix  Partner matrix Q
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @param j  Column index
     * @return  This object
     */
    protected SingleStep2x2 rightMultQ(Vector2 eigVec, Matrix matrix, int beginRow, int endRow, int j) {
        for(int i = beginRow; i < endRow; i++){
            double[] row = matrix.getRow(i);
            double p = row[j] * eigVec.x + row[j + 1] * eigVec.y;
            double q = row[j] * eigVec.y - row[j + 1] * eigVec.x;
            row[j    ] = p;
            row[j + 1] = q;
            matrix.setRow(i, row);
        }
        return this;
    }
    
    private QRStep base;
    
    /**
     * Data class for a 2-vector [x, y].
     */
    protected static final class Vector2 {
        
        /**
         * Vector elements
         */
        public final double x, y;

        /**
         * Constructor.
         * @param x  Element x
         * @param y  Element y
         */
        public Vector2(double x, double y) {
            this.x = x;
            this.y = y;
        }
    
    }
        
    private static final double EPS = 1e-16;
}
