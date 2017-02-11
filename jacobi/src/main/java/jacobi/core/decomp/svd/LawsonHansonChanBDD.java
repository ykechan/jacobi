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

package jacobi.core.decomp.svd;

import jacobi.api.Matrix;
import jacobi.core.decomp.qr.Householder;
import jacobi.core.decomp.qr.QRDecomp;
import jacobi.core.facade.FacadeProxy;
import jacobi.core.impl.DefaultMatrix;
import java.util.function.Consumer;

/**
 *
 * @author Y.K. Chan
 */
public class LawsonHansonChanBDD implements BiDiagDecomp {

    public LawsonHansonChanBDD() {
        this.qrDecomp = new QRDecomp();
        this.baseBdd = new GolubKahanBDD();
    }

    @Override
    public double[] compute(Mode mode, Matrix input, Consumer<Householder> qFunc, Consumer<Householder> vFunc) {
        this.qrDecomp.compute(input, qFunc);
        return this.baseBdd.compute(mode, this.trimmed(mode, input), qFunc, vFunc);
    }
    
    protected Matrix trimmed(Mode mode, Matrix input) {
        return null;
    }

    private QRDecomp qrDecomp;
    private BiDiagDecomp baseBdd;
}
