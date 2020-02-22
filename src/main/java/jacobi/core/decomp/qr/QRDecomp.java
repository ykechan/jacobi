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
 * <p>QR decomposition refers to consider A = Q*R where Q is orthogonal and R is 
 * upper triangular.</p>
 * 
 * <p>This class is mutating, i.e.&nbsp;it transform the input parameters.</p>
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
     * Compute QR decomposition with a partner matrix, i.e.&nbsp;given matrix A and
     * partner matrix B, transform A to R and B to Q^t * R, where A = Q * R, 
     * Q is orthogonal and R is lower trianguar.
     * @param matrix  Matrix A
     * @param partner  Partner matrix B
     * @return  Instance of A, now containing value of R
     */
    public Matrix compute(Matrix matrix, Matrix partner) {
        this.compute(matrix, (hh) -> hh.applyLeft(partner) );
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
    public void compute(Matrix matrix, Consumer<Householder> listener) {        
        
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
    protected void eliminate(Matrix matrix, Consumer<Householder> listener, int j) {
        double[] column = new double[matrix.getRowCount()];
        this.getColumn(matrix, j, j, column);
        Householder hh = new Householder(column, j);
        double norm = hh.normalize();
        if(norm == 0.0){
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
    
}
