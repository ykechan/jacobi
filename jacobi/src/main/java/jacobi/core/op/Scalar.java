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
package jacobi.core.op;

import jacobi.api.Matrix;
import java.util.Arrays;

/**
 *
 * @author Y.K. Chan
 */
class Scalar implements Matrix {
    
    public Scalar(int m, int n, double value) {
        this.m = m;
        this.n = n;
        this.row = new double[n];
        Arrays.fill(this.row, value);
    }

    @Override
    public int getRowCount() {
        return this.m;
    }

    @Override
    public int getColCount() {
        return this.n;
    }

    @Override
    public double[] getRow(int index) {
        return this.row; // NOPMD - Controlled usage
    }

    @Override
    public Matrix setRow(int index, double[] values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Matrix swapRow(int i, int j) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T ext(Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Matrix copy() {
        throw new UnsupportedOperationException();
    }

    private int m, n;    
    private double[] row;
}
