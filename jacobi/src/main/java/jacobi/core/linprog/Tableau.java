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

import jacobi.api.Matrix;

/**
 * Tableau structure for linear programming problem used in Simplex Algorithm.
 * 
 * <p>The Linear Programming problem is as follows:<br>
 * Maximize c^t * x s.t.&nbsp;A*x &lt;= b.</p>
 * 
 * <p>The feasibility inequality constraint A*x &lt;= b can be expressed as a system of linear equation
 * [A I]*[x s] = b where s &gt;= 0.</p>
 * 
 * <p>
 * Thus the problem can be expressed as 
 * <pre>
 * [ c^t  0 ][ x ]   [ z ]
 * [        ][   ] = [   ]
 * [  A   I ][ s ]   [ b ]
 * </pre>
 * </p>
 * 
 * <p>If b &gt;= 0, the trivial solution [0 b] is feasible. However if some b[k] &lt; 0, [0 b] is not feasible.
 * In this case, an auxiliary scalar variable is added s.t.&nbsp;[A I -1]*[x s t] = b, thus [0 s |min(b)|] is feasible. 
 * In such cases, the auxiliary problem becomes: <br>
 * <br>
 * min t -&gt; max -t s.t.&nbsp;[A I t]*[x s t] = b.
 * </p>
 * 
 * <p>
 * The full problem can be expressed as 
 * <pre>
 *             [ 0 ]
 * [  A  I -1 ][ x ]    [ z ]
 * [ c^t 0 -1 ][ s ] =  [ b ]
 * [  0  0 -1 ][ t ]    [ 0 ]
 * </pre>
 * </p>
 * 
 * <p>If t can not be optimized to 0, the problem is infeasible.</p>
 * 
 * @author Y.K. Chan
 */
public interface Tableau { 
    
    /**
     * Get the immutable matrix [A b].
     * @return  [A b]
     */
    public Matrix getMatrix();
    
    /**
     * Get coefficient of the objective function -c^t.
     * @return  Elements of -c^t
     */
    public double[] getCoeff();
    
    /**
     * Get mapping from column index of the full tableau to variable index.
     * @return  Array of variable indices
     */
    public int[] getVars();
    
}
