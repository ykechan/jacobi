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
