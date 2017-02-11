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
package jacobi.api;

import jacobi.core.impl.ColumnVector;
import jacobi.core.impl.CopyOnWriteMatrix;
import jacobi.core.impl.DefaultMatrix;
import jacobi.core.impl.DiagonalMatrix;
import jacobi.core.impl.Empty;
import jacobi.core.impl.ImmutableMatrix;
import java.util.Arrays;
import java.util.stream.IntStream;

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
                    if(i == j){
                        return i;
                    }
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
    
    /**
     * Create a matrix by the given 2-D array. The array will be directly used, without checking if it is jagged,
     * and mutation outside reflects the matrix value, but saving the overhead of checking and copying.
     * @param rows  Matrix elements
     * @return  Matrix instance
     */
    public static Matrix unsafe(double[][] rows) {
        return new DefaultMatrix(rows);
    }
    
    /**
     * Create a 1x1 matrix.
     * @param value  Matrix element
     * @return  Matrix instance
     */
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
            return new DefaultMatrix(matrix);
        }
        return matrix.copy();
    }
    
    /**
     * Create a column vector of evenly distributed values in [min, max].
     * @param min  Minimum of interval
     * @param max  Maximum of interval
     * @param num  Number of points
     * @return  An column vector of evenly distributed values
     * @throws IllegalArgumentException if number of points is negative or min &lt; max
     */
    public static Matrix lincol(double min, double max, int num) {        
        return num == 0 
                ? Empty.getInstance()
                : new ColumnVector(Matrices.linspace(min, max, num));
    }
    
    /**
     * Create a row vector of evenly distributed values in [min, max].
     * @param min  Minimum of interval
     * @param max  Maximum of interval
     * @param num  Number of points
     * @return  An row vector of evenly distributed values
     * @throws IllegalArgumentException if number of points is negative or min &lt; max
     */
    public static Matrix linrow(double min, double max, int num) {
        return new DefaultMatrix(new double[][]{ Matrices.linspace(min, max, num) });
    }
    
    /**
     * Create an array of evenly distributed values in [min, max].
     * @param min  Minimum of interval
     * @param max  Maximum of interval
     * @param num  Number of points
     * @return  An array of evenly distributed values
     * @throws IllegalArgumentException if number of points is negative or min &lt; max
     */
    protected static double[] linspace(double min, double max, int num) {
        if(num < 0 || max < min){
            throw new IllegalArgumentException();
        }
        return IntStream.range(0, num)
                .mapToDouble((i) -> (double) i / (double) (num - 1))
                .map((v) -> min + v * (max - min))
                .toArray();
    }
    
}
