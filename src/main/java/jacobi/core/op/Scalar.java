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
package jacobi.core.op;

import jacobi.core.impl.ImmutableMatrix;
import java.util.Arrays;

/**
 * A special matrix that all elements are of the same value.
 * 
 * @author Y.K. Chan
 */
class Scalar extends ImmutableMatrix {
    
    /**
     * Constructor.
     * @param m  Number of rows
     * @param n  Number of columns
     * @param value  Element value
     */
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

    private int m, n;    
    private double[] row;
}
