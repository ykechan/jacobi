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
import jacobi.core.util.IntArray;

/**
 * Tableau structure for linear programming problem used in Simplex Algorithm.
 * 
 * The Linear Programming problem is as follows:
 * Maximize c^t * x s.t. A*x &lt;= b.
 * 
 * The feasibility inequality constraint A*x &lt;= b can be expressed as a system of linear equation
 * [A|I]*[x|s] = b where s &gt;= 0.
 * 
 * Thus the problem can be expressed as 
 * [ 1 -c^t  0 ][ x ]   [ z ]
 * [           ][   ] = [   ]
 * [ 0    A  I ][ s ]   [ b ]
 * 
 * This class does not impose the requirement that b &gt;= 0. To cater such situation, instead of [A|I],
 * an [A*|J] is used where J = {sgn(b[k])*e^k}, with standard basis {e^k}, and A*^k = sgn(b[k])*A[k].
 * The signs of b[k] is stored and in the tableau the absolute value of b is used instead. Thus it becomes
 * [ 1 -c^t  0 ][ x ]   [ z ]
 * [           ][   ] = [   ]
 * [ 0   A*  J ][ s ]   [|b|]
 * 
 * The tableau can only be changed by swapping the basis through pivoting operation. After swapping a basis,
 * the column of the enter variable will be one of the standard basis, and the leaving variable change from 
 * the same standard basis, which can be conceptually swapped s.t. J can be maintained. Such swapping is only
 * possible when the pivot value p is chosen s.t. sgn(p) = sgn(b[k]), thus the elimination drive the column
 * in A*^k into sgn(b[k])*e^k.
 * 
 * Thus not all entries are necessary. This implementation keeps the following form:
 * [ 0 -c^t ]
 * [|b|  A* ]
 * 
 * @author Y.K. Chan
 */
public interface Tableau {
    
    /**
     * Get the immutable matrix [0 -c^t; |b| A*].
     * @return  Tableau entries as an immutable matrix
     */
    public Matrix getMatrix();
    
    /**
     * Get the diagonal of the matrix J.
     * @return  diag(J)
     */
    public IntArray getSigns();
    
    /**
     * Get the variable index of each row in the full vector [x s].
     * @return  Index of variables of each column.
     */
    public IntArray getVars();

}
