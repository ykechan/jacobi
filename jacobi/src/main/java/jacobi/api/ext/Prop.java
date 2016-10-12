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
import jacobi.api.annotations.Immutate;
import jacobi.core.decomp.chol.CholeskyDecomp;
import jacobi.core.decomp.eigen.EigenFinder;
import jacobi.core.prop.Determinant;
import jacobi.core.prop.Inverse;
import jacobi.core.prop.Rank;
import jacobi.core.prop.Transpose;
import jacobi.core.util.Pair;
import java.util.List;
import java.util.Optional;

/**
 * Extension for getting a property of a matrix, e.g. determinant and trace.
 * 
 * This extension is non-perturbative, i.e. it preserves the value of the matrices
 * it operates upon.
 * 
 * @author Y.K. Chan
 */
@Facade
@Immutate
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
     */
    @Implementation(Inverse.class)
    public Optional<Matrix> inv();
    
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
    
    /**
     * Eigenvalues of this matrix.
     * @return  A pair of column vectors A and B s.t. each row of A + Bi is a eigenvalue of the underlying matrix
     * @throws  UnsupportedOperationException if underlying matrix is not a square matrix
     */
    @Implementation(EigenFinder.class)
    public Pair eig();
}
