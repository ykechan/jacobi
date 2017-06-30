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
package jacobi.core.decomp.svd;

import jacobi.api.Matrix;

/**
 * Common interface for an iteration in SVD.
 * 
 * @author Y.K. Chan
 */
public interface SvdStep { 
    
    /**
     * Compute the iteration.
     * @param diag  Diagonal elements
     * @param supDiag  Super-diagonal elements
     * @param begin  Begin index of elements of interest
     * @param end  End index of elements of interest
     * @param uMat Matrix U that accepts actions applied on the left
     * @param vMat Matrix V that accepts actions applied on the right
     * @return Index of deflated sup-diagonal element, -1 otherwise
     */
    public int compute(double[] diag, double[] supDiag, int begin, int end, Matrix uMat, Matrix vMat);
    
    /**
     * Epsilon value for SVD.
     */
    public static final double EPSILON = 1e-16;
}
