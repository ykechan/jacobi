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

package jacobi.core.givens;

import jacobi.api.Matrix;
import jacobi.core.util.Real;
import jacobi.core.util.ParallelSupplier;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Process a list of Givens rotation on the right as a batch.
 * 
 * <p>Let {G[1], G[2], ... G[n]} be a set of Givens rotation s.t.&nbsp;G[n]*G[n-1]*...*G[1]*A = R,
 * where A is a Hessenberg matrix and R is upper triangular, this class computes 
 * B*G[1]^t*G[2]^t*...*G[n]^t for some input matrix B.</p>
 * 
 * <p>The set of Givens rotation is assumed to be irreducible, i.e.&nbsp;no superfluous operation
 * in between, done in the given order, and G[i] is for reducing the off-diagonal element
 * of row[i] by row[i - 1].</p>
 * 
 * <p>Such operations are embarrassingly parallel when dividing the task by row.</p>
 * 
 * @author Y.K. Chan
 */
public class GivensRQ {

    /**
     * Constructor.
     * @param rotList  List of Givens rotation
     */
    public GivensRQ(List<Givens> rotList) {
        this(0.0, rotList);
    }

    /**
     * Constructor.
     * @param shift  Shift value incorporated in the QR decomposition
     * @param rotList  List of Givens rotation
     */
    public GivensRQ(double shift, List<Givens> rotList) {
        this.shift = shift;
        this.rotList = rotList;
    }
    
    /**
     * Compute the product R * Q.
     * @param matrix Input matrix R
     * @param begin  Begin of rows of interest
     * @param end  End of rows of interest
     * @param mode  Mode of rotation
     * @return  The index of row that has a negligible off-diagonal entry, or negative if none are zero.
     */
    public int compute(Matrix matrix, int begin, int end, GivensMode mode) {
        int flopCount = (end - begin) * this.rotList.size(); 
        return flopCount > DEFAULT_LIMIT 
                ? this.rotateInParallel(matrix, begin, end, mode) 
                : this.rotateInSerial(matrix, begin, end, mode);
    }
    
    /**
     * Compute the product R * Q in serial.
     * @param matrix Input matrix R
     * @param begin  Begin of rows of interest
     * @param end  End of rows of interest
     * @param mode  Mode of rotation
     * @return  The index of row that has a negligible off-diagonal entry, or negative if none are zero.
     */
    protected int rotateInSerial(Matrix matrix, int begin, int end, GivensMode mode) {
        int deflated = -1;
        int beginRow = mode == GivensMode.DEFLATE ? begin : 0;
        int endRow = mode == GivensMode.FULL ? matrix.getRowCount() : end;
        for(int i = beginRow; i < endRow; i++){
            deflated = Math.max(deflated, this.rotateByRow(matrix, i, begin, mode));
        }
        return deflated;
    }
    
    /**
     * Compute the product R * Q in parallel.
     * @param matrix Input matrix R
     * @param begin  Begin of rows of interest
     * @param end  End of rows of interest
     * @param mode  Mode of rotation
     * @return  The index of row that has a negligible off-diagonal entry, or negative if none are zero.
     */
    protected int rotateInParallel(Matrix matrix, int begin, int end, GivensMode mode) {
        int beginRow = mode == GivensMode.DEFLATE ? begin : 0;
        int endRow = mode == GivensMode.FULL ? matrix.getRowCount() : end;
        int[] deflated = new int[endRow - beginRow];        
        ParallelSupplier.cyclic((i) -> deflated[i - beginRow] = this.rotateByRow(matrix, i, begin, mode), beginRow, endRow);
        return Arrays.stream(deflated).filter((i) -> i >= 0).findAny().orElse(-1);
    }
    
    /**
     * Compute a row of the product R * Q and update R.
     * @param matrix  Input matrix R
     * @param at  Row index
     * @param begin  Begin of rows of interest
     * @param mode  Mode of rotation
     * @return  Value of at if off-diagonal entry is negligible, negative otherwise.
     */
    protected int rotateByRow(Matrix matrix, int at, int begin, GivensMode mode) {
        int skip = mode == GivensMode.FULL || at < begin + 1 ? 0 : at - begin - 1;
        int beginCol = mode == GivensMode.FULL || at <= begin ? begin : at - 1;
        double[] row = matrix.getRow(at);
        this.rotList.get(skip).applyRight(row, beginCol);
        double off = row[beginCol];
        if(mode != GivensMode.FULL && at == begin){
            row[beginCol] += this.shift;
        }else if(mode != GivensMode.FULL && at > begin){
            row[beginCol + 1] += this.shift;
        }
        for(int i = skip + 1; i < this.rotList.size(); i++){ 
            this.rotList.get(i).applyRight(row, beginCol + i - skip);
        }
        matrix.setRow(at, row);
        return Real.isNegl(off) ? at : -1;
    }    
    
    private double shift;
    private List<Givens> rotList;
    
    private static final int DEFAULT_LIMIT = 128 * 128;
}
