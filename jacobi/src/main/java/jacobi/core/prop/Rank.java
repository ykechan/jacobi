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
package jacobi.core.prop;

import jacobi.api.Matrix;
import jacobi.core.decomp.gauss.GenericGaussianElim;
import jacobi.core.util.Throw;

/**
 * Find the rank of a given matrix.
 * 
 * @author Y.K. Chan
 */
public class Rank {

    /**
     * Constructor.
     */
    public Rank() {
        this.gaussElim = new GenericGaussianElim();
    }
    
    /**
     * Compute the rank of a given matrix A.
     * @param a  Matrix A
     * @return  Rank(A)
     */
    public int compute(Matrix a) {
        Throw.when()
            .isNull(() -> a, () -> "No matrix to rank.");
        if(a.getRowCount() == 1){
            return this.compute1x1(a);
        }
        this.gaussElim.compute(a);
        int rank = 0;
        int n = Math.min(a.getRowCount(), a.getColCount());
        for(int i = 0; i < n; i++){
            if(Math.abs(a.get(i, i)) >= EPSILON){
                rank++;
            }
        }
        return rank;
    }
    
    /**
     * Compute rank for a 1x1 matrix, a.k.a.&nbsp; a scalar.
     * @param a  Scalar matrix
     * @return   Rank(A)
     */
    protected int compute1x1(Matrix a) {
        return Math.abs(a.get(0, 0)) < EPSILON ? 0 : 1;
    }
    
    private GenericGaussianElim gaussElim;

    private static final double EPSILON = 1e-12;
}
