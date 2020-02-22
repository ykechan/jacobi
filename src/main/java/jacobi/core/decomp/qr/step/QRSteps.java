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

package jacobi.core.decomp.qr.step;

import java.util.Optional;

import jacobi.api.Matrix;

/**
 * Common composition of QR steps.
 * 
 * <p>Givens the wide range of QR steps, and possibly more for further expansion and optimization, and some steps may
 * require ordering in composing (though mostly does not), this class provide functions for building QR steps.</p>
 * 
 * @author Y.K. Chan
 */
public enum QRSteps implements QRStep {

	/**
	 * Standard QR step for general purposes
	 */
    STD(Optional.of(new DefaultQRStep())
            .map((s) -> new FrancisQR(s))
            .map((s) -> new ShiftedQR(s))
            .map((s) -> new ShiftedQR3x3(s))
            .map((s) -> new SingleStep2x2(s))
            .get()), 
    
    /**
     * QR step when only eigenvalues are needed
     */
    EIG(Optional.of(new DefaultQRStep())
            .map((s) -> new FrancisQR(s))
            .map((s) -> new ShiftedQR(s))
            .map((s) -> new ShiftedQR3x3(s))
            .map((s) -> new ByPass2x2(s))
            .get());

    /**
     * Constructor.
     * @param step  QR Step implementation
     */
    private QRSteps(QRStep step) {
		this.step = step;
	}

	@Override
	public int compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
		return this.step.compute(matrix, partner, beginRow, endRow, fullUpper);
	}	

	private QRStep step;
}
