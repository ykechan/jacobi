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
public final class DefaultMatrix implements Matrix {
    
    /**
     * Constructor.
     * @param m  Number of rows
     * @param n  Number of columns
     */
    public DefaultMatrix(int m, int n) {
        this.rows = new double[m][n];
        this.numCols = n;
    }

    /**
     * Constructor using a given backing array of rows.
     * @param rows  Array of rows
     */
    public DefaultMatrix(double[][] rows) { // NOPMD - intended
        this.rows = rows;
        this.numCols = rows == null || rows.length == 0 ? 0 : rows[0].length;
    }
    
    /**
     * Constructor using a given matrix. 
     * @param matrix  Matrix to be copied
     */
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
        return new DefaultMatrix(this);
    }
    
    private int numCols;
    private double[][] rows;
}
