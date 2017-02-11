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
package jacobi.core.solver;

import jacobi.api.Matrices;
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
public class LLSquaresSolver {

    public LLSquaresSolver() {
        this.qrDecomp = new QRDecomp();
    }
        
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
        if(a.getRowCount() == 0){
            return Optional.of(y);
        }
        Matrix x = y.copy();
        this.qrDecomp.compute(a, x);
        x = new Substitution(Mode.BACKWARD, a).compute(x);
        if(x == null){
            return Optional.empty();
        } 
        return Optional.of(this.trim(x, a.getColCount()));
    }
    
    /**
     * For a m-by-n matrix which m > n, trim the matrix to a k-by-k matrix
     * by discarding other rows.
     * @param matrix  Input m-by-n matrix
     * @param k  Number of rows to keep
     * @return   A n-by-n matrix with top n rows of input matrix
     */
    protected Matrix trim(Matrix matrix, int k) { 
        Matrix output = Matrices.zeros(k, matrix.getColCount());
        for(int i = 0; i < k; i++){
            output.setRow(i, matrix.getRow(i));
        }
        return output;
    }

    private QRDecomp qrDecomp;
}
