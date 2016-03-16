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

package jacobi.core.util;

import jacobi.api.Matrix;
import java.util.function.Supplier;

/**
 *
 * @author Y.K. Chan
 */
public final class Pairs {

    private Pairs() {
        throw new UnsupportedOperationException("Do not instantiate.");
    }
    
    public static Pair of(Matrix a, Matrix b) {
        return null;
    }
    
    public static Pair of(Matrix a, Supplier<Matrix> b) {
        return null;
    }
    
    public static Pair of(Supplier<Matrix> a, Matrix b) {
        return null;
    }

}
