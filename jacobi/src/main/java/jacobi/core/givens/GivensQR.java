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
            givens[k++] = this.computeQR(upper, lower, i, endCol);
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
        this.computeRQ(matrix, givens, 0, matrix.getRowCount(), 0);
    }
    
    /**
     * Given a lower triangular matrix R, multiply it with Q, within given 
     * limited region.
     * @param matrix  Input matrix R
     * @param givens  List of Givens rotation s.t. Q^t = G[n] * G[n - 1] * ...
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @param beginCol  Begin index of columns of interest
     * @return  The index of row that has a negligible off-diagonal entry, or negative if none are zero.
     */
    public int computeRQ(Matrix matrix, List<Givens> givens, int beginRow, int endRow, int beginCol) {
        int deflated = beginRow;
        for(int i = beginRow; i < endRow; i++){
            double[] row = matrix.getRow(i);
            int skip = i < beginCol + 1 ? 0 : i - beginCol - 1;                        
            List<Givens> rot = givens.subList(skip, givens.size());
            double off = this.computeRA(row, rot, beginCol + skip);
            if(Math.abs(off) < this.epsilon){
                deflated = i;
            }
            matrix.setRow(i, row);
        }
        return deflated == beginRow ? -1 : deflated;
    }
    
    /**
     * Compute QR decomposition on two rows, which equivalent to eliminate
     * the frontal element of the lower row by the upper row.
     * @param upper  Upper row
     * @param lower  Lower row
     * @param begin  Begin index of element of interest
     * @param end  End index of element of interest
     * @return  Applied Givens rotation
     */
    public Givens computeQR(double[] upper, double[] lower, int begin, int end) {
        return this.apply(Givens.of(upper[begin], lower[begin]), upper, lower, begin, end);
    }
    
    /**
     * Apply givens rotation on two rows.
     * @param givens Givens rotation
     * @param upper  Upper row
     * @param lower  Lower row
     * @param begin  Begin index of element of interest
     * @param end  End index of element of interest
     * @return  Applied Givens rotation
     */
    public Givens apply(Givens givens, double[] upper, double[] lower, int begin, int end) {
        for(int i = begin; i < end; i++){
            double a = upper[i];
            double b = lower[i];
            upper[i] = givens.rotateX(a, b); //givens.getCos() * a - givens.getSin() * b;
            lower[i] = givens.rotateY(a, b); //givens.getSin() * a + givens.getCos() * b;
        }
        return givens;
    }    
    
    /**
     * Apply a list of Givens rotation on the right of a row.
     * @param row  Input row
     * @param givens  List of Givens rotation
     * @param beginCol
     * @return  Return newly computed off diagonal value
     */
    protected double computeRA(double[] row, List<Givens> givens, int beginCol) { 
        double off = givens.get(0).rotateX(row[beginCol], row[beginCol + 1]);
        for(int i = 0; i < givens.size(); i++){ 
            Givens g = givens.get(i);            
            int k = beginCol + i;
            double a = row[k];
            double b = row[k + 1]; 
            row[k]    =  g.rotateX(a, b);
            row[k + 1] = g.rotateY(a, b);
            k++;
        }
        return off;
    }        
    
    private double epsilon;
}
