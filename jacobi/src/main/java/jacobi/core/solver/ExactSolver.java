/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
package jacobi.core.solver;

import jacobi.api.Matrix;
import jacobi.core.decomp.gauss.GaussianDecomp;
import jacobi.core.solver.Substitution.Mode;
import jacobi.core.util.Throw;
import java.util.Optional;

/**
 * Solve a determined system of linear equation y = A * x. Although y and x 
 * are written in lower case for familiarity, they are not necessarily vectors
 * and can be matrices. 
 * 
 * This class is for solving the exact solution of a full-rank square matrix A.
 * 
 * If A is not square, or not full ranked, UnsupportedOperationException will
 * be thrown.
 * 
 * This class uses Gaussian Eliminated to obtain the solution.
 * 
 * This class is perturbative, i.e. it destroys the value of the first parameter,
 * matrix A. Value of matrix y however, is not disturbed.
 * 
 * @author Y.K. Chan
 */
public class ExactSolver {

    public ExactSolver() {
        this.gaussDecomp = new GaussianDecomp();
    }
        
    /**
     * Solve the system of linear equation y = A * x.
     * @param a  Matrix A
     * @param y  Matrix y
     * @return   Solution x, or empty if A is not full rank
     */
    public Optional<Matrix> solve(Matrix a, Matrix y) {
        Throw.when()
            .isNull(() -> a, () -> "No system of linear equations. (A in y = A * x) ")
            .isNull(() -> y, () -> "No known values. (y in y = A * x)")
            .isTrue(
                () -> a.getRowCount() != a.getColCount(), 
                () -> "Matrix A is not a square matrix. (" 
                        + a.getRowCount() + "x" + a.getColCount() 
                        + ")"
            )
            .isTrue(
                () -> a.getRowCount() != y.getRowCount(), 
                () -> "Dimension mismatch. Encounters " 
                        + a.getRowCount() 
                        + " equations and " 
                        + y.getRowCount() + " known values."
            );
        if(a.getRowCount() == 0){
            return Optional.of(y);
        }
        Matrix x = y.copy();
        this.gaussDecomp.compute(a, x);
        return this.backwardSubs(a, x);
    }
    
    /**
     * Compute backward substitution on Ax = y, where A is upper triangular.
     * @param a  Input matrix A in Ax = y
     * @param y  Input matrix y in Ax = y
     * @return  Instance of y, transformed to x
     */
    protected Optional<Matrix> backwardSubs(Matrix a, Matrix y) {
        return Optional.ofNullable(new Substitution(Mode.BACKWARD, a).compute(y));
    }

    private GaussianDecomp gaussDecomp;
}
