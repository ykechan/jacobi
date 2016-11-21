/* 
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan
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
package jacobi.core.decomp.qr;

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.step.QRStep;
import jacobi.core.decomp.qr.step.QRSteps;
import java.util.Optional;

/**
 * Full suite of QR algorithm.
 * 
 * This implementation reduces the input matrix to Hessenberg form, and collects all QR strategies to further reduce
 * the Hessenberg matrix into Schur form.
 * 
 * @author Y.K. Chan
 */
public class QRAlgo implements QRStrategy {

    /**
     * Constructor.
     */
    public QRAlgo() {
        this(QRSteps.getStandard());
    }
    
    /**
     * Constructor with injected QR step. 
     * @param step  QR step
     */
    protected QRAlgo(QRStep step) {
        this.step = step;
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
    private QRStep step;
}
