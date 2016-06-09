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

package jacobi.core.decomp.qr.step;

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
     * Compute QR decomposition on a Hessenberg matrix H, and transform it
     * into R
     * @param matrix  Input Hesseberg matrix H
     * @return  List of Givens rotations applied
     */
    public List<Givens> computeQR(Matrix matrix) {
        return this.computeQR(matrix, 0, matrix.getRowCount(), matrix.getColCount());
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
        Givens[] givens = new Givens[n];
        for(int i = beginRow; i < n; i++){
            double[] upper = matrix.getRow(i);
            double[] lower = matrix.getRow(i + 1);
            givens[i] = this.computeQR(upper, lower, i, endCol);
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
        this.computeRQ(matrix, givens, 0, matrix.getRowCount(), matrix.getColCount());
    }
    
    /**
     * Given a lower triangular matrix R, multiply it with Q, within given 
     * limited region.
     * @param matrix  Input matrix R
     * @param givens  List of Givens rotation s.t. Q^t = G[n] * G[n - 1] * ...
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @param endCol  End index of columns of interest
     */
    public void computeRQ(Matrix matrix, List<Givens> givens, int beginRow, int endRow, int endCol) {
        for(int i = beginRow; i < endRow; i++){
            double[] row = matrix.getRow(i);
            this.computeRA(row, givens, i < 1 ? 0 : i - 1, givens.size());
            matrix.setRow(i, row);
        }
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
        Givens givens = this.of(upper[begin], lower[begin]);
        for(int i = begin; i < end; i++){
            double a = upper[i];
            double b = lower[i];
            upper[i] = givens.rotateX(a, b); //givens.getCos() * a - givens.getSin() * b;
            lower[i] = givens.rotateY(a, b); //givens.getSin() * a + givens.getCos() * b;
        }
        return givens;
    }
    
    /**
     * Reduce an almost Hessenberg matrix into Hessenberg form. This is essentially
     * the bulge-chasing algorithm, which the input matrix is almost Hessenberg
     * in the sense that only the first column of 3rd row is non-zero.
     * @param matrix  Input matrix in almost Hessenberg form
     * @param beginRow  Start of row of interest
     * @param endRow   End of row of interest
     * @return List of Givens rotation applied
     */
    public List<Givens> computeQHQt(Matrix matrix, int beginRow, int endRow) {
        return null;
    }
    
    protected List<Givens> chaseDiag(Matrix matrix, int beginRow, int endRow) {
        return null;
    }
    
    protected void computeRA(Matrix matrix, List<Givens> givens, int beginRow, int endRow) {
        for(int i = beginRow; i < endRow; i++){
            double[] row = matrix.getRow(i);
            this.computeRA(row, givens, 0, row.length);
            matrix.setRow(i, row);
        }
    }
    
    /**
     * Apply a list of Givens rotation on the right of a row.
     * @param row  Input row
     * @param givens  List of Givens rotation
     * @param begin  Begin index of element of interest
     * @param end   End index of element of interest
     */
    protected void computeRA(double[] row, List<Givens> givens, int begin, int end) { 
        for(int i = begin; i < end; i++){
            Givens g = givens.get(i);
            double a = row[i];
            double b = row[i + 1];   
            row[i]    =  g.transRevRotX(a, b);
            row[i + 1] = g.transRevRotY(a, b);
        }
    }        
    
    /**
     * Get Givens rotation for reducing [a, b] -&gt; [r, 0].
     * @param a  Upper element
     * @param b  Lower element
     * @return  Givens rotation
     */
    public Givens of(double a, double b) {
        double r = Math.sqrt(a * a + b * b); // TODO: change to hypot
        return new Givens(r, a / r, -b / r);
    }

    /**
     * Data object for Givens rotation for reducing [a, b] &gt; [r, 0].
     * The Givens rotation is of the form
     * [ c  -s ][ a ]   [ r ]
     * [       ][   ] = [   ]
     * [ s   c ][ b ]   [ 0 ]
     */
    public static final class Givens {

        /**
         * Constructor.
         * @param mag  Value of r
         * @param cos  Value of c
         * @param sin  Value of s
         */
        public Givens(double mag, double cos, double sin) {
            this.mag = mag;
            this.cos = cos;
            this.sin = sin;
        }

        /**
         * @return  Value of r
         */
        public double getMag() {
            return mag;
        }

        /**
         * @return  Value of c
         */
        public double getCos() {
            return cos;
        }

        /**
         * @return  Value of s
         */
        public double getSin() {
            return sin;
        }
        
        /**
         * Compute G * [a b]' and return the upper element.
         * @param a  Upper vector element 
         * @param b  Lower vector element
         * @return  Upper element of G * [a b]'
         */
        public double rotateX(double a, double b) {
            return this.getCos() * a - this.getSin() * b;
        }
        
        /**
         * Compute G * [a b]' and return the lower element.
         * @param a  Upper vector element 
         * @param b  Lower vector element
         * @return  Lower element of G * [a b]'
         */
        public double rotateY(double a, double b) {
            return this.getSin() * a + this.getCos() * b;
        }
        
        /**
         * Compute [a b] * G^t and return the upper element.
         * @param a  Upper vector element
         * @param b  Lower vector element
         * @return  Upper element of [a b] * G
         */
        public double transRevRotX(double a, double b) {
            return this.getCos() * a - this.getSin() * b;
        }
        
        /**
         * Compute [a b] * G^t and return the lower element.
         * @param a  Upper vector element
         * @param b  Lower vector element
         * @return  Lower element of [a b] * G
         */
        public double transRevRotY(double a, double b) {
            return this.getSin() * a + this.getCos() * b;
        }
        
        private double mag, cos, sin;
    }
}
