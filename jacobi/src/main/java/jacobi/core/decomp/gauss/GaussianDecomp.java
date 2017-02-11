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
package jacobi.core.decomp.gauss;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.impl.CopyOnWriteMatrix;
import jacobi.core.util.Triplet;

/**
 * 
 * This class decomposes a matrix A into P * E * U where E is the product of a 
 * series of elementary matrix, and U is upper triangular, and P is permutation
 * matrix.
 * 
 * 
 * @author Y.K. Chan
 */
public class GaussianDecomp {

    /**
     * Default constructor
     */
    public GaussianDecomp() {
        this(new GenericGaussianElim());
    }    

    /**
     * Constructor with injection.
     * @param gaussElim  Generic Gaussian elimination base
     */
    protected GaussianDecomp(GenericGaussianElim gaussElim) {
        this.gaussElim = gaussElim;
    }
    
    /**
     * Performs Gaussian Elimination with a partner matrix, i.e. transform A to 
     * R and partner matrix B to P^-1 * B, where A = P * L * R s.t. P is a permutation
     * matrix, L is lower triangular and R upper triangular.
     * @param matrix  Matrix A
     * @param partner  Partner matrix B
     * @return  Instance of A, transformed into R
     */
    public Matrix compute(Matrix matrix, Matrix partner) {
        this.gaussElim.compute(matrix, (op) -> new FullMatrixOperator(op, partner));
        return matrix;
    }
    
    /**
     * Performs Gaussian Elimination i.e. transform A to 
     * R and returns &lt;P, L, U&gt;, where A = P * L * R s.t. P is a permutation
     * matrix, L is lower triangular and R upper triangular.
     * @param matrix  Matrix A
     * @return  Instance of A, transformed into R
     */
    public Triplet compute(Matrix matrix) {
        Memento mem = this.gaussElim.compute(matrix, (op) -> new Memento(op));
        return Triplet.of(
            CopyOnWriteMatrix.of(mem.getPermutation().inv()), 
            mem.getLower(), 
            matrix);
    }
    
    private GenericGaussianElim gaussElim;

    private static class Memento extends AbstractElementaryOperatorDecor {

        public Memento(ElementaryOperator op) {
            super(op);
            this.perm = new int[op.getMatrix().getRowCount()];
            for(int i = 0; i < this.perm.length; i++){
                this.perm[i] = i;
            }
            this.lower = Matrices.identity(this.perm.length);
            this.order = 0;
        }
        
        public Permutation getPermutation() {
            return new Permutation(perm, order % 2 == 0 ? 1 : -1);
        }

        public Matrix getLower() {
            return lower;
        }

        @Override
        public void swapRows(int i, int j) {
            super.swapRows(i, j);
            if(i != j){
                int temp = this.perm[i];
                this.perm[i] = this.perm[j];
                this.perm[j] = temp;
                
                this.order++;
                this.swapLower(i, j);                
            }            
        }

        @Override
        public void rowOp(int i, double a, int j) {            
            super.rowOp(i, a, j);
            double[] row = this.lower.getRow(i);
            row[j] = -a;
            this.lower.setRow(i, row);
        }
        
        private void swapLower(int i, int j) {
            int n = Math.min(i, j);
            double[] u = this.lower.getRow(i);
            double[] v = this.lower.getRow(j);
            
            for(int k = 0; k < n; k++){
                double temp = u[k];
                u[k] = v[k];
                v[k] = temp;
            }
            this.lower.setRow(i, u).setRow(j, v);
        }
        
        private int order;
        private int[] perm;
        private Matrix lower;
    }
}
