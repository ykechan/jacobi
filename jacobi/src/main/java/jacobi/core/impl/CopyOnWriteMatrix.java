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

package jacobi.core.impl;

import jacobi.api.Matrix;
import jacobi.core.facade.FacadeProxy;

/**
 *
 * @author Y.K. Chan
 */
public final class CopyOnWriteMatrix implements Matrix {
    
    public static CopyOnWriteMatrix of(Matrix matrix) {
        return matrix instanceof CopyOnWriteMatrix
                ? (CopyOnWriteMatrix) matrix
                : new CopyOnWriteMatrix(matrix);
    }

    protected CopyOnWriteMatrix(Matrix matrix) {
        this.materialized = false;
        this.matrix = matrix;
    }

    @Override
    public int getRowCount() {
        return this.matrix.getRowCount();
    }

    @Override
    public int getColCount() {
        return this.matrix.getColCount();
    }

    @Override
    public double[] getRow(int index) {
        return this.matrix.getRow(index);
    }

    @Override
    public double get(int i, int j) {
        return this.matrix.get(i, j);
    }

    @Override
    public Matrix setRow(int index, double[] values) {
        this.materialize().setRow(index, values);
        return this;
    }

    @Override
    public Matrix swapRow(int i, int j) {
        this.materialize().swapRow(i, j);
        return this;
    }

    @Override
    public <T> T ext(Class<T> clazz) {
        return FacadeProxy.of(clazz, this.matrix, this);
    }

    @Override
    public Matrix copy() {
        return this.materialized
                ? this.matrix.copy()
                : new CopyOnWriteMatrix(this.matrix.copy());
    }
    
    private Matrix materialize() {
        if(!this.materialized){
            this.matrix = new DefaultMatrix(this.matrix);
            this.materialized = true;
        }
        return this.matrix;
    }

    private boolean materialized;
    private Matrix matrix;
}
