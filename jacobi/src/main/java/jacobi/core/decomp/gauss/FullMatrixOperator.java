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
 * Elementary row operation apply on a partner matrix on the fly.
 * 
 * When using Gaussian Elimination for solving a system of linear equations,
 * it would be desirable to perform the same elementary row operation to 
 * the other side of the equations. This class bundles the operation with the
 * other side with the operation of elimination.
 * 
 * @author Y.K. Chan
 */
public class FullMatrixOperator extends AbstractElementaryOperatorDecor {

    /**
     * Constructor. 
     * @param op  Base operator
     * @param partner   Partner matrix to also operates on
     */
    public FullMatrixOperator(ElementaryOperator op, Matrix partner) {        
        super(op);
        Throw.when()
            .isNull(() -> partner, () -> "No partner matrix.")
            .isTrue(
               () -> op.getMatrix().getRowCount() != partner.getRowCount(), 
               () -> "Row count mismatch. Partner matrix has " 
                       + partner.getRowCount() 
                       + " row(s) and matrix has "
                       + op.getMatrix().getRowCount()
                       + " row(s).");
        this.partner = partner;
    }

    /**
     * Get partner matrix.
     * @return  Partner matrix
     */
    public Matrix getPartner() {
        return partner;
    }

    @Override
    public void swapRows(int i, int j) {
        super.swapRows(i, j);
        this.partner.swapRow(i, j);
    }

    @Override
    public void rowOp(int i, double a, int j) {
        super.rowOp(i, a, j);
        double[] v = this.partner.getRow(i);
        double[] u = this.partner.getRow(j);
        for(int k = 0; k < v.length; k++){
            v[k] += a * u[k];
        }
        this.partner.setRow(i, v);
    }

    private Matrix partner;
}
