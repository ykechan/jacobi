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

/**
 *
 * An empty matrix, i.e. a matrix with no element.
 * 
 * This serves as a NULL object for matrix.
 * 
 * @author Y.K. Chan
 */
public final class Empty implements Matrix{
    
    /**
     * Get singleton instance.
     * @return  Instance of an Empty matrix
     */
    public static final Matrix getInstance() {
        return INST;
    }
    
    private Empty() {        
    }

    @Override
    public int getRowCount() {
        return 0;
    }

    @Override
    public int getColCount() {
        return 0;
    }

    @Override
    public double[] getRow(int index) {
        throw new ArrayIndexOutOfBoundsException(index);
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
        return INST;
    }
    
    private static final Matrix INST = new Empty();
}
