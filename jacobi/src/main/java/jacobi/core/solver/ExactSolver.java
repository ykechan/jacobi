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

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.gauss.FullMatrixOperator;
import jacobi.core.decomp.gauss.GenericGaussianElim;
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
        this.gaussElim = new GenericGaussianElim();
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
        Matrix x = Matrices.copy(y);
        this.gaussElim.compute(a, (op) -> new FullMatrixOperator(op, x));
        return Optional.ofNullable(new Substitution(Mode.BACKWARD, a).compute(x));
    }

    private GenericGaussianElim gaussElim;
}
