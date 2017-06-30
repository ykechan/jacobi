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

import jacobi.api.Matrix;

/**
 * A QR-Step that instantly deflate 2-by-2 (or 1-by-1) case without computing.
 * 
 * <p>For certain scenario, such as computing eigenvalues only, a non-reducible Schur form is not necessarily, and
 * the blocked Schur form would suffice even though the 2-by-2 block maybe further reduced to upper triangular.</p>
 * 
 * <p>This class saves efforts in computing those cases. 
 * In most such cases the 2x2 block will be processed after the fact.</p>
 * 
 * @author Y.K. Chan
 */
public class ByPass2x2 implements QRStep {

    /**
     * Constructor.
     * @param base  Base implementation
     */
    public ByPass2x2(QRStep base) {
        this.base = base;
    }

    @Override
    public int compute(Matrix matrix, Matrix partner, int beginRow, int endRow, boolean fullUpper) {
        if(endRow - beginRow < 3){
            return beginRow + 1;
        }
        return this.base.compute(matrix, partner, beginRow, endRow, fullUpper);
    }

    private QRStep base;
}
