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
 * A matrix decorator that copies the value of the underlying matrix
 * whenever written upon.
 * 
 * Computational result can take some special form, for example a diagonal 
 * matrix. It maybe advantageous to return an special immutable implementation 
 * that does not fully realize all matrix elements. 
 * 
 * However user may further change the value of the return results. This class
 * is for such cases, it would then materializes all elements to a vanilla
 * DefaultMatrix. All operations would be transparent with respect to the user.
 * 
 * @author Y.K. Chan
 */
public final class CopyOnWriteMatrix implements Matrix {
    
    /**
     * Decorate a matrix to a Copy-on-write matrix
     * @param matrix  Base matrix
     * @return  A copy-on-write matrix
     */
    public static CopyOnWriteMatrix of(Matrix matrix) {
        return matrix instanceof CopyOnWriteMatrix
                ? (CopyOnWriteMatrix) matrix
                : new CopyOnWriteMatrix( ImmutableMatrix.of(matrix) );
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
        return CopyOnWriteMatrix.of(this.matrix);
    }
    
    private Matrix materialize() {
        if(!this.materialized){
            synchronized(this) {
                if(!this.materialized){
                    this.matrix = new DefaultMatrix(this.matrix);
                    this.materialized = true;
                }
            }
        }
        return this.matrix;
    }
    
    private Matrix matrix;
    private volatile boolean materialized;
}
