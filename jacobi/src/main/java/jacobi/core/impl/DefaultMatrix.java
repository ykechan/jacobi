/*
 * Copyright (C) 2015 Y.K. Chan
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
package jacobi.core.impl;

import jacobi.api.Matrix;
import jacobi.core.facade.FacadeProxy;
import jacobi.core.util.Throw;
import java.util.Arrays;

/**
 *
 * Implementation of a dense matrix by a 2-D array.
 * 
 * @author Y.K. Chan
 */
public class DefaultMatrix implements Matrix {
    
    public DefaultMatrix(int m, int n) {
        this.rows = new double[m][n];
        this.numCols = n;
    }

    public DefaultMatrix(double[][] rows) { // NOPMD - intended
        this.rows = rows;
        this.numCols = (rows == null || rows.length == 0)? 0 : rows[0].length;
    }
    
    public DefaultMatrix(Matrix matrix) {
        this.numCols = matrix.getColCount();
        this.rows = new double[matrix.getRowCount()][];
        for(int i = 0; i < matrix.getRowCount(); i++){
            this.rows[i] = Arrays.copyOf(matrix.getRow(i), numCols);
        }
    }

    @Override
    public int getRowCount() {
        return this.rows.length;
    }

    @Override
    public int getColCount() {
        return this.numCols;
    }

    @Override
    public double[] getRow(int index) {
        return this.rows[index];
    }    

    @Override
    public Matrix setRow(int index, double[] values) {
        if(values == this.rows[index]){
            return this;
        }
        Throw.when()
           .isNull(() -> values, () -> "Unable to set a row to null.")
           .isFalse(
                () -> values.length == this.numCols,
                () -> "Unable to assign " 
                   + values.length 
                   + " values to a row of a "
                   + this.getRowCount() + "x" + this.getColCount()
                   + " matrix."
           );
        System.arraycopy(values, 0, this.rows[index], 0, this.numCols);
        return this;
    }
    
    @Override
    public Matrix set(int i, int j, double value) {
        this.rows[i][j] = value;
        return this;
    }

    @Override
    public Matrix swapRow(int i, int j) {
        if(i != j){
            double[] temp = this.rows[i];
            this.rows[i] = this.rows[j];
            this.rows[j] = temp;
        }
        return this;
    }

    @Override
    public <T> T ext(Class<T> clazz) {
        return FacadeProxy.of(clazz, this);
    }

    @Override
    public Matrix copy() {
        return new DefaultMatrix(this.rows);
    }
    
    private int numCols;
    private double[][] rows;
    
    private static final double[][] EMPTY = {};
}
