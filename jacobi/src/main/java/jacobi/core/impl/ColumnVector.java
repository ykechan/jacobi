/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
import jacobi.api.annotations.Delegate;
import jacobi.api.annotations.Immutate;
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
    public ColumnVector(double... vector) {
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
    @Immutate
    @Delegate(facade = Prop.class, method = "transpose")
    public Matrix transpose() {
        //return new DefaultMatrix(new double[][]{Arrays.copyOf(vector, )});
        return new DefaultMatrix(new double[][]{
            Arrays.copyOf(this.vector, this.vector.length)
        });
    }
    
    private double[] vector;

}
