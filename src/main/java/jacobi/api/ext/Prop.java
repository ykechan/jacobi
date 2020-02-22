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
import jacobi.api.annotations.Pure;
import jacobi.core.decomp.chol.CholeskyDecomp;
import jacobi.core.decomp.eigen.EigenFinder;
import jacobi.core.prop.Determinant;
import jacobi.core.prop.Inverse;
import jacobi.core.prop.Rank;
import jacobi.core.prop.Trace;
import jacobi.core.prop.Transpose;
import jacobi.core.util.Pair;
import java.util.Optional;

/**
 * Extension for getting a property of a matrix, e.g. determinant and trace.
 * 
 * This extension is immutating, i.e.&nbsp;it preserves the value of the matrices
 * it operates upon.
 * 
 * @author Y.K. Chan
 */
@Facade
@Pure
public interface Prop {
    
    /**
     * Trace of a matrix, i.e.&nbsp;tr(A)
     * @return  Trace value
     * @throws  IllegalArgumentException if underlying matrix is not a square matrix
     */
    @Implementation(Trace.class)
    public double tr();
    
    /**
     * Rank of a matrix.
     * @return  Matrix rank
     */
    @Implementation(Rank.class)
    public int rank();
    
    /**
     * Determinant of a matrix, i.e.&nbsp;det(A)
     * @return  Determinant value
     * @throws  IllegalArgumentException if underlying matrix is not a square matrix
     */
    @Implementation(Determinant.class)
    public double det();
    
    /**
     * Inverse of a matrix, i.e.&nbsp;B = A^-1 s.t.&nbsp;A * B = I
     * @return  Matrix inverse, or empty if matrix is not invertible.
     * @throws  UnsupportedOperationException if underlying matrix is not a square matrix
     */
    @Implementation(Inverse.class)
    public Optional<Matrix> inv();
    
    /**
     * Transpose of a matrix, i.e.&nbsp;exchanging rows and columns.
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
     * @return  A pair of column vectors A and B s.t.&nbsp;each row of A + Bi is a eigenvalue of the underlying matrix
     * @throws  IllegalArgumentException if underlying matrix is not a square matrix
     */
    @Implementation(EigenFinder.class)
    public Pair eig();
}
