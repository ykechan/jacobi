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
package jacobi.core.decomp.qr.step.shifts;

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.QRStep;
import jacobi.core.givens.Givens;
import java.util.ArrayList;
import java.util.List;

/**
 * Functionality class for performing bulge-chasing in Francis QR double step.
 * 
 * This class accepts the intermediate produce during Francis QR double step,
 * the a bulge is created in an otherwise Hessenberg matrix. This matrix is
 * almost Hessenberg except first 4 rows, e.g.
 * 
 *      x x x x x x
 *      x x x x x x
 * A =  x x x x x x
 *      x x x x x x
 *      0 0 0 x x x
 *      0 0 0 0 x x
 * 
 * Two givens rotation matrix G1 and G2 and be found to reduce the first
 * column of A' = G2*G1*A to Hessenberg, however B = A'*G1^t*G2^t has the bulge pushed,
 * i.e.
 * 
 *      x x x x x x
 *      x x x x x x
 * B =  0 x x x x x
 *      0 x x x x x
 *      0 x 0 x x x
 *      0 0 0 0 x x
 * 
 * Repeating the procedure would reduce the final product to Hessenberg form.
 * 
 * @author Y.K. Chan
 */
public class BulgeChaser implements QRStep {
    
    @Override
    public int compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {        
        int endCol = fullUpper ? matrix.getColCount() : endRow;
        Batch batch = this.pushBulgeToBottom(matrix, beginRow, endRow, endCol, () -> {});
        
        this.batchRotate(matrix, beginRow, endRow, batch, fullUpper ? Mode.UPPER : Mode.DEFLATE); 
        if(partner != null){
            this.batchRotate(partner, beginRow, endRow, batch, Mode.FULL);
        }
        
        return this.getLowestDeflatedIndex(batch, beginRow, endRow);
    } 
    
    /**
     * Push the bulge to the bottom of the sub-matrix of the input matrix A.
     * @param matrix  Input matrix A
     * @param beginRow  Begin row index of rows of interest
     * @param endRow  End row index of rows of interest
     * @param endCol  End column index of columns of interest
     * @param listener  Invoked exactly before the bulge is down by 1 row
     * @return Batch of Givens rotation done.
     */
    public Batch pushBulgeToBottom(Matrix matrix, int beginRow, int endRow, int endCol, Runnable listener) {
        int begin = beginRow;
        int end = endRow - 3;
        
        List<GivensPair> rotList = new ArrayList<>();
        for(int i = begin; i < end; i++){
            GivensPair pair = this.pushBulge(matrix, endRow, i, endCol, listener);
            rotList.add(pair);
        } 
        
        double[] upper = matrix.getRow(endRow - 2);
        double[] lower = matrix.getRow(endRow - 1);
        Givens last = Givens.of(upper[endRow - 3], lower[endRow - 3]).applyLeft(upper, lower, endRow - 3, endCol);
        matrix.setRow(endRow - 2, upper).setRow(endRow - 1, lower);
        
        return new Batch(rotList, last, lower[endRow - 2]);
    }
    
    /**
     * Push the 4x4 bulge one row lower. This method computes only partially
     * for applying on the right s.t. the next bulge and be determined. The rest
     * are left to be batch processed.
     * @param matrix  Input matrix
     * @param endRow  End of rows of interest
     * @param col  Column index of the bulge
     * @param endCol  End of columns of interest     
     * @param listener  Invoked exactly before the bulge is down by 1 row
     * @return A pair of Givens rotation applied
     */
    public GivensPair pushBulge(Matrix matrix, int endRow, int col, int endCol, Runnable listener) {
        double[] r0 = matrix.getRow(col + 1);
        double[] r1 = matrix.getRow(col + 2);
        double[] r2 = matrix.getRow(col + 3); 
        
        GivensPair giv = GivensPair.of(r0[col], r1[col], r2[col]); 
        r0[col] = giv.getAnchor();
        r1[col] = 0.0;
        r2[col] = 0.0;
        giv.applyLeft(r0, r1, r2, col + 1, endCol).applyRight(r1, col + 1).applyRight(r2, col + 1);
        
        matrix.setRow(col + 1, r0).setRow(col + 2, r1).setRow(col + 3, r2);
        
        listener.run();
        if(col + 4 < endRow){ 
          matrix.getAndSet(col + 4, (r) -> giv.applyRight(r, col + 1));
        }
        return giv;
    }
    
    /**
     * Apply Givens rotation on the right as a batch.
     * @param matrix  Input matrix
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @param batch  Batch of Givens rotation done
     * @param mode  Mode for batch rotation
     */
    protected void batchRotate(Matrix matrix, int beginRow, int endRow, Batch batch, Mode mode) {        
        int begin = mode == Mode.FULL || mode == Mode.UPPER ? 0 : beginRow;
        for(int i = begin; i < endRow; i++){
            double[] row = matrix.getRow(i);
            this.batchRotate(row, beginRow, i, batch.rotList, mode);
            batch.last.applyRight(row, endRow - 2);
            matrix.setRow(i, row);
        }
    }
    
    /**
     * Get the lowest deflated index from a batch of Givens rotation. 
     * @param batch  Batch of Givens rotation
     * @param beginRow  Start of index of rows of interest
     * @param endRow
     * @return  Lowest deflated index
     */
    protected int getLowestDeflatedIndex(Batch batch, int beginRow, int endRow) {
        for(int i = batch.rotList.size() - 1; i >= 0; i--){
            if(Math.abs(batch.rotList.get(i).getAnchor()) < QRStep.EPSILON){
                return beginRow + i + 1;
            }
        }
        if(Math.abs(batch.last.getMag()) < QRStep.EPSILON){
            return endRow - 2;
        }
        return Math.abs(batch.bottom) < QRStep.EPSILON ? endRow - 1 : -1;
    }
    
    /**
     * Apply Givens rotation on the right as a batch to row vector.
     * @param row  Input row vector
     * @param beginRow  Begin index of row of interest
     * @param index  Row index of the input row vector
     * @param rotList   List of Givens rotation
     * @param mode  Mode for batch rotation
     */
    protected void batchRotate(double[] row, int beginRow, int index, List<GivensPair> rotList, Mode mode) {
        int skip = index <= beginRow || mode == Mode.FULL ? 0 : index - beginRow - 1;
        int begin = beginRow + skip + 1;
        for(int i = skip; i < rotList.size(); i++){
            rotList.get(i).applyRight(row, begin + (i - skip));
        }
    }     
    
    /**
     * Mode for bulge chasing. 
     */
    protected enum Mode {
        FULL, UPPER, DEFLATE;        
    }
}
