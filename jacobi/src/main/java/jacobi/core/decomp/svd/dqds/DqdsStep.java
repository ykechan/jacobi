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
package jacobi.core.decomp.svd.dqds;

import java.util.OptionalDouble;

/**
 * Iteration step of differential quotient different with shifts (dqds).
 * 
 * Given a upper bi-diagonal matrix B where diag(B) = {a1, a2, ...}, supDiag(B) = {b1, b2, ...}.
 * Consider the LU iteration, i.e. Find U^t*U = B*B^t where U is also upper bi-diagonal.
 * 
 * Let diag(U) = {p1, p2, ...}, supDiag(U) = {q1, q2, ...}
 * 
 * diag(U^t*U) = {p1^2, p2^2 + q1^2, ...}
 * supDiag(U^t*U) = {p1*q1, p2*q2, ...}
 * 
 * diag(B*B^t) = {a1^2 + b1^2, a2^2 + b2^2, ..., aN^2}
 * supDiag(B*B^t) = {a2*b1, a3*b2, ...}
 * 
 * By comparing elements, let q[0] = 0
 * 
 * p[i]^2 = a[i]^2 + b[i]^2 - q[i-1]^2
 * q[i] = a[i + 1]*b[i] / p[i]
 * 
 * Consider d[i] = a[i]^2 - q[i-1]^2
 *               = a[i]^2 - a[i]^2*b[i-1]^2/p[i-1]^2
 *               = a[i]^2 * { p[i-1]^2 - b[i-1]^2 }/p[i-1]^2
 *               = a[i]^2 * { a[i-1]^2 + b[i-1]^2 }/p[i-1]^2
 *               = d[i-1] * { a[i]^2 / p[i-1]^2 }
 * 
 * Therefore p[i]^2 = d[i] + b[i]^2, q[i] = (a[i + 1]/p[i]) * b[i]
 * 
 * Since most operations are involved with squares, this interface accepts {a1^2, a2^2, ...} and {b1^2, b2^2, ...}
 * instead of B.
 * 
 * @author Y.K. Chan
 */
public interface DqdsStep { 
    
    /**
     * Compute an iteration of dqds.
     * @param zElem Elements {a1^2, b1^2, p1^2, q1^2, a2^2, b2^2, p2^2, q2^2, ...}
     * @param begin  Begin index of elements of interest
     * @param end  End index of elements of interest
     * @param shift  Shift value
     * @param forward  True if compute {a^2[i], b^2[i]} to {p^2[i], q^2[i]}, false the other way around
     * @return  A splitting index and shifted value, or the shifted value and next shift
     */
    public Result compute(double[] zElem, int begin, int end, double shift, boolean forward);
    
    /**
     * Data object for Dqds computation result
     */
    public static class Result {
        
        /**
         * Index for splitting
         */
        public final int split;
        
        /**
         * Shift value used, or empty if failed
         */
        public final OptionalDouble shifted;
        
        /**
         * Next shift value suggested
         */
        public final double guess;
        
        /**
         * Construct result with splitting point.
         * @param split  Index for splitting
         * @param shifted  Shift value used
         */
        public Result(int split, double shifted) {
            this(split, shifted, 0.0);
        }
        
        /**
         * Construct result with shift value used and next shift suggestion.
         * @param shift  Shift value used
         * @param guess  Next shift suggestion
         */
        public Result(double shift, double guess) {
            this(-1, shift, guess);
        }
        
        /**
         * Construct result with failed iteration but with next shift suggestion.
         * @param guess  Next shift suggestion
         */
        public Result(double guess) {
            this.split = -1;
            this.shifted = OptionalDouble.empty();
            this.guess = guess;
        }

        /**
         * Constructor.
         * @param split  Index for splitting
         * @param shifted  Shift value used
         * @param guess  Next shift suggestion
         */
        protected Result(int split, double shifted, double guess) {
            this.split = split;
            this.shifted = OptionalDouble.of(shifted);
            this.guess = guess;
        }
        
    }
    
}
