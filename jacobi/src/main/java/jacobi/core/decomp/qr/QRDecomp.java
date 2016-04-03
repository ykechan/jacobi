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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

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
        AtomicBoolean first = new AtomicBoolean(true);
        Matrix q = Matrices.zeros(matrix.getRowCount(), matrix.getRowCount());
        this.compute(matrix, (hh) -> {
            if(first.getAndSet(false)){
                for(int i = 0; i < q.getRowCount(); i++){
                    q.setRow(i, hh.getRow(i));
                }
            }else{
                hh.applyRight(q);
            }
        });
        return Pair.of(q, matrix);
    }
    
    /**
     * Compute QR decomposition with a partner matrix, i.e. given matrix A and
     * partner matrix B, transform A to R and B to Q^t * R, where A = Q * R, 
     * Q is orthogonal and R is lower trianguar.
     * @param matrix  Matrix A
     * @param partner  Partner matrix B
     * @return  Instance of A, now containing value of R
     */
    public Matrix compute(Matrix matrix, Matrix partner) {
        this.compute(matrix, (hh) -> hh.applyLeft(partner));
        return matrix;
    }
    
    /**
     * Compute QR decomposition which only R is interested.
     * @param matrix  Matrix A to be transformed to R
     * @return  Instance of matrix A that is transformed to R
     */
    public Matrix compute(Matrix matrix) {
        this.compute(matrix, (hh) -> {});
        return matrix;
    }
    
    /**
     * Compute QR decomposition with a listener listening each Householder
     * Reflection operation done.
     * @param matrix  Matrix A
     * @param listener  Householder reflection listener
     */
    protected void compute(Matrix matrix, Consumer<HouseholderReflector> listener) {        
        
        int n = Math.min(matrix.getRowCount(), matrix.getColCount());
        if(matrix.getRowCount() == matrix.getColCount()){
            n--; // last 1x1 matrix need no elimination
        }
        
        for(int j = 0; j < n; j++){
            this.eliminate(matrix, listener, j);
        }
        for(int i = 0; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            Arrays.fill(row, 0, Math.min(i, row.length), 0.0);
            matrix.setRow(i, row);
        }
        return;
    }    
    
    /**
     * Eliminate all sub-diagonal entries of a column 
     * in a matrix A by Householder reflection. 
     * @param matrix  Matrix  A
     * @param listener  Householder reflection listener
     * @param j   Column index of column to be eliminated
     */
    protected void eliminate(Matrix matrix, Consumer<HouseholderReflector> listener, int j) {
        double[] column = new double[matrix.getRowCount()];
        this.getColumn(matrix, j, j, column);
        HouseholderReflector hh = new HouseholderReflector(column, j);
        double norm = hh.normalize();
        if(Math.abs(norm) < EPSILON){
            return;
        }
        hh.applyLeft(matrix, j + 1);            
        matrix.set(j, j, norm);
        listener.accept(hh);        
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
