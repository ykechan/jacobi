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
import jacobi.core.util.Throw;

/**
 * Find the trace of an input matrix A.
 * 
 * <p>The trace of a matrix A tr(A) is the sum of its diagonal elements.</p>
 * 
 * @author Y.K. Chan
 */
public class Trace {
    
    /**
     * Find the trace of an input matrix A.
     * @param matrix  Input matrix A
     * @return  tr(A)
     * @throws  IllegalArgumentException if A is null or A is not a square matrix.
     */
    public double compute(Matrix matrix) {
        Throw.when()
            .isNull(() -> matrix, () -> "No matrix to compute.")
            .isFalse(
                () -> matrix.getRowCount() == matrix.getColCount(), 
                () -> "Trace not exists for non-square matrices");
        double tr = 0.0;
        for(int i = 0; i < matrix.getRowCount(); i++){
            tr += matrix.get(i, i);
        }
        return tr;
    }
    
}
