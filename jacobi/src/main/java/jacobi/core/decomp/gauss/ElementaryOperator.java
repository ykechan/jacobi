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
package jacobi.core.decomp.gauss;

import jacobi.api.Matrix;

/**
 * Common interface for an Elementary row operator in Gaussian Elimination.
 * 
 * Gaussian elimination can be carried out, with partial-pivoting, by 
 * only 2 types of elementary row operation:
 * 
 * - Swapping two rows, i.e. r[i] &lt;-&gt; r[j]
 * - Add another row multiplied with a scalar, i.e. r[i] &lt;- r[i] + k * r[j]
 * 
 * @author Y.K. Chan
 */
public interface ElementaryOperator {
   
    /**
     * Swapping two rows, i.e. r[i] &lt;-&gt; r[j]
     * @param i  row index
     * @param j  row index
     */
    public void swapRows(int i, int j);
    
    /**
     * Add another row multiplied with a scalar, i.e. r[i] &lt;- r[i] + a * r[j]
     * @param i  row index i
     * @param a  scalar value a
     * @param j  row index j
     */
    public void rowOp(int i, double a, int j);
    
    /**
     * Get the matrix the operator is working on.
     * @return  Operand matrix
     */
    public Matrix getMatrix();
    
}
