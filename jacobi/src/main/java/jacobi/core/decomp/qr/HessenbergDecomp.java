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
import jacobi.core.impl.CopyOnWriteMatrix;
import jacobi.core.prop.Transpose;
import jacobi.core.util.Pair;
import jacobi.core.util.Throw;
import jacobi.core.util.Triplet;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Hessenberg decomposition is decomposing a matrix A into Q * H * Q^t, where
 * Q is orthogonal and H is almost triangular. Almost triangular here refers to
 * almost upper triangular, in that all elements below the sub-diagonal are zero. 
 * Sub-diagonal is not necessarily zeroes though.
 * 
 * <p>Hessenberg decomposition may not attract much interest invested in itself, but
 * it is the first step of optimization of QR algorithm.</p>
 * 
 * <p>Hessenberg decomposition can be easily computed using Householder reflections.</p>
 * 
 * @author Y.K. Chan
 */
public class HessenbergDecomp {
    
    /**
     * Reduce a matrix A into a almost upper triangular matrix.
     * @param a  Matrix A
     * @return   Instance of matrix A transformed in upper sub-diagonal.
     */
    public Matrix compute(Matrix a) {        
        this.compute(a, (hh) -> {});
        return a;
    }
    
    /**
     * Compute A = Q * H * Q^t and gets &lt;Q, H&gt;, Q is orthogonal
     * and H is almost upper triangular. Since Q^t is somewhat
     * redundant and can easily computed from Q, its value is omitted.
     * @param a  Matrix A
     * @return   A pair of matrices &lt;Q, U&gt;
     */
    public Pair computeQH(Matrix a) {
        AtomicBoolean first = new AtomicBoolean(true);
        Matrix q = Matrices.zeros(a.getColCount());        
        this.compute(a, (hh) -> {
            if(first.getAndSet(false)){
                for(int i = 0; i < q.getRowCount(); i++){
                    q.setRow(i, hh.getRow(i));
                }
            }else{
                hh.applyRight(q);
            }
        });
        
        return Pair.of(CopyOnWriteMatrix.of(q), a);
    }
    
    /**
     * Compute A = Q * H * Q^t and gets &lt;Q, H, Q^t&gt;, Q is orthogonal
     * and H is almost upper triangular. Q^t is somewhat redundant but included
     * here for mathematical completeness.
     * @param a  Matrix A
     * @return   A pair of matrices &lt;Q, U&gt;
     */
    public Triplet computeQHQt(Matrix a) {
        Pair qh = this.computeQH(a);
        return Triplet.of(
            qh.getLeft(),
            a,
            new Transpose().compute(qh.getLeft()) );
    }
    
    /**
     * Reduce a matrix A into a almost upper triangular matrix, and listen
     * to each reflector operation.
     * @param matrix  Matrix A
     * @param listener   Listener of reflector operation
     */
    protected void compute(Matrix matrix, Consumer<Householder> listener) {
        this.validate(matrix);
        if(matrix.getRowCount() < 3){
            return;
        }
        int n = matrix.getColCount() - 2;
        double[] columnBuffer = new double[matrix.getRowCount()];
        for(int i = 0; i < n; i++){
            this.eliminate(matrix, i, columnBuffer, listener);
        }
        for(int i = 2; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            Arrays.fill(row, 0, i - 1, 0.0);
            matrix.setRow(i, row);
        }
    }
    
    /**
     * Eliminate all entries below sub-diagonal of a column in matrix A.
     * @param matrix  Matrix A
     * @param i  Column index 
     * @param column  Column buffer 
     * @param listener  Reflection listener
     */
    protected void eliminate(Matrix matrix, int i, double[] column, Consumer<Householder> listener) {
        this.getColumn(matrix, i + 1, i, column);
        Householder hh = new Householder(column, i + 1);
        double norm = hh.normalize();
        if(norm == 0.0){
            return;
        }
        matrix.set(i + 1, i, norm);
        hh.applyLeft(matrix, i + 1);
        hh.applyRight(matrix);
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
    
    private void validate(Matrix matrix) {
        Throw.when()
            .isNull(() -> matrix, () -> "No matrix to decompose.")
            .isTrue(
                () -> matrix.getRowCount() != matrix.getColCount(),
                () -> "Hessenburg decomposition not exists for non-square "
                    + matrix.getRowCount() + "x" + matrix.getColCount()
                    + " matrix.");
    }
    
}
