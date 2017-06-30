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

import jacobi.api.Matrix;

/**
 * Common strategy interface for QR algorithm.
 * 
 * <p>The basic idea of QR algorithm is referred to as follows: </p>
 * 
 * <p>Given a Hessenberg matrix H, find H = Q * R, 
 * where Q is orthogonal, and R is upper triangular.</p>
 * 
 * <p>Compute H2 = R * Q, and until H2 is upper triangular, repeat the process.</p>
 * 
 * <p>The vanilla QR algorithm, stated above, though works, takes a large number 
 * of iterations to converge. Many enhancements are available, and the
 * efficiency increases with the complexity.</p>
 * 
 * @author Y.K. Chan
 */
public interface QRStrategy {
    
    /**
     * Perform QR algorithm on a Hessenberg matrix H, with a partner matrix B.
     * Matrix H will be transformed to a upper triangular matrix, and B will be transformed
     * to Q * B, where H = Q * D * Q^t.
     * @param matrix  Hessenberg matrix H
     * @param partner  Partner matrix B
     * @param fullUpper True if whole upper triangular matrix is needed, false if only
     *          diagonal entries, which are eigenvalues, are interested.
     * @return Upper triangular matrix D
     */
    public Matrix compute(Matrix matrix, Matrix partner, boolean fullUpper);
    
}
