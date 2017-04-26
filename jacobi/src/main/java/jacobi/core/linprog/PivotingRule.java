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

package jacobi.core.linprog;

import java.util.function.BiFunction;

/**
 * Common interface for pivoting rules of the Simplex algorithm.
 * 
 * The Linear Programming problem is as follows:
 * Maximize c^t * x s.t. A*x &lt;= b, x &gt;= 0, for some matrix A, and column vector b and c.
 * 
 * For any k where c[k] &gt; 0, it can be chosen as the entering variable. However the choice
 * makes significant difference in the finishing rate. A number of different criteria exist. This
 * is the interface for finding a set of pivots from an immutable tableau.
 * 
 * Implementations of this interface should choose a set of distinct entering variables under a
 * given limit of quantity. Implementations can return lesser number of choice, but not more.
 * 
 * Returning a empty array in case no such enter variable exists.
 * 
 * @author Y.K. Chan
 */
public interface PivotingRule extends BiFunction<Tableau, Integer, int[]> {
    
    /**
     * Empty array for no enter variable can be chosen.
     */
    public static final int[] NONE = new int[0];

}
