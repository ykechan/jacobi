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
import jacobi.core.util.MapReducer;
import jacobi.core.util.Throw;
import java.util.function.DoubleBinaryOperator;
import java.util.stream.IntStream;

/**
 * Perform operations in a row-by-row manner.
 * 
 * <p>This class computes a matrix function C = f(A, B) where A, B, C are matrices
 * in the same dimension. </p>
 * 
 * <p>Also the function f has the property that there exists a vector field g s.t.
 * C[i,:] = g(A[i,:], B[i,:])
 * where A[i,:], B[i,:], C[i,:] are i-th row of A, B, C respectively.</p>
 * 
 * @author Y.K. Chan
 */
public class RowBased {
    
    /**
     * Construct with given element-wise function.
     * @param op   Element-wise function of g
     */
    public RowBased(DoubleBinaryOperator op) {
        this(RowOperation.forEach(op));
    }

    /**
     * Construct with given row-wise function.
     * @param oper Row-wise function g
     */
    public RowBased(RowOperation<Void> oper) {
        this.oper = oper;
    }
    
    /**
     * Implementation method. Compute C = f(A, B)
     * @param a  Matrix A
     * @param b  Matrix B
     * @return   Result matrix C.
     * @throws   IllegalArgumentException
     *    when A or B is null or not in same dimension.
     */
    public Matrix compute(Matrix a, Matrix b) {
        Throw.when()
            .isNull(() -> a, () -> "1st operand is null.")
            .isNull(() -> b, () -> "2nd operand is null.")
            .isFalse(
                () -> a.getRowCount() == b.getRowCount(),
                () -> "Row count mismatch. "
                      +  a.getRowCount() 
                      + " <> " 
                      + b.getRowCount())
            .isFalse(
                () -> a.getColCount() == b.getColCount(),
                () -> "Column count mismatch."
                      +  a.getColCount() 
                      + " <> " 
                      + b.getColCount());
        
        Matrix ans = Matrices.zeros(a.getRowCount(), a.getColCount());
        this.compute(a, b, ans);
        return ans;
    }
    
    /**
     * Compute result in with resultant matrix C created.
     * @param a  Matrix A
     * @param b  Matrix B
     * @param ans   Resultant matrix C
     */
    protected void compute(Matrix a, Matrix b, Matrix ans) {
        if(a.getRowCount() * a.getColCount() < DEFAULT_NUM_FLOP){
            this.serial(a, b, ans, 0, a.getRowCount());
        }else{
            this.forkJoin(a, b, ans, a.getColCount());
        }        
    }
    
    /**
     * Compute result in serial of given region of rows of interest.
     * @param a  Matrix A
     * @param b  Matrix B
     * @param ans  Result matrix C
     * @param begin  Start of index of row
     * @param end   End of index of row, exclusive
     */
    protected void serial(Matrix a, Matrix b, Matrix ans, int begin, int end) {
        for(int i = begin; i < end; i++){
            this.operate(a, b, ans, i);
        }
    }
    
    /**
     * Compute result in parallel by Streams.
     * @param a  Matrix A
     * @param b  Matrix B
     * @param ans  Result matrix C
     */
    protected void stream(Matrix a, Matrix b, Matrix ans) {
        IntStream.range(0, a.getRowCount()).parallel().forEach(
            (i) -> this.operate(a, b, ans, i)
        );
    }
    
    /**
     * Compute result in parallel by Fork-Join model.
     * @param a  Matrix A
     * @param b  Matrix B
     * @param ans  Result matrix C
     * @param rowFlop Number of flop to process a single row
     */
    protected void forkJoin(Matrix a, Matrix b, Matrix ans, int rowFlop) {        
        MapReducer.of(0, a.getRowCount())
                .flop(rowFlop)
                .forEach((begin, end) -> this.serial(a, b, ans, begin, end));
    }
    
    private void operate(Matrix a, Matrix b, Matrix ans, int i) {
        double[] w = ans.getRow(i);
        this.oper.apply(a.getRow(i), b.getRow(i), w);
        ans.setRow(i, w);
    }
    
    private RowOperation<Void> oper;
    
    private static final int DEFAULT_NUM_FLOP = 512 * 512;    
}
