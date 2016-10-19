/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
package jacobi.core.decomp.qr.step;

import jacobi.api.Matrix;
import java.util.List;

/**
 * An iteration of Pure QR algorithm.
 * 
 * Pure QR algorithm refers to the following: 
 * Consider the QR decomposition of a matrix A = Q * R, compute ~A = R * Q
 * If ~A is diagonal some upper triangular matrix U, therefore
 * R * Q = U -&gt; Q * U * Q^t = Q * R * Q * Q^t -&gt Q * U * Q^t = Q * R
 * Therefore A = Q * U * Q^t, which is the Schur decomposition of A.
 * 
 * The proof of convergence is omitted here.
 * 
 * @author Y.K. Chan
 */
public class PureQR implements QRStep {
    
    public PureQR() {
        this.givensQR = new GivensQR();
    }

    @Override
    public void compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        int endCol = fullUpper ? matrix.getColCount() : endRow;
        List<GivensQR.Givens> givens = this.givensQR.computeQR(matrix, beginRow, endRow, endCol);
        this.givensQR.computeRQ(matrix, givens, fullUpper ? 0 : beginRow, endRow, beginRow);
        if(partner != null){
            // TODO: compute partner matrix
            // ...
        }
    }

    private GivensQR givensQR;
}
