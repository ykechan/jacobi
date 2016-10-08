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

package jacobi.core.decomp.qr;

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.DefaultQRStep;
import jacobi.core.decomp.qr.step.FrancisQR;
import jacobi.core.decomp.qr.step.QRStep;
import jacobi.core.decomp.qr.step.ShiftedQR3x3;
import java.util.Optional;

/**
 * Full suite of QR algorithm.
 * 
 * This implementation reduces the input matrix to Hessenberg form, and collects all QR strategies to further reduce
 * the Hessenberg matrix into Schur form.
 * 
 * @author Y.K. Chan
 */
public class FullQR implements QRStrategy {

    /**
     * Constructor.
     */
    public FullQR() {
        QRStep step = Optional.of(new DefaultQRStep())
                .map((s) -> new FrancisQR(s))
                .map((s) -> new ShiftedQR3x3(s))
                .get();
        this.impl = Optional.of(new BasicQR(step))
                .map((q) -> new SymmTriDiagQR(q))
                .get();
        this.hess = new HessenbergDecomp();
    }

    @Override
    public Matrix compute(Matrix matrix, Matrix partner, boolean fullUpper) {        
        this.hess.compute(matrix, partner == null ? (hh) -> {} : (hh) -> {
            hh.applyLeft(partner);
        });
        return this.impl.compute(matrix, partner, fullUpper);
    }

    private HessenbergDecomp hess;
    private QRStrategy impl;
}
