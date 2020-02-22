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
import jacobi.core.decomp.qr.Householder;
import java.util.function.Consumer;

/**
 * Bi-Diagonal decomposition is to decompose a m-by-n matrix A into Q * E * V^t s.t.&nbsp;Q and V are othogonal, and E
 * is a bi-diagonal matrix. E can be chosen to be upper triangular and lower triangular.
 * 
 * <p>This class mutates the value of the input matrix A and the resultant value is in-determined. Only the returned
 * bi-diagonal elements should be relied upon.</p>
 * 
 * <p>The bi-diagonal elements are returned as {a1, b1, a2, b2, ...} 
 * where diag(E) = {a1, a2, ...}, sup-diag(E) = {b1, b2, ...}</p>
 * 
 * @author Y.K. Chan
 */
public interface BiDiagDecomp {
    
    /**
     * Compute bi-diagonal decomposition.
     * @param mode  Indicate E to be upper or lower triangular matrix
     * @param input  Input matrix A
     * @return Bi-diagonal elements as {a1, b1, a2, b2, ...} where diag(E) = {a1, a2, ...}, sup-diag(E) = {b1, b2, ...}
     */
    public default double[] compute(Mode mode, Matrix input) {
        return this.compute(mode, input, (hh) -> {}, (hh) -> {});
    }
    
    /**
     * Compute bi-diagonal decomposition.
     * @param mode  Indicate E to be upper or lower triangular matrix
     * @param input  Input matrix A
     * @param qFunc  Listener of Q1, Q2, ... where Q^t = Qn*...Q2*Q1
     * @param vFunc  Listener of V1, V2, ... where V^t = V1*V2*...*Vn
     * @return  Bi-diagonal elements as {a1, b1, a2, b2, ...} where diag(E) = {a1, a2, ...}, sup-diag(E) = {b1, b2, ...}
     */
    public double[] compute(Mode mode, Matrix input, Consumer<Householder> qFunc, Consumer<Householder> vFunc);
    
    /**
     * Mode of decomposition.
     */
    public enum Mode {
        /**
         * Decompose to upper triangular matrix
         */
        UPPER, 
        /**
         * Decompose to lower triangular matrix
         */
        LOWER
    }
    
}
