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
public abstract class ImmutableMatrix implements Matrix { // NOPMD
    
    public static ImmutableMatrix of(Matrix base) {
        return new ImmutableMatrix() {

            @Override
            public int getRowCount() {
                return base.getRowCount();
            }

            @Override
            public int getColCount() {
                return base.getColCount();
            }

            @Override
            public double[] getRow(int index) {
                return base.getRow(index);
            }

            @Override
            public Matrix copy() {
                return base.copy();
            }
        };
    }

    @Override
    public final Matrix setRow(int index, double[] values) {
        return this.throwUnsupported();
    }

    @Override
    public final Matrix set(int i, int j, double value){
        return this.throwUnsupported();
    }

    @Override
    public final Matrix swapRow(int i, int j) {
        return this.throwUnsupported();
    }

    @Override
    public <T> T ext(Class<T> clazz) {
        return FacadeProxy.of(clazz, this);
    }

    private Matrix throwUnsupported() {
        throw new UnsupportedOperationException(this.getClass() + " is immutable.");
    }
}
