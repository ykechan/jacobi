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
import jacobi.core.util.Throw;
import jacobi.core.util.Triplet;

/**
 * Hessenberg decomposition is decompose a matrix A into Q * U * Q^t, where
 * Q is orthogonal and U is almost upper triangular. Almost triangular here refers to
 * almost upper triangular, in that all elements under sub-diagonal are zero. 
 * Sub-diagonal is not necessarily zeroes though.
 * 
 * Hessenberg decomposition may not have much interest invested in itself, but
 * it is the first step of optimization of QR algorithm.
 * 
 * Hessenberg decomposition can be easily computed using Householder reflections.
 * 
 * @author Y.K. Chan
 */
public class Hessenberg {
    
    public Matrix compute(Matrix a) {        
        this.compute(a, null);
        return a;
    }
    
    public Triplet compute3(Matrix matrix) {
        // ...
        return null;
    }
    
    protected void compute(Matrix matrix, Matrix partner) {
        this.validate(matrix);
        if(matrix.getRowCount() < 3){
            return;
        }
        int n = matrix.getColCount() - 2;
        double[] columnBuffer = new double[matrix.getRowCount()];
        for(int i = 0; i < n; i++){
            this.eliminate(matrix, partner, i, columnBuffer);
        }
    }
    
    protected void eliminate(Matrix matrix, Matrix partner, int i, double[] column) {
        this.getColumn(matrix, i + 1, i, column);
        HouseholderReflector hh = new HouseholderReflector(column, i + 1);
        double norm = hh.normalize();
        if(Math.abs(norm) < EPSILON){
            return;
        }
        matrix.set(i + 1, i, norm);
        this.applyLeft(matrix, partner, i, hh);
        this.applyRight(matrix, i, hh);
    }
    
    protected void applyLeft(Matrix matrix, Matrix partner, int i, HouseholderReflector hh) {
        hh.applyLeft(matrix, i + 1);
        if(partner != null){
            hh.applyLeft(partner);
        }
    }
    
    protected void applyRight(Matrix matrix, int i, HouseholderReflector hh) {
        hh.applyRight(matrix);
    }

    /**
     * Get the Householder reflector of a column.
     * @param matrix  Matrix A
     * @param fromRow  Start row index of reflection
     * @param colIndex  Column index of column to be reflected
     * @param column  Column reflector buffer
     */
    protected void getColumn(Matrix matrix, int fromRow, int colIndex, double[] column) {
        for(int i = fromRow; i < matrix.getRowCount(); i++){
            column[i] = matrix.get(i, colIndex);
        }
    }
    
    private void validate(Matrix matrix) {
        Throw.when()
            .isNull(() -> matrix, () -> "No matrix to decompose.")
            .isTrue(
                () -> matrix.getRowCount() == 0, 
                () -> "Unable to decompose an empty matrix."
            )
            .isTrue(
                () -> matrix.getRowCount() != matrix.getColCount(),
                () -> "Hessenburg decomposition not exists for non-square "
                    + matrix.getRowCount() + "x" + matrix.getColCount()
                    + " matrix.");
    }
    
    private static final double EPSILON = 1e-10;
}
