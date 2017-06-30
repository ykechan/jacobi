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
package jacobi.api.ext;

import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.annotations.Immutate;
import jacobi.core.linprog.LinearProg;
import jacobi.core.solver.ExactSolver;
import jacobi.core.solver.LLSquaresSolver;
import java.util.Optional;

/**
 * Solving a system of linear equations, either exactly or in linear-least
 * square sense.
 * 
 * @author Y.K. Chan
 */
@Immutate
@Facade
public interface Solver {
    
    /**
     * Solve a determined system of linear equations y = A * x. y and x though
     * written in lower case are not necessarily vectors and can be matrices.
     * Matrix A is the facade parameter.
     * @param y  Matrix y.
     * @return  Solution x, or empty if no solution exists.
     */
    @Implementation(ExactSolver.class)
    public Optional<Matrix> exact(Matrix y);
    
    /**
     * Solve a over-determined system of linear equations y = A * x. y and x though
     * written in lower case are not necessarily vectors and can be matrices.
     * Matrix A is the facade parameter.
     * Determined system is also accepted.
     * @param y  Matrix y.
     * @return  Solution x or empty if system is under-determined.
     */
    @Implementation(LLSquaresSolver.class)
    public Optional<Matrix> llsquare(Matrix y);
    
    /**
     * Solver a linear programming problem. The linear programming problem here is defined as: 
     * Maximize c^t*x = 0 s.t.&nbsp;A*x &lt;= b.
     * Column vector c is the facade parameter.
     * @param a  Constraint matrix A
     * @param b  Constraint b
     * @return  Solution x, or empty if un-feasible.
     */
    @Implementation(LinearProg.class)
    public Optional<Matrix> linprog(Matrix a, Matrix b);
    
}
