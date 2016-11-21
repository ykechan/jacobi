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

/**
 * Compute Schur decomposition of a 2x2 sub-matrix in a single step.
 * 
 * For a 2x2 matrix, A = [a  b;c  d], the eigenvalues can be found by solving
 * the characteristic polynomial det(A - kI) = 0.
 * 
 * Therefore
 * (a - k) * (d - k) - b * c = 0
 * k^2 - (a + d) * k + (a*d - b*c) = 0
 * k^2 - tr(A)*k + det(A) = 0
 * 
 * The roots can be easily solved by the quadratic formula
 * delta = tr(A)*tr(A) - 4*det(A)
 * k = [tr(A) +- sqrt(delta)]/2
 * 
 * If delta &lt; 0, further decomposition into real upper triangular form 
 * is not possible.
 * 
 * If full decomposition is need, i.e. Q is needed, the eigenvector can be found.
 * 
 * WLOG, assume k be the larger eigenvalue.
 * Find eigenvector by solving Av = k*v
 * a*x + b*y = k*x, where v = [x ; y]
 * y = [(k - a)/b] * x
 * 
 * Imposing v to be a unit vector,
 * x^2 + y^2 = 1
 * x^2 + (k - a)^2/b^2 * x^2 = 1
 * x^2 ( k^2 - 2*a*k + a^2 + b^2 )/b^2 = 1
 * x = +- b / sqrt( k^2 - 2*a*k + a^2 + b^2 )
 * 
 * In general the two eigenvectors are not orthogonal, thus to introduce an 
 * orthogonal basis, find a vector w that is orthongal to v.
 * 
 * A trivial choice would be w = [+-y ; -+x]. And the schur decomposition can be 
 * obtained by A = [ v w ] U [ v w ]^t. As the signs are freely chosen, a clever
 * choice of w would be [y ; -x] s.t. Q = [ x y ; y -x ] is orthogonal and
 * symmetric, thus no need to worry multiplying Q or Q^t, and therefore less
 * error-prone.
 * 
 * This class is a decorator on a more general class for computing Schur
 * decomposition on a general matrix, and handles the 2-by-2 case.
 * 
 * @author Y.K. Chan
 */
public class SingleStep2x2 implements QRStep {

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
            return beginRow;
        }
        if(endRow - beginRow == 2){
            this.compute2x2(matrix, partner, beginRow, fullUpper);
            return beginRow + 1;
        }        
        return this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
    }
    
    /**
     * Compute Schur decomposition for a 2x2 sub-matrix.
     * @param matrix  Input matrix
     * @param partner  Partner matrix
     * @param i  Upper-left index
     * @param fullUpper  Full upper matrix needed
     */
    protected void compute2x2(Matrix matrix, Matrix partner, int i, boolean fullUpper) {
        State state = new State(matrix, i);
        double tr = state.a + state.d;
        double det = state.a * state.d - state.b * state.c;
        
        double delta = tr * tr - 4 * det;
        System.out.println("delta = " + delta);
        for(double[] row : matrix.toArray()){
            for(double elem : row){
                System.out.print(elem + " ");
            }
            System.out.println();
        }
        if(delta < 0.0){
            return;
        }
        delta = Math.sqrt(delta);
        state.eig0 = (tr + delta) / 2.0;
        state.eig1 = (tr - delta) / 2.0;
        
        this.computeEigenvector(state);
        
        this.leftMultQ(state, matrix, i, i);
        this.rightMultQ(state, matrix, 0, i + 2, i);
        if(partner != null){
            this.leftMultQ(state, partner, i, i);
        }
    }
    
    protected void computeEigenvector(State state) {
        double a = Math.abs(state.b) > EPSILON ? state.a : state.c;
        double b = Math.abs(state.b) > EPSILON ? state.b : state.d;
        
        double eig = Math.abs(state.eig0) > Math.abs(state.eig1) ? state.eig0 : state.eig1;
        double denom = eig*eig - 2*a*eig + a*a + b*b;
        
        state.x = b / Math.sqrt(denom);
        state.y = state.x*(eig - a)/b;
    }
    
    protected void leftMultQ(State state, Matrix matrix, int i, int beginCol) {
        double[] upper = matrix.getRow(i);
        double[] lower = matrix.getRow(i + 1);
        this.leftMultQ(state, upper, lower, beginCol);
        matrix.setRow(i, upper);
        matrix.setRow(i + 1, lower);
    }
    
    protected void leftMultQ(State state, double[] upper, double[] lower, int begin) {
        for(int i = begin; i < upper.length; i++){
            double p = state.x * upper[i] + state.y * lower[i];
            double q = state.y * upper[i] - state.x * lower[i];
            upper[i] = p;
            lower[i] = q;
        }
    }
    
    protected void rightMultQ(State state, Matrix matrix, int beginRow, int endRow, int j) {
        for(int i = beginRow; i < endRow; i++){
            double[] row = matrix.getRow(i);
            double p = row[j] * state.x + row[j + 1] * state.y;
            double q = row[j] * state.y - row[j + 1] * state.x;
            row[j    ] = p;
            row[j + 1] = q;
            matrix.setRow(i, row);
        }
    }
    
    private QRStep base;
    
    /**
     * Data object for computation intermediate state
     */
    protected class State {
        
        public final double a, b, c, d;
        
        public double eig0, eig1;
        
        public double x, y;
        
        public State(Matrix matrix, int i) {
            this.a = matrix.get(i, i);
            this.b = matrix.get(i, i + 1);
            this.c = matrix.get(i + 1, i);
            this.d = matrix.get(i + 1, i + 1);
        }
        
    }
        
    private static final double EPSILON = 1e-16;
}
