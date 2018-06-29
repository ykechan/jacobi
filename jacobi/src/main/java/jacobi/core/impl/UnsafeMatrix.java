/*
 * The MIT License
 *
 * Copyright (c) 2018 Y.K. Chan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jacobi.core.impl;

import jacobi.api.Matrix;

import java.util.function.Function;

/**
 * @author Y.K. Chan
 */
public class UnsafeMatrix implements Matrix {

    public UnsafeMatrix(double[][] rows) {
        this(rows, cls -> {
            throw new UnsupportedOperationException("");
        });
    }

    public UnsafeMatrix(double[][] rows, Function<Class, ?> facadeFactory) {
        this.numCols = rows == null ? 0 : rows[0].length;
        this.rows = rows;
        this.facadeFactory = facadeFactory;
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
        double[] row = this.rows[index];
        if(row != values) {
            System.arraycopy(values, 0, row, 0, row.length);
        }
        return this;
    }

    @Override
    public Matrix swapRow(int i, int j) {
        if(i != j){
            double[] tmp = this.rows[i];
            this.rows[i] = this.rows[j];
            this.rows[j] = tmp;
        }
        return this;
    }

    @Override
    public <T> T ext(Class<T> clazz) {
        return (T) this.facadeFactory.apply(clazz);
    }

    @Override
    public Matrix copy() {
        return new UnsafeMatrix(this.toArray(), this.facadeFactory);
    }

    public double[][] getRows() {
        return rows;
    }

    private int numCols;
    private double[][] rows;
    private Function<Class, ?> facadeFactory;
}
