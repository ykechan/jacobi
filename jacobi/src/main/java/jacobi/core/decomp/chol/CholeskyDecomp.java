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

package jacobi.core.decomp.chol;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.NonPerturbative;
import jacobi.core.impl.CopyOnWriteMatrix;
import jacobi.core.util.Pair;
import jacobi.core.util.Throw;
import jacobi.core.util.Triplet;

/**
 *
 * Implementation of Cholesky decomposition.
 * 
 * Cholesky decomposition is given a square matrix A, find lower triangular L 
 * s.t. A = L * L^t.
 * 
 * @author Y.K. Chan
 */
@NonPerturbative
public class CholeskyDecomp {
    
    /**
     * Find if a matrix is positive-definite by Cholesky decomposition.
     * @param matrix  Matrix A
     * @return  True if A is positive-definite, false otherwise
     */
    public boolean isPositiveDefinite(Matrix matrix) {
        try {
            this.compute(matrix);
            return true;
        }catch(UnsupportedOperationException ex){
            return false;
        }
    }
    
    /**
     * Compute both L and L^t, where A = L * L^t. 
     * @param matrix  Matrix A
     * @return  A pair of matrix, L and L^t
     */
    public Pair computeBoth(Matrix matrix) {
        Matrix lower = this.compute(matrix);
        return Pair.of(
            CopyOnWriteMatrix.of(lower),
            () -> this.transpose(lower)
        );
        
    }
    
    public Triplet computeLDL() {
        return null;
    }
    
    /**
     * Find lower triangular L of a matrix A s.t. A = L * L^t.
     * @param matrix  Matrix A
     * @return  Matrix L
     * @throws  
     *     IllegalArgumentException if A is null or A is not square
     *     UnsupportedOperationException if A is not positive-definite
     */
    public Matrix compute(Matrix matrix) { 
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
                throw new UnsupportedOperationException("Matrix is not positive definite.");
            }
            row[n] = Math.sqrt(target[n] - sumOfSquares);
            lower.setRow(i, row);
        }
        return lower;
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
