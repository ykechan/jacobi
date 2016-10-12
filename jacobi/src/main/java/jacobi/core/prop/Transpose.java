/*
 * Copyright (C) 2016 Y.K. Chan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
 * The transpose matrix A^t of matrix A can be obtained by reflect A over its diagonal.
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
            int n = this.fetch(matrix, temp, i);
            
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
     * @param temp  Temp buffer
     * @param from  Index to start fetching rows
     * @return  Number of rows fetched
     */
    protected int fetch(Matrix matrix, double[][] temp, int from) {
        int n = Math.min(matrix.getRowCount() - from, temp.length);
        for(int i = 0; i < n; i++){
            temp[i] = matrix.getRow(from + i);
        }
        return n;
    }
    
    private static final int FETCH_SIZE = 8;
}
