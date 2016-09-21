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
package jacobi.api.ext;

import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.annotations.NonPerturbative;
import jacobi.core.solver.ExactSolver;
import jacobi.core.solver.LLSquareSolver;
import java.util.Optional;

/**
 * Solving a system of linear equations, either exactly or in linear-least
 * square sense.
 * 
 * @author Y.K. Chan
 */
@NonPerturbative
@Facade
public interface Solver {
    
    /**
     * Solve a determined system of linear equations y = A * x. y and x though
     * written in lower case are not necessarily vectors and can be matrices.
     * Matrix A is the facade parameter.
     * @param y  Matrix y.
     * @return  Solution x
     */
    @Implementation(ExactSolver.class)
    public Optional<Matrix> exact(Matrix y);
    
    /**
     * Solve a over-determined system of linear equations y = A * x. y and x though
     * written in lower case are not necessarily vectors and can be matrices.
     * Matrix A is the facade parameter.
     * Determined system is also accepted.
     * @param y  Matrix y.
     * @return  Solution x
     */
    @Implementation(LLSquareSolver.class)
    public Optional<Matrix> llsquare(Matrix y);
    
}
