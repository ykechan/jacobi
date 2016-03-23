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

package jacobi.core.prop;

import jacobi.api.Matrix;
import jacobi.core.decomp.gauss.GenericGaussianElim;
import jacobi.core.util.Throw;
import java.util.function.Function;

/**
 *
 * @author Y.K. Chan
 */
public class Rank {
    
    public int compute(Matrix a) {
        Throw.when()
            .isNull(() -> a, () -> "No matrix to rank.")
            .isTrue(
                () -> a.getRowCount() == 0, 
                () -> "Empty matrix has no rank.");
        new GenericGaussianElim<>(a, Function.identity()).compute(null);
        int rank = 0;
        for(int i = 0; i < a.getRowCount(); i++){
            if(Math.abs(a.get(i, i)) >= EPSILON){
                rank++;
            }
        }
        return rank;
    }
    
    protected int compute1x1(Matrix a) {
        return Math.abs(a.get(0, 0)) < EPSILON ? 0 : 1;
    }    

    private static final double EPSILON = 1e-12;
}
