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

package jacobi.core.decomp.gauss;

import jacobi.api.Matrix;
import jacobi.core.util.Throw;

/**
 * Decorator for interface ElementaryOperator.
 * 
 * @author Y.K. Chan
 */
public abstract class AbstractElementaryOperatorDecor implements ElementaryOperator {

    public AbstractElementaryOperatorDecor(ElementaryOperator op) {
        Throw.when().isNull(() -> op, () -> "No base operator.");
        this.op = op;
    }

    @Override
    public void swapRows(int i, int j) {
        this.isValidRow(i).isValidRow(j).op.swapRows(i, j);
    }

    @Override
    public void rowOp(int i, double a, int j) {
        this.isValidRow(i).isValidRow(j).op.rowOp(i, a, j);
    }

    @Override
    public Matrix getMatrix() {
        return this.op.getMatrix();
    }
    
    protected AbstractElementaryOperatorDecor isValidRow(int i) {
        Throw.when()
                .isTrue(
                    () -> i < 0 || i >= this.getMatrix().getRowCount(),
                    () -> "Invalid row index " + i 
                            + " for a " 
                            + this.getMatrix().getRowCount() 
                            + "x" 
                            + this.getMatrix().getColCount() 
                            + " matrix.");
        return this;
    }

    protected final ElementaryOperator op;
}
