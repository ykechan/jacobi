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
import java.util.List;

/**
 * Implementation to perform delayed Givens rotation as a batch.
 * 
 * In bulge chasing algorithms, it is more efficient to leave out the Givens rotation on the right, i.e.
 * computing A*G for each chasing step, and compute those as a batch after the bulge was chased. The reason of 
 * doing Givens rotations by batch being more efficient is it turns a column-based access to a row-based access
 * and cache locality can be taken too the fullest, plus each operations for each rows are embarrassingly parallel.
 * 
 * @author Y.K. Chan
 */
public class GivensBatchRQ {        

    public GivensBatchRQ(GivensPair implicitG, List<GivensPair> rotList, Givens last) {
        this.implicitG = implicitG;
        this.rotList = rotList;
        this.last = last;
    }

    public GivensBatchRQ(GivensBatch batch) {
        this(batch.implicitG, batch.rotList, batch.last);
    }
    
    /**
     * Perform delayed Givens rotation as a batch.
     * @param matrix  Input matrix A
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @param mode  Indicate the scope of rotation
     * @return  The value of the bottom off-diagonal value
     */
    public double compute(Matrix matrix, int beginRow, int endRow, GivensMode mode) {
        return this.serial(matrix, beginRow, endRow, mode);
    }
    
    /**
     * Perform delayed Givens rotation as a batch.
     * @param matrix  Input matrix A
     * @param beginRow  Begin index of rows of interest
     * @param endRow  End index of rows of interest
     * @param mode  Indicate the scope of rotation
     * @return  The value of the bottom off-diagonal value
     */
    protected double serial(Matrix matrix, int beginRow, int endRow, GivensMode mode) {
        int begin = mode == GivensMode.DEFLATE ? beginRow : 0;
        int end = mode == GivensMode.FULL ? matrix.getRowCount() : endRow;
        double off = 0.0;
        for(int i = begin; i < end; i++){
            double[] row = matrix.getRow(i);
            this.rotate(row, i, beginRow, endRow, mode);
            off = row[endRow - 2];
            matrix.setRow(i, row);            
        }
        return off;
    }
    
    /**
     * Apply Givens rotation on the right as a batch to row vector.
     * @param row  Input row vector
     * @param at  Row index of input row vector
     * @param beginRow  Begin index of row of interest
     * @param endRow  End index of row of interest
     * @param mode  Mode for batch rotation
     */
    protected void rotate(double[] row, int at, int beginRow, int endRow, GivensMode mode) {
        if(at < beginRow || mode == GivensMode.FULL){
            implicitG.applyRight(row, beginRow);
        }
        int skip = at < beginRow || mode == GivensMode.FULL ? 0 : at - beginRow;
        int begin = beginRow + skip + 1;
        for(int i = skip; i < rotList.size(); i++){
            rotList.get(i).applyRight(row, begin + (i - skip));
        }
        last.applyRight(row, endRow - 2);
    } 
    
    private GivensPair implicitG;
    private List<GivensPair> rotList;
    private Givens last;
}
