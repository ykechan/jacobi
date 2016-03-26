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

package jacobi.core.decomp.qr;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.util.Pair;
import java.util.Arrays;

/**
 * Implementation class for QR Decomposition.
 * 
 * QR decomposition refers to consider A = Q*R where Q is orthogonal and R is 
 * upper triangular.
 * 
 * This class is perturbative, that is it transform the input parameters.
 * 
 * @author Y.K. Chan
 */
public class QRDecomp {
    
    /**
     * Compute QR Decomposition of a matrix A = Q * R.
     * @param matrix  Matrix A to be transformed to R
     * @return  A pair of matrix &lt;Q, R&gt;
     */
    public Pair computeQR(Matrix matrix) {
        double[] column = new double[matrix.getRowCount()];
        this.getColumn(matrix, 0, 0, column);
        
        HouseholderReflector hh = new HouseholderReflector(column, 0);
        double norm = hh.normalize();
        
        Matrix q = null;
        if(Math.abs(norm) < EPSILON){
            q = Matrices.identity(matrix.getRowCount());
        }else{
            hh.applyLeft(matrix);
            matrix.set(0, 0, norm);
        }
        if(matrix.getColCount() == 1){            
            return Pair.of(q, matrix);
        }        
        this.compute(matrix, q, 1);
        return Pair.of(q, matrix);
    }
    
    /**
     * Compute QR decomposition with a partner matrix, i.e. given matrix A and
     * partner matrix B, transform A to R and B to Q^t * R, where A = Q * R, 
     * Q is orthogonal and R is lower trianguar.
     * @param matrix  Matrix A
     * @param partner  Partner matrix B
     */
    public void compute(Matrix matrix, Matrix partner) {
        this.compute(matrix, partner, 0);
    }
    
    /**
     * Compute QR decomposition with a partner matrix, i.e. given matrix A and
     * partner matrix B, transform A to R and B to Q^t * R, where A = Q * R, 
     * Q is orthogonal and R is lower trianguar. This method only interests in
     * columns beyond a certain index in A, but full matrix of B.
     * @param matrix  Matrix A
     * @param partner  Matrix B
     * @param from  Start index of columns of interest
     */
    protected void compute(Matrix matrix, Matrix partner, int from) {
        double[] column = new double[matrix.getRowCount()];
        int n = Math.min(matrix.getRowCount(), matrix.getColCount()) - 1;
        for(int j = from; j < n; j++){
            this.eliminate(matrix, partner, j, column);
        }
        for(int i = 0; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            Arrays.fill(row, 0, Math.min(i, row.length), 0.0);
            matrix.setRow(i, row);
        }
        return;
    }
    
    /**
     * Eliminate a column of a matrix A such that all sub-diagonal entries
     * of this column are zeroes.
     * @param matrix  Matrix A
     * @param partner Partner matrix B
     * @param j  Column index
     * @param column  Column buffer
     */
    protected void eliminate(Matrix matrix, Matrix partner, int j, double[] column) {
        this.getColumn(matrix, j, j, column);
        HouseholderReflector hh = new HouseholderReflector(column, j);
        double norm = hh.normalize();
        if(Math.abs(norm) < EPSILON){
            return;
        }        
        hh.applyLeft(matrix, j + 1);            
        matrix.set(j, j, norm);
        if(partner != null){
            hh.applyLeft(partner);
        }
    }
    
    /**
     * Get the Householder reflector of a column.
     * @param matrix  Matrix A
     * @param fromRow  Start row index of reflection
     * @param colIndex  Column index of column to be reflected
     * @param column  Column reflector buffer
     */
    protected void getColumn(Matrix matrix, int fromRow, int colIndex, double[] column) {
        for(int i = fromRow; i < matrix.getRowCount(); i++){
            column[i] = matrix.get(i, colIndex);
        }
    }
    
    private static final double EPSILON = 1e-10;
}
