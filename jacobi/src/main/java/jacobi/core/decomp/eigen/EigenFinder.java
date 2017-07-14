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

package jacobi.core.decomp.eigen;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Immutate;
import jacobi.core.decomp.qr.SchurDecomp;
import jacobi.core.decomp.qr.QRStrategy;
import jacobi.core.decomp.qr.step.QRStep;
import jacobi.core.decomp.qr.step.QRSteps;
import jacobi.core.decomp.qr.step.shifts.DoubleShift;
import jacobi.core.impl.ColumnVector;
import jacobi.core.impl.DefaultMatrix;
import jacobi.core.util.Pair;
import jacobi.core.util.Throw;

/**
 * Finding eigenvalues of a matrix.
 * 
 * @author Y.K. Chan
 */
@Immutate
public class EigenFinder {

    /**
     * Constructor.
     */
    public EigenFinder() {
        this(new SchurDecomp(QRSteps.EIG));
    }

    /**
     * Constructor. 
     * @param qrImpl  Implementation of QR algorithm 
     */
    protected EigenFinder(QRStrategy qrImpl) {
        this.qrImpl = qrImpl;
    }
    
    /**
     * Compute the eigenvalue of square matrix.
     * @param matrix  Input matrix A.
     * @return  A pair of column vectors A and B s.t.&nbsp;each row of A + Bi is a eigenvalue of the underlying matrix
     * @throws  UnsupportedOperationException if unable to compute
     */
    public Pair compute(Matrix matrix) {
        Throw.when()
                .isNull(() -> matrix, () -> "No input matrix.")
                .isFalse(() -> matrix.getRowCount() == matrix.getColCount(), () -> "Input matrix is not square.");
        switch(matrix.getRowCount()){
            case 0 :                
                return Pair.EMPTY;
            case 1 :
                return Pair.of(Matrices.scalar(matrix.get(0, 0)), Matrices.zeros(1));
            case 2 :
                return DoubleShift.of(matrix, 0).eig();
            default :
                break;
        }        
        return this.findEig(this.qrImpl.compute(new DefaultMatrix(matrix), null, false));
    }
    
    /**
     * Find eigen-values of a square matrix in Schur form.
     * @param schur  Input matrix in Schur form
     * @return  A pair of column vectors A and B s.t.&nbsp;each row of A + Bi is a eigenvalue of the underlying matrix
     */
    protected Pair findEig(Matrix schur) {
        int n = schur.getRowCount() - 1;
        int k = 0;
        double[] re = new double[schur.getRowCount()];
        double[] im = new double[schur.getRowCount()];
        while(k < n){            
            if(Math.abs(schur.get(k + 1, k)) < QRStep.EPSILON){
                re[k] = schur.get(k, k); 
                k++;
            }else{
                Pair eig = DoubleShift.of(schur, k).eig();
                re[k] = eig.getLeft().get(0, 0);
                re[k + 1] = eig.getLeft().get(1, 0);
                im[k] = eig.getRight().get(0, 0);   
                im[k + 1] = eig.getRight().get(1, 0); 
                k += 2;
            }
        }
        if(k < schur.getRowCount()){
            re[k] = schur.get(k, k);
        }
        return Pair.of(new ColumnVector(re), new ColumnVector(im));
    }        

    private QRStrategy qrImpl;
}
