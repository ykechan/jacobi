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

package jacobi.core.decomp.qr;

import jacobi.api.Matrix;
import jacobi.core.impl.DefaultMatrix;
import jacobi.core.util.Pair;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Implementation class for QR Decomposition.
 * 
 * QR decomposition refers to consider A = Q*R where Q is orthogonal and R is 
 * upper triangular.
 * 
 * This class is perturbative, that is it transform the input parameters.
 * 
 * @author Y.K. Chan
 */
public class QRDecomp {
    
    /**
     * Compute QR Decomposition of a matrix A = Q * R.
     * @param matrix  Matrix A to be transformed to R
     * @return  A pair of matrix &lt;Q, R&gt;
     */
    public Pair computeQR(Matrix matrix) {
        double[] column = new double[matrix.getRowCount()];
        double norm = this.getColumn(matrix, 0, 0, column);
        HouseholderReflector refl = new HouseholderReflector(column, 0);
        refl.applyLeft(matrix);
        matrix.set(0, 0, norm);
        Matrix q = new DefaultMatrix(refl.toArray());
        if(matrix.getColCount() == 1){            
            return Pair.of(q, matrix);
        }        
        this.compute(matrix, q, 1);
        return Pair.of(q, matrix);
    }
    
    /**
     * Compute QR decomposition with a partner matrix, i.e. given matrix A and
     * partner matrix B, transform A to R and B to Q^t * R, where A = Q * R, 
     * Q is orthogonal and R is lower trianguar.
     * @param matrix  Matrix A
     * @param partner  Partner matrix B
     */
    public void compute(Matrix matrix, Matrix partner) {
        this.compute(matrix, partner, 0);
    }
    
    protected void compute(Matrix matrix, Matrix partner, int from) {
        double[] column = new double[matrix.getRowCount()];
        int n = Math.min(matrix.getRowCount(), matrix.getColCount()) - 1;
        for(int j = from; j < n; j++){
            this.eliminate(matrix, partner, j, column);
        }
        for(int i = 0; i < matrix.getRowCount(); i++){
            double[] row = matrix.getRow(i);
            Arrays.fill(row, 0, Math.min(i, row.length), 0.0);
            matrix.setRow(i, row);
        }
        return;
    }
    
    protected void eliminate(Matrix matrix, Matrix partner, int j, double[] column) {
        double norm = this.getColumn(matrix, j, j, column);
        HouseholderReflector refl = new HouseholderReflector(column, j);
        refl.applyLeft(matrix, j + 1);            
        matrix.set(j, j, norm);
        if(partner != null){
            refl.applyLeft(partner);
        }
    }
    
    protected double getColumn(Matrix matrix, int fromRow, int colIndex, double[] column) {
        for(int i = fromRow; i < matrix.getRowCount(); i++){
            column[i] = matrix.get(i, colIndex);
        }
        return this.normalize(column, fromRow);
    }

    protected double normalize(double[] vector, int from) {
        double temp = 0.0;
        for(int i = from + 1; i < vector.length; i++){
            temp += vector[i] * vector[i];
        }
        double norm = Math.sqrt(temp + vector[from] * vector[from]);
        if(vector[from] < 0.0){
            vector[from] -= norm;            
        }else{
            vector[from] += norm;
            norm *= -1.0;
        }
        double newNorm = Math.sqrt(temp + vector[from] * vector[from]);
        
        for(int i = from; i < vector.length; i++){
            vector[i] /= newNorm;
        }
        return norm;
    }
}
