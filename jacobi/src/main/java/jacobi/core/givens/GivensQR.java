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
package jacobi.core.givens;

import jacobi.api.Matrix;
import java.util.Arrays;
import java.util.List;

/**
 * Perform QR Decomposition on a upper Hessenberg matrix.
 * 
 * Given the special structure of a upper Hessenberg matrix, it is much easier
 * to perform QR decomposition by Givens rotation instead of Householder Reflection
 * which is used in the general case.
 * 
 * This class is designed to perform QR algorithm. 
 * 
 * This class doesn't work with general matrix, and for performance it doesn't 
 * check if the matrix is actually Hessenberg.
 * 
 * @author Y.K. Chan
 */
public class GivensQR {

    /**
     * Constructor.
     */
    public GivensQR() {
        this(1e-12);
    }

    /**
     * Constructor with given machine epsilon.
     * @param epsilon  Machine epsilon
     */
    public GivensQR(double epsilon) {
        this.epsilon = epsilon;
    }
    
    /**
     * Compute QR decomposition on a Hessenberg matrix H, and transform it
     * into R, with given limited range.
     * @param matrix  Input Hessenberg matrix H
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @param endCol  End index of columns of interest
     * @return  List of Givens rotation applied
     */
    public List<Givens> computeQR(Matrix matrix, int beginRow, int endRow, int endCol) {
        int n = endRow - 1;
        Givens[] givens = new Givens[n - beginRow];
        int k = 0;
        for(int i = beginRow; i < n; i++){
            double[] upper = matrix.getRow(i);
            double[] lower = matrix.getRow(i + 1);
            givens[k++] = this.applyGivens(upper, lower, i, endCol);
            matrix.setRow(i, upper).setRow(i + 1, lower);
        }
        return Arrays.asList(givens);
    }
    
    /**
     * Given a lower triangular matrix R, multiply it with Q.
     * @param matrix  Input matrix R
     * @param givens  List of Givens rotation s.t. Q^t = G[n] * G[n - 1] * ...
     */    
    public void computeRQ(Matrix matrix, List<Givens> givens) {
        this.computeRQ(matrix, givens, 0, matrix.getRowCount(), 0, true);
    }
    
    /**
     * Given a lower triangular matrix R, multiply it with Q, within given 
     * limited region.
     * @param matrix  Input matrix R
     * @param givens  List of Givens rotation s.t. Q^t = G[n] * G[n - 1] * ...
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @param beginCol  Begin index of columns of interest
     * @param isUpper  True if input matrix is upper triangular, false otherwise
     * @return  The index of row that has a negligible off-diagonal entry, or negative if none are zero.
     */
    public int computeRQ(Matrix matrix, List<Givens> givens, int beginRow, int endRow, int beginCol, boolean isUpper) {
        int deflated = beginRow;
        for(int i = beginRow; i < endRow; i++){
            double[] row = matrix.getRow(i);
            int skip = !isUpper || i < beginCol + 1 ? 0 : i - beginCol - 1; 
            List<Givens> rot = givens.subList(skip, givens.size());
            double off = this.computeRQ(row, rot, beginCol + skip);
            if(Math.abs(off) < this.epsilon){
                deflated = i;
            }
            matrix.setRow(i, row);
        }
        return deflated == beginRow ? -1 : deflated;
    }    
    
    /**
     * Apply a list of Givens rotation on the right of a row.
     * @param row  Input row
     * @param givens  List of Givens rotation
     * @param beginCol  Index of column of interest
     * @return  Return newly computed off diagonal value
     */
    protected double computeRQ(double[] row, List<Givens> givens, int beginCol) { 
        double off = givens.get(0).rotateX(row[beginCol], row[beginCol + 1]);
        for(int i = 0; i < givens.size(); i++){ 
            givens.get(i).applyRight(row, beginCol + i);
        }
        return off;
    }
    
    /**
     * Apply Givens rotation to two vectors to reduce the frontal element of the lower vector.
     * @param upper  Upper vector
     * @param lower  Lower vector
     * @param begin  Begin of columns of interest
     * @param end  End of columns of interest
     * @return  The Givens rotation applied.
     */
    protected Givens applyGivens(double[] upper, double[] lower, int begin, int end) {
        return Givens.of(upper[begin], lower[begin]).applyLeft(upper, lower, begin, end);
    }
    
    private double epsilon;
}
