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
import jacobi.core.util.Throw;

/**
 *
 * @author Y.K. Chan
 */
public class Trace {

    public Trace(Matrix matrix) {
        Throw.when()
            .isNull(() -> matrix, () -> "No matrix to compute.")
            .isTrue(() -> matrix.getRowCount() == 0, () -> "No matrix to compute.")
            .isFalse(
                () -> matrix.getRowCount() == matrix.getColCount(), 
                () -> "Trace not exists for non-square matrices");
        this.matrix = matrix;
    }
    
    public double compute() {
        double tr = 0.0;
        for(int i = 0; i < this.matrix.getRowCount(); i++){
            tr += this.matrix.get(i, i);
        }
        return tr;
    }

    private Matrix matrix;
}
