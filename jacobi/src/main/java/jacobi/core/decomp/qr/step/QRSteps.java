/*
 * The MIT License
 *
 * Copyright 2016 Y.K. Chan.
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

/**
 * Factory of common composite QR steps.
 * 
 * Givens the wide range of QR steps, and possibly more for further expansion and optimization, and some steps may
 * require ordering in composing (though mostly does not), this class provide functions for building QR steps.
 * 
 * @author Y.K. Chan
 */
public abstract class QRSteps {

    private QRSteps() {
    }

    /**
     * Get the Standard QR step, suitable up to around 100x100 matrix.
     * @return  Instance of QR step
     */
    public static QRStep getStandard() {
        return STD;
    }
    
    /**
     * Get QR step when only eigenvalues are desired.
     * @return  Instance of QR step.
     */
    public static QRStep forEigOnly() {
        return EIGONLY;
    }
    
    private static final QRStep STD = Optional.of(new DefaultQRStep())
            //.map((s) -> new FrancisQR(s))
            .map((s) -> new FrancisGivensQR(s))
            .map((s) -> new ShiftedQR(s))
            .map((s) -> new ShiftedQR3x3(s))
            .map((s) -> new SingleStep2x2(s))
            .get();
    
    private static final QRStep EIGONLY = Optional.of(new DefaultQRStep())
            .map((s) -> new FrancisQR(s))
            .map((s) -> new ShiftedQR(s))
            .map((s) -> new ShiftedQR3x3(s))
            .map((s) -> new ByPass2x2(s))
            .get();

}
