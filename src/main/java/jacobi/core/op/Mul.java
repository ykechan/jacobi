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
package jacobi.core.op;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Pure;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.ParallelSupplier;
import jacobi.core.util.Throw;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * Matrix Multiplication operator, i.e.&nbsp;computes C = A * B.
 * 
 * @author Y.K. Chan
 */
@Pure
public class Mul {
    
    /**
     * Default partition size for cache utilization.
     */
    public static final int DEFAULT_STRIDE_LENGTH = 8;

    /**
     * Constructor.
     */
    public Mul() {
        this(DEFAULT_STRIDE_LENGTH);
    }

    /**
     * Constructor.
     * @param stride  Partition size
     */
    public Mul(int stride) {
        this.stride = stride;
        this.mulT = new MulT();
    }
    
    /**
     * Compute matrix C where C = A * B.
     * @param a  Input matrix A
     * @param b  Input matrix B
     * @return   Resultant matrix C
     */
    public Matrix compute(Matrix a, Matrix b) {
        Throw.when()
            .isNull(() -> a, () -> "First operand is missing.")
            .isNull(() -> b, () -> "Second operand is missing.")
            .isTrue(
                () -> a.getColCount() != b.getRowCount(), 
                () -> "Dimension mismatch. Unable to multiply a "
                    + a.getRowCount()+ "x" + a.getColCount()
                    + " matrix with a "
                    + b.getRowCount()+ "x" + b.getColCount()
                    + " matrix.");
        
        if(b instanceof ColumnVector){
            return a.getRowCount() == 1 
                ? Matrices.scalar(this.dot(a.getRow(0), ((ColumnVector) b).getVector()))
                : this.mulVector(a, (ColumnVector) b);
        }
        
        Matrix ans = Matrices.zeros(a.getRowCount(), b.getColCount());
        this.compute(a, this.copy(b), ans);
        return ans;
    }
    
    /**
     * Fill the entries of resultant matrix C where C = A * B.
     * @param a  Input matrix A
     * @param b  Input matrix B
     * @param ans   Resultant matrix C
     */
    protected void compute(Matrix a, Matrix b, Matrix ans) {
        long numFlop = ((long) ans.getRowCount() * ans.getColCount()) * a.getColCount();
        if(a.getRowCount() < this.stride || numFlop < ParallelSupplier.DEFAULT_FLOP_THRESHOLD ){
            this.serial(a, b, ans);            
        }else{ 
            this.parallel(a, b, ans);
        }
    }

    /**
     * Fill the entries of resultant matrix C where C = A * B in serial.
     * @param a  Input matrix A
     * @param b  Input matrix B
     * @param ans   Resultant matrix C
     */
    protected void serial(Matrix a, Matrix b, Matrix ans) {
        for(int i = 0; i < ans.getRowCount(); i++){
            double[] u = a.getRow(i);
            double[] v = ans.getRow(i);
            this.computeRow(u, b, v);
            ans.setRow(i, v);
        }
    }
    
    /**
     * Fill the entries of resultant matrix C where C = A * B in parallel.
     * @param a  Input matrix A
     * @param b  Input matrix B
     * @param ans   Resultant matrix C
     */
    protected void parallel(Matrix a, Matrix b, Matrix ans) {
        int numThreads = Math.min(this.stride, ParallelSupplier.DEFAULT_NUM_THREADS);
        IntConsumer task = (i) -> ans.getAndSet(i, (r) -> this.computeRow(a.getRow(i), b, r));
        ParallelSupplier.cyclic(task, 0, a.getRowCount(), numThreads);
    }
    
    /**
     * Compute v = u * B, where u and v are vectors and B is a matrix.
     * @param u  Input vector u
     * @param b  Input matrix B
     * @param v  Output vector v
     */
    protected void computeRow(double[] u, Matrix b, double[] v) {
        int numRows = u.length % this.stride == 0 ? u.length : (1 + u.length / this.stride) * this.stride;
        int numCols = v.length % this.stride == 0 ? v.length : (1 + v.length / this.stride) * this.stride;
        for(int i = 0; i < numRows; i += this.stride){
            int rowSpan = Math.min(this.stride, u.length - i);            
            for(int j = 0; j < numCols; j += this.stride){
                int colSpan = Math.min(this.stride, v.length - j);
                this.computeBlock(u, b, i, i + rowSpan, j, j + colSpan, v);
            }
        }
    }
    
    /**
     * Compute v[p:q] = v[p:q] + u[a:b] * B[a:b, p:q], where B[a:b, p:q] is a selected sub-matrix of B.
     * @param u  Input vector u
     * @param b  Input matrix B
     * @param rowBegin  Begin of rows selected
     * @param rowEnd  End of rows selected
     * @param colBegin  Begin of columns selected
     * @param colEnd  End of columns selected
     * @param v   Output vector v
     */
    protected void computeBlock(double[] u, Matrix b, int rowBegin, int rowEnd, int colBegin, int colEnd, double[] v) {
        for(int i = rowBegin; i < rowEnd; i++){
            double elem = u[i];            
            for(int j = colBegin; j < colEnd; j++){
                double[] r = b.getRow(i);
                v[j] += elem * r[j];
            }
        }
    }
    
    /**
     * Compute A * b where b is a column vector.
     * @param a  Input matrix A
     * @param b  Input column vector b
     * @return  A * b
     */
    protected Matrix mulVector(Matrix a, ColumnVector b) {
        return this.mulT.compute(a, Matrices.wrap(new double[][]{ b.getVector() }));
    }
    
    /**
     * Compute the dot product of vector u and v.
     * @param u  Input vector u
     * @param v  Input vector v
     * @return  Dot product
     */
    protected double dot(double[] u, double[] v) {
        return Dot.prod(u, v);
    }
    
    /**
     * Shallow copy of a matrix, i.e.&nbsp;only row references are copied.
     * The copy of the matrix should not be mutated.
     * @param mat  Matrix to be copied
     * @return  Shallow copy of a matrix
     */
    protected Matrix copy(Matrix mat) {
        return Matrices.wrap(IntStream.range(0, mat.getRowCount())
            .mapToObj(mat::getRow)
            .toArray(n -> new double[n][]));
    }
    
    private int stride;
    private MulT mulT;
}
