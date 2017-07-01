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

package jacobi.core.decomp.svd;

import jacobi.api.Matrix;
import jacobi.core.util.Divider;
import jacobi.core.util.Throw;

/**
 * Implementation of Singular Value Decomposition.
 * 
 * <p>Given a matrix A, find orthogonal matrices U and V s.t.&nbsp;A = U*E*V^t where E is a diagonal matrix.</p>
 * 
 * @author Y.K. Chan
 */
public class SingularValueDecomp {

    /**
     * Constructor.
     */
    public SingularValueDecomp() {
        this(new GolubKahanBDD(), new GolubKahanSVD());
    }
    
    /**
     * Constructor.
     * @param bdd  Bi-diagonal decomposition implementation
     * @param step   Iteration implementation for SVD
     */
    public SingularValueDecomp(BiDiagDecomp bdd, SvdStep step) {
        this.bdd = bdd;
        this.step = step;
    }
    
    /**
     * Find singular values of a matrix.
     * @param matrix  Input matrix
     * @return  Singular values
     */
    public double[] compute(Matrix matrix) {
        return this.compute(matrix, null, null);
    }
    
    /**
     * Compute SVD with partner matrices.
     * @param matrix  Input matrix A
     * @param left  Partner matrix B to be transformed into U^t * B.
     * @param right  Partner matrix C to be transformed into C * V
     * @return  Singular values
     */
    public double[] compute(Matrix matrix, Matrix left, Matrix right) {
        Throw.when()
                .isNull(() -> matrix, () -> "No matrix to decompose.")
                .isFalse(() -> left == null || left.getColCount() == matrix.getRowCount(), 
                         () -> "Dimension mismatch for left partner matrix.")
                .isFalse(() -> right == null || right.getRowCount() == matrix.getColCount(), 
                         () -> "Dimension mismatch for right partner matrix.");
        if(matrix.getRowCount() == 0){
            if(left != null || right != null){
                throw new IllegalArgumentException("Illegal usage.");
            }
            return new double[0];
        }
        double[] biDiag = this.bdd.compute(BiDiagDecomp.Mode.UPPER, matrix);
        if(left == null || right == null){
            // only singular values are needed
            // ...
        }
        double[][] diags = this.separate(biDiag);
        return Divider.repeats((begin, end) -> this.step.compute(diags[0], diags[1], begin, end, left, right))
                .visit(0, diags[0].length)
                .echo(diags[0]);
    }    
    
    /**
     * Separate diagonal and sup-diagonal elements from B-notation.
     * @param biDiag  Bi-diagonal elements in B-notation.
     * @return  Diagonal and sup-diagonal elements
     */
    protected double[][] separate(double[] biDiag) {
        double[] diag = new double[biDiag.length / 2];
        double[] supDiag = new double[biDiag.length / 2];
        for(int i = 0; i < diag.length; i++){
            diag[i] = biDiag[2*i];
            supDiag[i] = biDiag[2*i + 1];
        }
        return new double[][]{ diag, supDiag };
    }
    
    private BiDiagDecomp bdd;
    private SvdStep step;
}
