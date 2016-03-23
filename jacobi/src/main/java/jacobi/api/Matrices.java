/*
 * Copyright (C) 2015 Y.K. Chan
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
package jacobi.api;

import jacobi.core.impl.ColumnVector;
import jacobi.core.impl.CopyOnWriteMatrix;
import jacobi.core.impl.DefaultMatrix;
import jacobi.core.impl.DiagonalMatrix;
import jacobi.core.impl.Empty;
import jacobi.core.impl.ImmutableMatrix;
import jacobi.core.util.Throw;
import java.util.Arrays;

/**
 * Factory for creating Matrices.
 * 
 * @author Y.K. Chan
 */
public final class Matrices {
    
    private Matrices() { 
        throw new UnsupportedOperationException("Do not instaniate.");
    }
    
    /**
     * Create a matrix by the given 2-D array.
     * @param rows  Matrix elements.
     * @return  Matrix instance.
     */
    public static Matrix of(double[][] rows) {
        if(rows == null || rows.length == 0){
            return Empty.getInstance();
        }
        int n = Arrays.stream(rows)
                .mapToInt((r) -> r.length)
                .reduce((i, j) -> {
                    throw new UnsupportedOperationException(
                        "Column count mismatch. " + i + " <> " + j
                    );
                })
                .orElse(0);
        if(n == 0){
            return Empty.getInstance();
        }
        return new DefaultMatrix(rows);
    }
    
    public static Matrix scalar(double value) {
        return new DefaultMatrix(new double[][]{{ value }});
    }
    
    /**
     * Create a square matrix with all elements zero.
     * @param n  Number of rows / columns
     * @return  Matrix instance
     */
    public static Matrix zeros(int n) {
        return Matrices.zeros(n, n);
    }
    
    /**
     * Create a matrix with all elements zero.
     * @param m  Number of rows
     * @param n  Number of columns
     * @return  Matrix instance.
     */
    public static Matrix zeros(int m, int n) {
        if(m < 0 || n < 0){
            throw new IllegalArgumentException("Invalid number of rows / columns : " 
                    + m + "x" + n);
        }
        return (m == 0 || n == 0)
                ? Empty.getInstance() 
                : (n == 1)
                    ? new ColumnVector(m)
                    : new DefaultMatrix(m, n);
    }
    
    /**
     * Create an identity matrix.
     * @param n  Number of rows / columns
     * @return  Matrix instance.
     */
    public static Matrix identity(int n) {
        Matrix eye = Matrices.zeros(n);
        for(int i = 0; i < n; i++){
            eye.set(i, i, 1.0);
        }
        return eye;
    }
    
    /**
     * Create a square diagonal matrix with given diagonal values.
     * @param values  Diagonal values
     * @return  Matrix instance.
     */
    public static Matrix diag(double[] values) {        
        return Matrices.diag((values == null)? 0 : values.length, values);
    }
    
    /**
     * Create a diagonal matrix with given diagonal values.
     * @param m  Number of rows
     * @param values  Diagonal values
     * @return   Matrix instance.
     */
    public static Matrix diag(int m, double[] values) {
        return (values == null || values.length == 0)
                ? Empty.getInstance()
                : CopyOnWriteMatrix.of(new DiagonalMatrix(values));
    }        
    
    /**
     * Create a copy of a matrix that is mutable.
     * @param matrix  Matrix instance
     * @return   A copy of the matrix
     */
    public static Matrix copy(Matrix matrix) {
        if(matrix == null){
            return null;
        }
        if(matrix instanceof ImmutableMatrix){
            return new DefaultMatrix(matrix.toArray());
        }
        return matrix.copy();
    }
}
