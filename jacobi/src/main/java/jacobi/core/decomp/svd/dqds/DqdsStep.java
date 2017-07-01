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

import jacobi.core.util.Real;

/**
 * Iteration step of differential quotient different with shifts (dqds).
 * 
 * <p>Given a upper bi-diagonal matrix B where diag(B) = {a1, a2, ...}, supDiag(B) = {b1, b2, ...}.
 * Consider the LU iteration, i.e.&nbsp;Find U^t*U = B*B^t where U is also upper bi-diagonal.</p>
 * 
 * <p>Let diag(U) = {p1, p2, ...}, supDiag(U) = {q1, q2, ...}</p>
 * 
 * <p>
 * diag(U^t*U) = {p1^2, p2^2 + q1^2, ...}<br>
 * supDiag(U^t*U) = {p1*q1, p2*q2, ...}
 * </p>
 * 
 * <p>
 * diag(B*B^t) = {a1^2 + b1^2, a2^2 + b2^2, ..., aN^2}<br>
 * supDiag(B*B^t) = {a2*b1, a3*b2, ...}
 * </p>
 * 
 * <p>Comparing elements, let q[0] = 0</p>
 * 
 * <p>p[i]^2 = a[i]^2 + b[i]^2 - q[i-1]^2
 * q[i] = a[i + 1]*b[i] / p[i]</p>
 * 
 * <pre>
 * Consider d[i] = a[i]^2 - q[i-1]^2
 *               = a[i]^2 - a[i]^2*b[i-1]^2/p[i-1]^2
 *               = a[i]^2 * { p[i-1]^2 - b[i-1]^2 }/p[i-1]^2
 *               = a[i]^2 * { a[i-1]^2 + b[i-1]^2 }/p[i-1]^2
 *               = d[i-1] * { a[i]^2 / p[i-1]^2 }
 * </pre>
 * 
 * <p>Therefore p[i]^2 = d[i] + b[i]^2, q[i] = (a[i + 1]/p[i]) * b[i]</p>
 * 
 * <p>Since most operations are involved with squares, this interface accepts {a1^2, a2^2, ...} and {b1^2, b2^2, ...}
 * instead of B.</p>
 * 
 * @author Y.K. Chan
 */
public interface DqdsStep { 
    
    /**
     * Compute an iteration of dqds.
     * @param zElem Elements {a1^2, b1^2, p1^2, q1^2, a2^2, b2^2, p2^2, q2^2, ...}
     * @param begin  Begin index of elements of interest
     * @param end  End index of elements of interest     
     * @param forward  True if compute {a^2[i], b^2[i]} to {p^2[i], q^2[i]}, false the other way around
     * @param prev  Result of previous iteration
     * @return  A splitting index and shifted value, or the shifted value and next shift
     */
    public State compute(double[] zElem, int begin, int end, boolean forward, State prev);
    
    /**
     * Data object for Dqds computation result
     */
    public static final class State { 
        
        /**
         * Get state instance for no useful information available.
         * @return  State instance
         */
        public static State empty() {
            return EMPTY;
        }
        
        /**
         * Create a state instance for a failed iteration.
         * @param guess  Next shift value suggestion
         * @return  State instance
         */
        public static State failed(double guess) {
            return new State(-1, Double.NaN, guess, 1.0, 1.0, 1.0);
        }
        
        /**
         * Index for splitting
         */
        public final int split;
        
        /**
         * Shift value used, or NaN if failed
         */
        public final double shifted;
        
        /**
         * Next shift value suggested
         */
        public final double guess;
        
        /**
         * Maximum diagonal element and minimum sup-diagonal element found
         */
        public final double maxElem, minElem, minErr;

        /**
         * Constructor.
         * @param split  Index for splitting
         * @param shifted  Shift value used, or empty if failed
         * @param guess  Next shift value suggested
         * @param maxElem  Maximum diagonal element
         * @param minElem  Minimum diagonal element
         * @param minErr  Minimum sup-diagonal element found
         */
        protected State(int split, double shifted, double guess, double maxElem, double minElem, double minErr) {
            this.split = split;
            this.shifted = shifted;
            this.guess = guess;
            this.maxElem = maxElem;
            this.minElem = minElem;
            this.minErr = minErr;
        }
        
        /**
         * Check if the previous iteration has failed.
         * @return  True if failed, false otherwise
         */
        public boolean isFailed() {
            return Double.isNaN(this.shifted);
        }
        
        private static final State EMPTY = new State(-1, 0.0, 0.0, 1.0, 0.0, Real.EPSILON);
    }
    
}
