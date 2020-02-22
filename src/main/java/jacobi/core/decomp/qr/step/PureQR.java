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
package jacobi.core.decomp.qr.step;

import jacobi.api.Matrix;
import jacobi.core.givens.Givens;
import jacobi.core.givens.GivensMode;
import jacobi.core.givens.GivensRQ;
import java.util.Arrays;
import java.util.List;

/**
 * An iteration of Pure QR algorithm.
 * 
 * <p>
 * Pure QR algorithm refers to the following: 
 * Consider the QR decomposition of a matrix A = Q * R, compute ~A = R * Q
 * If ~A is diagonal some upper triangular matrix U, therefore
 * R * Q = U -&gt; Q * U * Q^t = Q * R * Q * Q^t -&gt; Q * U * Q^t = Q * R
 * Therefore A = Q * U * Q^t, which is the Schur decomposition of A.
 * </p>
 * 
 * <p>The proof of convergence is omitted here.</p>
 * 
 * @author Y.K. Chan
 */
public class PureQR implements QRStep {

    @Override
    public int compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        int endCol = fullUpper ? matrix.getColCount() : endRow;
        List<Givens> givens = this.qrDecomp(matrix, beginRow, endRow, endCol);
        GivensRQ rq = new GivensRQ(givens);
        int next = rq.compute(matrix, beginRow, endRow, fullUpper ? GivensMode.UPPER : GivensMode.DEFLATE);
        if(partner != null){
            rq.compute(partner, beginRow, endRow, GivensMode.FULL);
        }
        return next;
    }
    
    /**
     * Compute QR decomposition of an Hessenberg matrix by Givens rotation.
     * @param matrix  Input matrix A
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @param endCol  End index of columns of interest
     * @return  List of Givens rotation applied.
     */
    protected List<Givens> qrDecomp(Matrix matrix, int beginRow, int endRow, int endCol) {        
        int n = endRow - 1;
        Givens[] givens = new Givens[n - beginRow];
        int k = 0;
        for(int i = beginRow; i < n; i++){
            double[] upper = matrix.getRow(i);
            double[] lower = matrix.getRow(i + 1);
            givens[k++] = Givens.of(upper[i], lower[i]).applyLeft(upper, lower, i, endCol);
            matrix.setRow(i, upper).setRow(i + 1, lower);
        }
        return Arrays.asList(givens);
    }

}
