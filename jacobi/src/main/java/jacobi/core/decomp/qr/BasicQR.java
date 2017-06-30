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

import jacobi.core.decomp.qr.step.QRStep;
import jacobi.api.Matrix;
import jacobi.core.util.Divider;
import jacobi.core.util.Real;
import jacobi.core.util.Throw;

/**
 * Basic QR algorithm implementation with given iteration implementation.
 * 
 * <p>Basic QR algorithm goes as the following: </p>
 * 
 * <p>Given a Hessenberg matrix A, find f(A) with some shifting strategy
 * Find Q*R = f(A), s.t.&nbsp;Q is orthogonal and R upper triangular.<br/>
 * Compute A' = R*Q, and ~A = f^-1(A').<br/>
 * Repeat until ~A is upper triangular.
 * </p>
 * 
 * <p>When a sub-diagonal entry of A is close to zero, A can be deflated into
 * two separate matrices, and perform the iteration isolated. Depending on
 * whether whole Schur form is required or only eigenvalues, computes are
 * not necessary for columns beyond the upper left corner.</p>
 * 
 * <p>This class is to perform the iteration given the step of each iteration.</p>
 * 
 * @author Y.K. Chan
 */
public class BasicQR implements QRStrategy {

    /**
     * Constructor.
     * @param step  Implementation to perform an iteration.
     */
    public BasicQR(QRStep step) {
        this.step = step;
    }

    @Override
    public Matrix compute(Matrix matrix, Matrix partner, boolean fullUpper) {
        Throw.when()
            .isNull(() -> matrix, () -> "No matrix to compute.")
            .isTrue(
                () -> matrix.getRowCount() != matrix.getColCount(), 
                () -> "Unable to compute a non-square "
                    + matrix.getRowCount() + "x" + matrix.getColCount()
                    + " matrix.")
            .isTrue(
                () -> partner != null && matrix.getRowCount() != partner.getRowCount(), 
                () -> "Mismatch partner matrix having " + partner.getRowCount() + " rows.");
        //MapReducer.divide((begin, end) -> this.step.compute(matrix, partner, begin, end, fullUpper), 0, matrix.getRowCount());
        return Divider.repeats((begin, end) -> this.step.compute(matrix, partner, begin, end, fullUpper))
                .visit(0, matrix.getRowCount())
                .echo(matrix);
    }    
    
    /**
     * Find the index of the last non-zero entry in sub-diagonal
     * @param matrix  Input matrix
     * @param begin  Begin index of row of interest
     * @param end  End index of row of interest
     * @return   Index of last zero entry, end - 1 if none was found.
     */
    protected int deflate(Matrix matrix, int begin, int end) {
        int k = end - 1;
        while(k > begin){
            if(Real.isNegl(matrix.get(k, k - 1))){
                break;
            }
            k--;
        }
        return k + 1;
    }    

    private QRStep step;  
}
