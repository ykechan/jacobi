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

package jacobi.core.decomp.qr.step;

import jacobi.api.Matrix;
import java.util.List;

/**
 * An iteration of Pure QR algorithm.
 * 
 * Pure QR algorithm refers to the following: 
 * Consider the QR decomposition of a matrix A = Q * R, compute ~A = R * Q
 * If ~A is diagonal some upper triangular matrix U, therefore
 * R * Q = U -&gt; Q * U * Q^t = Q * R * Q * Q^t -&gt Q * U * Q^t = Q * R
 * Therefore A = Q * U * Q^t, which is the Schur decomposition of A.
 * 
 * The proof of convergence is omitted here.
 * 
 * @author Y.K. Chan
 */
public class PureQR implements QRStep {
    
    public PureQR() {
        this.givensQR = new GivensQR();
    }

    @Override
    public void compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        int endCol = fullUpper ? matrix.getColCount() : endRow;
        List<GivensQR.Givens> givens = this.givensQR.computeQR(matrix, beginRow, endRow, endCol);
        this.givensQR.computeRQ(matrix, givens);
        if(partner != null){
            // TODO: compute partner matrix
            // ...
        }
    }

    private GivensQR givensQR;
}
