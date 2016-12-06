/*
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan.
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

package jacobi.core.decomp.qr.step;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.core.decomp.qr.QRStrategy;
import jacobi.core.decomp.qr.step.shifts.DoubleShift;
import java.util.Arrays;
import java.util.List;

/**
 * QR step with 2^k shifts where k &gt; 1. 
 * 
 * For small-to-medium size (say &lt; 100) matrices, Francis QR double shifts seems to serves well. However, as matrices
 * grow larger, each scan of the whole matrices tries only to converge 2 eigenvalues to the bottom, which maybe 
 * inadequate and a waste of memory caching. Instead of using only 2 shifts, n &gt; 2 shifts can be used. (N is picked
 * as a power of 2 for convenience of parallel computation).
 * 
 * Good candidates of shifts are the eigenvalues of the bottom n-by-n sub-matrix. This class finds the eigenvalues
 * of the bottom n-by-n sub-matrix, pair them in conjugate pairs (if complex), and create a chain of bulges. Then 
 * it chases the chain of bulges to the bottom and eliminate them. The chain of bulges can be created by introduce
 * a bulge with given 2 shifts, and chase them down for the next bulge to be created.
 * 
 * @author Y.K. Chan
 */
public class MultiShiftsQR implements QRStep {

    public MultiShiftsQR(QRStep base, int threshold, QRStrategy qrAlgo) {
        this.base = base;
        this.qrAlgo = qrAlgo;
        this.threshold = Math.max(threshold, 4);
    }

    @Override
    public int compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {        
        int num = this.getNumberOfShifts(beginRow, endRow);
        if(num == 0){
            return this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
        }
        return beginRow;
    }        
    
    /**
     * Find k shifts from the trailing k-by-k sub-matrix of input matrix A
     * @param matrix  Input matrix A
     * @param endRow  End of row of interest
     * @param k  Number of shifts
     * @return  List of paired double shifts
     */
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    protected List<DoubleShift> computeShifts(Matrix matrix, int endRow, int k) {
        Matrix schur = this.qrAlgo.compute(this.getTrailingMatrix(matrix, endRow, k), null, false);
        DoubleShift[] shifts = new DoubleShift[k / 2];
        double eig0 = 0.0; 
        int i = 0, n = 0, index = 0, end = k - 1;
        while(i < end){
            if(Math.abs(schur.get(i + 1, i)) < QRStep.EPSILON){
                if(++n % 2 == 0){
                    double eig1 = schur.get(i, i);
                    shifts[index++] = new DoubleShift(eig0 + eig1, eig0 * eig1);
                }else{
                    eig0 = schur.get(i, i);
                }
                i++;
            }else{
                shifts[index++] = DoubleShift.of(matrix, i);
                i += 2;
            }
        }
        if(i < schur.getRowCount()){
            double eig1 = schur.get(n, n);
            shifts[index++] = new DoubleShift(eig0 + eig1, eig0 * eig1);
        }
        return Arrays.asList(shifts);
    }    

    /**
     * Copy the k-by-k trailing matrix of a sub-matrix of input matrix A
     * @param matrix  Input matrix A
     * @param endRow  End of row of sub-matrix
     * @param k  Number of row/columns of the trailing matrix
     * @return  Copy of the trailing matrix
     */
    protected Matrix getTrailingMatrix(Matrix matrix, int endRow, int k) {
        Matrix mat = Matrices.zeros(k);
        int beginRow = endRow - k;
        for(int i = beginRow; i < endRow; i++){
            double[] row = mat.getRow(i - beginRow);
            System.arraycopy(matrix.getRow(i), beginRow, row, 0, row.length);
            mat.setRow(i, row);
        }
        return mat;
    }
    
    protected int getNumberOfShifts(int beginRow, int endRow) {
        int size = endRow - beginRow;
        if(size < this.threshold){
            return 0;
        }
        int lower = (size / 4) / 2;
        int n = 2;
        while(n < lower){
            n *= 2; 
        }
        return n;
    }
    
    private int threshold;
    private QRStep base; 
    private QRStrategy qrAlgo;     
}
