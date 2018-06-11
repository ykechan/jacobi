/*
 * The MIT License
 *
 * Copyright (c) 2018 Y.K. Chan.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jacobi.core.filter.fft;

/**
 * Compute DFT of a vector by applying the basis DFT transformation matrix.
 *
 * <p>This class is slow for large vectors, but fast for small fixed size vectors as it exploits the special
 * structure of the transformation matrix, which also maintains a higher level of numerical stability.</p>
 *
 * @author Y.K. Chan
 */
public class BasisDft implements CooleyTukeyMerger {

    @Override
    public void merge(ComplexVector vector, int offset, int length) {
        switch(length){
            case 0 :
            case 1 :
                break;
            case 2 :
                this.compute2(vector, offset);
                break;
            case 3 :
                this.compute3(vector, offset);
                break;
            case 6 :
                this.compute6(vector, offset);
                break;
            default :
                if(length < 0){
                    throw new IllegalArgumentException("Invalid length " + length);
                }
                this.computeN(vector, offset, length);
                break;
        }
    }

    protected void compute2(ComplexVector vector, int offset) {
        double tmpRe = vector.real[offset];
        double tmpIm = vector.imag[offset];

        vector.real[offset] += vector.real[offset + 1];
        vector.imag[offset] += vector.imag[offset + 1];

        vector.real[offset + 1] = tmpRe - vector.real[offset + 1];
        vector.imag[offset + 1] = tmpIm - vector.imag[offset + 1];
    }

    protected void compute3(ComplexVector vector, int offset) {
        double addRe = vector.real[offset + 1] + vector.real[offset + 2];
        double addIm = vector.imag[offset + 1] + vector.imag[offset + 2];

        double subRe = vector.real[offset + 1] - vector.real[offset + 2];
        double subIm = vector.imag[offset + 1] - vector.imag[offset + 2];

        double tmpRe = vector.real[offset];
        double tmpIm = vector.imag[offset];

        vector.real[offset] = tmpRe + addRe;
        vector.real[offset + 1] = tmpRe - (addRe + subIm * SQRT3) / 2.0;
        vector.real[offset + 2] = tmpRe - (addRe - subIm * SQRT3) / 2.0;

        vector.imag[offset] = tmpIm + addIm;
        vector.imag[offset + 1] = tmpIm - (addIm + subRe * SQRT3) / 2.0;
        vector.imag[offset + 2] = tmpIm - (addIm - subRe * SQRT3) / 2.0;
    }

    protected void compute6(ComplexVector vector, int offset) {
        double addRe03 = vector.real[offset] + vector.real[offset + 3];
        double addRe12 = vector.real[offset + 1] + vector.real[offset + 2];
        double addRe45 = vector.real[offset + 4] + vector.real[offset + 5];

        double subRe03 = vector.real[offset] - vector.real[offset + 3];
        double subRe12 = vector.real[offset + 1] - vector.real[offset + 2];
        double subRe45 = vector.real[offset + 4] - vector.real[offset + 5];

        double addIm03 = vector.imag[offset] + vector.imag[offset + 3];
        double addIm12 = vector.imag[offset + 1] + vector.imag[offset + 2];
        double addIm45 = vector.imag[offset + 4] + vector.imag[offset + 5];

        double subIm03 = vector.imag[offset] - vector.imag[offset + 3];
        double subIm12 = vector.imag[offset + 1] - vector.imag[offset + 2];
        double subIm45 = vector.imag[offset + 4] - vector.imag[offset + 5];

        // re(F) * re(v) - im(F) * im(v)
        vector.real[offset] = addRe03 + addRe12 + addRe45;
        vector.real[offset + 1] = subRe03 + subRe12 - subRe45 + SQRT3*(addIm03 - addIm45) / 2.0;
        vector.real[offset + 2] = addRe03 - addRe12 - addRe45 + SQRT3*(subIm03 + subIm45) / 2.0;
        vector.real[offset + 3] = subRe03 - subRe12 + subRe45;
        vector.real[offset + 4] = addRe03 - addRe12 - addRe45 - SQRT3*(subIm03 + subIm45) / 2.0;
        vector.real[offset + 5] = subRe03 + subRe12 - subRe45 - SQRT3*(addIm03 - addIm45) / 2.0;

        // re(F) * im(v) + im(F) * re(v)
        vector.imag[offset] = addIm03 + addIm12 + addIm45;
        vector.imag[offset + 1] = subIm03 + subIm12 - subIm45 + SQRT3*(addRe03 - addRe45) / 2.0;
        vector.imag[offset + 2] = addIm03 - addIm12 - addIm45 + SQRT3*(subRe03 + subRe45) / 2.0;
        vector.imag[offset + 3] = subIm03 - subIm12 + subIm45;
        vector.imag[offset + 4] = addIm03 - addIm12 - addIm45 - SQRT3*(subRe03 + subRe45) / 2.0;
        vector.imag[offset + 5] = subIm03 + subIm12 - subIm45 - SQRT3*(addRe03 - addRe45) / 2.0;
    }

    protected void computeN(ComplexVector vector, int offset, int length) {
        double c = Math.cos(2 * Math.PI / length);
        double s = -Math.sin(2 * Math.PI / length);

        ComplexVector out = ComplexVector.of(new double[length], new double[length]);
        System.arraycopy(out.real, 0, vector.real, offset, length);
        System.arraycopy(out.imag, 0, vector.imag, offset, length);
    }

    private static final double SQRT3 = Math.sqrt(3.0);
}
