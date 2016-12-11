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

package jacobi.core.decomp.qr.step.shifts;

import jacobi.api.Matrix;
import jacobi.core.util.Threads;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Y.K. Chan
 */
public class BatchGivens {

    public BatchGivens(int limit) {
        this.limit = limit;
    }
    
    public void rotate(List<Batch> batch, Matrix matrix, int beginRow, int endRow, Mode mode) {
        
    }
    
    public void rotate(Batch batch, Matrix matrix, int beginRow, int endRow, Mode mode) {
        if(endRow - beginRow < limit){
            this.serial(batch, matrix, beginRow, endRow, mode);
        }else{
            this.parallel(batch, matrix, beginRow, endRow, mode);
        }
    }
    
    protected void serial(Batch batch, Matrix matrix, int beginRow, int endRow, Mode mode) {
        int begin = mode == Mode.DEFLATE ? beginRow : 0;
        int end = mode == Mode.FULL ? matrix.getRowCount() : endRow - 1;
        for(int i = begin; i < end; i++){
            double[] row = matrix.getRow(i);
            this.rotate(batch, row, i, beginRow, endRow, mode);
            matrix.setRow(i, row);
        }
    }
    
    protected void parallel(Batch batch, Matrix matrix, int beginRow, int endRow, Mode mode) {
        int numWorkers = 4 * Runtime.getRuntime().availableProcessors();
        int begin = mode == Mode.DEFLATE ? beginRow : 0;
        int end = mode == Mode.FULL ? matrix.getRowCount() : endRow - 1;
        AtomicInteger index = new AtomicInteger(begin);        
        Threads.invoke(() -> {
            int k;
            while((k = index.getAndIncrement()) < end){
                double[] row = matrix.getRow(k);
                this.rotate(batch, row, k, beginRow, endRow, mode);
                matrix.setRow(k, row);
            }
            return null;
        }, numWorkers);
    }
    
    /**
     * Apply Givens rotation on the right as a batch to row vector.
     * @param batch  Batch Givens rotations
     * @param row  Input row vector
     * @param at  Row index of input row vector
     * @param beginRow  Begin index of row of interest
     * @param endRow  End index of row of interest
     * @param mode  Mode for batch rotation
     */
    protected void rotate(Batch batch, double[] row, int at, int beginRow, int endRow, Mode mode) {
        if(at < beginRow || mode == Mode.FULL){
            batch.implicitG.applyRight(row, beginRow);
        }
        int skip = at < beginRow || mode == Mode.FULL ? 0 : at - beginRow;
        int begin = beginRow + skip + 1;
        for(int i = skip; i < batch.rotList.size(); i++){
            batch.rotList.get(i).applyRight(row, begin + (i - skip));
        }
        batch.last.applyRight(row, endRow - 2);
    } 
    
    private int limit;        

    public enum Mode {
        FULL, UPPER, DEFLATE
    }
}
