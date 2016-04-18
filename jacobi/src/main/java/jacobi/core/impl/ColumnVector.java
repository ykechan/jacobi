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
import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.NonPerturbative;
import jacobi.api.ext.Prop;
import jacobi.core.facade.FacadeProxy;
import jacobi.core.util.Throw;
import java.util.Arrays;

/**
 * 
 * A column vector, i.e. a matrix with a single column.
 * 
 * This class uses an 1-D array internally.
 * 
 * @author Y.K. Chan
 */
public class ColumnVector implements Matrix {
    
    /**
     * Construct a column vector in n-dimension, i.e. a n-by-1 matrix
     * @param n   Number of dimension/rows
     */
    public ColumnVector(int n) {
        this.vector = new double[n];
    }

    /**
     * Construct a column vector with given values.
     * @param vector   Vector elements
     */
    public ColumnVector(double[] vector) {
        this(vector, vector == null ? 0 : vector.length);
    }
    
    /**
     * Construct a column vector with given values.
     * @param vector   Vector elements
     */
    public ColumnVector(double[] vector, int n) {
        Throw.when().isNull(() -> vector, () -> "Unable to use null array as vector.");
        this.vector = Arrays.copyOf(vector, n);
    }

    @Override
    public int getRowCount() {
        return this.vector.length;
    }

    @Override
    public int getColCount() {
        return 1;
    }

    public double[] getVector() {
        return vector; // NOPMD - intented to be mutable
    }
    
    @Override
    public double[] getRow(int index) {
        return new double[]{ this.vector[index] };
    }

    @Override
    public Matrix setRow(int index, double[] values) { // NOPMD - false positive
        this.vector[index] = values[0]; 
        return this;
    }    

    @Override
    public Matrix swapRow(int i, int j) {
        double temp = this.vector[i];
        this.vector[i] = this.vector[j];
        this.vector[j] = temp;
        return this;
    }
    
    @Override
    public <T> T ext(Class<T> clazz) {
        return FacadeProxy.of(clazz, this);
    }

    @Override
    public Matrix copy() {
        return new ColumnVector(this.vector);
    }
    
    /**
     * Construct the transpose of the column vector, i.e. a row vector.
     * @return  Row vector
     */
    @NonPerturbative
    @Delegate(facade = Prop.class, method = "transpose")
    public Matrix transpose() {
        //return new DefaultMatrix(new double[][]{Arrays.copyOf(vector, )});
        return new DefaultMatrix(new double[][]{
            Arrays.copyOf(this.vector, this.vector.length)
        });
    }        
    
    private double[] vector;

}
