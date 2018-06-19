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
package jacobi.core.decomp.chol;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Pure;
import jacobi.core.impl.CopyOnWriteMatrix;
import jacobi.core.util.Pair;
import jacobi.core.util.Throw;
import java.util.Optional;

/**
 *
 * Implementation of Cholesky decomposition.
 * 
 * Cholesky decomposition is given a square matrix A, find lower triangular L 
 * s.t.&nbsp;A = L * L^t.
 * 
 * @author Y.K. Chan
 */
@Pure
public class CholeskyDecomp {
    
    /**
     * Find if a matrix is positive-definite by Cholesky decomposition.
     * @param matrix  Matrix A
     * @return  True if A is positive-definite, false otherwise
     */
    public boolean isPositiveDefinite(Matrix matrix) {
        return this.compute(matrix).isPresent();
    }
    
    /**
     * Compute both L and L^t, where A = L * L^t. 
     * @param matrix  Matrix A
     * @return  A pair of matrix, L and L^t
     */
    public Optional<Pair> computeBoth(Matrix matrix) {        
        return this.compute(matrix)
                .map((lower) -> Pair.of(CopyOnWriteMatrix.of(lower), () -> this.transpose(lower)))
                .map((pair) -> Optional.of(pair))
                .orElse(Optional.empty());
    }
    
    /**
     * Compute the Cholesky decomposition on a symmetric tri-diagonal matrix A, and return the squared elements.
     * @param diags  Diagonal and sup-diagonal elements of A in B-notation
     * @return  L in B-notation, or empty if A is not positive definite
     */
    public Optional<double[]> computeSquared(double[] diags) {
        double[] elem = new double[2*diags.length];
        double prev = 0.0;
        for(int i = 0, j = 0; i < diags.length; i += 2, j += 4){
            if(diags[i] < prev){
                return Optional.empty();
            }
            elem[j] = diags[i] - prev;
            elem[j + 1] = diags[i + 1] * (diags[i + 1] / elem[2*i]); 
            prev = elem[j + 1];
        }
        return Optional.of(elem);
    }
    
    /**
     * Find lower triangular L of a matrix A s.t.&nbsp;A = L * L^t.
     * @param matrix  Matrix A
     * @return  Matrix L or empty if A is not positive definite
     * @throws  IllegalArgumentException if A is null or A is not square
     */
    public Optional<Matrix> compute(Matrix matrix) { 
        Throw.when()
            .isNull(() -> matrix, () -> "No matrix to decompose.")
            .isTrue(
                () -> matrix.getRowCount() != matrix.getColCount(),
                () -> "Unable to decompose a " 
                        + matrix.getRowCount() + "x" + matrix.getColCount()
                        + " matrix.");
        
        Matrix lower = Matrices.zeros(matrix.getRowCount());
        for(int i = 0; i < lower.getRowCount(); i++){ 
            double[] target = matrix.getRow(i);
            double[] row = lower.getRow(i);
            
            int n = i;
            double sumOfSquares = this.compute(target, lower, row, n);
            if(target[n] < sumOfSquares){
                return Optional.empty();
            }
            row[n] = Math.sqrt(target[n] - sumOfSquares);
            lower.setRow(i, row);
        }
        return Optional.of(lower);
    }            

    /**
     * Compute a 1 to n columns of row of lower triangular matrix L.
     * @param target  N-th row in matrix A
     * @param lower  Lower matrix L
     * @param row  Row to be computed
     * @param n  Row index of the row to be computed
     * @return  Sum of squares of 1 to n columns computed
     */
    protected double compute(double[] target, Matrix lower, double[] row, int n) {
        double sumOfSquares = 0.0;
        for(int i = 0; i < n; i++){
            double[] col = lower.getRow(i);
            double temp = target[i];
            for(int j = 0; j < i; j++){
                temp -= row[j] * col[j];
            }
            row[i] = temp / col[i];
            sumOfSquares += row[i] * row[i];
        }
        return sumOfSquares;
    }
    
    /**
     * Find the transpose of a lower triangular matrix.
     * @param lower  Lower triangular matrix
     * @return  A upper triangular matrix
     */
    protected Matrix transpose(Matrix lower) {
        Matrix upper = Matrices.zeros(lower.getRowCount());
        for(int i = 0; i < upper.getRowCount(); i++){
            double[] row = upper.getRow(i);
            for(int j = i; j < row.length; j++){
                row[j] = lower.get(j, i);
            }
            upper.setRow(i, row);
        }
        return upper;
    }
}
