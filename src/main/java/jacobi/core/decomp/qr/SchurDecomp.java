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
package jacobi.core.decomp.qr;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.QRStep;
import jacobi.core.decomp.qr.step.QRSteps;
import jacobi.core.impl.CopyOnWriteMatrix;
import jacobi.core.prop.Transpose;
import jacobi.core.util.Pair;
import jacobi.core.util.Throw;
import jacobi.core.util.Triplet;
import java.util.Optional;

/**
 * Compute the Schur Decomposition.
 * 
 * <p>This implementation reduces the input matrix to Hessenberg form, and collects all QR strategies to further reduce
 * the Hessenberg matrix into Schur form.</p>
 * 
 * <p>Current implementation uses the QR algorithm.</p>
 * 
 * @author Y.K. Chan
 */
public class SchurDecomp implements QRStrategy {

    /**
     * Constructor.
     */
    public SchurDecomp() {
        this(QRSteps.STD);
    }
    
    /**
     * Constructor with injected QR step. 
     * @param step  QR step
     */
    public SchurDecomp(QRStep step) {
        this.impl = Optional.of(new BasicQR(step))
                .map((q) -> new SymmTriDiagQR(q))
                .get();
        this.hess = new HessenbergDecomp();
    }
    
    /**
     * Compute Schur decomposition A = Q * S * Q^t for only S.
     * @param matrix  Input matrix A
     * @return  Schur form of A
     */
    public Matrix compute(Matrix matrix) {
        return this.compute(matrix, null, true);
    }
    
    /**
     * Compute Schur decomposition A = Q * S * Q^t for S and Q.
     * @param matrix  Input matrix A
     * @return  &lt;Q, S&gt; where S is the schur form of A, and A = Q * S * Q^t
     */
    public Pair computeBoth(Matrix matrix) {
        Throw.when()
            .isNull(() -> matrix, () -> "No matrix to compute.")
            .isFalse(() -> matrix.getRowCount() == matrix.getColCount(), () -> "Unable to compute schur form of non-square matrix.");
        Matrix q = Matrices.identity(matrix.getRowCount());
        return Pair.of(q, this.compute(matrix, q, true));
    }
    
    /**
     * Compute Schur decomposition A = Q * S * Q^t.
     * @param matrix  Input matrix A
     * @return  &lt;Q, S, Q^t&gt;
     */
    public Triplet computeAll(Matrix matrix) {
        Pair pair = this.computeBoth(matrix);
        return Triplet.of(
            CopyOnWriteMatrix.of(pair.getLeft()), 
            pair.getRight(), 
            () -> new Transpose().compute(pair.getLeft())
        );
    }

    @Override
    public Matrix compute(Matrix matrix, Matrix partner, boolean fullUpper) {        
        this.hess.compute(matrix, partner == null ? (hh) -> {} : (hh) ->  hh.applyRight(partner));
        return this.impl.compute(matrix, partner, fullUpper);
    }    

    private HessenbergDecomp hess;
    private QRStrategy impl;
}
