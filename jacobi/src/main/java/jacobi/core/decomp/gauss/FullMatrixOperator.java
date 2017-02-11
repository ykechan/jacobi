/* 
 * The MIT License
 *
 * Copyright 2017 Y.K. Chan
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
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
