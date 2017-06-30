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
package jacobi.core.prop;

import jacobi.api.Matrices;
import jacobi.api.Matrix;
import jacobi.api.annotations.Immutate;
import jacobi.core.impl.ColumnVector;
import jacobi.core.util.Throw;

/**
 * Create the transpose of the input matrix.
 * 
 * <p>The transpose matrix A^t of matrix A can be obtained by reflect A over its diagonal.</p>
 * 
 * @author Y.K. Chan
 */
@Immutate
public class Transpose {
    
    /**
     * Create the transpose of the input matrix.
     * @param matrix  Input matrix A
     * @return  Transpose matrix A^T
     */
    public Matrix compute(Matrix matrix) {
        Throw.when().isNull(() -> matrix, () -> "No matrix to transpose.");
        if(matrix.getRowCount() == 1){
            return new ColumnVector(matrix.getRow(0));
        }        
        Matrix trans = Matrices.zeros(matrix.getColCount(), matrix.getRowCount());
        double[][] temp = new double[FETCH_SIZE][];
        for(int i = 0; i < matrix.getRowCount(); i += temp.length){
            int n = this.fetch(matrix, i, temp);
            
            for(int j = 0; j < trans.getRowCount(); j++){
                double[] row = trans.getRow(j);
                for(int k = 0; k < n; k++){
                    row[i + k] = temp[k][j];
                }
                trans.setRow(j, row);
            }
        }
        return trans;
    }
    
    /**
     * Fetch rows from input matrix into temp buffer.
     * @param matrix  Input matrix A
     * @param from  Index to start fetching rows
     * @param temp  Temp buffer
     * @return  Number of rows fetched
     */
    protected int fetch(Matrix matrix, int from, double[][] temp) {
        int n = Math.min(matrix.getRowCount() - from, temp.length);
        for(int i = 0; i < n; i++){
            temp[i] = matrix.getRow(from + i);
        }
        return n;
    }
    
    private static final int FETCH_SIZE = 8;
}
