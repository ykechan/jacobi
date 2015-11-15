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
package jacobi.api;

import jacobi.core.impl.DefaultMatrix;

/**
 * Factory for creating Matrices.
 * 
 * @author Y.K. Chan
 */
public final class Matrices {
    
    private Matrices() { 
        throw new UnsupportedOperationException("Do not instaniate.");
    }
    
    /**
     * Create a matrix with all elements zero.
     * @param m  Number of rows
     * @param n  Number of columns
     * @return  Matrix instance.
     */
    public static Matrix zeros(int m, int n) {
        return new DefaultMatrix(m, n);
    }
    
}
