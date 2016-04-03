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
import jacobi.core.decomp.gauss.GaussianDecomp;
import jacobi.core.decomp.qr.Hessenberg;
import jacobi.core.decomp.qr.QRDecomp;
import jacobi.core.util.Pair;
import jacobi.core.util.Triplet;
import java.util.function.Supplier;

/**
 * Extension for matrix decompositions.
 * 
 * This extension provide perturbative version and non-perturbative version
 * of each decomposition. Non-perturbative methods accepts no argument and
 * returns a Pair / Triplet of matrices. 
 * 
 * Non-Perturbative methods accepts a single matrix argument to 
 * apply multiplication of partner matrix on the fly. For example, in QR
 * decomposition with argument A, the method would transform the matrix into
 * R, and A to Q * A.
 * 
 * Definition of main result and partner matrix maybe different for each 
 * decomposition. For example, in QR decomposition R is the main result and
 * Q is the partner matrix.
 * 
 * @author Y.K. Chan
 */
@Facade
public interface Decomp extends Supplier<Matrix> { 
    
    /**
     * Compute lower triangular L from Cholesky decomposition, i.e. A = L * L^t
     * where L is lower triangular.
     * @return  Lower triangular matrix L
     * @throws  UnsupportedOperationException  if matrix is not positive-definite.
     */
    @NonPerturbative
    @Implementation(CholeskyDecomp.class)
    public Decomp chol();
    
    /**
     * Compute Cholesky decomposition, i.e. A = L * L^t where L is lower triangular
     * @return  A pair of matrices &lt;L, L^t&gt;
     * @throws  UnsupportedOperationException  if matrix is not positive-definite.
     */
    @NonPerturbative
    @Implementation(CholeskyDecomp.class)
    public Pair chol2();
    
    /**
     * Compute QR decomposition, i.e. A = Q * R where Q is orthogonal 
     * and R is upper triangular.
     * @return 
     */
    @NonPerturbative
    @Implementation(QRDecomp.class)
    public Pair qr();
    
    /**
     * Compute QR decomposition by transforming parameter matrix A, and
     * with a partner matrix B. This method computes A = Q * R by
     * transforming A to R, with a partner matrix B, which will be transformed
     * to Q^t * B.
     * @param partner  Partner matrix B
     * @return  Instance of matrix A now transformed to matrix R
     */
    @Implementation(QRDecomp.class)
    public Matrix qr(Matrix partner);
    
    /**
     * Compute Gaussian Decomposition, a.k.a. PLU Decomposition, i.e.
     * A = P * L * U, where P is a permutation matrix, L is lower triangular
     * matrix that is a product of elementary matrices, U is upper triangular.
     * @return  A triplet of matrices &lt;P, L, U&gt;
     */
    @NonPerturbative
    @Implementation(GaussianDecomp.class)
    public Triplet gauss();
    
    /**
     * Compute Gaussian Decomposition by transforming parameter matrix A, and
     * with a partner matrix B. This method computes A = P * L * U, 
     * where P is a permutation matrix, L is lower triangular matrix that is a 
     * product of elementary matrices, U is upper triangular.
     * @param partner  Partner matrix B
     * @return  A triplet of matrices &lt;P, L, U&gt;
     */
    @Implementation(GaussianDecomp.class)
    public Matrix gauss(Matrix partner);
    
    /**
     * Compute Hessenberg Decomposition of parameter matrix A into Q * H * Q^t,
     * where Q is orthogonal, and H is upper Hessenberg, and gets H only.
     * @return   Upper Hessenberg matrix H
     */
    @NonPerturbative
    @Implementation(Hessenberg.class)
    public Matrix hess();
    
    /**
     * Compute Hessenberg Decomposition of parameter matrix A into Q * H * Q^t,
     * where Q is orthogonal, and H is upper Hessenberg, and gets &lt;Q, H&gt; only.
     * @return   A pair of matrices &lt;Q, H&gt;
     */
    @NonPerturbative
    @Implementation(Hessenberg.class)
    public Pair hessQH();        
    
    /**
     * Compute Hessenberg Decomposition of parameter matrix A into Q * H * Q^t,
     * where Q is orthogonal, and H is upper Hessenberg, and gets &lt;Q, H, Q^t&gt;.
     * @return   A triplet of matrices &lt;Q, H, Q^t&gt;
     */
    @Implementation(Hessenberg.class)
    public Triplet hessQHQt();
}
