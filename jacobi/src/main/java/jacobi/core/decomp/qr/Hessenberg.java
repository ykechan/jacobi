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
 * Hessenberg decomposition may not attract much interest invested in itself, but
 * it is the first step of optimization of QR algorithm.
 * 
 * Hessenberg decomposition can be easily computed using Householder reflections.
 * 
 * @author Y.K. Chan
 */
public class Hessenberg {
    
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
                hh.applyLeft(q);
            }
        });
        
        return Pair.of(CopyOnWriteMatrix.of(q), a);
    }
    
    /**
     * Compute A = Q * H * Q^t and gets &lt;Q, H, Q^t&gt;, Q is orthogonal
     * and H is almost upper triangular. Q^t is somewhat redundant but included
     * here for mathematical wholeness.
     * @param a  Matrix A
     * @return   A pair of matrices &lt;Q, U&gt;
     */
    public Triplet computeQHQt(Matrix a) {
        Pair qh = this.computeQH(a);
        return Triplet.of(
            qh.getLeft(),
            a,
            new Transpose().compose(qh.getRight()) );
    }
    
    /**
     * Reduce a matrix A into a almost upper triangular matrix, and listen
     * to each reflector operation.
     * @param matrix  Matrix A
     * @param listener   Listener of reflector operation
     */
    protected void compute(Matrix matrix, Consumer<HouseholderReflector> listener) {
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
    protected void eliminate(Matrix matrix, int i, double[] column, Consumer<HouseholderReflector> listener) {
        this.getColumn(matrix, i + 1, i, column);
        HouseholderReflector hh = new HouseholderReflector(column, i + 1);
        double norm = hh.normalize();
        if(Math.abs(norm) < EPSILON){
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
                () -> matrix.getRowCount() == 0, 
                () -> "Unable to decompose an empty matrix."
            )
            .isTrue(
                () -> matrix.getRowCount() != matrix.getColCount(),
                () -> "Hessenburg decomposition not exists for non-square "
                    + matrix.getRowCount() + "x" + matrix.getColCount()
                    + " matrix.");
    }
    
    private static final double EPSILON = 1e-10;
}
