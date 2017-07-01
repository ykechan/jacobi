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
import jacobi.api.annotations.Immutate;
import jacobi.core.util.Throw;
import java.util.Optional;

/**
 * Implementation class for Linear Programming problems.
 * 
 * <p>The Linear Programming (LP) problem is standardize as follows:</p>
 * 
 * <p>Given column vector c, b, matrix a, find x &gt;= 0 s.t.&nbsp;c^t * x attains maximum and A * x &lt;= b.</p>
 * 
 * <p>
 * For minimization problems, user can simply negate c. 
 * For greater-than conditions, user can simply negate the equation. b here is allowed to be negative.
 * </p>
 * 
 * @author Y.K. Chan
 */
@Immutate
public class LinearProg {

    /**
     * Constructor.
     */
    public LinearProg() {        
        this.simplexAlgo = new StandardSimplex(LIMIT, new DantzigsRule());
    }
    
    /**
     * Compute the LP max c^t * x s.t.&nbsp;A*x &lt;= b.
     * @param c  Coefficient to the linear objective function
     * @param a  Constraint matrix
     * @param b  Constraint boundary
     * @return  A solution to the LP problem, or empty if the constraint is unbounded/infeasible.
     */
    public Optional<Matrix> compute(Matrix c, Matrix a, Matrix b) {
        Throw.when().isNull(() -> c, () -> "No objective function.")
                .isNull(() -> a, () -> "No constraint matrix. Use an empty matrix for no constraint.")
                .isNull(() -> b, () -> "No constraint boundary. Use an empty matrix for no boundary.");
        if(a.getRowCount() == 0 || b.getRowCount() == 0){
            // all LP problems are unbounded/infeasible if no constraint
            return Optional.empty(); 
        }
        return this.simplexAlgo.compute(c, a, b);
    }

    private StandardSimplex simplexAlgo;
    
    private static final long LIMIT = 65536L;
}
