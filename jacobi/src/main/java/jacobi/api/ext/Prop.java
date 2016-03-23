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
import jacobi.core.decomp.chol.CholeskyDecomp;
import jacobi.core.prop.Determinant;
import jacobi.core.prop.Inverse;
import jacobi.core.prop.Rank;
import jacobi.core.prop.Transpose;

/**
 * Extension for getting a property of a matrix, e.g. determinant and trace.
 * 
 * This extension is non-perturbative, i.e. it preserves the value of the matrices
 * it operates upon.
 * 
 * @author Y.K. Chan
 */
@Facade
@NonPerturbative
public interface Prop {
    
    /**
     * Trace of a matrix, i.e. tr(A)
     * @return  Trace value
     * @throws  UnsupportedOperationException if underlying matrix is not a square matrix
     */
    public double tr();
    
    /**
     * Rank of a matrix.
     * @return 
     */
    @Implementation(Rank.class)
    public int rank();
    
    /**
     * Determinant of a matrix, i.e. det(A)
     * @return  Determinant value
     * @throws  UnsupportedOperationException if underlying matrix is not a square matrix
     */
    @Implementation(Determinant.class)
    public double det();
    
    /**
     * Inverse of a matrix, i.e. B = A^-1 s.t. A * B = I
     * @return  Matrix inverse.
     * @throws  UnsupportedOperationException if underlying matrix is not a square matrix
     */
    @Implementation(Inverse.class)
    public Matrix inv();
    
    /**
     * Transpose of a matrix, i.e. exchanging rows and columns.
     * @return   Matrix transpose
     */
    @Implementation(Transpose.class)
    public Matrix transpose();
    
    /**
     * Inspect if matrix is positive-definite.
     * @return  True if positive-definite, false otherwise
     */
    @Implementation(CholeskyDecomp.class)
    public boolean isPositiveDefinite();
}
