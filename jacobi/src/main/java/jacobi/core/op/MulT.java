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
import jacobi.api.annotations.Immutate;
import jacobi.core.util.MapReducer;
import jacobi.core.util.Throw;

/**
 * Compute the product of A * B^t given matrix A and B.
 * 
 * @author Y.K. Chan
 */
@Immutate
public class MulT {
    
    /**
     * Compute matrix C where C = A * B^t.
     * @param a  Input matrix A
     * @param b  Input matrix B
     * @return   Resultant matrix C
     */
    public Matrix compute(Matrix a, Matrix b) {
        Throw.when()
            .isNull(() -> a, () -> "First operand is missing.")
            .isNull(() -> b, () -> "Second operand is missing.")
            .isTrue(
                () -> a.getColCount() != b.getColCount(), 
                () -> "Dimension mismatch. Unable to multiply a "
                    + a.getRowCount()+ "x" + a.getColCount()
                    + " matrix with a "
                    + b.getRowCount()+ "x" + b.getColCount()
                    + " matrix.");
        return this.compute(a, b, Matrices.zeros(a.getRowCount(), b.getRowCount()));
    }

    /**
     * Compute matrix C where C = A * B^t with C created.
     * @param a  Input matrix A
     * @param b  Input matrix B
     * @param ans  Resultant matrix C
     * @return  Resultant matrix C
     */
    protected Matrix compute(Matrix a, Matrix b, Matrix ans) {
        int numFlop = a.getRowCount() * a.getColCount() * b.getRowCount();
        if(numFlop < DEFAULT_LIMIT){
            this.serial(a, b, ans, 0, ans.getRowCount());
        }else{
            this.parallel(a, b, ans, a.getRowCount() * a.getColCount());
        }
        return ans;
    }
    
    /**
     * Compute matrix C where C = A * B^t with C created in parallel.
     * @param a  Input matrix A
     * @param b  Input matrix B
     * @param ans  Resultant matrix C
     * @param rowFlop  Number of flop for each row
     * @return  Resultant matrix C
     */
    protected Matrix parallel(Matrix a, Matrix b, Matrix ans, int rowFlop) {
        return MapReducer.of(0, ans.getRowCount())
                .flop(rowFlop)
                .forEach((begin, end) -> serial(a, b, ans, begin, end), ans);
    }
    
    /**
     * Compute matrix C where C = A * B^t with C created in parallel.
     * @param a  Input matrix A
     * @param b  Input matrix B
     * @param ans  Resultant matrix C
     * @param begin  Begin index of rows of interest
     * @param end  End index of rows of interest
     * @return   Resultant matrix C
     */
    protected Matrix serial(Matrix a, Matrix b, Matrix ans, int begin, int end) {
        for(int i = begin; i < end; i++){
            ans.setRow(i, this.compute(a.getRow(i), b, ans.getRow(i)));
        }
        return ans;
    }        
    
    /**
     * Compute u^t * B * w, where B is matrix and u and w are column vectors.
     * @param u  Column vector u
     * @param b  Matrix B
     * @param w  Column vector w
     * @return  u^t * B * w
     */
    protected double[] compute(double[] u, Matrix b, double[] w) {
        for(int k = 0; k < w.length; k++){
            double elem = 0.0;
            double[] v = b.getRow(k);
            for(int i = 0; i < u.length; i++){
                elem += u[i] * v[i];
            }
            w[k] = elem;
        }
        return w;
    }
    
    /**
     * Default minimum number of FLOP to justify computing in parallel
     */
    protected static final int DEFAULT_LIMIT = 128 * 128 * 128;
}
