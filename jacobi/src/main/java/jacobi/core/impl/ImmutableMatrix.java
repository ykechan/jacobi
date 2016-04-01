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
import java.util.Arrays;

/**
 * Common parent class for a matrix implementation that is immutable.
 * 
 * Usually child class represents matrix that is of special form or value,
 * such as an identity matrix. Instead of fully realize all matrix entries,
 * it may be favorable to compute the matrix elements on demand, because
 * what is interesting is not the elements but its operations. For example the 
 * product of any matrix with an identity matrix is instantly known, which
 * is itself, saving O(n^3) operations.
 * 
 * These operations can be implemented as delegates can will be chosen
 * preferably by the facade engine through extension interfaces.
 * 
 * However such advantage is lost if matrix element values are changed, and it
 * is no long, say, an identity matrix. Therefore the implementation of an
 * identity matrix must be immutable.
 * 
 * An immutable matrix would not return its inner array in getRow(...), and
 * methods swapRow(...), set(...), setRow(...) would results in an
 * UnsupportedOperationException begin thrown.
 * 
 * @author Y.K. Chan
 */
public abstract class ImmutableMatrix implements Matrix { // NOPMD
    
    /**
     * Decorate a base matrix to make it immutable.
     * @param base  Base matrix
     * @return  An immutable matrix
     */
    public static ImmutableMatrix of(Matrix base) {
        if(base instanceof ImmutableMatrix){
            return (ImmutableMatrix) base;
        }
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
                return Arrays.copyOf(base.getRow(index), base.getColCount());
            }

            @Override
            public Matrix copy() {
                return base.copy();
            }

            @Override
            public <T> T ext(Class<T> clazz) {
                return FacadeProxy.of(clazz, base, this);
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

    @Override
    public Matrix copy() {
        return new DefaultMatrix(this);
    }

    private Matrix throwUnsupported() {
        throw new UnsupportedOperationException(this.getClass() + " is immutable.");
    }
}
