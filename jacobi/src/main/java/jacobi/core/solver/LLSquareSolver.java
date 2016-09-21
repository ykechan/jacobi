/*
 * Copyright (C) 2016 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package jacobi.core.solver;

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.QRDecomp;
import jacobi.core.solver.Substitution.Mode;
import jacobi.core.util.Throw;
import java.util.Optional;

/**
 * Solve a over-determined system of linear equations in a linear-least
 * square sense.
 * 
 * A system of linear equations y = A * x is over-determined if A has more 
 * rows then columns, and rows are not mostly degenerated. A linear-least
 * square estimator of x is that such estimator x' minimizes || y - A * x' ||.
 * 
 * Derivation omitted, x' can be obtained by solving the system
 *   (A^t * A)x' = A^t * y
 * 
 * A^t * A is positive definite and square and the system can be solved by
 * Gaussian Eliminated or Cholesky decomposition. However to compute this matrix
 * involves slow large matrix multiplication, and poor numerical stability since
 * the condition number squared with the multiplication. Instead, it can also
 * be solved by decomposition A = Q * R where Q is orthogonal and R upper triangular,
 * and solve R * x = Q^t * y, since
 * 
 * A^t * A = R^t * Q^t * Q * R = R^t * R
 * A^t * y = R^t * Q^t * y
 * 
 * Therefore R^t * R * x' = R^t * Q^t * y 
 * Since R * x' = Q^t * y -&gt; (A^t * A)x' = A^t * y, result follows.
 * 
 * SVD can be be used to solve this, but SVD is an iterative algorithm and
 * much more complicated to implement than what is necessary here.
 * 
 * This class is perturbative, i.e. it destroys the value of the first parameter,
 * matrix A. Value of matrix y however, is not disturbed.
 * 
 * @author Y.K. Chan
 */
public class LLSquareSolver {
        
    /**
     * Solve the system of linear equation y = A * x.
     * @param a  Matrix A
     * @param y  Matrix y
     * @return   Solution x or empty if A is not over-determined
     */
    public Optional<Matrix> solve(Matrix a, Matrix y) {
        Throw.when()
            .isNull(() -> a, () -> "No system of linear equations. (A in y = A * x) ")
            .isNull(() -> y, () -> "No known values. (y in y = A * x)")
            .isTrue(() -> a.getRowCount() == 0, () -> "No equations.")
            .isTrue(
                () -> a.getRowCount() < a.getColCount(), 
                () -> "Matrix A is not over-determined. (" 
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
        new QRDecomp().compute(a, y);
        return Optional.ofNullable(new Substitution(Mode.BACKWARD, a).compute(y));
    }

}
