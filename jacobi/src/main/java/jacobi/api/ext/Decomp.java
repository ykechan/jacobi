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
package jacobi.api.ext;

import jacobi.api.Matrix;
import jacobi.api.annotations.Facade;
import jacobi.api.annotations.Implementation;
import jacobi.api.annotations.Immutate;
import jacobi.core.decomp.chol.CholeskyDecomp;
import jacobi.core.decomp.gauss.GaussianDecomp;
import jacobi.core.decomp.qr.HessenbergDecomp;
import jacobi.core.decomp.qr.QRDecomp;
import jacobi.core.decomp.qr.SchurDecomp;
import jacobi.core.util.Pair;
import jacobi.core.util.Triplet;
import java.util.Optional;
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
    @Immutate
    @Implementation(CholeskyDecomp.class)
    public Optional<Decomp> chol();
    
    /**
     * Compute Cholesky decomposition, i.e. A = L * L^t where L is lower triangular
     * @return  A pair of matrices &lt;L, L^t&gt;
     * @throws  UnsupportedOperationException  if matrix is not positive-definite.
     */
    @Immutate
    @Implementation(CholeskyDecomp.class)
    public Optional<Pair> chol2();
    
    /**
     * Compute QR decomposition, i.e. A = Q * R where Q is orthogonal 
     * and R is upper triangular.
     * @return 
     */
    @Immutate
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
    @Immutate
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
    @Immutate
    @Implementation(HessenbergDecomp.class)
    public Matrix hess();
    
    /**
     * Compute Hessenberg Decomposition of parameter matrix A into Q * H * Q^t,
     * where Q is orthogonal, and H is upper Hessenberg, and gets &lt;Q, H&gt; only.
     * @return   A pair of matrices &lt;Q, H&gt;
     */
    @Immutate
    @Implementation(HessenbergDecomp.class)
    public Pair hessQH();        
    
    /**
     * Compute Hessenberg Decomposition of parameter matrix A into Q * H * Q^t,
     * where Q is orthogonal, and H is upper Hessenberg, and gets &lt;Q, H, Q^t&gt;.
     * @return   A triplet of matrices &lt;Q, H, Q^t&gt;
     */
    @Immutate
    @Implementation(HessenbergDecomp.class)
    public Triplet hessQHQt();
    
    /**
     * Compute the Schur form S of the underlying matrix A, s.t. S = Q * A * Q^t for some orthogonal
     * matrix Q and almost upper triangular matrix S. Almost upper triangular here refers to a matrix
     * that contains all zeros below the sub-diagonal.
     * @return  Schur form S of the underlying matrix A, or empty if computation failed.
     */
    @Immutate
    @Implementation(SchurDecomp.class)
    public Matrix schur();
    
    /**
     * Compute the Schur form S of the underlying matrix A, s.t. S = Q * A * Q^t for some orthogonal
     * matrix Q and almost upper triangular matrix S. Almost upper triangular here refers to a matrix
     * that contains all zeros below the sub-diagonal.
     * @return  Pair of matrices &lt;Q, S&gt; s.t. S is schur form of A and A = Q * S * Q^t
     */
    @Immutate
    @Implementation(SchurDecomp.class)
    public Pair schurQS();
    
    /**
     * Compute the Schur form S of the underlying matrix A, s.t. S = Q * A * Q^t for some orthogonal
     * matrix Q and almost upper triangular matrix S. Almost upper triangular here refers to a matrix
     * that contains all zeros below the sub-diagonal.
     * This method may incur extra memory consumption for Q^t that may not be necessary. You may want to use
     * schurQS instead.
     * @return  Triplet of matrices &lt;Q, S, Q^t&gt; s.t. S is schur form of A and A = Q * S * Q^t
     */
    @Immutate
    @Implementation(SchurDecomp.class)
    public Triplet schurQSQt();
    
    
}
